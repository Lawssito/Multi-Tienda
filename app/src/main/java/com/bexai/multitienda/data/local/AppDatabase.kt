package com.bexai.multitienda.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bexai.multitienda.data.dao.AlertaRevisadaDao
import com.bexai.multitienda.data.dao.ConteoDao
import com.bexai.multitienda.data.dao.DespachoDao
import com.bexai.multitienda.data.dao.MovimientoDao
import com.bexai.multitienda.data.dao.ProductoDao
import com.bexai.multitienda.data.dao.RecepcionDao
import com.bexai.multitienda.data.dao.ReposicionDao
import com.bexai.multitienda.data.dao.StockSucursalDao
import com.bexai.multitienda.data.dao.SucursalDao
import com.bexai.multitienda.data.dao.UsuarioDao
import com.bexai.multitienda.model.AlertaRevisada
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.ConteoDetalle
import com.bexai.multitienda.model.Despacho
import com.bexai.multitienda.model.DespachoDetalle
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.Recepcion
import com.bexai.multitienda.model.RecepcionDetalle
import com.bexai.multitienda.model.Reposicion
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.model.Usuario
import java.util.Date

// =====================================================================
// Base de datos Room de la app: convertidores de tipos y la clase AppDatabase (Singleton).
// =====================================================================

/** Convierte Date <-> Long para que Room pueda guardar fechas. */
class ConvertidoresAe {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

/**
 * AppDatabase: base Room "app_database" con todas las entidades y sus DAO.
 * Si cambia el esquema se sube [version]; con fallbackToDestructiveMigration la base se
 * recrea (y DemoDataSeeder vuelve a cargar los datos de prueba).
 */
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

        /**
         * Devuelve la única instancia de la base (patrón Singleton con doble verificación):
         * todas las pantallas comparten la misma conexión a SQLite.
         */
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build().also { instancia = it }
            }
    }
}
