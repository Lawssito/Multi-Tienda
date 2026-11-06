package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Conteo: cabecera de un conteo físico.
 * Un conteo pueden estar abierto o cerrado.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Ubicacion::class,
            parentColumns = ["id"],
            childColumns = ["ubicacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ubicacionId")]
)
data class Conteo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ubicacionId: Int,
    val estado: String = "ABIERTO", // ABIERTO, CERRADO
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaCierre: Long? = null
)

/**
 * ConteoDetalle: línea de un conteo.
 * Guarda el stock teórico del sistema y el stock contado físicamente.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Conteo::class,
            parentColumns = ["id"],
            childColumns = ["conteoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conteoId"), Index("productoId")]
)
data class ConteoDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val conteoId: Int,
    val productoId: Int,
    val stockSistema: Int,
    val stockContado: Int,
    val diferencia: Int = stockSistema - stockContado
)
