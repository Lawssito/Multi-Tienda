package com.bexai.multitienda.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Usuario

/**
 * Acceso a la tabla usuario. Hoy el login usa las cuentas fijas de UsuarioRepository; este DAO
 * queda listo para cuando los usuarios se guarden en Room.
 */
@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(usuarios: List<Usuario>)
}