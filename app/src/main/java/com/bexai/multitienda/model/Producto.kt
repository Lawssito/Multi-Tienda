package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Producto: artículo del catálogo (tabla `producto`).
 *
 * [stock] es el total de la red, la suma del stock de todas las sucursales (ver StockSucursal).
 * El id lo genera Room; por eso vale 0 al crear un producto nuevo.
 */
@Entity
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val stock: Int,              // total de unidades en todas las sucursales
    val codigoBarras: String = "",
    val categoria: String = "",
    val stockMinimo: Int = 10   // bajo este stock el producto está "bajo mínimo"
)
