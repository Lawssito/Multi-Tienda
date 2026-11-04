package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario: persona que inicia sesión en la aplicación.
 *
 * El correo es único para que no existan dos usuarios con el mismo email.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: Rol
)
