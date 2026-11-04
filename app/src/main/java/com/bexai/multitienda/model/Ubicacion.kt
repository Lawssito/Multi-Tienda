package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Ubicacion: representa una sucursal o tienda física.
 *
 * - [activa] permite deshabilitar una sucursal sin borrarla.
 * - [fechaCreacion] se guarda con un TypeConverter (Date <-> Long).
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
