package com.bexai.multitienda.model

/** Totales del día en una sucursal (tarjeta de Inicio del operador). Lo calcula MovimientoDao.resumenDelDia. */
data class ResumenDia (
    val entradas: Int,
    val salidas: Int,
    val movimientos: Int,
    val contados: Int
)