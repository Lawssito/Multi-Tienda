package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Movimiento de stock en una sucursal. En ENTRADA y SALIDA [cantidad] son las unidades movidas;
 * en AJUSTE es el nuevo total (y [motivo] explica el ajuste).
 */
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
    val sucursalId: Int,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val motivo: String? = null,
    val fechaHora: Long // milisegundos
)
