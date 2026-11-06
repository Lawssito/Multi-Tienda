package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Ubicacion: representa una sucursal o tienda física.
 * active = false significa deshabilitada.
 */
@Entity(tableName = "ubicaciones")
data class Ubicacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val direccion: String,
    val activa: Boolean = true,
    val fechaCreacion: Date = Date()
)
