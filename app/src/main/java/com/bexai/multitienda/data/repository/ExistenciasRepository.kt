package com.bexai.multitienda.data.repository

import androidx.room.withTransaction
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.model.StockSucursal

/**
 * ExistenciasRepository: único lugar donde cambia el stock.
 *
 * Todo movimiento (entrada, salida, ajuste, conteo, recepción) pasa por aquí con su sucursal.
 * Actualiza el stock de esa sucursal y, en la misma transacción, el total global del producto
 * (`Producto.stock`), así el total siempre es la suma de las sucursales.
 */
class ExistenciasRepository(private val db: AppDatabase) {

    private val stockDao = db.stockSucursalDao()
    private val productoDao = db.productoDao()

    /** Stock del producto en la sucursal (0 si aún no tiene registro). */
    suspend fun cantidad(productoId: Int, sucursalId: Int): Int =
        stockDao.cantidad(productoId, sucursalId) ?: 0

    /** Suma (o resta, con delta negativo) unidades en la sucursal y en el total global. */
    suspend fun sumar(productoId: Int, sucursalId: Int, delta: Int) = db.withTransaction {
        stockDao.crearSiNoExiste(StockSucursal(productoId, sucursalId, 0))
        stockDao.sumar(productoId, sucursalId, delta)
        productoDao.sumarStock(productoId, delta)
    }

    /** Deja el stock de la sucursal en [cantidad] y corrige el total global con la diferencia. */
    suspend fun fijar(productoId: Int, sucursalId: Int, cantidad: Int) = db.withTransaction {
        val actual = stockDao.cantidad(productoId, sucursalId) ?: 0
        sumar(productoId, sucursalId, cantidad - actual)
    }
}
