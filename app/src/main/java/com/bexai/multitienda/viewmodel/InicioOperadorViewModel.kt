package com.bexai.multitienda.viewmodel

import android.app.Application
import android.icu.util.Calendar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.ResumenDia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    private fun inicioDelDia(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}