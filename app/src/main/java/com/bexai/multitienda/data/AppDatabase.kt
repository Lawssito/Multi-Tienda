package com.bexai.multitienda.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.StockUbicacion
import com.bexai.multitienda.model.Ubicacion
import com.bexai.multitienda.model.Usuario

/**
 * AppDatabase: conexión única con la base de datos Room.
 *
 * Sigue el mismo patrón del ejemplo del profesor:
 * - @Database indica las entidades y la versión del esquema.
 * - El companion object guarda una sola instancia (singleton).
 * - getDatabase(context) crea la base de datos si todavía no existe.
 *
 * La versión 2 agrega la tabla productos. La migración conserva los datos
 * de usuarios, sucursales y stock que ya existieran en la versión 1.
 */
@Database(
    entities = [Usuario::class, Ubicacion::class, StockUbicacion::class, Producto::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun stockUbicacionDao(): StockUbicacionDao
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migración 1 -> 2: crea el catálogo de productos.
         * El stock por sucursal ya vive en stock_ubicacion desde la versión 1.
         */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `productos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nombre` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multitienda.db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}
