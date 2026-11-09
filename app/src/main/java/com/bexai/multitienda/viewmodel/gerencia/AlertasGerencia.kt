package com.bexai.multitienda.viewmodel.gerencia

import com.bexai.multitienda.model.AlertaRevisada
import com.bexai.multitienda.model.DescalceConteo
import com.bexai.multitienda.model.EstadoReposicion
import com.bexai.multitienda.model.IncidenciaCaja
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.Reposicion
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal

// =====================================================================
// AlertasGerencia: arma la lista de alertas que ve Gerencia a partir de los datos de Room.
//
// Son funciones puras (sin base de datos ni Android): reciben listas y devuelven alertas.
// GerenciaViewModel solo las combina (los modelos AlertaItem y TipoAlerta están en GerenciaViewModel.kt). Cada búsqueda por id usa un mapa (associateBy) para
// no recorrer las listas completas por cada fila.
// =====================================================================

/** Nombre de la sucursal o "Sucursal N" si ya no existe. */
private fun Map<Int, String>.nombreDeSucursal(id: Int) = this[id] ?: "Sucursal $id"

/** Cantidad sugerida para reponer un producto bajo el mínimo: 2 × mínimo − stock (al menos 1). */
internal fun cantidadSugerida(stock: Int, minimo: Int) = (minimo * 2 - stock).coerceAtLeast(1)

/** Alertas de mínimo, descalce de conteo y reposición (las de cajas se agregan aparte). */
internal fun construirAlertasBase(
    productos: List<Producto>,
    stocks: List<StockSucursal>,
    tiendas: List<Sucursal>,
    reposiciones: List<Reposicion>,
    descalces: List<DescalceConteo>
): List<AlertaItem> {
    val nomSuc = tiendas.associate { it.id to it.nombre }
    val prodPorId = productos.associateBy { it.id }
    val lista = mutableListOf<AlertaItem>()

    // ---------- Bajo el mínimo: una alerta por producto y sucursal ----------
    stocks.forEach { s ->
        val p = prodPorId[s.productoId] ?: return@forEach
        if (s.cantidad < p.stockMinimo) {
            lista += AlertaItem(
                "MIN:${s.productoId}:${s.sucursalId}", TipoAlerta.MINIMO, p.nombre,
                "${nomSuc.nombreDeSucursal(s.sucursalId)} · ${s.cantidad} de mín. ${p.stockMinimo}",
                null, s.productoId, s.sucursalId, s.cantidad <= 0, false,
                cantidad = cantidadSugerida(s.cantidad, p.stockMinimo)
            )
        }
    }

    // ---------- Descalces: lo contado no coincide con el sistema ----------
    descalces.forEach { d ->
        val signo = if (d.contado - d.sistema > 0) "+" else ""
        lista += AlertaItem(
            "DESC:${d.conteoId}:${d.productoId}", TipoAlerta.DESCALCE, d.nombre,
            "${nomSuc.nombreDeSucursal(d.sucursalId)} · $signo${d.contado - d.sistema} en conteo",
            d.fecha, d.productoId, d.sucursalId, false, false
        )
    }

    // ---------- Reposiciones declaradas por los encargados ----------
    reposiciones.forEach { r ->
        val nombre = prodPorId[r.productoId]?.nombre ?: "Producto ${r.productoId}"
        lista += AlertaItem(
            "REP:${r.id}", TipoAlerta.REPOSICION, nombre,
            "${nomSuc.nombreDeSucursal(r.sucursalId)} · pide ${r.cantidad}" + (if (r.urgente) " · urgente" else "") +
                when (r.estado) {
                    EstadoReposicion.APROBADA -> " · aprobada, despacho ${r.despachoCodigo ?: ""}"
                    EstadoReposicion.RECHAZADA -> " · rechazada: ${r.motivoRechazo ?: "sin motivo"}"
                    else -> ""
                },
            r.fecha, r.productoId, r.sucursalId, r.urgente && !r.revisada, r.revisada,
            nota = r.nota.takeIf { it.isNotBlank() }, cantidad = r.cantidad
        )
    }
    return lista
}

/** Una alerta por cada línea de caja con faltantes, sobrantes o dañados. */
internal fun alertasDeCajas(cajas: List<IncidenciaCaja>, tiendas: List<Sucursal>): List<AlertaItem> {
    val nomSuc = tiendas.associate { it.id to it.nombre }
    return cajas.map { c ->
        val problema = listOfNotNull(
            (c.esperado - c.recibido).takeIf { it > 0 }?.let { "faltan $it" },
            (c.recibido - c.esperado).takeIf { it > 0 }?.let { "sobran $it" },
            c.danado.takeIf { it > 0 }?.let { "dañados $it" }
        ).joinToString(", ")
        AlertaItem(
            "CAJA:${c.detalleId}", TipoAlerta.CAJA, c.nombre,
            "${nomSuc.nombreDeSucursal(c.sucursalId)} · ${c.codigo} · $problema",
            c.fecha, c.productoId, c.sucursalId, false, false
        )
    }
}

/**
 * Completa cada alerta con la hora (si no la tiene, la del último movimiento de ese producto en
 * esa sucursal), la marca de revisada y la nota de Gerencia. Devuelve la lista, la más reciente primero.
 *
 * @param historial movimientos ordenados del más reciente al más antiguo.
 */
internal fun completarAlertas(
    alertas: List<AlertaItem>,
    marcas: List<AlertaRevisada>,
    historial: List<Movimiento>
): List<AlertaItem> {
    val notas = marcas.associate { it.clave to it.nota }
    // Primer movimiento (el más reciente) de cada par producto-sucursal
    val ultimaHora = HashMap<Pair<Int, Int>, Long>()
    historial.forEach { m -> ultimaHora.putIfAbsent(m.productoId to m.sucursalId, m.fechaHora) }
    return alertas.map { a ->
        a.copy(
            hora = a.hora ?: ultimaHora[a.productoId to a.sucursalId],
            revisada = a.revisada || a.clave in notas,
            nota = a.nota ?: notas[a.clave]?.takeIf { it.isNotBlank() }
        )
    }.sortedByDescending { it.hora ?: 0L }
}
