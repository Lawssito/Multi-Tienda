package com.bexai.multitienda.viewmodel.encargado

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.data.local.DemoDataSeeder
import com.bexai.multitienda.data.repository.ConteoRepository
import com.bexai.multitienda.data.repository.RecepcionRepository
import com.bexai.multitienda.data.sesion.SesionSucursal
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.EstadoReposicion
import com.bexai.multitienda.model.LineaConteo
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.RecepcionPendiente
import com.bexai.multitienda.model.Reposicion
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.viewmodel.comun.manejadorErrores
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

/** Sucursal por defecto mientras el encargado no elige una (TEMPORAL, hasta tener sesión de usuario). */
private const val SUCURSAL_POR_DEFECTO = 1

/** Ventana de "ajustes recientes" que ve el encargado: últimas 24 horas. */
private const val VENTANA_AJUSTES_MS = 24L * 60 * 60 * 1000

/**
 * EncargadoViewModel: estado y acciones del Encargado de sucursal.
 *
 * Bloques: stock de la sucursal con filtros, diferencias del último conteo, reposiciones
 * declaradas a gerencia, conteos por aprobar, recepciones de cajas por revisar y el
 * contador de alertas de la barra inferior. Todo se observa desde Room con Flow.
 */
class EncargadoViewModel(app: Application) : AndroidViewModel(app) {

    // ---------- Dependencias ----------

    private val db = AppDatabase.obtener(app)
    private val conteoRepo = ConteoRepository(db)
    private val recepcionRepo = RecepcionRepository(db)
    private val errores = manejadorErrores("EncargadoViewModel")

    init {
        viewModelScope.launch(errores) { DemoDataSeeder.sembrar(db) }
    }

    // ---------- Sucursal activa ----------
    val sucursales: StateFlow<List<Sucursal>> = db.sucursalDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // La sucursal se comparte con SesionSucursal (TEMPORAL, hasta tener el usuario logueado):
    // así todas las pestañas del encargado, cada una con su ViewModel, ven la misma sucursal.
    private val _sucursalSeleccionadaId: StateFlow<Int?> = SesionSucursal.actual
        .map { it.id as Int? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SesionSucursal.id)
    val sucursalSeleccionadaId: StateFlow<Int?> = _sucursalSeleccionadaId

    /** Id de la sucursal activa, nunca nulo (usa la por defecto si no hay selección). */
    private val sucursalActivaId: Flow<Int> = _sucursalSeleccionadaId.map { it ?: SUCURSAL_POR_DEFECTO }

    // ---------- Stock de la sucursal y filtros ----------

