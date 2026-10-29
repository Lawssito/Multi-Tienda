package com.bexai.multitienda.model

/**
 * FormularioModel: guarda el estado del formulario de productos.
 *
 * - Los campos [nombre], [precio] y [stock] guardan lo que escribe el usuario.
 * - Los campos [nombreError], [precioError] y [stockError] guardan el mensaje
 *   de error de cada campo (null significa que ese campo está bien).
 *
 * Así la vista solo se encarga de mostrar y el ViewModel de validar,
 * igual que en el proyecto de ejemplo con Room.
 */
data class FormularioModel(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null
)
