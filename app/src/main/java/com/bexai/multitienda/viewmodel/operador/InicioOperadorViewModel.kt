package com.bexai.multitienda.viewmodel.operador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.data.sesion.SesionSucursal
import com.bexai.multitienda.model.ResumenDia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Estado de la pantalla Inicio del operador: sucursal, fecha de hoy y resumen del día. */
data class InicioOperadorUiState(
    val sucursal: String = "",
    val fecha: String = "",
    val resumen: ResumenDia = ResumenDia(0, 0, 0, 0)
)

/** InicioOperadorViewModel: resumen de movimientos del día en la sucursal elegida. */
class InicioOperadorViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.obtener(app)

    // Se recalcula si el operador elige otra sucursal
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InicioOperadorUiState> =
        SesionSucursal.actual
            .flatMapLatest { activa ->
                db.movimientoDao().resumenDelDia(activa.id, inicioDelDia())
                    .map { resumen ->
                        InicioOperadorUiState(
                            sucursal = activa.nombre,
                            fecha = fechaDeHoy(),
                            resumen = resumen
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InicioOperadorUiState()
            )

    /** Fecha corta de hoy en español, por ejemplo "29 sep". */
    private fun fechaDeHoy(): String =
        SimpleDateFormat("d MMM", Locale.forLanguageTag("es")).format(Date()).removeSuffix(".")

    /** Medianoche de hoy en milisegundos: el resumen cuenta los movimientos desde esa hora. */
    private fun inicioDelDia(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}