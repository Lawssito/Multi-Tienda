package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.ConteoDetalle
import kotlinx.coroutines.flow.Flow

@Dao
interface ConteoDao {
    @Insert
    suspend fun insertar(conteo: Conteo): Long

    @Insert
    suspend fun insertarDetalle(detalle: ConteoDetalle)

    @Insert
    suspend fun insertarDetalles(detalles: List<ConteoDetalle>)

    @Query("SELECT * FROM conteo WHERE id = :id")
    suspend fun buscarPorId(id: Int): Conteo?

    @Query("SELECT * FROM conteo WHERE ubicacionId = :ubicacionId ORDER BY fechaInicio DESC")
    fun listarPorUbicacion(ubicacionId: Int): Flow<List<Conteo>>

    @Query("SELECT * FROM conteo ORDER BY fechaInicio DESC LIMIT 20")
    fun listarUltimos(): Flow<List<Conteo>>

    @Query("UPDATE conteo SET estado = :estado, fechaCierre = :fechaCierre WHERE id = :id")
    suspend fun cerrar(id: Int, estado: String, fechaCierre: Long)

    @Query("SELECT * FROM conteodetalle WHERE conteoId = :conteoId")
    fun listarDetalles(conteoId: Int): Flow<List<ConteoDetalle>>

    @Query("SELECT * FROM conteodetalle WHERE conteoId = :conteoId")
    suspend fun obtenerDetalles(conteoId: Int): List<ConteoDetalle>
}