    // Catálogo con el stock de la sucursal seleccionada (Producto.stock es el total global;
    // aquí se reemplaza por el de la sucursal). La consulta ya viene filtrada por sucursal.
    @OptIn(ExperimentalCoroutinesApi::class)
    val productos: StateFlow<List<Producto>> = combine(
        db.productoDao().listar(),
        sucursalActivaId.flatMapLatest { db.stockSucursalDao().listarPorSucursal(it) }
    ) { lista, stocks ->
        val porProducto = stocks.associate { it.productoId to it.cantidad }
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

    // ---------- Conteos masivos enviados por el operador (Jira 31) ----------

    // Conteos de la sucursal seleccionada (1 mientras no haya selección) esperando revisión
    @OptIn(ExperimentalCoroutinesApi::class)
    val conteosEnviados: StateFlow<List<Conteo>> = _sucursalSeleccionadaId
        .flatMapLatest { conteoRepo.enviados(it ?: SUCURSAL_POR_DEFECTO) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Diferencias del último conteo enviado por el operador (mockup "Diferencias")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ultimoConteo: StateFlow<Conteo?> = _sucursalSeleccionadaId
        .flatMapLatest { conteoRepo.ultimoConteo(it ?: SUCURSAL_POR_DEFECTO) }
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
        // Índice por id: evita recorrer el catálogo completo por cada línea
        val minimoPorProducto = catalogo.associate { it.id to it.stockMinimo }
        lineas.map { l ->
            DiferenciaStock(
                productoId = l.productoId,
                nombre = l.nombre,
                registrado = l.sistema,
                contado = l.contado,
                minimo = minimoPorProducto[l.productoId] ?: 0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Reposiciones declaradas a gerencia ----------

    /** Reposiciones de esta sucursal con su estado: enviada, aprobada o rechazada. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val reposicionesEnviadas: StateFlow<List<Reposicion>> = sucursalActivaId
        .flatMapLatest { db.reposicionDao().porSucursal(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Quita del historial una reposición ya resuelta (las pendientes no se pueden eliminar). */
    fun eliminarReposicion(id: Int) {
        viewModelScope.launch(errores) { db.reposicionDao().eliminarResuelta(id) }
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
        viewModelScope.launch(errores) {
            db.reposicionDao().insertar(
                Reposicion(
                    sucursalId = _sucursalSeleccionadaId.value ?: SUCURSAL_POR_DEFECTO,
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
        .flatMapLatest { db.movimientoDao().ajustesDesde(it ?: SUCURSAL_POR_DEFECTO, System.currentTimeMillis() - VENTANA_AJUSTES_MS) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Detalle de un conteo ----------

    /** (productos contados, total de productos) de un conteo. */
    fun progresoDe(conteoId: Int): Flow<Pair<Int, Int>> = combine(
        db.conteoDao().lineas(conteoId), db.conteoDao().totalLineas(conteoId)
    ) { contadas, total -> contadas.size to total }

    /** Faltantes y sobrantes de un conteo. */
    fun diferenciasDe(conteoId: Int): Flow<List<LineaConteo>> = conteoRepo.diferencias(conteoId)

    // ---------- Recepciones de cajas enviadas por el operador (Jira 47) ----------

    @OptIn(ExperimentalCoroutinesApi::class)
    val recepcionesPendientes: StateFlow<List<RecepcionPendiente>> = _sucursalSeleccionadaId
        .flatMapLatest { recepcionRepo.pendientes(it ?: SUCURSAL_POR_DEFECTO) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Cantidad de tareas pendientes (para el contador de la pestaña Alertas). */
    val totalAlertas: StateFlow<Int> = combine(
        conteosEnviados, recepcionesPendientes, bajoMinimoSinReposicion
    ) { c, r, b -> c.size + r.size + b.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun lineasRecepcion(recepcionId: Int): Flow<List<LineaRecepcion>> = recepcionRepo.lineas(recepcionId)

    fun decidirLinea(detalleId: Int, decision: String) {
        viewModelScope.launch(errores) { recepcionRepo.decidir(detalleId, decision) }
    }

    /** @param onResultado true si quedó revisada; false si falta decidir alguna línea. */
    fun marcarRecepcionRevisada(recepcionId: Int, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch(errores) { onResultado(recepcionRepo.marcarRevisada(recepcionId)) }
    }

    /** Aprueba el conteo: el stock queda igual a lo contado. */
    fun aprobarConteo(conteoId: Int, onResultado: (String) -> Unit) {
        viewModelScope.launch(errores) {
            onResultado(if (conteoRepo.aprobar(conteoId)) "Conteo aprobado: stock actualizado" else "El conteo ya no está pendiente")
        }
    }

    /** Rechaza el conteo sin tocar el stock. */
    fun rechazarConteo(conteoId: Int, onResultado: (String) -> Unit) {
        viewModelScope.launch(errores) {
            onResultado(if (conteoRepo.rechazar(conteoId)) "Conteo rechazado: el stock no cambió" else "El conteo ya no está pendiente")
        }
    }

    // ---------- Filtros de la pantalla Stock ----------

    fun onQueryChange(nuevoQuery: String) {
        _query.value = nuevoQuery
    }

    fun onEstadoFiltroChange(estado: String) {
        _estadoFiltro.value = estado
    }
}
