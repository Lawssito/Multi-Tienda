package com.bexai.multitienda.data.repository

import androidx.room.withTransaction
import com.bexai.multitienda.data.local.AppDatabase
import com.bexai.multitienda.model.EstadoRecepcion
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.Recepcion
import com.bexai.multitienda.model.RecepcionDetalle
import com.bexai.multitienda.model.RecepcionPendiente
import com.bexai.multitienda.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow

/** Lo que el encargado puede decidir por línea de una recepción. */
object DecisionRecepcion {
    const val RECLAMAR = "RECLAMAR"   // reclamar el faltante al proveedor
    const val ESPERAR = "ESPERAR"     // esperar un segundo envío
    const val DEVOLVER = "DEVOLVER"   // devolver al proveedor
    const val BAJA = "BAJA"           // dar de baja (merma)
    const val LIBERAR = "LIBERAR"     // los dañados sí suman al stock
    const val ACEPTAR = "ACEPTAR"     // aceptar el sobrante
}

/** Resultado de escanear un producto dentro de una recepción. */
data class LecturaRecepcion(val linea: LineaRecepcion, val noEsperado: Boolean)

/**
 * RecepcionRepository: recibir una caja (despacho) escaneando sus productos.
 *
 * El operador escanea y marca dañados; el stock NO cambia hasta [enviar]. Al enviar entran
 * al stock solo las unidades buenas (recibidas menos dañadas, incluidos los sobrantes), en una
 * sola transacción. Faltantes, sobrantes y dañados los resuelve después el encargado con
 * [decidir] y [marcarRevisada].
 */
class RecepcionRepository(private val db: AppDatabase) {

    private val dao = db.recepcionDao()
    private val despachoDao = db.despachoDao()
    private val productoDao = db.productoDao()
    private val existencias = ExistenciasRepository(db)
    private val movimientoDao = db.movimientoDao()

    /** Retoma la recepción en curso de ese despacho en esta sucursal o crea una nueva. */
    suspend fun iniciar(despachoId: Int, sucursalId: Int): Int = db.withTransaction {
        dao.enCurso(despachoId, sucursalId)?.let { return@withTransaction it.id }
        val id = dao.insertar(
            Recepcion(despachoId = despachoId, sucursalId = sucursalId, fechaInicio = System.currentTimeMillis())
        ).toInt()
        despachoDao.detalles(despachoId).forEach { d ->
            dao.insertarDetalle(RecepcionDetalle(recepcionId = id, productoId = d.productoId, esperado = d.esperado))
        }
        id
    }

    fun lineas(recepcionId: Int): Flow<List<LineaRecepcion>> = dao.lineas(recepcionId)

    suspend fun lineasUnaVez(recepcionId: Int): List<LineaRecepcion> = dao.lineasUnaVez(recepcionId)

    suspend fun recepcion(id: Int): Recepcion? = dao.buscar(id)

    /** Suma 1 recibida. Si el producto no venía en el despacho, crea la línea con esperado 0. */
    suspend fun recibir(recepcionId: Int, producto: Producto): LecturaRecepcion? = db.withTransaction {
        val recepcion = dao.buscar(recepcionId)
        if (recepcion == null || recepcion.estado != EstadoRecepcion.EN_CURSO) return@withTransaction null
        val existente = dao.buscarDetalle(recepcionId, producto.id)
        val noEsperado = existente == null
        if (existente == null) {
            dao.insertarDetalle(
                RecepcionDetalle(recepcionId = recepcionId, productoId = producto.id, esperado = 0, recibido = 1)
            )
        } else {
            dao.actualizarDetalle(existente.copy(recibido = existente.recibido + 1))
        }
        val linea = dao.lineasUnaVez(recepcionId).first { it.productoId == producto.id }
        LecturaRecepcion(linea, noEsperado)
    }

    /**
     * Fija las unidades recibidas de un producto (hoja "Cambiar cantidad"). Si el producto no
     * venía en el despacho y queda en 0, la línea se elimina. Los dañados no pueden superar lo recibido.
     * @return false si la recepción ya no está en curso o la línea no existe.
     */
    suspend fun fijarRecibido(recepcionId: Int, productoId: Int, cantidad: Int): Boolean = db.withTransaction {
        val recepcion = dao.buscar(recepcionId)
        if (recepcion == null || recepcion.estado != EstadoRecepcion.EN_CURSO || cantidad < 0) {
            return@withTransaction false
        }
        val detalle = dao.buscarDetalle(recepcionId, productoId) ?: return@withTransaction false
        if (cantidad == 0 && detalle.esperado == 0) {
            dao.eliminarDetalle(detalle)
        } else {
            val danado = minOf(detalle.danado, cantidad)
            dao.actualizarDetalle(
                detalle.copy(
                    recibido = cantidad,
                    danado = danado,
                    motivoDanado = if (danado > 0) detalle.motivoDanado else null,
                    nota = if (danado > 0) detalle.nota else null
                )
            )
        }
        true
    }

