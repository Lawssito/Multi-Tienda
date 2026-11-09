package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.SesionSucursal
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.EstadoConteo
import com.bexai.multitienda.model.LineaConteo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConteoUiState(
    val sucursal: String = "Tienda Centro",
    val inicio: String = "",              // hora de inicio, ej. "10:05"
    val total: Int = 0,                   // productos que hay que contar
    val lineas: List<LineaConteo> = emptyList()
) {
    val contados: Int get() = lineas.size
    val sinDiferencia: Int get() = lineas.count { it.diferencia == 0 }
    val conDiferencia: Int get() = lineas.count { it.diferencia != 0 }
    val porcentaje: Int get() = if (total == 0) 0 else contados * 100 / total
    val faltantes: Int get() = lineas.count { it.diferencia < 0 }
    val sobrantes: Int get() = lineas.count { it.diferencia > 0 }
    val sinContar: Int get() = (total - contados).coerceAtLeast(0)
    // Las de mayor diferencia primero
    val conDiferenciaOrdenadas: List<LineaConteo>
        get() = lineas.filter { it.diferencia != 0 }.sortedByDescending { abs(it.diferencia) }
}

class ConteoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val dao = db.conteoDao()
    private val sucursalId: Int get() = SesionSucursal.id   // la que eligió el operador

    private val conteoId = MutableStateFlow<Int?>(null)
    private val horaInicio = MutableStateFlow("")

    init {
        // Retoma el conteo en curso de la sucursal; si no hay, crea uno nuevo
        viewModelScope.launch {
            val existente = dao.enCurso(sucursalId)
            val id: Int
            val inicio: Long
            if (existente != null) {
                id = existente.id
                inicio = existente.fechaInicio
            } else {
                inicio = System.currentTimeMillis()
                // Crea el conteo y congela el stock de todos los productos, todo o nada
                id = db.withTransaction {
                    val nuevo = dao.insertar(Conteo(sucursalId = sucursalId, fechaInicio = inicio)).toInt()
                    dao.congelarStock(nuevo, sucursalId)
                    nuevo
                }
            }
            horaInicio.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(inicio))
            conteoId.value = id
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ConteoUiState> = combine(
        conteoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else dao.lineas(id)
        },
        conteoId.flatMapLatest { id ->
            if (id == null) flowOf(0) else dao.totalLineas(id)
        },
        horaInicio
    ) { lineas, total, hora ->
        ConteoUiState(inicio = hora, total = total, lineas = lineas)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConteoUiState()
    )

    /**
     * Recontar: la línea se marca para recontar. Conserva lo contado hasta que se vuelva a
     * escanear; el primer escaneo nuevo reemplaza el valor y empieza otra vez desde 1.
     */
    fun recontar(productoId: Int) {
        viewModelScope.launch {
            val id = conteoId.value ?: return@launch
            dao.marcarRecontar(id, productoId)
        }
    }

    /**
     * Enviar al encargado: el conteo pasa a ENVIADO. NO cambia el stock;
     * eso ocurre solo cuando el encargado lo aprueba. Devuelve cuántas diferencias tenía.
     */
    fun enviar(alTerminar: (diferencias: Int) -> Unit) {
        viewModelScope.launch {
            val id = conteoId.value ?: return@launch
            val diferencias = uiState.value.conDiferencia
            dao.cambiarEstado(id, EstadoConteo.ENVIADO, System.currentTimeMillis())
            alTerminar(diferencias)
        }
    }
}
