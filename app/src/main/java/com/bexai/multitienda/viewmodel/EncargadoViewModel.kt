package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.ConteoRepository
import com.bexai.multitienda.data.DemoDataSeeder
import com.bexai.multitienda.model.AlertaReposicion
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.LineaConteo
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AjusteRealizado(
    val productoId: Int,
    val nombre: String,
    val stockAnterior: Int,
    val stockNuevo: Int,
    val motivo: String,
    val fecha: Long = System.currentTimeMillis()
)

/**
 * EncargadoViewModel: lógica y estado para el perfil de Encargado de Bodega.
 * Totalmente conectado a Room y operaciones de stock, diferencias y alertas.
 */
class EncargadoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)

    init {
        viewModelScope.launch {
            DemoDataSeeder.sembrar(db)
        }
    }

    // Tiendas / Sucursales
    val sucursales: StateFlow<List<Sucursal>> = db.sucursalDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sucursalSeleccionadaId = MutableStateFlow<Int?>(null)
    val sucursalSeleccionadaId: StateFlow<Int?> = _sucursalSeleccionadaId.asStateFlow()

    // Catálogo de Productos desde Room
    val productos: StateFlow<List<Producto>> = db.productoDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtros de búsqueda
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categoriaFiltro = MutableStateFlow("Todas")
    val categoriaFiltro: StateFlow<String> = _categoriaFiltro.asStateFlow()

    private val _estadoFiltro = MutableStateFlow("Todos") // Todos, Normal, Bajo Stock, Quiebre
    val estadoFiltro: StateFlow<String> = _estadoFiltro.asStateFlow()

    // Lista reactiva filtrada
    val productosFiltrados: StateFlow<List<Producto>> = combine(
        productos,
        _query,
        _categoriaFiltro,
        _estadoFiltro
    ) { lista, q, cat, est ->
        lista.filter { prod ->
            val matchQuery = q.isBlank() ||
                    prod.nombre.contains(q, ignoreCase = true) ||
                    prod.codigoBarras.contains(q) ||
                    prod.categoria.contains(q, ignoreCase = true)

            val matchCat = cat == "Todas" || prod.categoria.equals(cat, ignoreCase = true)

            val matchEstado = when (est) {
                "Quiebre" -> prod.stock == 0
                "Bajo Stock" -> prod.stock in 1..9
                "Normal" -> prod.stock >= 10
                else -> true
            }

            matchQuery && matchCat && matchEstado
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DIFERENCIAS Y CONTEO FÍSICO ---
    // Mapa de productoId -> stock contado ingresado
    private val _conteoFisico = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val conteoFisico: StateFlow<Map<Int, Int>> = _conteoFisico.asStateFlow()

    // --- CONTEOS MASIVOS ENVIADOS POR EL OPERADOR (Jira 31) ---
    private val conteoRepo = ConteoRepository(db)

    // Conteos de la sucursal seleccionada (1 mientras no haya selección) esperando revisión
    @OptIn(ExperimentalCoroutinesApi::class)
    val conteosEnviados: StateFlow<List<Conteo>> = _sucursalSeleccionadaId
        .flatMapLatest { conteoRepo.enviados(it ?: 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Faltantes y sobrantes de un conteo. */
    fun diferenciasDe(conteoId: Int): Flow<List<LineaConteo>> = conteoRepo.diferencias(conteoId)

    /** Aprueba el conteo: el stock queda igual a lo contado. */
    fun aprobarConteo(conteoId: Int, onResultado: (String) -> Unit) {
        viewModelScope.launch {
            onResultado(if (conteoRepo.aprobar(conteoId)) "Conteo aprobado: stock actualizado" else "El conteo ya no está pendiente")
        }
    }

    /** Rechaza el conteo sin tocar el stock. */
    fun rechazarConteo(conteoId: Int, onResultado: (String) -> Unit) {
        viewModelScope.launch {
            onResultado(if (conteoRepo.rechazar(conteoId)) "Conteo rechazado: el stock no cambió" else "El conteo ya no está pendiente")
        }
    }

    // Último ajuste realizado (para Undo)
    private val _ultimoAjuste = MutableStateFlow<AjusteRealizado?>(null)
    val ultimoAjuste: StateFlow<AjusteRealizado?> = _ultimoAjuste.asStateFlow()

    // --- ALERTAS DE REPOSICIÓN DECLARADAS ---
    private val _alertasDeclaradas = MutableStateFlow(
        listOf(
            AlertaReposicion(
                productoId = 5,
                nombreProducto = "Aceite Maravilla 1L",
                categoria = "Abarrotes",
                stockActual = 0,
                cantidadMinima = 30,
                motivo = "Quiebre total: alta demanda en pasillo 3",
                urgencia = "ALTA"
            ),
            AlertaReposicion(
                productoId = 6,
                nombreProducto = "Detergente Líquido 3L",
                categoria = "Limpieza",
                stockActual = 3,
                cantidadMinima = 20,
                motivo = "Stock crítico de fin de semana",
                urgencia = "MEDIA"
            )
        )
    )
    val alertasDeclaradas: StateFlow<List<AlertaReposicion>> = _alertasDeclaradas.asStateFlow()

    fun onQueryChange(nuevoQuery: String) {
        _query.value = nuevoQuery
    }

    fun onCategoriaChange(categoria: String) {
        _categoriaFiltro.value = categoria
    }

    fun onEstadoFiltroChange(estado: String) {
        _estadoFiltro.value = estado
    }

    fun onSucursalChange(id: Int) {
        _sucursalSeleccionadaId.value = id
    }

    fun actualizarStockContado(productoId: Int, nuevoStockContado: Int) {
        _conteoFisico.update { actual ->
            actual + (productoId to nuevoStockContado.coerceAtLeast(0))
        }
    }

    /**
     * Aplica el ajuste en base al stock físico contado,
     * persistiendo en Room e insertando el Movimiento de auditoría.
     */
    fun aplicarAjuste(producto: Producto, stockContado: Int, motivo: String, onExito: () -> Unit) {
        viewModelScope.launch {
            val stockAnterior = producto.stock
            val dif = stockContado - stockAnterior
            val sucursal = _sucursalSeleccionadaId.value ?: 1

            // 1. Fijar nuevo stock en Producto
            db.productoDao().fijarStock(producto.id, stockContado)

            // 2. Registrar movimiento de auditoría
            val motivoTexto = if (motivo.isNotBlank()) motivo else "Ajuste físico: dif $dif uds"
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = producto.id,
                    sucursalId = sucursal,
                    tipo = TipoMovimiento.AJUSTE,
                    cantidad = stockContado,
                    motivo = motivoTexto,
                    fechaHora = System.currentTimeMillis()
                )
            )

            // 3. Guardar último ajuste para deshacer
            _ultimoAjuste.value = AjusteRealizado(
                productoId = producto.id,
                nombre = producto.nombre,
                stockAnterior = stockAnterior,
                stockNuevo = stockContado,
                motivo = motivoTexto
            )

            // 4. Limpiar conteo temporal de este producto
            _conteoFisico.update { it - producto.id }

            onExito()
        }
    }

    /**
     * Deshace el último ajuste devolviendo el producto a su stock anterior.
     */
    fun deshacerUltimoAjuste(onExito: (String) -> Unit) {
        val ajuste = _ultimoAjuste.value ?: return
        viewModelScope.launch {
            db.productoDao().fijarStock(ajuste.productoId, ajuste.stockAnterior)
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = ajuste.productoId,
                    sucursalId = _sucursalSeleccionadaId.value ?: 1,
                    tipo = TipoMovimiento.AJUSTE,
                    cantidad = ajuste.stockAnterior,
                    motivo = "Deshacer ajuste previo (${ajuste.stockNuevo} -> ${ajuste.stockAnterior})",
                    fechaHora = System.currentTimeMillis()
                )
            )
            _ultimoAjuste.value = null
            onExito("Se restauró ${ajuste.nombre} a ${ajuste.stockAnterior} unidades")
        }
    }

    /**
     * Registra una nueva alerta de reposición declarada por el encargado.
     */
    fun declararAlerta(
        producto: Producto,
        cantidadMinima: Int,
        motivo: String,
        urgencia: String,
        onExito: () -> Unit
    ) {
        val nuevaAlerta = AlertaReposicion(
            productoId = producto.id,
            nombreProducto = producto.nombre,
            categoria = producto.categoria,
            stockActual = producto.stock,
            cantidadMinima = cantidadMinima,
            motivo = motivo.ifBlank { "Reposición requerida por rotación" },
            urgencia = urgencia
        )
        _alertasDeclaradas.update { listOf(nuevaAlerta) + it }
        onExito()
    }

    /**
     * Resuelve/archiva una alerta de reposición atendida.
     */
    fun resolverAlerta(alertaId: String) {
        _alertasDeclaradas.update { actual ->
            actual.filterNot { it.id == alertaId }
        }
    }
}
