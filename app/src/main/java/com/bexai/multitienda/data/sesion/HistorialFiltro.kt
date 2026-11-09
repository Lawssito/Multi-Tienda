package com.bexai.multitienda.data.sesion

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * HistorialFiltro: filtro que deja "Ver historial" (en Alertas de Gerencia) para que el
 * Historial se abra ya filtrado por ese producto y sucursal. El Historial lo lee y lo limpia.
 */
object HistorialFiltro {
    /** (productoId, sucursalId) pendiente de aplicar, o null. */
    val pendiente = MutableStateFlow<Pair<Int, Int>?>(null)
}
