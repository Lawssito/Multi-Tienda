package com.bexai.multitienda.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.ConteoDetalle
import com.bexai.multitienda.model.Despacho
import com.bexai.multitienda.model.DespachoDetalle
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.Recepcion
import com.bexai.multitienda.model.RecepcionDetalle
import com.bexai.multitienda.model.AlertaRevisada
import com.bexai.multitienda.model.Reposicion
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.model.Usuario
import java.util.Date

class ConvertidoresAe {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

@TypeConverters(ConvertidoresAe::class)
@Database(
    entities = [
        Usuario::class,
        Producto::class,
        Movimiento::class,
        Sucursal::class,
        Conteo::class,
        ConteoDetalle::class,
        Despacho::class,
        DespachoDetalle::class,
        Recepcion::class,
        RecepcionDetalle::class,
        Reposicion::class,
        StockSucursal::class,
        AlertaRevisada::class
    ],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun productoDao(): ProductoDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun conteoDao(): ConteoDao
    abstract fun despachoDao(): DespachoDao
    abstract fun recepcionDao(): RecepcionDao
    abstract fun reposicionDao(): ReposicionDao
    abstract fun stockSucursalDao(): StockSucursalDao
    abstract fun alertaRevisadaDao(): AlertaRevisadaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            val instanciaLocal = instancia
            if (instanciaLocal != null) return instanciaLocal

            return synchronized(this) {
                val instanciaSincronizada = instancia
                if (instanciaSincronizada != null) {
                    instanciaSincronizada
                } else {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    ).fallbackToDestructiveMigration().build()
                    instancia = db
                    db
                }
            }
        }
    }
}
