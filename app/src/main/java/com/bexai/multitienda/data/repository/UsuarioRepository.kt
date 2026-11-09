package com.bexai.multitienda.data.repository

import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.model.Usuario

/**
 * UsuarioRepository: autentica contra las tres cuentas de prueba (una por rol).
 * TEMPORAL: reemplazar por usuarios guardados en Room o en un servidor.
 */
class UsuarioRepository {
    private val usuarios = listOf(
        Usuario(1,"Operador Demo",
            "operador@stockmulti.com","opedemo123", Rol.OPERADOR),
        Usuario(2,"Encargado Demo",
            "encargado@stockmulti.com","opedemo123", Rol.ENCARGADO),
        Usuario(3,"Gerencia Demo",
            "gerencia@stockmulti.com","opedemo123", Rol.GERENCIA)

    )

    fun autenticar(email: String, password: String): Usuario? {
        return usuarios.firstOrNull { it.email.equals(email.trim(), ignoreCase = true)
                && it.password == password }
    }
}