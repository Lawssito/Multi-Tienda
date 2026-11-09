package com.bexai.multitienda.ui.view.gerencia

import android.content.Context
import com.bexai.multitienda.model.IncidenciaCaja
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.util.ReportePdf

private val MOTIVOS_CONOCIDOS = listOf("Merma", "Dañado", "Contado mal")

/** Agrupa el motivo de un ajuste: los tres conocidos y "Otro" para el texto libre. */
internal fun motivoAjuste(motivo: String?): String =
    MOTIVOS_CONOCIDOS.firstOrNull { it.equals(motivo?.trim(), ignoreCase = true) } ?: "Otro"

/** Ajustes manuales del operador (se excluye el ajuste que genera un conteo aprobado). */
internal fun ajustesManuales(movs: List<Movimiento>): List<Movimiento> =
    movs.filter { it.tipo == TipoMovimiento.AJUSTE && it.motivo != "Conteo masivo" }

/** Nombre corto de cada sucursal por id, para no buscar en la lista por cada fila del PDF. */
private fun nombresCortos(tiendas: List<Sucursal>): Map<Int, String> = tiendas.associate { it.id to nombreCorto(it) }

private fun Map<Int, String>.nombreDeSucursal(id: Int) = this[id] ?: "Sucursal $id"

/** PDF de mermas y pérdidas del período: ajustes del operador y unidades dañadas en cajas. */
internal fun exportarMermasPdf(
    contexto: Context,
    periodo: String,
    desde: Long,
    movimientos: List<Movimiento>,
    incidencias: List<IncidenciaCaja>,
    productos: List<Producto>,
    tiendas: List<Sucursal>
) {
    val nombresProducto = productos.associate { it.id to it.nombre }
    val nombres = nombresCortos(tiendas)
    fun prod(id: Int) = nombresProducto[id] ?: "Producto $id"
    val ajustesRaw = ajustesManuales(movimientos.filter { it.fechaHora >= desde })
    val danadosRaw = incidencias.filter { (it.fecha ?: 0L) >= desde && it.danado > 0 }
    val ajustes = ajustesRaw.map {
        Triple(it.fechaHora, "Ajuste", listOf(
            fechaHora(it.fechaHora), "Ajuste", nombres.nombreDeSucursal(it.sucursalId), prod(it.productoId),
            "${motivoAjuste(it.motivo)}: ${it.motivo ?: "—"} · total ${it.cantidad}"
        ))
    }
    val danados = danadosRaw.map {
        Triple(it.fecha ?: 0L, "Caja", listOf(
            fechaHora(it.fecha), "Caja ${it.codigo}", nombres.nombreDeSucursal(it.sucursalId), it.nombre,
            "${it.danado} dañadas" + (it.motivoDanado?.let { m -> " ($m)" } ?: "")
        ))
    }
    val filas = (ajustes + danados).sortedByDescending { it.first }.map { it.third }
    val porMotivo = ajustesRaw.groupingBy { motivoAjuste(it.motivo) }.eachCount()
        .entries.joinToString(" · ") { "${it.key} ${it.value}" }
    ReportePdf.exportar(contexto) {
        ReportePdf.generar(
            contexto, "mermas_perdidas", "Mermas y pérdidas",
            "$periodo · ${ajustes.size} ajustes · ${danadosRaw.sumOf { it.danado }} unidades dañadas en cajas" +
                if (porMotivo.isNotEmpty()) " · $porMotivo" else "",
            listOf("Fecha", "Origen", "Sucursal", "Producto", "Detalle"), filas, listOf(1.6f, 1.8f, 1.8f, 3.4f, 4f)
        )
    }
}

/** PDF del Historial con los filtros aplicados: movimientos o, en la pestaña Cajas, incidencias. */
internal fun exportarHistorialPdf(
    contexto: Context,
    esCajas: Boolean,
    subtitulo: String,
    movimientos: List<Movimiento>,
    incidencias: List<IncidenciaCaja>,
    nombreProducto: (Int) -> String,
    nombreSucursal: (Int) -> String
) {
    ReportePdf.exportar(contexto) {
        if (!esCajas) {
            ReportePdf.generar(
                contexto, "historial_movimientos", "Historial de movimientos", subtitulo,
                listOf("Fecha", "Tipo", "Producto", "Cant.", "Sucursal", "Motivo"),
                movimientos.map {
                    listOf(
                        fechaHora(it.fechaHora), it.tipo.name, nombreProducto(it.productoId), "${it.cantidad}",
                        nombreSucursal(it.sucursalId), it.motivo.orEmpty()
                    )
                },
                listOf(2f, 1.6f, 4f, 1f, 2.6f, 4f)
            )
        } else {
            ReportePdf.generar(
                contexto, "incidencias_cajas", "Incidencias de cajas", subtitulo,
                listOf("Caja", "Sucursal", "Producto", "Falta", "Sobra", "Dañ.", "Decisión"),
                incidencias.map {
                    listOf(
                        it.codigo, nombreSucursal(it.sucursalId), it.nombre,
                        "${(it.esperado - it.recibido).coerceAtLeast(0)}", "${(it.recibido - it.esperado).coerceAtLeast(0)}",
                        "${it.danado}", textoDecision(it.decision)
                    )
                },
                listOf(2f, 2.6f, 4f, 1f, 1f, 1f, 3.6f)
            )
        }
    }
}
