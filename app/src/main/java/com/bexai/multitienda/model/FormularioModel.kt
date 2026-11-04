package com.bexai.multitienda.model

/**
 * FormularioModel: guarda el estado del formulario de productos.
 *
 * - Los campos [nombre] y [stock] guardan lo que escribe el usuario.
 * - Los campos [nombreError] y [stockError] guardan el mensaje
 *   de error de cada campo (null significa que ese campo está bien).
 *
 * Así la vista solo se encarga de mostrar y el ViewModel de validar,
 * igual que en el proyecto de ejemplo con Room.
 */
data class FormularioModel(
    val nombre: String = "",
    val stock: String = "",
    val nombreError: String? = null,
    val stockError: String? = null,
    val guardando: Boolean = false
)
