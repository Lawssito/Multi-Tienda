package com.bexai.multitienda.model

/** Estado del formulario de login: lo que se escribe, los errores de cada campo y el usuario autenticado. */
data class LoginModel(
    val email: String = "",
    val password: String = "",
    val passwordError: String? = null,
    val emailError: String? = null,
    val errorGeneral: String? = null,
    val usuarioAutenticado: Usuario? = null
)
