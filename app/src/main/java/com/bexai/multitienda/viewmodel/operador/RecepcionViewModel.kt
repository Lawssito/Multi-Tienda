package com.bexai.multitienda.viewmodel.operador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.data.repository.RecepcionRepository
import com.bexai.multitienda.data.sesion.SesionSucursal
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.TotalesRecepcion
import com.bexai.multitienda.model.totales
import com.bexai.multitienda.viewmodel.comun.manejadorErrores
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de las pantallas Recepción y Resumen de recepción de una caja. */
data class RecepcionUiState(
    val cargado: Boolean = false,
    val recepcionId: Int = 0,
    val codigo: String = "",
    val proveedor: String = "",
    val guia: String = "",
    val lineas: List<LineaRecepcion> = emptyList(),
    val totales: TotalesRecepcion = TotalesRecepcion()
) {
    /** Porcentaje de lo esperado que ya se recibió (de 0 a 100). */
    val porcentaje: Int get() =
        if (totales.esperadas == 0) 0 else (minOf(totales.recibidas, totales.esperadas) * 100 / totales.esperadas)

    /** Líneas con diferencia (faltan, sobran o dañados) para el resumen. */
    val conDiferencia: List<LineaRecepcion> get() = lineas.filter { it.conProblema }
}

/**
 * RecepcionViewModel: la recepción de una caja. El despacho llega como argumento de la ruta
 * (`despachoId`); si ya había una recepción en curso de ese despacho en esta sucursal, la retoma.
 */
class RecepcionViewModel(app: Application, estadoGuardado: SavedStateHandle) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val repo = RecepcionRepository(db)
    private val despachoId: Int = estadoGuardado["despachoId"] ?: 0
    private val errores = manejadorErrores("RecepcionViewModel")

    private val recepcionId = MutableStateFlow<Int?>(null)
    private val cabecera = MutableStateFlow(Triple("", "", ""))

    // ---------- Carga: cabecera del despacho y recepción en curso ----------

    init {
        viewModelScope.launch(errores) {
            val despacho = db.despachoDao().buscar(despachoId) ?: return@launch
            cabecera.value = Triple(despacho.codigo, despacho.proveedor, despacho.guia)
            recepcionId.value = repo.iniciar(despachoId, SesionSucursal.id)
        }
    }

    // ---------- Estado observado por la UI ----------

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RecepcionUiState> = combine(
        recepcionId.filterNotNull(),
        cabecera,
        recepcionId.filterNotNull().flatMapLatest { repo.lineas(it) }
    ) { id, (codigo, proveedor, guia), lineas ->
        RecepcionUiState(
            cargado = true,
            recepcionId = id,
            codigo = codigo,
            proveedor = proveedor,
            guia = guia,
            lineas = lineas,
            totales = lineas.totales()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecepcionUiState())

    // ---------- Acciones del operador ----------

    /** @param onResultado null si se guardó; si no, el mensaje de error. */
    fun marcarDanado(detalleId: Int, cantidad: Int, motivo: String, nota: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch(errores) { onResultado(repo.marcarDanado(detalleId, cantidad, motivo, nota)) }
    }

    /** Envía la recepción: entran al stock las unidades buenas. [onListo] recibe cuántas alertas generó. */
    fun enviar(onListo: (Int) -> Unit) {
        val id = recepcionId.value ?: return
        viewModelScope.launch(errores) {
            val alertas = repo.lineasUnaVez(id).count { it.conProblema }
            if (repo.enviar(id)) onListo(alertas)
        }
    }
}
