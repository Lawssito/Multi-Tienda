package com.bexai.multitienda.model

/** Fila lista para mostrar en la pantalla de conteo: detalle + datos del producto. */
data class LineaConteo(
    val productoId: Int,
    val nombre: String,
    val codigoBarras: String,
    val contado: Int,
    val sistema: Int
) {
    val diferencia: Int get() = contado - sistema   // negativo = falta, positivo = sobra
}

/** Línea de un conteo enviado cuya cantidad contada no coincide con el sistema (para las alertas de Gerencia). */
data class DescalceConteo(
    val conteoId: Int,
    val sucursalId: Int,
    val productoId: Int,
    val nombre: String,
    val sistema: Int,
    val contado: Int,
    val fecha: Long?
)