    /**
     * Marca unidades dañadas de una línea. [cantidad] va de 0 (quita la marca) a lo recibido;
     * con cantidad mayor que 0 el motivo es obligatorio.
     * @return null si está bien, o el mensaje de error.
     */
    suspend fun marcarDanado(detalleId: Int, cantidad: Int, motivo: String, nota: String): String? {
        val detalle = dao.buscarDetallePorId(detalleId) ?: return "La línea ya no existe"
        if (cantidad < 0 || cantidad > detalle.recibido) return "Máximo ${detalle.recibido} (las recibidas)"
        if (cantidad > 0 && motivo.isBlank()) return "Elige un motivo"
        dao.actualizarDetalle(
            detalle.copy(
                danado = cantidad,
                motivoDanado = if (cantidad > 0) motivo else null,
                nota = if (cantidad > 0 && nota.isNotBlank()) nota.trim() else null
            )
        )
        return null
    }

    /** @return true si se envió; false si no existe o ya no estaba en curso. */
    suspend fun enviar(recepcionId: Int): Boolean = db.withTransaction {
        val recepcion = dao.buscar(recepcionId)
        if (recepcion == null || recepcion.estado != EstadoRecepcion.EN_CURSO) return@withTransaction false
        val codigo = despachoDao.buscar(recepcion.despachoId)?.codigo ?: "despacho"
        val ahora = System.currentTimeMillis()
        dao.lineasUnaVez(recepcionId).filter { it.buenas > 0 }.forEach { linea ->
            movimientoDao.insertar(
                Movimiento(
                    productoId = linea.productoId,
                    sucursalId = recepcion.sucursalId,
                    tipo = TipoMovimiento.ENTRADA,
                    cantidad = linea.buenas,
                    motivo = "Recepción $codigo",
                    fechaHora = ahora
                )
            )
            existencias.sumar(linea.productoId, recepcion.sucursalId, linea.buenas)
        }
        dao.cambiarEstado(recepcionId, EstadoRecepcion.ENVIADA, ahora)
        true
    }

    // ---------- Encargado ----------

    fun pendientes(sucursalId: Int): Flow<List<RecepcionPendiente>> = dao.pendientes(sucursalId)

    /**
     * Guarda lo que decidió el encargado para una línea. LIBERAR suma al stock las unidades
     * dañadas (con su Entrada); si luego elige otra cosa, se deshace con una Salida.
     * DEVOLVER en un sobrante saca esas unidades con una Salida; si cambia, se deshace con una Entrada.
     */
    suspend fun decidir(detalleId: Int, decision: String) = db.withTransaction {
        val detalle = dao.buscarDetallePorId(detalleId) ?: return@withTransaction
        val recepcion = dao.buscar(detalle.recepcionId) ?: return@withTransaction
        if (recepcion.estado != EstadoRecepcion.ENVIADA) return@withTransaction
        val codigo = despachoDao.buscar(recepcion.despachoId)?.codigo ?: "despacho"
        val liberaba = detalle.decision == DecisionRecepcion.LIBERAR
        val liberara = decision == DecisionRecepcion.LIBERAR
        if (detalle.danado > 0 && liberaba != liberara) {
            movimientoDao.insertar(
                Movimiento(
                    productoId = detalle.productoId,
                    sucursalId = recepcion.sucursalId,
                    tipo = if (liberara) TipoMovimiento.ENTRADA else TipoMovimiento.SALIDA,
                    cantidad = detalle.danado,
                    motivo = if (liberara) "Dañados liberados · $codigo" else "Liberación deshecha · $codigo",
                    fechaHora = System.currentTimeMillis()
                )
            )
            existencias.sumar(detalle.productoId, recepcion.sucursalId, if (liberara) detalle.danado else -detalle.danado)
        }
        // Sobrante (sin dañados): ya entró al stock al enviar; DEVOLVER lo saca con una Salida.
        val sobran = (detalle.recibido - detalle.esperado).coerceAtLeast(0)
        val devolvia = detalle.decision == DecisionRecepcion.DEVOLVER
        val devolvera = decision == DecisionRecepcion.DEVOLVER
        if (detalle.danado == 0 && sobran > 0 && devolvia != devolvera) {
            movimientoDao.insertar(
                Movimiento(
                    productoId = detalle.productoId,
                    sucursalId = recepcion.sucursalId,
                    tipo = if (devolvera) TipoMovimiento.SALIDA else TipoMovimiento.ENTRADA,
                    cantidad = sobran,
                    motivo = if (devolvera) "Sobrante devuelto · $codigo" else "Devolución deshecha · $codigo",
                    fechaHora = System.currentTimeMillis()
                )
            )
            existencias.sumar(detalle.productoId, recepcion.sucursalId, if (devolvera) -sobran else sobran)
        }
        dao.guardarDecision(detalleId, decision)
    }

    /** Marca la recepción revisada. Solo si cada línea con problema tiene una decisión. */
    suspend fun marcarRevisada(recepcionId: Int): Boolean = db.withTransaction {
        val recepcion = dao.buscar(recepcionId)
        if (recepcion == null || recepcion.estado != EstadoRecepcion.ENVIADA) return@withTransaction false
        if (dao.lineasUnaVez(recepcionId).any { it.conProblema && it.decision == null }) return@withTransaction false
        dao.cambiarEstado(recepcionId, EstadoRecepcion.REVISADA, recepcion.fechaEnvio)
        true
    }
}
