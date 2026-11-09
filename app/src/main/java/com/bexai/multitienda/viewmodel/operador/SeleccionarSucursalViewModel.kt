package com.bexai.multitienda.viewmodel.operador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.data.local.DemoDataSeeder
import com.bexai.multitienda.data.sesion.SesionSucursal
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.viewmodel.comun.manejadorErrores
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista las sucursales y guarda la que elige el operador. */
class SeleccionarSucursalViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val errores = manejadorErrores("SeleccionarSucursalVM")

    init {
        // Crea los datos de prueba (sucursales incluidas) si todavía no existen
        viewModelScope.launch(errores) { DemoDataSeeder.sembrar(db) }
    }

    val sucursales: StateFlow<List<Sucursal>> = db.sucursalDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun elegir(sucursal: Sucursal) = SesionSucursal.elegir(sucursal)
}
