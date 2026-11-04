package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bexai.multitienda.model.Ubicacion
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {

    @Query("SELECT * FROM ubicaciones ORDER BY nombre")
    fun listar(): Flow<List<Ubicacion>>

    @Query("SELECT * FROM ubicaciones WHERE activa = 1 ORDER BY nombre")
    fun listarActivas(): Flow<List<Ubicacion>>

    @Query("SELECT COUNT(*) FROM ubicaciones")
    suspend fun contar(): Int

    @Query("SELECT * FROM ubicaciones WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Ubicacion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ubicacion: Ubicacion): Long

    @Update
    suspend fun actualizar(ubicacion: Ubicacion)

    @Delete
    suspend fun eliminar(ubicacion: Ubicacion)
}
