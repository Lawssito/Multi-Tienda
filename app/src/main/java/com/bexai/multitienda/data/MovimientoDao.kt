package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.ResumenDia
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {
    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    @Query("UPDATE movimiento SET cantidad = :cantidad WHERE id = :id")
    suspend fun actualizarCantidad(id: Int, cantidad: Int)

    @Query("UPDATE movimiento SET cantidad = :cantidad, motivo = :motivo WHERE id = :id")
    suspend fun actualizarAjuste(id: Int, cantidad: Int, motivo: String)

    // Ajustes manuales del operador (sin los del conteo masivo) desde una fecha, el más reciente primero
    @Query("""
        SELECT * FROM movimiento
        WHERE sucursalId = :sucursalId AND tipo = 'AJUSTE'
          AND (motivo IS NULL OR motivo != 'Conteo masivo') AND fechaHora >= :desde
        ORDER BY fechaHora DESC LIMIT 20
    """)
    fun ajustesDesde(sucursalId: Int, desde: Long): Flow<List<Movimiento>>

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
}
