package com.bexai.multitienda.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.Usuario


@Database(entities = [Usuario::class, Producto::class, Movimiento::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context) : AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { instancia = it }
            }
    }
}