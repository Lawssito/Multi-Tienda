package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Producto: artículo del catálogo general.
 *
 * La cantidad disponible no vive aquí: cada sucursal tiene su propia
 * cantidad en [StockUbicacion]. Así un mismo producto puede tener
 * existencias distintas en cada tienda.
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)
