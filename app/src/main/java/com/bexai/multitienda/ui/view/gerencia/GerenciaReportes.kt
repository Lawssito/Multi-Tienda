package com.bexai.multitienda.ui.view.gerencia

import android.content.Context
import com.bexai.multitienda.model.IncidenciaCaja
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.StockSucursal
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

private fun nombreSuc(tiendas: List<Sucursal>, id: Int) =
    tiendas.firstOrNull { it.id == id }?.let { nombreCorto(it) } ?: "Sucursal $id"

/** PDF de reposición sugerida: lo que está bajo el mínimo en cada sucursal y cuánto pedir. */
internal fun exportarReposicionPdf(
    contexto: Context,
    productos: List<Producto>,
    stocks: List<StockSucursal>,
    tiendas: List<Sucursal>
) {
    val filas = stocks.mapNotNull { s ->
        val p = productos.firstOrNull { it.id == s.productoId } ?: return@mapNotNull null
        if (s.cantidad >= p.stockMinimo) return@mapNotNull null
        Triple(s, p, (p.stockMinimo * 2 - s.cantidad).coerceAtLeast(1))
    }.sortedWith(compareBy({ it.first.sucursalId }, { it.second.nombre }))
        .map { (s, p, sugerida) ->
            listOf(nombreSuc(tiendas, s.sucursalId), p.nombre, "${s.cantidad}", "${p.stockMinimo}", "$sugerida")
        }
    val archivo = ReportePdf.generar(
        contexto, "reposicion_sugerida", "Reposición sugerida",
        "${filas.size} productos bajo el mínimo · sugerido = 2 × mínimo − stock",
        listOf("Sucursal", "Producto", "Stock", "Mín.", "Sugerida"), filas, listOf(2f, 4f, 1f, 1f, 1.4f)
    )
    ReportePdf.compartir(contexto, archivo)
}

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
    fun prod(id: Int) = productos.firstOrNull { it.id == id }?.nombre ?: "Producto $id"
    val ajustesRaw = ajustesManuales(movimientos.filter { it.fechaHora >= desde })
    val danadosRaw = incidencias.filter { (it.fecha ?: 0L) >= desde && it.danado > 0 }
    val ajustes = ajustesRaw.map {
        Triple(it.fechaHora, "Ajuste", listOf(
            fechaHora(it.fechaHora), "Ajuste", nombreSuc(tiendas, it.sucursalId), prod(it.productoId),
            "${motivoAjuste(it.motivo)}: ${it.motivo ?: "—"} · total ${it.cantidad}"
        ))
    }
    val danados = danadosRaw.map {
        Triple(it.fecha ?: 0L, "Caja", listOf(
            fechaHora(it.fecha), "Caja ${it.codigo}", nombreSuc(tiendas, it.sucursalId), it.nombre,
            "${it.danado} dañadas" + (it.motivoDanado?.let { m -> " ($m)" } ?: "")
        ))
    }
    val filas = (ajustes + danados).sortedByDescending { it.first }.map { it.third }
    val porMotivo = ajustesRaw.groupingBy { motivoAjuste(it.motivo) }.eachCount()
        .entries.joinToString(" · ") { "${it.key} ${it.value}" }
    val archivo = ReportePdf.generar(
        contexto, "mermas_perdidas", "Mermas y pérdidas",
        "$periodo · ${ajustes.size} ajustes · ${danadosRaw.sumOf { it.danado }} unidades dañadas en cajas" +
            if (porMotivo.isNotEmpty()) " · $porMotivo" else "",
        listOf("Fecha", "Origen", "Sucursal", "Producto", "Detalle"), filas, listOf(1.6f, 1.8f, 1.8f, 3.4f, 4f)
    )
    ReportePdf.compartir(contexto, archivo)
}

