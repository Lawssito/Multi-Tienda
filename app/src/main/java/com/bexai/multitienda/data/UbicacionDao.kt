package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bexai.multitienda.model.Ubicacion
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {
    @Insert
    suspend fun insertar(ubicacion: Ubicacion): Long

    @Query("SELECT * FROM ubicaciones ORDER BY nombre")
    fun listar(): Flow<List<Ubicacion>>

    @Query("SELECT * FROM ubicaciones WHERE id = :id")
    suspend fun buscarPorId(id: Int): Ubicacion?

    @Query("UPDATE ubicaciones SET activa = :activa WHERE id = :id")
    suspend fun actualizarActiva(id: Int, activa: Boolean)

    @Update
    suspend fun actualizar(ubicacion: Ubicacion)

    @Query("DELETE FROM ubicaciones WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT COUNT(*) FROM ubicaciones")
    suspend fun contar(): Int
}
