package com.bexai.multitienda.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Reposicion
import kotlinx.coroutines.flow.Flow

/** Acceso a la tabla Reposicion: pedidos de reposición del encargado y su resolución por Gerencia. */
@Dao
interface ReposicionDao {
    @Insert
    suspend fun insertar(reposicion: Reposicion): Long

    @Query("SELECT * FROM Reposicion ORDER BY fecha DESC")
    fun todas(): Flow<List<Reposicion>>

    // Reposiciones de una sucursal (el filtro lo hace SQLite, no la app)
    @Query("SELECT * FROM Reposicion WHERE sucursalId = :sucursalId ORDER BY fecha DESC")
    fun porSucursal(sucursalId: Int): Flow<List<Reposicion>>

    // Gerencia aprueba (con el código del despacho) o rechaza (con el motivo)
    @Query("UPDATE Reposicion SET estado = :estado, revisada = 1, motivoRechazo = :motivo, despachoCodigo = :codigo WHERE id = :id")
    suspend fun resolver(id: Int, estado: String, motivo: String?, codigo: String?)

    @Query("DELETE FROM Reposicion WHERE id = :id AND estado != 'PENDIENTE'")
    suspend fun eliminarResuelta(id: Int)
}
