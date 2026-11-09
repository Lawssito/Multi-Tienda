package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bexai.multitienda.model.StockSucursal
import kotlinx.coroutines.flow.Flow

@Dao
interface StockSucursalDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun crearSiNoExiste(fila: StockSucursal)

    @Query("UPDATE StockSucursal SET cantidad = cantidad + :delta WHERE productoId = :productoId AND sucursalId = :sucursalId")
    suspend fun sumar(productoId: Int, sucursalId: Int, delta: Int)

    @Query("SELECT cantidad FROM StockSucursal WHERE productoId = :productoId AND sucursalId = :sucursalId")
    suspend fun cantidad(productoId: Int, sucursalId: Int): Int?

    @Query("SELECT * FROM StockSucursal")
    fun listarTodos(): Flow<List<StockSucursal>>

    @Query("SELECT * FROM StockSucursal WHERE sucursalId = :sucursalId")
    fun listarPorSucursal(sucursalId: Int): Flow<List<StockSucursal>>

    @Query("SELECT COUNT(*) FROM StockSucursal")
    suspend fun contar(): Int
}
