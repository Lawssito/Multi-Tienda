package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Conteo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sucursalId: Int,
    val estado: EstadoConteo = EstadoConteo.EN_CURSO,
    val fechaInicio: Long,        // milisegundos
    val fechaEnvio: Long? = null  // se llena al enviarlo al encargado
)
