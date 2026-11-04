package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bexai.multitienda.model.StockUbicacion
import kotlinx.coroutines.flow.Flow

@Dao
interface StockUbicacionDao {

    @Query("SELECT * FROM stock_ubicacion ORDER BY ubicacionId, productoId")
    fun listar(): Flow<List<StockUbicacion>>

    @Query(
        "SELECT * FROM stock_ubicacion " +
            "WHERE ubicacionId = :ubicacionId ORDER BY productoId"
    )
    fun listarPorUbicacion(ubicacionId: Int): Flow<List<StockUbicacion>>

    @Query(
        "SELECT * FROM stock_ubicacion " +
            "WHERE productoId = :productoId AND ubicacionId = :ubicacionId LIMIT 1"
    )
    suspend fun buscar(productoId: Int, ubicacionId: Int): StockUbicacion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(stock: StockUbicacion)

    @Update
    suspend fun actualizar(stock: StockUbicacion)

    @Delete
    suspend fun eliminar(stock: StockUbicacion)

    @Query("DELETE FROM stock_ubicacion WHERE productoId = :productoId")
    suspend fun eliminarPorProducto(productoId: Int)

    @Query("DELETE FROM stock_ubicacion WHERE ubicacionId = :ubicacionId")
    suspend fun eliminarPorUbicacion(ubicacionId: Int)
}
