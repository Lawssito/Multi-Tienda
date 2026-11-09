package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.ConteoDetalle
import com.bexai.multitienda.model.DescalceConteo
import com.bexai.multitienda.model.EstadoConteo
import com.bexai.multitienda.model.LineaConteo
import kotlinx.coroutines.flow.Flow

@Dao
interface ConteoDao {
    @Insert
    suspend fun insertar(conteo: Conteo): Long

    @Query("SELECT * FROM Conteo WHERE sucursalId = :sucursalId AND estado = 'EN_CURSO' LIMIT 1")
    suspend fun enCurso(sucursalId: Int): Conteo?

    @Query("SELECT * FROM ConteoDetalle WHERE conteoId = :conteoId AND productoId = :productoId LIMIT 1")
    suspend fun buscarDetalle(conteoId: Int, productoId: Int): ConteoDetalle?

    // Si ya existe la línea (mismo conteo y producto) la reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarDetalle(detalle: ConteoDetalle)

    // Congela el stock de todos los productos al iniciar el conteo (contado queda en null)
    @Query("""
        INSERT INTO ConteoDetalle (conteoId, productoId, contado, sistema, actualizado)
        SELECT :conteoId, p.id, NULL, COALESCE(s.cantidad, 0), 0
        FROM producto p
        LEFT JOIN StockSucursal s ON s.productoId = p.id AND s.sucursalId = :sucursalId
    """)
    suspend fun congelarStock(conteoId: Int, sucursalId: Int)

    // Descartar: la línea vuelve a "sin contar", pero conserva su stock congelado
    @Query("""
        UPDATE ConteoDetalle SET contado = NULL, actualizado = 0
        WHERE conteoId = :conteoId AND productoId = :productoId
    """)
    suspend fun descontar(conteoId: Int, productoId: Int)

    // Recontar: la línea conserva su valor, pero el próximo escaneo vuelve a empezar desde 1
    // (-1 = ConteoDetalle.RECONTAR)
    @Query("""
        UPDATE ConteoDetalle SET actualizado = -1
        WHERE conteoId = :conteoId AND productoId = :productoId
    """)
    suspend fun marcarRecontar(conteoId: Int, productoId: Int)

    @Query("""
        SELECT d.productoId AS productoId, p.nombre AS nombre, p.codigoBarras AS codigoBarras,
               COALESCE(d.contado, 0) AS contado, d.sistema AS sistema
        FROM ConteoDetalle d
        INNER JOIN producto p ON p.id = d.productoId
        WHERE d.conteoId = :conteoId AND d.contado IS NOT NULL
        ORDER BY d.actualizado DESC
    """)
    fun lineas(conteoId: Int): Flow<List<LineaConteo>>

    // Total de productos del conteo (los congelados al iniciar)
    @Query("SELECT COUNT(*) FROM ConteoDetalle WHERE conteoId = :conteoId")
    fun totalLineas(conteoId: Int): Flow<Int>

    // Último conteo que el operador envió (aprobado, rechazado o aún pendiente) de la sucursal
    @Query("SELECT * FROM Conteo WHERE sucursalId = :sucursalId AND estado != 'EN_CURSO' ORDER BY fechaEnvio DESC LIMIT 1")
    fun ultimoEnviadoOAprobado(sucursalId: Int): Flow<Conteo?>

    // Diferencias de todos los conteos ya enviados, de todas las sucursales (para Gerencia)
    @Query("""
        SELECT c.id AS conteoId, c.sucursalId AS sucursalId, d.productoId AS productoId, p.nombre AS nombre,
               d.sistema AS sistema, d.contado AS contado, c.fechaEnvio AS fecha
        FROM ConteoDetalle d
        INNER JOIN Conteo c ON c.id = d.conteoId
        INNER JOIN producto p ON p.id = d.productoId
        WHERE c.estado != 'EN_CURSO' AND d.contado IS NOT NULL AND d.contado != d.sistema
        ORDER BY c.fechaEnvio DESC
        LIMIT 100
    """)
    fun descalces(): Flow<List<DescalceConteo>>

    @Query("SELECT * FROM Conteo WHERE id = :id")
    suspend fun buscar(id: Int): Conteo?

    // Conteos esperando revisión del encargado, el más reciente primero
    @Query("SELECT * FROM Conteo WHERE sucursalId = :sucursalId AND estado = 'ENVIADO' ORDER BY fechaEnvio DESC")
    fun enviados(sucursalId: Int): Flow<List<Conteo>>

    @Query("SELECT * FROM Conteo WHERE sucursalId = :sucursalId AND estado = 'ENVIADO' ORDER BY fechaEnvio DESC LIMIT 1")
    suspend fun ultimoEnviado(sucursalId: Int): Conteo?

    // Igual que lineas(), pero una sola lectura (para aprobar dentro de una transacción)
    @Query("""
        SELECT d.productoId AS productoId, p.nombre AS nombre, p.codigoBarras AS codigoBarras,
               COALESCE(d.contado, 0) AS contado, d.sistema AS sistema
        FROM ConteoDetalle d
        INNER JOIN producto p ON p.id = d.productoId
        WHERE d.conteoId = :conteoId AND d.contado IS NOT NULL
    """)
    suspend fun lineasContadas(conteoId: Int): List<LineaConteo>

    @Query("UPDATE Conteo SET estado = :estado, fechaEnvio = :fechaEnvio WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: EstadoConteo, fechaEnvio: Long?)
}
