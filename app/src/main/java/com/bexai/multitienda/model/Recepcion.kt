package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EstadoRecepcion {
    EN_CURSO,   // el operador sigue escaneando la caja
    ENVIADA,    // las unidades buenas ya entraron al stock; espera la revisión del encargado
    REVISADA    // el encargado resolvió cada diferencia
}

/** Recepción de una caja (despacho) en una sucursal. */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Despacho::class,
            parentColumns = ["id"],
            childColumns = ["despachoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("despachoId")]
)
data class Recepcion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val despachoId: Int,
    val sucursalId: Int,
    val estado: EstadoRecepcion = EstadoRecepcion.EN_CURSO,
    val fechaInicio: Long,
    val fechaEnvio: Long? = null
)

/**
 * Línea de una recepción. [esperado] se copia del despacho al empezar (0 si el producto
 * no venía en la caja). [decision] es lo que resolvió el encargado.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Recepcion::class,
            parentColumns = ["id"],
            childColumns = ["recepcionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recepcionId", "productoId"], unique = true), Index("productoId")]
)
data class RecepcionDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recepcionId: Int,
    val productoId: Int,
    val esperado: Int,
    val recibido: Int = 0,
    val danado: Int = 0,
    val motivoDanado: String? = null,
    val nota: String? = null,
    val decision: String? = null
)

/** Fila para mostrar: línea de recepción + datos del producto. */
data class LineaRecepcion(
    val detalleId: Int,
    val productoId: Int,
    val nombre: String,
    val codigoBarras: String,
    val esperado: Int,
    val recibido: Int,
    val danado: Int,
    val motivoDanado: String?,
    val nota: String?,
    val decision: String?
) {
    val faltan: Int get() = (esperado - recibido).coerceAtLeast(0)
    val sobran: Int get() = (recibido - esperado).coerceAtLeast(0)
    val buenas: Int get() = (recibido - danado).coerceAtLeast(0)
    val completo: Boolean get() = recibido == esperado && danado == 0
    val conProblema: Boolean get() = faltan > 0 || sobran > 0 || danado > 0
}

/** Recepción enviada que espera revisión del encargado. */
data class RecepcionPendiente(
    val id: Int,
    val codigo: String,
    val proveedor: String,
    val guia: String,
    val fechaEnvio: Long?
)

/** Totales de una recepción, para el resumen y el progreso. */
data class TotalesRecepcion(
    val esperadas: Int = 0,
    val recibidas: Int = 0,
    val entran: Int = 0,      // unidades buenas que entran al stock
    val faltan: Int = 0,
    val sobran: Int = 0,
    val danadas: Int = 0,
    val completos: Int = 0,   // productos completos
    val productosFaltan: Int = 0,
    val productosSobran: Int = 0,
    val productosDanados: Int = 0
)

fun List<LineaRecepcion>.totales() = TotalesRecepcion(
    esperadas = sumOf { it.esperado },
    recibidas = sumOf { it.recibido },
    entran = sumOf { it.buenas },
    faltan = sumOf { it.faltan },
    sobran = sumOf { it.sobran },
    danadas = sumOf { it.danado },
    completos = count { it.completo && it.esperado > 0 },
    productosFaltan = count { it.faltan > 0 },
    productosSobran = count { it.sobran > 0 },
    productosDanados = count { it.danado > 0 }
)

/** Línea de caja con problema (faltan, sobran o dañados) y lo que decidió el encargado; la ve Gerencia. */
data class IncidenciaCaja(
    val detalleId: Int,
    val recepcionId: Int,
    val codigo: String,
    val proveedor: String,
    val sucursalId: Int,
    val productoId: Int,
    val nombre: String,
    val esperado: Int,
    val recibido: Int,
    val danado: Int,
    val motivoDanado: String?,
    val decision: String?,
    val fecha: Long?
)
