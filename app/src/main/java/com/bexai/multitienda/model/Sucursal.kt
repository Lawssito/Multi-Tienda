package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Sucursal: representa una sucursal o tienda física.
 * active = false significa deshabilitada.
 */
@Entity(tableName = "sucursales")
data class Sucursal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val direccion: String,
    val activa: Boolean = true,
    val fechaCreacion: Date = Date()
)
