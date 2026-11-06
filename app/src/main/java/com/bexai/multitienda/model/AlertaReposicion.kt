package com.bexai.multitienda.model

import java.util.UUID

/**
 * Representa una alerta de reposición de mercadería declarada por el Encargado de bodega.
 */
data class AlertaReposicion(
    val id: String = UUID.randomUUID().toString(),
    val productoId: Int,
    val nombreProducto: String,
    val categoria: String = "",
    val stockActual: Int,
    val cantidadMinima: Int,
    val motivo: String,
    val urgencia: String = "MEDIA", // ALTA, MEDIA, BAJA
    val fechaHora: Long = System.currentTimeMillis()
)
