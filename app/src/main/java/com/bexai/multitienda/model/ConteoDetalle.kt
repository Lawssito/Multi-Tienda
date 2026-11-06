package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una línea del conteo. Al iniciar el conteo se crea una línea por cada producto, con
 * [sistema] = el stock de ese momento (copia congelada, no cambia hasta el final).
 * [contado] es null mientras el producto no se ha contado.
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
    // Un producto aparece una sola vez por conteo
    indices = [Index(value = ["conteoId", "productoId"], unique = true), Index("productoId")]
)
data class ConteoDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val conteoId: Int,
    val productoId: Int,
    val contado: Int? = null,
    val sistema: Int,
    val actualizado: Long = 0   // cuándo se contó por última vez (para ordenar la lista)
)
