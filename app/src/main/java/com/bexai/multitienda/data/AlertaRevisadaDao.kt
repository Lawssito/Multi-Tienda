package com.bexai.multitienda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bexai.multitienda.model.AlertaRevisada
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaRevisadaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(marca: AlertaRevisada)

    @Query("SELECT * FROM AlertaRevisada")
    fun todas(): Flow<List<AlertaRevisada>>
}
