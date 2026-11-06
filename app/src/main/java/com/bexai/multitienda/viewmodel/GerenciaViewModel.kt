package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.DemoDataSeeder
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.model.Ubicacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    val totalInventario: StateFlow<Int> = productos.map { lista ->
        lista.sumOf { it.stock }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalProductos: StateFlow<Int> = productos.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val productosQuiebre: StateFlow<List<Producto>> = productos.map { lista ->
        lista.filter { it.stock == 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productosStockBajo: StateFlow<List<Producto>> = productos.map { lista ->
        lista.filter { it.stock in 1..9 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productosNormales: StateFlow<List<Producto>> = productos.map { lista ->
        lista.filter { it.stock >= 10 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tiendas / Sucursales
    val tiendas: StateFlow<List<Ubicacion>> = db.ubicacionDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTiendasActivas: StateFlow<Int> = tiendas.map { lista ->
        lista.count { it.activa }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Historial de Movimientos de Auditoría
    val historialMovimientos: StateFlow<List<Movimiento>> = db.movimientoDao().listarHistorial(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Resumen Operativo Global del Día
    val resumenDia: StateFlow<ResumenDia> = db.movimientoDao().resumenGlobal(inicioDelDia())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenDia(0, 0, 0, 0))

    // --- ACCIONES DE GESTIÓN DE TIENDAS ---

    fun cambiarEstadoTienda(id: Int, nuevaActiva: Boolean) {
        viewModelScope.launch {
            db.ubicacionDao().actualizarActiva(id, nuevaActiva)
        }
    }

    fun agregarTienda(nombre: String, direccion: String, onExito: () -> Unit) {
        if (nombre.isBlank() || direccion.isBlank()) return
        viewModelScope.launch {
            db.ubicacionDao().insertar(
                Ubicacion(
                    nombre = nombre.trim(),
                    direccion = direccion.trim(),
                    activa = true,
                    fechaCreacion = Date()
                )
            )
            onExito()
        }
    }

    fun eliminarTienda(id: Int) {
        viewModelScope.launch {
            db.ubicacionDao().eliminarPorId(id)
        }
    }

    // --- REPORTES Y EXPORTACIÓN ---

    fun generarCSV(tipoReporte: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val fechaActual = dateFormat.format(Date())

        return when (tipoReporte) {
            "QUIEBRES" -> {
                val sb = java.lang.StringBuilder()
                sb.append("ID,Producto,Categoria,Stock,Estado,FechaReporte\n")
                productosQuiebre.value.forEach { p ->
                    sb.append("${p.id},\"${p.nombre}\",\"${p.categoria}\",${p.stock},QUIEBRE,$fechaActual\n")
                }
                productosStockBajo.value.forEach { p ->
                    sb.append("${p.id},\"${p.nombre}\",\"${p.categoria}\",${p.stock},BAJO_STOCK,$fechaActual\n")
                }
                sb.toString()
            }
            "MOVIMIENTOS" -> {
                val sb = java.lang.StringBuilder()
                sb.append("ID,ProductoID,Tipo,Cantidad,Motivo,Fecha\n")
                historialMovimientos.value.forEach { m ->
                    val fecha = dateFormat.format(Date(m.fechaHora))
                    sb.append("${m.id},${m.productoId},${m.tipo},${m.cantidad},\"${m.motivo ?: ""}\",$fecha\n")
                }
                sb.toString()
            }
            else -> { // INVENTARIO GLOBAL
                val sb = java.lang.StringBuilder()
                sb.append("ID,Producto,CodigoBarras,Categoria,Stock,Estado,Fecha\n")
                productos.value.forEach { p ->
                    val estado = when {
                        p.stock == 0 -> "QUIEBRE"
                        p.stock < 10 -> "BAJO"
                        else -> "NORMAL"
                    }
                    sb.append("${p.id},\"${p.nombre}\",\"${p.codigoBarras}\",\"${p.categoria}\",${p.stock},$estado,$fechaActual\n")
                }
                sb.toString()
            }
        }
    }

    fun generarTextoResumen(tipoReporte: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = dateFormat.format(Date())

        return when (tipoReporte) {
            "QUIEBRES" -> {
                """
                === REPORTE DE QUIEBRES Y STOCK CRÍTICO ===
                Generado: $fecha
                Productos en quiebre total (0 unidades): ${productosQuiebre.value.size}
                Productos con stock bajo (<10 unidades): ${productosStockBajo.value.size}
                
                Detalle Quiebres:
                ${productosQuiebre.value.joinToString("\n") { "• ${it.nombre} (${it.categoria}): 0 uds" }}
                
                Detalle Stock Bajo:
                ${productosStockBajo.value.joinToString("\n") { "• ${it.nombre} (${it.categoria}): ${it.stock} uds" }}
                """.trimIndent()
            }
            "MOVIMIENTOS" -> {
                """
                === REPORTE DE AUDITORÍA Y MODIFICACIONES ===
                Generado: $fecha
                Total movimientos auditados: ${historialMovimientos.value.size}
                
                Últimos movimientos:
                ${historialMovimientos.value.take(15).joinToString("\n") { m ->
                    val f = dateFormat.format(Date(m.fechaHora))
                    "• [${m.tipo}] Prod #${m.productoId}: ${m.cantidad} uds | ${m.motivo ?: "Sin motivo"} ($f)"
                }}
                """.trimIndent()
            }
            else -> {
                """
                === REPORTE DE INVENTARIO GLOBAL ===
                Generado: $fecha
                Total unidades en cadena: ${totalInventario.value}
                Variedad de productos: ${totalProductos.value}
                Tiendas activas: ${totalTiendasActivas.value} de ${tiendas.value.size}
                Productos normales: ${productosNormales.value.size}
                Productos en riesgo / quiebre: ${productosQuiebre.value.size + productosStockBajo.value.size}
                """.trimIndent()
            }
        }
    }

    private fun inicioDelDia(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
