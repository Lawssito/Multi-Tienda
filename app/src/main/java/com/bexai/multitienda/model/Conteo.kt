package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Conteo masivo de una sucursal: lo inicia el operador y lo aprueba o rechaza el encargado. */
@Entity
data class Conteo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sucursalId: Int,
    val estado: EstadoConteo = EstadoConteo.EN_CURSO,
    val fechaInicio: Long,        // milisegundos
    val fechaEnvio: Long? = null  // se llena al enviarlo al encargado
)
