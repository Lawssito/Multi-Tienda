package com.bexai.multitienda.model

import androidx.room.Entity
import java.util.Date

/**
 * StockUbicacion: cantidad de un producto en una sucursal.
 *
 * La clave primaria es compuesta: un producto solo puede tener una fila
 * por sucursal. [fechaActualizacion] se guarda con un TypeConverter.
 */
@Entity(
    tableName = "stock_ubicacion",
    primaryKeys = ["productoId", "ubicacionId"]
)
data class StockUbicacion(
    val productoId: Int,
    val ubicacionId: Int,
    val cantidad: Int = 0,
    val fechaActualizacion: Date = Date()
)
