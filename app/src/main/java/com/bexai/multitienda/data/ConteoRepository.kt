package com.bexai.multitienda.data

import androidx.room.withTransaction
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.EstadoConteo
import com.bexai.multitienda.model.LineaConteo
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ConteoRepository: la revisión de un conteo masivo por parte del encargado.
 *
 * El operador cuenta y envía (estado ENVIADO). El stock NO cambia al enviar: solo cambia
 * cuando el encargado aprueba. La pantalla del encargado solo necesita llamar a estas funciones:
 *
 * - [enviados]: conteos de una sucursal que esperan revisión.
 * - [diferencias]: las líneas de un conteo donde lo contado no coincide con el sistema.
 * - [aprobar]: genera un Ajuste "Conteo masivo" por cada diferencia, deja el stock igual a
 *   lo contado y marca el conteo como APROBADO. Todo en una sola transacción.
 * - [rechazar]: marca el conteo como RECHAZADO sin tocar el stock.
 *
 * Reglas:
 * - Solo se puede aprobar o rechazar un conteo ENVIADO; con cualquier otro estado no hace
 *   nada y devuelve false (así un doble toque no aplica los ajustes dos veces).
 * - Los productos "sin contar" no generan ajuste.
 * - La aprobación manda sobre el stock: queda igual a lo contado, aunque haya habido
 *   Entradas o Salidas mientras el conteo esperaba.
 */
class ConteoRepository(private val db: AppDatabase) {

    private val conteoDao = db.conteoDao()
    private val movimientoDao = db.movimientoDao()
    private val existencias = ExistenciasRepository(db)

    fun enviados(sucursalId: Int): Flow<List<Conteo>> = conteoDao.enviados(sucursalId)

    /** Líneas contadas cuya diferencia (contado - sistema) no es cero. */
    fun diferencias(conteoId: Int): Flow<List<LineaConteo>> =
        conteoDao.lineas(conteoId).map { lineas -> lineas.filter { it.diferencia != 0 } }

    /** Último conteo enviado por el operador, sea cual sea su estado actual. */
    fun ultimoConteo(sucursalId: Int): Flow<Conteo?> = conteoDao.ultimoEnviadoOAprobado(sucursalId)

    suspend fun ultimoEnviado(sucursalId: Int): Conteo? = conteoDao.ultimoEnviado(sucursalId)

    /** @return true si se aprobó; false si el conteo no existe o no estaba ENVIADO. */
    suspend fun aprobar(conteoId: Int): Boolean = db.withTransaction {
        val conteo = conteoDao.buscar(conteoId)
        if (conteo == null || conteo.estado != EstadoConteo.ENVIADO) return@withTransaction false

        val ahora = System.currentTimeMillis()
        conteoDao.lineasContadas(conteoId)
            .filter { it.diferencia != 0 }
            .forEach { linea ->
                movimientoDao.insertar(
                    Movimiento(
                        productoId = linea.productoId,
                        sucursalId = conteo.sucursalId,
                        tipo = TipoMovimiento.AJUSTE,
                        cantidad = linea.contado,      // el ajuste guarda la nueva cantidad
                        motivo = "Conteo masivo",
                        fechaHora = ahora
                    )
                )
                existencias.fijar(linea.productoId, conteo.sucursalId, linea.contado)
            }
        conteoDao.cambiarEstado(conteoId, EstadoConteo.APROBADO, conteo.fechaEnvio)
        true
    }

    /** @return true si se rechazó; false si el conteo no existe o no estaba ENVIADO. */
    suspend fun rechazar(conteoId: Int): Boolean = db.withTransaction {
        val conteo = conteoDao.buscar(conteoId)
        if (conteo == null || conteo.estado != EstadoConteo.ENVIADO) return@withTransaction false

        conteoDao.cambiarEstado(conteoId, EstadoConteo.RECHAZADO, conteo.fechaEnvio)
        true
    }
}
