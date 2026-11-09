package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Reposicion: pedido que el encargado declara a gerencia cuando un producto de su sucursal
 * está bajo el mínimo. Gerencia la ve en Alertas y la marca como revisada.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoId"), Index("sucursalId")]
)
data class Reposicion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sucursalId: Int,
    val productoId: Int,
    val cantidad: Int,
    val urgente: Boolean = false,
    val nota: String = "",
    val fecha: Long,
    val revisada: Boolean = false,        // true cuando gerencia ya la resolvió (aprobada o rechazada)
    val estado: String = EstadoReposicion.PENDIENTE,
    val motivoRechazo: String? = null,
    val despachoCodigo: String? = null    // despacho que se generó al aprobarla
)

object EstadoReposicion {
    const val PENDIENTE = "PENDIENTE"
    const val APROBADA = "APROBADA"
    const val RECHAZADA = "RECHAZADA"
}
