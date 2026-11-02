package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Usuario

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(usuarios: List<Usuario>)
}