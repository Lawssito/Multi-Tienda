package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Producto::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productoId")]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productoId: Int,
    val ubicacionId: Int,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val motivo: String? = null,
    val fechaHora: Long // milisegundos
)
