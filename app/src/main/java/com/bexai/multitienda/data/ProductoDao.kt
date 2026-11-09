package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bexai.multitienda.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(producto: Producto)

    @Query("SELECT * FROM producto WHERE codigoBarras = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Producto?

    // delta positivo suma stock, negativo resta
    @Query("UPDATE producto SET stock = stock + :delta WHERE id = :id")
    suspend fun sumarStock(id: Int, delta: Int)

    @Query("SELECT * FROM producto ORDER BY nombre")
    fun listar(): Flow<List<Producto>>

    @Query("SELECT COUNT(*) FROM producto")
    suspend fun contar(): Int

    @Query("UPDATE producto SET stockMinimo = :minimo WHERE id = :id")
    suspend fun actualizarMinimo(id: Int, minimo: Int)
}
