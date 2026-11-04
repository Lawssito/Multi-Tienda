package com.bexai.multitienda.repository

import androidx.room.withTransaction
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.ProductoStock
import com.bexai.multitienda.model.StockUbicacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * ProductoRepository: acceso al catálogo y al stock por sucursal.
 *
 * El catálogo (Producto) y las cantidades (StockUbicacion) se guardan en
 * tablas distintas. Al agregar un producto se crea también su fila de stock
 * para la sucursal seleccionada. Al eliminarlo se borran sus cantidades.
 * Las operaciones que tocan las dos tablas se hacen en una transacción.
 */
class ProductoRepository(
    private val baseDeDatos: AppDatabase
) {

    private val productoDao = baseDeDatos.productoDao()
    private val stockUbicacionDao = baseDeDatos.stockUbicacionDao()

    // Evita que dos pantallas creen los productos de ejemplo al mismo tiempo
    private val mutex = Mutex()

    fun listar(): Flow<List<Producto>> = productoDao.listar()

    /**
     * Productos con la cantidad que tienen en [ubicacionId].
     */
    fun listarConStock(ubicacionId: Int): Flow<List<ProductoStock>> {
        return productoDao.listarConStock(ubicacionId)
    }

    /**
     * Crea el producto en el catálogo y su stock inicial en la sucursal.
     */
    suspend fun agregar(nombre: String, cantidad: Int, ubicacionId: Int) {
        baseDeDatos.withTransaction {
            val id = productoDao.insertar(Producto(nombre = nombre))
            stockUbicacionDao.insertar(
                StockUbicacion(
                    productoId = id.toInt(),
                    ubicacionId = ubicacionId,
                    cantidad = cantidad
                )
            )
        }
    }

    /**
     * Elimina el producto del catálogo y todas sus cantidades por sucursal.
     */
    suspend fun eliminar(productoId: Int) {
        baseDeDatos.withTransaction {
            stockUbicacionDao.eliminarPorProducto(productoId)
            productoDao.eliminarPorId(productoId)
        }
    }

    /**
     * Deja productos de ejemplo la primera vez que se abre el catálogo.
     * El stock inicial se asigna a la sucursal desde donde se abrió la
     * pantalla; las demás sucursales parten en 0.
     */
    suspend fun asegurarProductosDemo(ubicacionId: Int) {
        mutex.withLock {
            baseDeDatos.withTransaction {
                if (productoDao.contar() > 0) return@withTransaction

                val lecheId = productoDao.insertar(Producto(nombre = "Leche entera 1 L"))
                stockUbicacionDao.insertar(
                    StockUbicacion(
                        productoId = lecheId.toInt(),
                        ubicacionId = ubicacionId,
                        cantidad = 24
                    )
                )

                val panId = productoDao.insertar(Producto(nombre = "Pan tajado"))
                stockUbicacionDao.insertar(
                    StockUbicacion(
                        productoId = panId.toInt(),
                        ubicacionId = ubicacionId,
                        cantidad = 0
                    )
                )
            }
        }
    }
}
