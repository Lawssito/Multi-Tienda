package com.bexai.multitienda.model

data class LoginModel(
    val email: String = "",
    val password: String = "",
    val passwordError: String? = null,
    val emailError: String? = null,
    val errorGeneral: String? = null,
    val usuarioAutenticado: Usuario? = null
)
