package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Resumen de lo último escaneado: el mismo producto repetido acumula la cantidad (x N). */
data class UltimaLectura(
    val productoId: Int,
    val nombre: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val stockAntes: Int,
    val stockDespues: Int
)

data class EscanerUiState(
    val lecturas: Int = 0,
    val ultima: UltimaLectura? = null,
    val mensaje: String? = null
)

class EscanerViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val ubicacionId = 1   // temporal: luego saldrá del usuario logueado

    private val _uiState = MutableStateFlow(EscanerUiState())
    val uiState: StateFlow<EscanerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { sembrarProductosDemo() }
    }

    /** Cada lectura mueve 1 unidad: +1 en Entrada, -1 en Salida. */
    fun procesarCodigo(codigo: String, tipo: TipoMovimiento) {
        viewModelScope.launch {
            if (tipo == TipoMovimiento.AJUSTE) {
                avisar("El ajuste se completa en su propia pantalla (pendiente)")
                return@launch
            }

            val producto = db.productoDao().buscarPorCodigo(codigo)
            if (producto == null) {
                avisar("Producto no encontrado: $codigo")
                return@launch
            }

            val delta = if (tipo == TipoMovimiento.ENTRADA) 1 else -1
            if (producto.stock + delta < 0) {
                avisar("Sin stock de ${producto.nombre}")
                return@launch
            }

            // Movimiento y stock se guardan juntos: o se guardan los dos o ninguno
            db.withTransaction {
                db.movimientoDao().insertar(
                    Movimiento(
                        productoId = producto.id,
                        ubicacionId = ubicacionId,
                        tipo = tipo,
                        cantidad = 1,
                        fechaHora = System.currentTimeMillis()
                    )
                )
                db.productoDao().sumarStock(producto.id, delta)
            }

            _uiState.update { actual ->
                val previa = actual.ultima
                val ultima =
                    if (previa != null && previa.productoId == producto.id && previa.tipo == tipo) {
                        previa.copy(
                            cantidad = previa.cantidad + 1,
                            stockDespues = producto.stock + delta
                        )
                    } else {
                        UltimaLectura(
                            productoId = producto.id,
                            nombre = producto.nombre,
                            tipo = tipo,
                            cantidad = 1,
                            stockAntes = producto.stock,
                            stockDespues = producto.stock + delta
                        )
                    }
                actual.copy(lecturas = actual.lecturas + 1, ultima = ultima, mensaje = null)
            }
        }
    }

    fun avisarSinCodigo() = avisar("No se encontró código, intenta de nuevo")

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    private fun avisar(texto: String) = _uiState.update { it.copy(mensaje = texto) }

    // TEMPORAL: productos de prueba (códigos EAN-13 válidos) hasta tener el catálogo real.
    private suspend fun sembrarProductosDemo() {
        val demo = listOf(
            Producto(nombre = "Cuaderno universitario 100 hojas", stock = 48, codigoBarras = "7800000000016"),
            Producto(nombre = "Lápiz grafito HB", stock = 120, codigoBarras = "7800000000023"),
            Producto(nombre = "Resma de papel carta 500 hojas", stock = 30, codigoBarras = "7800000000030")
        )
        demo.forEach { p ->
            if (db.productoDao().buscarPorCodigo(p.codigoBarras) == null) {
                db.productoDao().insertar(p)
            }
        }
    }
}
