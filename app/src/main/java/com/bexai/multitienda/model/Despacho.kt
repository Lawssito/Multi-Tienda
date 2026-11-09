package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Despacho: una caja o envío que llega a una sucursal. El QR de la caja contiene [codigo]
 * (por ejemplo "DSP-0042").
 */
@Entity(indices = [Index(value = ["codigo"], unique = true)])
data class Despacho(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigo: String,
    val proveedor: String,
    val guia: String,
    val sucursalId: Int      // sucursal destino
)

/** Línea de un despacho: qué producto y cuántas unidades se esperan. */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Despacho::class,
            parentColumns = ["id"],
            childColumns = ["despachoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("despachoId"), Index("productoId")]
)
data class DespachoDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val despachoId: Int,
    val productoId: Int,
    val esperado: Int
)

/** Cuántos productos y unidades trae un despacho (para la hoja "Caja detectada"). */
data class ResumenDespacho(
    val productos: Int,
    val unidades: Int
)
