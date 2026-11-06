package com.bexai.multitienda.data

import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.model.Sucursal
import java.util.Date

/**
 * Seeder inicial para garantizar que la base de datos cuente con tiendas,
 * catálogo diverso con quiebres y movimientos para auditoría.
 */
object DemoDataSeeder {
    suspend fun sembrar(db: AppDatabase) {
        // 1. Tiendas iniciales
        if (db.sucursalDao().contar() == 0) {
            db.sucursalDao().insertar(
                Sucursal(
                    nombre = "Sucursal Centro",
                    direccion = "Av. Libertador Bernardo O'Higgins 1020, Santiago",
                    activa = true,
                    fechaCreacion = Date()
                )
            )
            db.sucursalDao().insertar(
                Sucursal(
                    nombre = "Sucursal Norte",
                    direccion = "Av. Recoleta 450, Recoleta",
                    activa = true,
                    fechaCreacion = Date()
                )
            )
            db.sucursalDao().insertar(
                Sucursal(
                    nombre = "Bodega Central",
                    direccion = "Av. Las Industrias 880, San Joaquín",
                    activa = true,
                    fechaCreacion = Date()
                )
            )
        }

        // 2. Productos de prueba con categorías y niveles variados de stock
        val productosDemo = listOf(
            Producto(nombre = "Cuaderno universitario 100 hojas", stock = 48, codigoBarras = "7800000000016", categoria = "Papelería"),
            Producto(nombre = "Lápiz grafito HB", stock = 120, codigoBarras = "7800000000023", categoria = "Librería"),
            Producto(nombre = "Resma de papel carta 500 hojas", stock = 30, codigoBarras = "7800000000030", categoria = "Papelería"),
            Producto(nombre = "Café Tostado Grano 500g", stock = 4, codigoBarras = "7800000000047", categoria = "Abarrotes"),
            Producto(nombre = "Aceite Maravilla 1L", stock = 0, codigoBarras = "7800000000054", categoria = "Abarrotes"),
            Producto(nombre = "Detergente Líquido 3L", stock = 3, codigoBarras = "7800000000061", categoria = "Limpieza"),
            Producto(nombre = "Arroz Grado 1 1kg", stock = 65, codigoBarras = "7800000000078", categoria = "Abarrotes"),
            Producto(nombre = "Leche Entera 1L", stock = 0, codigoBarras = "7800000000085", categoria = "Lácteos"),
            Producto(nombre = "Jabón Líquido Antibacterial 500ml", stock = 15, codigoBarras = "7800000000092", categoria = "Limpieza")
        )

        for (prod in productosDemo) {
            val existente = db.productoDao().buscarPorCodigo(prod.codigoBarras)
            if (existente == null) {
                db.productoDao().insertar(prod)
            }
        }

        // 3. Movimientos iniciales para auditoría si la tabla está vacía
        val pArroz = db.productoDao().buscarPorCodigo("7800000000078")
        val pCuaderno = db.productoDao().buscarPorCodigo("7800000000016")
        val pCafe = db.productoDao().buscarPorCodigo("7800000000047")
        val pAceite = db.productoDao().buscarPorCodigo("7800000000054")

        val horaBase = System.currentTimeMillis() - 86400000L // ayer

        if (pArroz != null && pCuaderno != null && pCafe != null && pAceite != null) {
            // Se insertan movimientos iniciales de demostración
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = pArroz.id,
                    sucursalId = 1,
                    tipo = TipoMovimiento.ENTRADA,
                    cantidad = 65,
                    motivo = "Recepción proveedor Abarrotes SA",
                    fechaHora = horaBase + 3600000L
                )
            )
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = pCuaderno.id,
                    sucursalId = 1,
                    tipo = TipoMovimiento.ENTRADA,
                    cantidad = 50,
                    motivo = "Ingreso de bodega central",
                    fechaHora = horaBase + 7200000L
                )
            )
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = pCafe.id,
                    sucursalId = 1,
                    tipo = TipoMovimiento.SALIDA,
                    cantidad = 6,
                    motivo = "Despacho a sucursal norte",
                    fechaHora = System.currentTimeMillis() - 7200000L
                )
            )
            db.movimientoDao().insertar(
                Movimiento(
                    productoId = pAceite.id,
                    sucursalId = 1,
                    tipo = TipoMovimiento.AJUSTE,
                    cantidad = 0,
                    motivo = "Ajuste por conteo físico: stock agotado",
                    fechaHora = System.currentTimeMillis() - 3600000L
                )
            )
        }
    }
}
