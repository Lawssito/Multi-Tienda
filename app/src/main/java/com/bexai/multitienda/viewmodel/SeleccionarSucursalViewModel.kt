package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.DemoDataSeeder
import com.bexai.multitienda.data.SesionSucursal
import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista las sucursales y guarda la que elige el operador. */
class SeleccionarSucursalViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)

    init {
        // TEMPORAL: crea las sucursales de prueba si todavía no existen
        viewModelScope.launch { DemoDataSeeder.sembrar(db) }
    }

    val sucursales: StateFlow<List<Sucursal>> = db.sucursalDao().listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun elegir(sucursal: Sucursal) = SesionSucursal.elegir(sucursal)
}
