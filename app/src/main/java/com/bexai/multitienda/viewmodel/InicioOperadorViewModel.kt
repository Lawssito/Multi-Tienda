package com.bexai.multitienda.viewmodel

import android.app.Application
import android.icu.util.Calendar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.model.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InicioOperadorUiState(
    val sucursal: String = "",
    val fecha: String = "",
    val resumen: ResumenDia = ResumenDia(0,0,0,0)
)

class InicioOperadorViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.obtener(app)
    private val ubicacionId = 1   // temporal

    val uiState: StateFlow<InicioOperadorUiState> =
        db.movimientoDao().resumenDelDia(ubicacionId, inicioDelDia())
            .map { resumen ->
                InicioOperadorUiState(
                    sucursal = "Tienda Centro",
                    fecha = "29 sep",
                    resumen = resumen
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InicioOperadorUiState()
            )

    // TEMPORAL: para probar que el resumen reacciona. Se borra al hacer el formulario real.
    fun registrarPrueba(tipo: TipoMovimiento) {
        viewModelScope.launch {
            db.productoDao().insertar(Producto(id = 1, nombre = "Producto de prueba", stock = 0))
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = 1,
                    ubicacionId = ubicacionId,
                    tipo = tipo,
                    cantidad = 5,
                    motivo = if (tipo == TipoMovimiento.AJUSTE) "Conteo masivo" else null,
                    fechaHora = System.currentTimeMillis()
                )
            )
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