package com.bexai.multitienda.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.flow.Flow

/** Acceso a la tabla sucursales: listar, registrar y activar o desactivar tiendas. */
@Dao
interface SucursalDao {
    @Insert
    suspend fun insertar(sucursal: Sucursal): Long

    @Query("SELECT * FROM sucursales ORDER BY nombre")
    fun listar(): Flow<List<Sucursal>>

    @Query("UPDATE sucursales SET activa = :activa WHERE id = :id")
    suspend fun actualizarActiva(id: Int, activa: Boolean)

    @Query("SELECT COUNT(*) FROM sucursales")
    suspend fun contar(): Int
}
