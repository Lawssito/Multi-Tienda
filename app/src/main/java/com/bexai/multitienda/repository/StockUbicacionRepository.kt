package com.bexai.multitienda.repository

import com.bexai.multitienda.data.StockUbicacionDao
import com.bexai.multitienda.model.StockUbicacion
import kotlinx.coroutines.flow.Flow

/**
 * StockUbicacionRepository: cantidades de productos por sucursal.
 *
 * Room entrega las listas como Flow; las operaciones que escriben son
 * funciones suspend para llamarlas desde un viewModelScope.
 */
class StockUbicacionRepository(
    private val stockUbicacionDao: StockUbicacionDao
) {

    fun listar(): Flow<List<StockUbicacion>> = stockUbicacionDao.listar()

    fun listarPorUbicacion(ubicacionId: Int): Flow<List<StockUbicacion>> {
        return stockUbicacionDao.listarPorUbicacion(ubicacionId)
    }

    suspend fun buscar(productoId: Int, ubicacionId: Int): StockUbicacion? {
        return stockUbicacionDao.buscar(productoId, ubicacionId)
    }

    suspend fun guardar(stock: StockUbicacion) = stockUbicacionDao.insertar(stock)

    suspend fun actualizar(stock: StockUbicacion) = stockUbicacionDao.actualizar(stock)

    suspend fun eliminar(stock: StockUbicacion) = stockUbicacionDao.eliminar(stock)
}
