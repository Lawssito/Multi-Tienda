package com.bexai.multitienda.data

import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sucursal en la que está trabajando el usuario. */
data class SucursalActiva(val id: Int, val nombre: String)

/**
 * SesionSucursal: guarda la sucursal que el operador eligió al entrar.
 *
 * Los ViewModel del operador leen [id] en vez de usar un número fijo. Vive solo en memoria:
 * si Android cierra la app, vuelve a la sucursal 1 hasta que se vuelva a elegir.
 * TEMPORAL: cuando exista el SesionViewModel con el usuario logueado (Jira 19), este objeto
 * se reemplaza por él.
 */
object SesionSucursal {
    private val _actual = MutableStateFlow(SucursalActiva(id = 1, nombre = ""))
    val actual: StateFlow<SucursalActiva> = _actual.asStateFlow()

    val id: Int get() = _actual.value.id

    fun elegir(sucursal: Sucursal) {
        _actual.value = SucursalActiva(sucursal.id, sucursal.nombre)
    }
}
