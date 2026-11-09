package com.bexai.multitienda.viewmodel

import android.app.Application
import android.icu.util.Calendar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.SesionSucursal
import com.bexai.multitienda.model.ResumenDia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InicioOperadorUiState(
    val sucursal: String = "",
    val fecha: String = "",
    val resumen: ResumenDia = ResumenDia(0,0,0,0)
)

class InicioOperadorViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.obtener(app)

    // Cambia solo si el operador elige otra sucursal
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InicioOperadorUiState> =
        SesionSucursal.actual
            .flatMapLatest { activa ->
                db.movimientoDao().resumenDelDia(activa.id, inicioDelDia())
                    .map { resumen ->
                        InicioOperadorUiState(
                            sucursal = activa.nombre,
                            fecha = "29 sep",
                            resumen = resumen
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InicioOperadorUiState()
            )

    private fun inicioDelDia(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}