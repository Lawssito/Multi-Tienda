package com.bexai.multitienda.data

import com.bexai.multitienda.model.Despacho
import com.bexai.multitienda.model.DespachoDetalle
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Seeder inicial para garantizar que la base de datos cuente con tiendas,
 * catálogo diverso con quiebres y movimientos para auditoría.
 */
object DemoDataSeeder {
    /**
     * Reparte el stock de cada producto entre las sucursales (Producto.stock queda como el total):
     * la primera recibe la mitad, la segunda un tercio y la última el resto.
     * Solo corre si todavía no hay stock por sucursal.
     */
    private suspend fun sembrarStockSucursal(db: AppDatabase) {
        if (db.stockSucursalDao().contar() > 0) return
        val sucursales = db.sucursalDao().listar().first()
        if (sucursales.isEmpty()) return
        db.productoDao().listar().first().forEach { p ->
            var restante = p.stock
            sucursales.forEachIndexed { i, suc ->
                val parte = when {
                    i == sucursales.lastIndex -> restante
                    i == 0 -> p.stock / 2
                    else -> p.stock / 3
                }.coerceAtMost(restante)
                restante -= parte
                db.stockSucursalDao().crearSiNoExiste(StockSucursal(p.id, suc.id, parte))
            }
        }
    }

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
            Producto(nombre = "Cuaderno universitario 100 hojas", stock = 48, codigoBarras = "7800000000016", categoria = "Papelería", stockMinimo = 20),
            Producto(nombre = "Lápiz grafito HB", stock = 120, codigoBarras = "7800000000023", categoria = "Librería", stockMinimo = 40),
            Producto(nombre = "Resma de papel carta 500 hojas", stock = 30, codigoBarras = "7800000000030", categoria = "Papelería", stockMinimo = 15),
            Producto(nombre = "Café Tostado Grano 500g", stock = 4, codigoBarras = "7800000000047", categoria = "Abarrotes", stockMinimo = 10),
            Producto(nombre = "Aceite Maravilla 1L", stock = 0, codigoBarras = "7800000000054", categoria = "Abarrotes", stockMinimo = 20),
            Producto(nombre = "Detergente Líquido 3L", stock = 3, codigoBarras = "7800000000061", categoria = "Limpieza", stockMinimo = 12),
            Producto(nombre = "Arroz Grado 1 1kg", stock = 65, codigoBarras = "7800000000078", categoria = "Abarrotes", stockMinimo = 25),
            Producto(nombre = "Leche Entera 1L", stock = 0, codigoBarras = "7800000000085", categoria = "Lácteos", stockMinimo = 20),
            Producto(nombre = "Jabón Líquido Antibacterial 500ml", stock = 15, codigoBarras = "7800000000092", categoria = "Limpieza", stockMinimo = 10)
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

        sembrarDespachos(db)
        sembrarStockSucursal(db)
    }

    /**
     * Dos despachos de prueba para la recepción de cajas. El QR de cada caja contiene su código
     * (DSP-0042 y DSP-0043); también se puede escribir en Código manual.
     */
    private suspend fun sembrarDespachos(db: AppDatabase) {
        if (db.despachoDao().contar() > 0) return

        val extras = listOf(
            Producto(nombre = "Pegamento en barra", stock = 20, codigoBarras = "7800000000108", categoria = "Papelería", stockMinimo = 15),
            Producto(nombre = "Corrector líquido", stock = 13, codigoBarras = "7800000000115", categoria = "Papelería", stockMinimo = 10),
            Producto(nombre = "Goma de borrar", stock = 0, codigoBarras = "7800000000122", categoria = "Papelería", stockMinimo = 10)
        )
        extras.forEach { p ->
            if (db.productoDao().buscarPorCodigo(p.codigoBarras) == null) db.productoDao().insertar(p)
        }

        suspend fun id(codigo: String): Int? = db.productoDao().buscarPorCodigo(codigo)?.id

        val dao = db.despachoDao()
        val lineas42 = listOf(
            "7800000000016" to 24,  // Cuaderno universitario 100 hojas
            "7800000000023" to 48,  // Lápiz grafito HB
            "7800000000108" to 24,  // Pegamento en barra
            "7800000000115" to 12,  // Corrector líquido
            "7800000000122" to 12   // Goma de borrar
        )
        val lineas43 = listOf(
            "7800000000030" to 10,  // Resma de papel carta
            "7800000000078" to 20   // Arroz
        )
        listOf(
            Despacho(codigo = "DSP-0042", proveedor = "Distribuidora Andes", guia = "18452", sucursalId = 1) to lineas42,
            Despacho(codigo = "DSP-0043", proveedor = "Papelera del Sur", guia = "20931", sucursalId = 1) to lineas43
        ).forEach { (despacho, lineas) ->
            val despachoId = dao.insertar(despacho).toInt()
            lineas.forEach { (codigo, esperado) ->
                val productoId = id(codigo) ?: return@forEach
                dao.insertarDetalle(DespachoDetalle(despachoId = despachoId, productoId = productoId, esperado = esperado))
            }
        }
    }
}
