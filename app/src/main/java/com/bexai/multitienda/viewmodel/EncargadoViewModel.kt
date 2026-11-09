package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.ConteoRepository
import com.bexai.multitienda.data.RecepcionRepository
import com.bexai.multitienda.data.DemoDataSeeder
import com.bexai.multitienda.data.SesionSucursal
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.LineaConteo
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.RecepcionPendiente
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.EstadoReposicion
import com.bexai.multitienda.model.Reposicion
import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Una línea del último conteo donde lo contado no coincide con lo registrado. */
data class DiferenciaStock(
    val productoId: Int,
    val nombre: String,
    val registrado: Int,
    val contado: Int,
    val minimo: Int
) {
    val diferencia: Int get() = contado - registrado   // negativo = falta, positivo = sobra
}

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

    // La sucursal se comparte con SesionSucursal (TEMPORAL, hasta tener el usuario logueado):
    // así todas las pestañas del encargado, cada una con su ViewModel, ven la misma sucursal.
    private val _sucursalSeleccionadaId: StateFlow<Int?> = SesionSucursal.actual
        .map { it.id as Int? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SesionSucursal.id)
    val sucursalSeleccionadaId: StateFlow<Int?> = _sucursalSeleccionadaId

    // Catálogo de Productos desde Room, con el stock de la sucursal seleccionada
    // (Producto.stock es el total global; aquí se reemplaza por el de la sucursal)
    val productos: StateFlow<List<Producto>> = combine(
        db.productoDao().listar(),
        db.stockSucursalDao().listarTodos(),
        _sucursalSeleccionadaId
    ) { lista, stocks, sucId ->
        val porProducto = stocks.filter { it.sucursalId == (sucId ?: 1) }.associate { it.productoId to it.cantidad }
        lista.map { it.copy(stock = porProducto[it.id] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtros de búsqueda
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _estadoFiltro = MutableStateFlow("Todos") // Todos, Normal, Bajo Stock, Quiebre
    val estadoFiltro: StateFlow<String> = _estadoFiltro.asStateFlow()

    // Lista reactiva filtrada
    val productosFiltrados: StateFlow<List<Producto>> = combine(
        productos,
        _query,
        _estadoFiltro
    ) { lista, q, est ->
        lista.filter { prod ->
            val matchQuery = q.isBlank() ||
                    prod.nombre.contains(q, ignoreCase = true) ||
                    prod.codigoBarras.contains(q) ||
                    prod.categoria.contains(q, ignoreCase = true)

            val matchEstado = when (est) {
                "Quiebre" -> prod.stock <= 0
                "Bajo stock" -> prod.stock > 0 && prod.stock < prod.stockMinimo
                "Normal" -> prod.stock >= prod.stockMinimo
                else -> true
            }

            matchQuery && matchEstado
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CONTEOS MASIVOS ENVIADOS POR EL OPERADOR (Jira 31) ---
    private val conteoRepo = ConteoRepository(db)

    // Conteos de la sucursal seleccionada (1 mientras no haya selección) esperando revisión
    @OptIn(ExperimentalCoroutinesApi::class)
    val conteosEnviados: StateFlow<List<Conteo>> = _sucursalSeleccionadaId
        .flatMapLatest { conteoRepo.enviados(it ?: 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Diferencias del último conteo enviado por el operador (mockup "Diferencias")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ultimoConteo: StateFlow<Conteo?> = _sucursalSeleccionadaId
        .flatMapLatest { conteoRepo.ultimoConteo(it ?: 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Hora del último conteo enviado (null si no hay). */
    val fechaUltimoConteo: StateFlow<Long?> = ultimoConteo
        .map { it?.fechaEnvio }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val diferenciasUltimoConteo: StateFlow<List<DiferenciaStock>> = combine(
        ultimoConteo.flatMapLatest { c -> if (c == null) flowOf(emptyList()) else conteoRepo.diferencias(c.id) },
        productos
    ) { lineas, catalogo ->
        lineas.map { l ->
            DiferenciaStock(
                productoId = l.productoId,
                nombre = l.nombre,
                registrado = l.sistema,
                contado = l.contado,
                minimo = catalogo.firstOrNull { it.id == l.productoId }?.stockMinimo ?: 0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Reposiciones de esta sucursal con su estado: enviada, aprobada o rechazada. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val reposicionesEnviadas: StateFlow<List<Reposicion>> = combine(
        db.reposicionDao().todas(), _sucursalSeleccionadaId
    ) { lista, sucId -> lista.filter { it.sucursalId == (sucId ?: 1) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Quita del historial una reposición ya resuelta (las pendientes no se pueden eliminar). */
    fun eliminarReposicion(id: Int) {
        viewModelScope.launch { db.reposicionDao().eliminarResuelta(id) }
    }

    /** Productos bajo el mínimo que todavía no tienen una reposición declarada. */
    val bajoMinimoSinReposicion: StateFlow<List<Producto>> = combine(
        productos, reposicionesEnviadas
    ) { lista, reps ->
        // Ya tienen reposición: las enviadas y las aprobadas (una rechazada se puede volver a declarar)
        val declarados = reps.filter { it.estado != EstadoReposicion.RECHAZADA }.map { it.productoId }.toSet()
        lista.filter { it.stock < it.stockMinimo && it.id !in declarados }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Envía a gerencia un pedido de reposición. */
    fun declararReposicion(productoId: Int, cantidad: Int, urgente: Boolean, nota: String, onListo: () -> Unit) {
        viewModelScope.launch {
            db.reposicionDao().insertar(
                Reposicion(
                    sucursalId = _sucursalSeleccionadaId.value ?: 1,
                    productoId = productoId,
                    cantidad = cantidad,
                    urgente = urgente,
                    nota = nota.trim(),
                    fecha = System.currentTimeMillis()
                )
            )
            onListo()
        }
    }

    /** Ajustes manuales del operador en las últimas 24 h (solo informativo para el encargado). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val ajustesRecientes: StateFlow<List<Movimiento>> = _sucursalSeleccionadaId
        .flatMapLatest { db.movimientoDao().ajustesDesde(it ?: 1, System.currentTimeMillis() - 24L * 60 * 60 * 1000) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** (productos contados, total de productos) de un conteo. */
    fun progresoDe(conteoId: Int): Flow<Pair<Int, Int>> = combine(
        db.conteoDao().lineas(conteoId), db.conteoDao().totalLineas(conteoId)
    ) { contadas, total -> contadas.size to total }

    /** Faltantes y sobrantes de un conteo. */
    fun diferenciasDe(conteoId: Int): Flow<List<LineaConteo>> = conteoRepo.diferencias(conteoId)

    // --- RECEPCIONES DE CAJAS ENVIADAS POR EL OPERADOR (Jira 47) ---
    private val recepcionRepo = RecepcionRepository(db)

    @OptIn(ExperimentalCoroutinesApi::class)
    val recepcionesPendientes: StateFlow<List<RecepcionPendiente>> = _sucursalSeleccionadaId
        .flatMapLatest { recepcionRepo.pendientes(it ?: 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Cantidad de tareas pendientes (para el contador de la pestaña Alertas). */
    val totalAlertas: StateFlow<Int> = combine(
        conteosEnviados, recepcionesPendientes, bajoMinimoSinReposicion
    ) { c, r, b -> c.size + r.size + b.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun lineasRecepcion(recepcionId: Int): Flow<List<LineaRecepcion>> = recepcionRepo.lineas(recepcionId)

    fun decidirLinea(detalleId: Int, decision: String) {
        viewModelScope.launch { recepcionRepo.decidir(detalleId, decision) }
    }

    /** @param onResultado true si quedó revisada; false si falta decidir alguna línea. */
    fun marcarRecepcionRevisada(recepcionId: Int, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch { onResultado(recepcionRepo.marcarRevisada(recepcionId)) }
    }

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

    fun onQueryChange(nuevoQuery: String) {
        _query.value = nuevoQuery
    }

    fun onEstadoFiltroChange(estado: String) {
        _estadoFiltro.value = estado
    }
}
