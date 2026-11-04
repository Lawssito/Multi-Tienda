package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.ProductoStock
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY id")
    fun listar(): Flow<List<Producto>>

    /**
     * Catálogo con la cantidad de cada producto en una sucursal.
     * Si el producto no tiene stock registrado allí, muestra 0.
     */
    @Query(
        """
        SELECT p.id AS id,
               p.nombre AS nombre,
               COALESCE(s.cantidad, 0) AS stock
        FROM productos p
        LEFT JOIN stock_ubicacion s
               ON s.productoId = p.id AND s.ubicacionId = :ubicacionId
        ORDER BY p.id
        """
    )
    fun listarConStock(ubicacionId: Int): Flow<List<ProductoStock>>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto): Long

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}
