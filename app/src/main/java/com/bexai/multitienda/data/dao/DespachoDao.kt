package com.bexai.multitienda.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bexai.multitienda.model.Despacho
import com.bexai.multitienda.model.DespachoDetalle
import com.bexai.multitienda.model.ResumenDespacho

/** Acceso a Despacho y DespachoDetalle: las cajas que se envían a una sucursal y lo que traen. */
@Dao
interface DespachoDao {
    @Insert
    suspend fun insertar(despacho: Despacho): Long

    @Insert
    suspend fun insertarDetalle(detalle: DespachoDetalle)

    // El QR se compara sin distinguir mayúsculas ni espacios sobrantes
    @Query("SELECT * FROM Despacho WHERE UPPER(codigo) = UPPER(:codigo) LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Despacho?

    @Query("SELECT * FROM Despacho WHERE id = :id")
    suspend fun buscar(id: Int): Despacho?

    @Query("SELECT * FROM DespachoDetalle WHERE despachoId = :despachoId")
    suspend fun detalles(despachoId: Int): List<DespachoDetalle>

    @Query("""
        SELECT COUNT(*) AS productos, COALESCE(SUM(esperado), 0) AS unidades
        FROM DespachoDetalle WHERE despachoId = :despachoId
    """)
    suspend fun resumen(despachoId: Int): ResumenDespacho

    // Código del último despacho creado (para generar el siguiente DSP-00NN)
    @Query("SELECT codigo FROM Despacho ORDER BY id DESC LIMIT 1")
    suspend fun ultimoCodigo(): String?

    @Query("SELECT COUNT(*) FROM Despacho")
    suspend fun contar(): Int
}
