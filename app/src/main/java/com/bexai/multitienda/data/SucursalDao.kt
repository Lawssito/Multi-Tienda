package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.flow.Flow

@Dao
interface SucursalDao {
    @Insert
    suspend fun insertar(sucursal: Sucursal): Long

    @Query("SELECT * FROM sucursales ORDER BY nombre")
    fun listar(): Flow<List<Sucursal>>

    @Query("SELECT * FROM sucursales WHERE id = :id")
    suspend fun buscarPorId(id: Int): Sucursal?

    @Query("UPDATE sucursales SET activa = :activa WHERE id = :id")
    suspend fun actualizarActiva(id: Int, activa: Boolean)

    @Update
    suspend fun actualizar(sucursal: Sucursal)

    @Query("DELETE FROM sucursales WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT COUNT(*) FROM sucursales")
    suspend fun contar(): Int
}
