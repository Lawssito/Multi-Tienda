package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.ResumenDia
import kotlinx.coroutines.flow.Flow

data class ResumenMovimientoProducto(
    val productoId: Int,
    val cantidad: Int,
    val fecha: Long
)

@Dao
interface MovimientoDao {
    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    @Query("UPDATE movimiento SET cantidad = :cantidad WHERE id = :id")
    suspend fun actualizarCantidad(id: Int, cantidad: Int)

    @Query("UPDATE movimiento SET cantidad = :cantidad, motivo = :motivo WHERE id = :id")
    suspend fun actualizarAjuste(id: Int, cantidad: Int, motivo: String)

    @Query("DELETE FROM movimiento WHERE id = :id")
    suspend fun eliminar(id: Int)

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE 0 END), 0) AS entradas,
            COALESCE(SUM(CASE WHEN tipo = 'SALIDA' THEN cantidad ELSE 0 END), 0) AS salidas,
            COUNT(*) AS movimientos,
            COALESCE(SUM(CASE WHEN tipo = 'AJUSTE' AND motivo = 'Conteo masivo' THEN 1 ELSE 0 END), 0) AS contados
        FROM movimiento
        WHERE sucursalId = :sucursalId AND fechaHora >= :desde
    """)
    fun resumenDelDia(sucursalId: Int, desde: Long): Flow<ResumenDia>

    @Query("SELECT * FROM movimiento ORDER BY fechaHora DESC LIMIT :limite")
    fun listarHistorial(limite: Int = 100): Flow<List<Movimiento>>

    @Query("SELECT * FROM movimiento WHERE sucursalId = :sucursalId ORDER BY fechaHora DESC LIMIT :limite")
    fun listarPorSucursal(sucursalId: Int, limite: Int = 100): Flow<List<Movimiento>>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE 0 END), 0) AS entradas,
            COALESCE(SUM(CASE WHEN tipo = 'SALIDA' THEN cantidad ELSE 0 END), 0) AS salidas,
            COUNT(*) AS movimientos,
            COALESCE(SUM(CASE WHEN tipo = 'AJUSTE' AND motivo = 'Conteo masivo' THEN 1 ELSE 0 END), 0) AS contados
        FROM movimiento
        WHERE fechaHora >= :desde
    """)
    fun resumenGlobal(desde: Long): Flow<ResumenDia>

    @Query("""
        SELECT productoId, COUNT(*) AS cantidad, MAX(fechaHora) AS fecha FROM movimiento
        GROUP BY productoId
        ORDER BY fecha DESC
    """)
    fun listarMovimientosPorProducto(): Flow<List<ResumenMovimientoProducto>>
}
