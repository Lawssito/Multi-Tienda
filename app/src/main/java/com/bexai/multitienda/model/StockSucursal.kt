package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * StockSucursal: unidades de un producto en una sucursal.
 *
 * Es la fuente de verdad del stock por sucursal. `Producto.stock` se mantiene como el total
 * global (la suma de todas las sucursales); lo actualiza [com.bexai.multitienda.data.repository.ExistenciasRepository].
 */
@Entity(
    primaryKeys = ["productoId", "sucursalId"],
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sucursalId")]
)
data class StockSucursal(
    val productoId: Int,
    val sucursalId: Int,
    val cantidad: Int
)
