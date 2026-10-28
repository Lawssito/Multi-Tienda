package com.bexai.multitienda.data

import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.model.Usuario

class UsuarioRepository {
    private val usuarios = listOf(
        Usuario(1,"Operador Demo",
            "operador@stockmulti.com","opedemo123", Rol.OPERDADOR),
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