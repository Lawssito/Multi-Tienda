package com.bexai.multitienda.model

import androidx.room.Entity

@Entity
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: Rol,
)
