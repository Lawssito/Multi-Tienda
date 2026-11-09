package com.bexai.multitienda.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bexai.multitienda.model.EstadoRecepcion
import com.bexai.multitienda.model.IncidenciaCaja
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.Recepcion
import com.bexai.multitienda.model.RecepcionDetalle
import com.bexai.multitienda.model.RecepcionPendiente
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a Recepcion y RecepcionDetalle: la recepción de una caja en curso, sus líneas
 * (esperado, recibido y dañado), las pendientes de revisar y las incidencias para Gerencia.
 */
@Dao
interface RecepcionDao {
    @Insert
    suspend fun insertar(recepcion: Recepcion): Long

    @Insert
    suspend fun insertarDetalle(detalle: RecepcionDetalle): Long

    @Update
    suspend fun actualizarDetalle(detalle: RecepcionDetalle)

    @Delete
    suspend fun eliminarDetalle(detalle: RecepcionDetalle)

    @Query("SELECT * FROM Recepcion WHERE id = :id")
    suspend fun buscar(id: Int): Recepcion?

    @Query("""
        SELECT * FROM Recepcion
        WHERE despachoId = :despachoId AND sucursalId = :sucursalId AND estado = 'EN_CURSO' LIMIT 1
    """)
    suspend fun enCurso(despachoId: Int, sucursalId: Int): Recepcion?

    @Query("SELECT * FROM RecepcionDetalle WHERE recepcionId = :recepcionId AND productoId = :productoId LIMIT 1")
    suspend fun buscarDetalle(recepcionId: Int, productoId: Int): RecepcionDetalle?

    @Query("SELECT * FROM RecepcionDetalle WHERE id = :id")
    suspend fun buscarDetallePorId(id: Int): RecepcionDetalle?

    @Query("""
        SELECT d.id AS detalleId, d.productoId AS productoId, p.nombre AS nombre,
               p.codigoBarras AS codigoBarras, d.esperado AS esperado, d.recibido AS recibido,
               d.danado AS danado, d.motivoDanado AS motivoDanado, d.nota AS nota, d.decision AS decision
        FROM RecepcionDetalle d
        INNER JOIN producto p ON p.id = d.productoId
        WHERE d.recepcionId = :recepcionId
        ORDER BY p.nombre
    """)
    fun lineas(recepcionId: Int): Flow<List<LineaRecepcion>>

    @Query("""
        SELECT d.id AS detalleId, d.productoId AS productoId, p.nombre AS nombre,
               p.codigoBarras AS codigoBarras, d.esperado AS esperado, d.recibido AS recibido,
               d.danado AS danado, d.motivoDanado AS motivoDanado, d.nota AS nota, d.decision AS decision
        FROM RecepcionDetalle d
        INNER JOIN producto p ON p.id = d.productoId
        WHERE d.recepcionId = :recepcionId
        ORDER BY p.nombre
    """)
    suspend fun lineasUnaVez(recepcionId: Int): List<LineaRecepcion>

    @Query("UPDATE Recepcion SET estado = :estado, fechaEnvio = :fechaEnvio WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: EstadoRecepcion, fechaEnvio: Long?)

    @Query("UPDATE RecepcionDetalle SET decision = :decision WHERE id = :detalleId")
    suspend fun guardarDecision(detalleId: Int, decision: String?)

    // Recepciones enviadas esperando la revisión del encargado, la más reciente primero
    @Query("""
        SELECT r.id AS id, d.codigo AS codigo, d.proveedor AS proveedor, d.guia AS guia,
               r.fechaEnvio AS fechaEnvio
        FROM Recepcion r
        INNER JOIN Despacho d ON d.id = r.despachoId
        WHERE r.sucursalId = :sucursalId AND r.estado = 'ENVIADA'
        ORDER BY r.fechaEnvio DESC
    """)
    fun pendientes(sucursalId: Int): Flow<List<RecepcionPendiente>>

    // Todas las líneas con problema de las cajas ya revisadas por el encargado, de todas las sucursales (para Gerencia)
    @Query("""
        SELECT dt.id AS detalleId, r.id AS recepcionId, d.codigo AS codigo, d.proveedor AS proveedor,
               r.sucursalId AS sucursalId, dt.productoId AS productoId, p.nombre AS nombre,
               dt.esperado AS esperado, dt.recibido AS recibido, dt.danado AS danado,
               dt.motivoDanado AS motivoDanado, dt.decision AS decision, r.fechaEnvio AS fecha
        FROM RecepcionDetalle dt
        INNER JOIN Recepcion r ON r.id = dt.recepcionId
        INNER JOIN Despacho d ON d.id = r.despachoId
        INNER JOIN producto p ON p.id = dt.productoId
        WHERE r.estado = 'REVISADA' AND (dt.recibido != dt.esperado OR dt.danado > 0)
        ORDER BY r.fechaEnvio DESC
        LIMIT 200
    """)
    fun incidencias(): Flow<List<IncidenciaCaja>>
}
