package com.bexai.multitienda.model

/**
 * SesionModel: datos de la sesión actual.
 *
 * El SesionViewModel guarda aquí el usuario autenticado, su rol y la
 * sucursal (tienda) que eligió para trabajar.
 */
data class SesionModel(
    val usuario: Usuario? = null,
    val rol: Rol? = null,
    val tienda: Ubicacion? = null,
    val error: String? = null
)
