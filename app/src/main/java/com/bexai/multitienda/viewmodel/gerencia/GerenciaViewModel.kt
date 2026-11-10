package com.bexai.multitienda.viewmodel.gerencia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.data.local.DemoDataSeeder
import com.bexai.multitienda.model.AlertaRevisada
import com.bexai.multitienda.model.Despacho
import com.bexai.multitienda.model.DespachoDetalle
import com.bexai.multitienda.model.EstadoReposicion
import com.bexai.multitienda.model.IncidenciaCaja
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.viewmodel.comun.manejadorErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

// =====================================================================
// Modelos de estado de las pantallas de Gerencia (alertas, formulario de tienda y panel).
// =====================================================================

/** Origen de una alerta: producto bajo el mínimo, diferencia en un conteo, reposición pedida o caja con problemas. */
enum class TipoAlerta { MINIMO, DESCALCE, REPOSICION, CAJA }

/** Alerta que ve Gerencia. [clave] la identifica para marcarla como revisada. */
data class AlertaItem(
    val clave: String,
    val tipo: TipoAlerta,
    val titulo: String,
    val detalle: String,
    val hora: Long?,
    val productoId: Int,
    val sucursalId: Int,
    val critica: Boolean,
    val revisada: Boolean,
    val nota: String? = null,
    val cantidad: Int = 0     // cantidad sugerida o pedida, para crear el despacho
)

/** Estado del formulario de nueva sucursal, con el error de cada campo. */
data class FormularioTienda(
    val nombre: String = "",
    val direccion: String = "",
    val nombreError: String? = null,
    val direccionError: String? = null
)

/** Stock total de una sucursal (para las barras del Panel). */
data class UbicacionStock(val sucursal: Sucursal, val total: Int)

// =====================================================================
// ViewModel
// =====================================================================

/** Cuántos movimientos recientes carga el historial de Gerencia. */
private const val LIMITE_HISTORIAL = 300

/**
 * GerenciaViewModel: estado y acciones de Gerencia (toda la red de sucursales).
 *
 * Bloques: datos base (catálogo, stock por sucursal, tiendas, historial), alertas y su
 * resolución (despachos, reposiciones), stock por sucursal para el Panel y la gestión de
 * tiendas con su formulario validado.
 */
class GerenciaViewModel(app: Application) : AndroidViewModel(app) {

    // ---------- Dependencias ----------

    private val db = AppDatabase.obtener(app)
    private val errores = manejadorErrores("GerenciaViewModel")

    init {
        viewModelScope.launch(errores) { DemoDataSeeder.sembrar(db) }
    }

    // ---------- Datos base observados desde Room ----------

