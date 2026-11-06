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
