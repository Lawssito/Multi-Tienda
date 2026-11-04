package com.bexai.multitienda.repository

import com.bexai.multitienda.data.UsuarioDao
import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * UsuarioRepository: acceso a los usuarios guardados en Room.
 *
 * La vista y el ViewModel no conocen el DAO: solo usan este repositorio.
 * Las listas se observan con Flow y las escrituras son suspend.
 */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    fun listar(): Flow<List<Usuario>> = usuarioDao.listar()

    suspend fun autenticar(email: String, password: String): Usuario? {
        val usuario = usuarioDao.buscarPorEmail(email.trim())
        return usuario?.takeIf { it.password == password }
    }

    suspend fun insertar(usuario: Usuario): Long = usuarioDao.insertar(usuario)

    suspend fun actualizar(usuario: Usuario) = usuarioDao.actualizar(usuario)

    suspend fun eliminar(usuario: Usuario) = usuarioDao.eliminar(usuario)

    /**
     * Deja usuarios de prueba la primera vez que se abre la base de datos.
     * El login los necesita para poder entrar mientras no existe un módulo
     * de administración de usuarios.
     */
    suspend fun asegurarUsuariosDemo() {
        if (usuarioDao.contar() > 0) return

        usuarioDao.insertar(
            Usuario(
                nombre = "Operador Demo",
                email = "operador@stockmulti.com",
                password = "opedemo123",
                rol = Rol.OPERADOR
            )
        )
        usuarioDao.insertar(
            Usuario(
                nombre = "Encargado Demo",
                email = "encargado@stockmulti.com",
                password = "opedemo123",
                rol = Rol.ENCARGADO
            )
        )
        usuarioDao.insertar(
            Usuario(
                nombre = "Gerencia Demo",
                email = "gerencia@stockmulti.com",
                password = "opedemo123",
                rol = Rol.GERENCIA
            )
        )
    }
}