    // Catálogo de Productos y métricas
    val productos: StateFlow<List<Producto>> = db.productoDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock de cada producto en cada sucursal (Producto.stock es el total)
    val stockSucursal: StateFlow<List<StockSucursal>> = db.stockSucursalDao().listarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Incidencias de cajas (faltan, sobran o dañados) con la decisión del encargado
    val incidenciasCajas: StateFlow<List<IncidenciaCaja>> = db.recepcionDao().incidencias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tiendas / Sucursales
    val tiendas: StateFlow<List<Sucursal>> = db.sucursalDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historial de Movimientos de Auditoría
    val historialMovimientos: StateFlow<List<Movimiento>> = db.movimientoDao().listarHistorial(LIMITE_HISTORIAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Alertas (mínimo, descalce, reposición y caja) ----------
    // La construcción de cada alerta vive en AlertasGerencia.kt (funciones puras, fáciles de probar).

    private val alertasBase = combine(
        productos, stockSucursal, tiendas,
        db.reposicionDao().todas(), db.conteoDao().descalces()
    ) { prods, stocks, tds, reps, descalces ->
        construirAlertasBase(prods, stocks, tds, reps, descalces)
    }

    val alertas: StateFlow<List<AlertaItem>> = combine(
        alertasBase, incidenciasCajas, db.alertaRevisadaDao().todas(), historialMovimientos, tiendas
    ) { base, cajas, marcas, historial, tds ->
        completarAlertas(base + alertasDeCajas(cajas, tds), marcas, historial)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Acciones sobre alertas ----------

    /** Resuelve una alerta (mínimo, descalce o caja) dejando una nota opcional. */
    fun marcarAlertaRevisada(alerta: AlertaItem, nota: String = "") {
        viewModelScope.launch(errores) {
            db.alertaRevisadaDao().insertar(AlertaRevisada(alerta.clave, System.currentTimeMillis(), nota.trim()))
        }
    }

    /** Crea un despacho (caja a recibir) con un producto para una sucursal y devuelve su código. */
    private suspend fun nuevoDespacho(productoId: Int, sucursalId: Int, cantidad: Int, proveedor: String): String =
        db.withTransaction {
            val ultimo = db.despachoDao().ultimoCodigo()
            val numero = (ultimo?.substringAfter("-")?.toIntOrNull() ?: 0) + 1
            val codigo = String.format(Locale.US, "DSP-%04d", numero)
            val id = db.despachoDao().insertar(
                Despacho(codigo = codigo, proveedor = proveedor.trim().ifBlank { "Proveedor" }, guia = "", sucursalId = sucursalId)
            ).toInt()
            db.despachoDao().insertarDetalle(DespachoDetalle(despachoId = id, productoId = productoId, esperado = cantidad))
            codigo
        }

    /** Aprueba una reposición: genera el despacho que el operador recibirá en esa sucursal. */
    fun aprobarReposicion(alerta: AlertaItem, cantidad: Int, proveedor: String, onListo: (String) -> Unit) {
        val id = alerta.clave.removePrefix("REP:").toIntOrNull() ?: return
        if (cantidad < 1) return
        viewModelScope.launch(errores) {
            val codigo = db.withTransaction {
                val c = nuevoDespacho(alerta.productoId, alerta.sucursalId, cantidad, proveedor)
                db.reposicionDao().resolver(id, EstadoReposicion.APROBADA, null, c)
                c
            }
            onListo(codigo)
        }
    }

    /** Rechaza una reposición con un motivo, que el encargado verá en sus Alertas. */
    fun rechazarReposicion(alerta: AlertaItem, motivo: String) {
        val id = alerta.clave.removePrefix("REP:").toIntOrNull() ?: return
        if (motivo.isBlank()) return
        viewModelScope.launch(errores) { db.reposicionDao().resolver(id, EstadoReposicion.RECHAZADA, motivo.trim(), null) }
    }

    /** Crea un despacho desde una alerta de mínimo y la deja resuelta con el código en la nota. */
    fun crearDespachoDesdeAlerta(alerta: AlertaItem, cantidad: Int, proveedor: String, onListo: (String) -> Unit) {
        if (cantidad < 1) return
        viewModelScope.launch(errores) {
            val codigo = db.withTransaction {
                val c = nuevoDespacho(alerta.productoId, alerta.sucursalId, cantidad, proveedor)
                db.alertaRevisadaDao().insertar(AlertaRevisada(alerta.clave, System.currentTimeMillis(), "Despacho $c · $cantidad uds"))
                c
            }
            onListo(codigo)
        }
    }

    // ---------- Panel: stock total por sucursal ----------

    val stockPorUbicacion: StateFlow<List<UbicacionStock>> = combine(tiendas, stockSucursal) { tds, stocks ->
        // Se suma una sola vez por sucursal en vez de filtrar la lista completa por cada tienda
        val totalPorSucursal = stocks.groupBy { it.sucursalId }.mapValues { (_, filas) -> filas.sumOf { it.cantidad } }
        tds.map { t -> UbicacionStock(t, totalPorSucursal[t.id] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Stock mínimo de un producto ----------

    fun actualizarMinimo(productoId: Int, minimo: Int, onListo: () -> Unit) {
        if (minimo < 0) return
        viewModelScope.launch(errores) {
            db.productoDao().actualizarMinimo(productoId, minimo)
            onListo()
        }
    }

    // ---------- Gestión de tiendas ----------

    fun cambiarEstadoTienda(id: Int, nuevaActiva: Boolean) {
        viewModelScope.launch(errores) {
            db.sucursalDao().actualizarActiva(id, nuevaActiva)
        }
    }

    // Formulario "Registrar nueva sucursal": la pantalla solo muestra este estado, la validación vive aquí
    private val _formTienda = MutableStateFlow(FormularioTienda())
    val formTienda: StateFlow<FormularioTienda> = _formTienda.asStateFlow()

    fun onNombreTiendaChange(nombre: String) {
        _formTienda.update { it.copy(nombre = nombre, nombreError = null) }
    }

    fun onDireccionTiendaChange(direccion: String) {
        _formTienda.update { it.copy(direccion = direccion, direccionError = null) }
    }

    fun limpiarFormTienda() {
        _formTienda.value = FormularioTienda()
    }

    fun agregarTienda(onExito: () -> Unit) {
        val form = _formTienda.value
        val nombre = form.nombre.trim()
        val direccion = form.direccion.trim()
        val nombreError = when {
            nombre.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            nombre.length > 40 -> "El nombre no puede superar los 40 caracteres"
            tiendas.value.any { it.nombre.equals(nombre, ignoreCase = true) } -> "Ya existe una sucursal con ese nombre"
            else -> null
        }
        val direccionError = when {
            direccion.length < 5 -> "La dirección debe tener al menos 5 caracteres"
            direccion.length > 100 -> "La dirección no puede superar los 100 caracteres"
            else -> null
        }
        _formTienda.update { it.copy(nombreError = nombreError, direccionError = direccionError) }
        if (nombreError != null || direccionError != null) return

        viewModelScope.launch(errores) {
            db.sucursalDao().insertar(
                Sucursal(nombre = nombre, direccion = direccion, activa = true, fechaCreacion = Date())
            )
            _formTienda.value = FormularioTienda()
            onExito()
        }
    }
}
