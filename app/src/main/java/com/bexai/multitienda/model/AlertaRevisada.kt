package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AlertaRevisada: marca de Gerencia en una alerta (mínimo, descalce o caja).
 * [clave] identifica la alerta, por ejemplo "MIN:3:1" (producto 3, sucursal 1). Las
 * reposiciones guardan su propio "revisada" en [Reposicion].
 */
@Entity
data class AlertaRevisada(
    @PrimaryKey val clave: String,
    val fecha: Long,
    val nota: String = ""
)
