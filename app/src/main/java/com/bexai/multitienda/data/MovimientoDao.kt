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

    @Query("DELETE FROM movimiento WHERE id = :id")
    suspend fun eliminar(id: Int)

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE 0 END), 0) AS entradas,
            COALESCE(SUM(CASE WHEN tipo = 'SALIDA' THEN cantidad ELSE 0 END), 0) AS salidas,
            COUNT(*) AS movimientos,
            COALESCE(SUM(CASE WHEN tipo = 'AJUSTE' AND motivo = 'Conteo masivo' THEN 1 ELSE 0 END), 0) AS contados
        FROM movimiento
        WHERE ubicacionId = :ubicacionId AND fechaHora >= :desde
    """)
    fun resumenDelDia(ubicacionId: Int, desde: Long): Flow<ResumenDia>
}