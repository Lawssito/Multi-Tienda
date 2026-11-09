package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.DemoDataSeeder
import com.bexai.multitienda.model.IncidenciaCaja
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal
import androidx.room.withTransaction
import com.bexai.multitienda.model.AlertaRevisada
import com.bexai.multitienda.model.Despacho
import com.bexai.multitienda.model.DespachoDetalle
import com.bexai.multitienda.model.EstadoReposicion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

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

/**
 * GerenciaViewModel: lógica y estado para el perfil Gerencial.
 * Proporciona tableros analíticos, auditoría global, gestión de tiendas y exportación de reportes.
 */
class GerenciaViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)

    init {
        viewModelScope.launch {
            DemoDataSeeder.sembrar(db)
        }
    }

    // Catálogo de Productos y métricas
    val productos: StateFlow<List<Producto>> = db.productoDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock de cada producto en cada sucursal (Producto.stock es el total)
    val stockSucursal: StateFlow<List<StockSucursal>> = db.stockSucursalDao().listarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Incidencias de cajas (faltan, sobran o dañados) con la decisión del encargado
    val incidenciasCajas: StateFlow<List<IncidenciaCaja>> = db.recepcionDao().incidencias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun actualizarMinimo(productoId: Int, minimo: Int, onListo: () -> Unit) {
        if (minimo < 0) return
        viewModelScope.launch {
            db.productoDao().actualizarMinimo(productoId, minimo)
            onListo()
        }
    }

    // Tiendas / Sucursales
    val tiendas: StateFlow<List<Sucursal>> = db.sucursalDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historial de Movimientos de Auditoría
    val historialMovimientos: StateFlow<List<Movimiento>> = db.movimientoDao().listarHistorial(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ALERTAS (mínimo, descalce, reposición y caja) ---
    private val alertasBase = combine(
        productos, stockSucursal, tiendas,
        db.reposicionDao().todas(), db.conteoDao().descalces()
    ) { prods, stocks, tds, reps, descalces ->
        val nomSuc = tds.associate { it.id to it.nombre }
        fun suc(id: Int) = nomSuc[id] ?: "Sucursal $id"
        val lista = mutableListOf<AlertaItem>()
        stocks.forEach { s ->
            val p = prods.firstOrNull { it.id == s.productoId } ?: return@forEach
            if (s.cantidad < p.stockMinimo) {
                lista += AlertaItem(
                    "MIN:${s.productoId}:${s.sucursalId}", TipoAlerta.MINIMO, p.nombre,
                    "${suc(s.sucursalId)} · ${s.cantidad} de mín. ${p.stockMinimo}",
                    null, s.productoId, s.sucursalId, s.cantidad <= 0, false,
                    cantidad = (p.stockMinimo * 2 - s.cantidad).coerceAtLeast(1)
                )
            }
        }
        descalces.forEach { d ->
            val signo = if (d.contado - d.sistema > 0) "+" else ""
            lista += AlertaItem(
                "DESC:${d.conteoId}:${d.productoId}", TipoAlerta.DESCALCE, d.nombre,
                "${suc(d.sucursalId)} · $signo${d.contado - d.sistema} en conteo",
                d.fecha, d.productoId, d.sucursalId, false, false
            )
        }
        reps.forEach { r ->
            val nombre = prods.firstOrNull { it.id == r.productoId }?.nombre ?: "Producto ${r.productoId}"
            lista += AlertaItem(
                "REP:${r.id}", TipoAlerta.REPOSICION, nombre,
                "${suc(r.sucursalId)} · pide ${r.cantidad}" + (if (r.urgente) " · urgente" else "") +
                    when (r.estado) {
                        EstadoReposicion.APROBADA -> " · aprobada, despacho ${r.despachoCodigo ?: ""}"
                        EstadoReposicion.RECHAZADA -> " · rechazada: ${r.motivoRechazo ?: "sin motivo"}"
                        else -> ""
                    },
                r.fecha, r.productoId, r.sucursalId, r.urgente && !r.revisada, r.revisada,
                nota = r.nota.takeIf { it.isNotBlank() }, cantidad = r.cantidad
            )
        }
        lista.toList()
    }

    val alertas: StateFlow<List<AlertaItem>> = combine(
        alertasBase, incidenciasCajas, db.alertaRevisadaDao().todas(), historialMovimientos, tiendas
    ) { base, cajas, marcas, historial, tds ->
        val notas = marcas.associate { it.clave to it.nota }
        val nomSuc = tds.associate { it.id to it.nombre }
        val deCajas = cajas.map { c ->
            val problema = listOfNotNull(
                (c.esperado - c.recibido).takeIf { it > 0 }?.let { "faltan $it" },
                (c.recibido - c.esperado).takeIf { it > 0 }?.let { "sobran $it" },
                c.danado.takeIf { it > 0 }?.let { "dañados $it" }
            ).joinToString(", ")
            AlertaItem(
                "CAJA:${c.detalleId}", TipoAlerta.CAJA, c.nombre,
                "${nomSuc[c.sucursalId] ?: "Sucursal ${c.sucursalId}"} · ${c.codigo} · $problema",
                c.fecha, c.productoId, c.sucursalId, false, false
            )
        }
        (base + deCajas).map { a ->
            val hora = a.hora ?: historial.firstOrNull { it.productoId == a.productoId && it.sucursalId == a.sucursalId }?.fechaHora
            a.copy(
                hora = hora,
                revisada = a.revisada || a.clave in notas,
                nota = a.nota ?: notas[a.clave]?.takeIf { it.isNotBlank() }
            )
        }.sortedByDescending { it.hora ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Resuelve una alerta (mínimo, descalce o caja) dejando una nota opcional. */
    fun marcarAlertaRevisada(alerta: AlertaItem, nota: String = "") {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch { db.reposicionDao().resolver(id, EstadoReposicion.RECHAZADA, motivo.trim(), null) }
    }

    /** Crea un despacho desde una alerta de mínimo y la deja resuelta con el código en la nota. */
    fun crearDespachoDesdeAlerta(alerta: AlertaItem, cantidad: Int, proveedor: String, onListo: (String) -> Unit) {
        if (cantidad < 1) return
        viewModelScope.launch {
            val codigo = db.withTransaction {
                val c = nuevoDespacho(alerta.productoId, alerta.sucursalId, cantidad, proveedor)
                db.alertaRevisadaDao().insertar(AlertaRevisada(alerta.clave, System.currentTimeMillis(), "Despacho $c · $cantidad uds"))
                c
            }
            onListo(codigo)
        }
    }

    // Stock total por sucursal (barras del Panel)
    val stockPorUbicacion: StateFlow<List<UbicacionStock>> = combine(tiendas, stockSucursal) { tds, stocks ->
        tds.map { t -> UbicacionStock(t, stocks.filter { it.sucursalId == t.id }.sumOf { it.cantidad }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACCIONES DE GESTIÓN DE TIENDAS ---

    fun cambiarEstadoTienda(id: Int, nuevaActiva: Boolean) {
        viewModelScope.launch {
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
            else -> null
        }
        _formTienda.update { it.copy(nombreError = nombreError, direccionError = direccionError) }
        if (nombreError != null || direccionError != null) return

        viewModelScope.launch {
            db.sucursalDao().insertar(
                Sucursal(nombre = nombre, direccion = direccion, activa = true, fechaCreacion = Date())
            )
            _formTienda.value = FormularioTienda()
            onExito()
        }
    }
}
