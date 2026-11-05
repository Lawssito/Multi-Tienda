package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bexai.multitienda.model.Producto

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
}