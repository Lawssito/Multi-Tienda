package com.bexai.multitienda.data

import androidx.room.TypeConverter
import com.bexai.multitienda.model.Rol
import java.util.Date

/**
 * Convertidores de tipos para Room.
 *
 * Room no sabe guardar Date ni enums directamente, así que los convierte:
 * - Date <-> Long (milisegundos)
 * - Rol <-> String (nombre del enum)
 */
class Convertidores {

    @TypeConverter
    fun deFechaAEntero(fecha: Date?): Long? {
        return fecha?.time
    }

    @TypeConverter
    fun deEnteroAFecha(valor: Long?): Date? {
        return valor?.let { Date(it) }
    }

    @TypeConverter
    fun deRolAString(rol: Rol?): String? {
        return rol?.name
    }

    @TypeConverter
    fun deStringARol(valor: String?): Rol? {
        return valor?.let { nombre ->
            Rol.entries.firstOrNull { it.name == nombre }
        }
    }
}
