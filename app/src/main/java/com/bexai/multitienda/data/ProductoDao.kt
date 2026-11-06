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

    @Query("UPDATE producto SET stock = :stock WHERE id = :id")
    suspend fun fijarStock(id: Int, stock: Int)

    @Query("SELECT * FROM producto ORDER BY nombre")
    fun listar(): Flow<List<Producto>>

    @Query("SELECT * FROM producto WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Producto?

    @Query("SELECT COUNT(*) FROM producto")
    suspend fun contar(): Int

    @Query("SELECT SUM(stock) FROM producto")
    suspend fun sumaStock(): Int?

    @Query("SELECT * FROM producto WHERE stock = 0 ORDER BY nombre")
    fun listarEnQuiebre(): Flow<List<Producto>>

    @Query("SELECT * FROM producto WHERE stock < :minimo AND stock > 0 ORDER BY stock ASC")
    fun listarBajoMinimo(minimo: Int = 10): Flow<List<Producto>>

    @Query("UPDATE producto SET nombre = :nombre, stock = :stock, codigoBarras = :codigo, categoria = :categoria WHERE id = :id")
    suspend fun actualizar(id: Int, nombre: String, stock: Int, codigo: String, categoria: String)

    @Query("DELETE FROM producto WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}