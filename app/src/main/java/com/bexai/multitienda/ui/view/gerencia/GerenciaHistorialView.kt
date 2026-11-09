package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.data.DecisionRecepcion
import com.bexai.multitienda.data.HistorialFiltro
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.util.ReportePdf
import com.bexai.multitienda.viewmodel.GerenciaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun textoDecision(d: String?): String = when (d) {
    DecisionRecepcion.RECLAMAR -> "Reclamar al proveedor"
    DecisionRecepcion.ESPERAR -> "Esperar"
    DecisionRecepcion.DEVOLVER -> "Devolver"
    DecisionRecepcion.BAJA -> "Dar de baja"
    DecisionRecepcion.LIBERAR -> "Liberar dañados"
    DecisionRecepcion.ACEPTAR -> "Aceptar sobrante"
    null -> "Sin decisión"
    else -> d.orEmpty()
}

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }

/** Convierte "dd/MM/aaaa" a milisegundos (inicio del día), o null si no es una fecha válida. */
private fun parsearFecha(texto: String): Long? = try {
    formatoFecha.parse(texto.trim())?.time
} catch (e: Exception) {
    null
}

private fun finDelDia(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms
    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
}.timeInMillis

/**
 * Historial de Gerencia (mockup "Historial"): filtros Tienda, Fecha y Tipo, lista de movimientos
 * y botón Exportar, que abre la hoja "Exportar reporte" (PDF). El tipo "Cajas" muestra las
 * incidencias de cajas con la decisión del encargado.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GerenciaHistorialView(
    modifier: Modifier = Modifier,
    viewModel: GerenciaViewModel = viewModel()
) {
    val movimientos by viewModel.historialMovimientos.collectAsState()
    val incidencias by viewModel.incidenciasCajas.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val tiendas by viewModel.tiendas.collectAsState()
    val pendiente by HistorialFiltro.pendiente.collectAsState()
    val contexto = LocalContext.current

    var sucursalId by remember { mutableStateOf<Int?>(null) }
    var fecha by remember { mutableStateOf("Hoy") }
    var tipo by remember { mutableStateOf("Todos") }
    var productoId by remember { mutableStateOf<Int?>(null) }
    var rangoDesde by remember { mutableStateOf<Long?>(null) }
    var rangoHasta by remember { mutableStateOf<Long?>(null) }
    var pidiendoRango by remember { mutableStateOf(false) }
    var exportando by remember { mutableStateOf(false) }

    // "Ver historial" desde Alertas: se abre filtrado por ese producto y sucursal
    LaunchedEffect(pendiente) {
        pendiente?.let { (prod, suc) ->
            productoId = prod
            sucursalId = suc
            fecha = "Todo"
            tipo = "Todos"
            HistorialFiltro.pendiente.value = null
        }
    }

    fun nombreSucursal(id: Int) = tiendas.firstOrNull { it.id == id }?.nombre ?: "Sucursal $id"
    fun nombreProducto(id: Int) = productos.firstOrNull { it.id == id }?.nombre ?: "Producto $id"

    val desde: Long? = when (fecha) {
        "Hoy" -> desdeDias(0)
        "7 días" -> desdeDias(6)
        "30 días" -> desdeDias(29)
        "Rango" -> rangoDesde
        else -> null
    }
    val hasta: Long? = if (fecha == "Rango") rangoHasta?.let { finDelDia(it) } else null

    val movVisibles = movimientos.filter { m ->
        (sucursalId == null || m.sucursalId == sucursalId) &&
            (productoId == null || m.productoId == productoId) &&
            (desde == null || m.fechaHora >= desde) &&
            (hasta == null || m.fechaHora <= hasta) &&
            when (tipo) {
                "Entrada" -> m.tipo == TipoMovimiento.ENTRADA
                "Salida" -> m.tipo == TipoMovimiento.SALIDA
                "Ajuste" -> m.tipo == TipoMovimiento.AJUSTE
                else -> true
            }
    }
    val incVisibles = incidencias.filter { i ->
        (sucursalId == null || i.sucursalId == sucursalId) &&
            (productoId == null || i.productoId == productoId) &&
            (desde == null || (i.fecha ?: 0L) >= desde) &&
            (hasta == null || (i.fecha ?: 0L) <= hasta)
    }
    val esCajas = tipo == "Cajas"
    val total = if (esCajas) incVisibles.size else movVisibles.size

    val textoFecha = if (fecha == "Rango" && rangoDesde != null && rangoHasta != null)
        "${formatoFecha.format(Date(rangoDesde!!))} – ${formatoFecha.format(Date(rangoHasta!!))}" else fecha
    val textoTienda = sucursalId?.let { nombreSucursal(it) } ?: "Todas"

    LazyColumn(
        modifier = modifier.fillMaxSize().background(GFondo).statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Historial", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                Text(
                    "↓ Exportar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier
                        .background(Azul, RoundedCornerShape(24.dp))
                        .clickable { exportando = true }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FiltroCaja("Tienda", textoTienda, Modifier.weight(1f)) { cerrar ->
                    DropdownMenuItem(text = { Text("Todas") }, onClick = { sucursalId = null; cerrar() })
                    tiendas.forEach { t -> DropdownMenuItem(text = { Text(t.nombre) }, onClick = { sucursalId = t.id; cerrar() }) }
                }
                FiltroCaja("Fecha", textoFecha, Modifier.weight(1f)) { cerrar ->
                    listOf("Hoy", "7 días", "30 días", "Todo").forEach { op ->
                        DropdownMenuItem(text = { Text(op) }, onClick = { fecha = op; cerrar() })
                    }
                    DropdownMenuItem(text = { Text("Rango…") }, onClick = { pidiendoRango = true; cerrar() })
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FiltroCaja("Tipo", tipo, Modifier.weight(1f)) { cerrar ->
                    listOf("Todos", "Entrada", "Salida", "Ajuste", "Cajas").forEach { op ->
                        DropdownMenuItem(text = { Text(op) }, onClick = { tipo = op; cerrar() })
                    }
                }
                if (productoId != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                            .clickable { productoId = null }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text("Producto ✕", fontSize = 14.sp, color = GTextoSuave)
                        Text(nombreProducto(productoId!!), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GTexto, maxLines = 1)
                    }
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
        item {
            Text(
                "$total ${if (esCajas) "INCIDENCIAS" else "MOVIMIENTOS"}",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GTextoSuave
            )
        }

        if (total == 0) item { Text("Sin resultados para estos filtros.", fontSize = 15.sp, color = GTextoSuave) }

        if (!esCajas) {
            items(movVisibles, key = { "mov-${it.id}" }) { m ->
                val (color, fondo, etiqueta) = when (m.tipo) {
                    TipoMovimiento.ENTRADA -> Triple(GAzulTexto, GAzulFondo, "Entrada +${m.cantidad}")
                    TipoMovimiento.SALIDA -> Triple(GRojoTexto, GRojoFondo, "Salida −${m.cantidad}")
                    TipoMovimiento.AJUSTE -> Triple(GNaranjaTexto, GNaranjaFondo, "Ajuste → ${m.cantidad}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nombreProducto(m.productoId), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GTexto)
                        Text("${fechaHora(m.fechaHora)} · ${nombreSucursal(m.sucursalId)}", fontSize = 14.sp, color = GTextoSuave)
                        if (!m.motivo.isNullOrBlank()) Text(m.motivo, fontSize = 13.sp, color = GTextoSuave)
                    }
                    Text(
                        etiqueta, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color,
                        modifier = Modifier.background(fondo, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        } else {
            items(incVisibles, key = { "inc-${it.detalleId}" }) { i ->
                val falta = (i.esperado - i.recibido).coerceAtLeast(0)
                val sobra = (i.recibido - i.esperado).coerceAtLeast(0)
                val detalle = listOfNotNull(
                    if (falta > 0) "Faltan $falta" else null,
                    if (sobra > 0) "Sobran $sobra" else null,
                    if (i.danado > 0) "Dañados ${i.danado}" + (i.motivoDanado?.let { " ($it)" } ?: "") else null
                ).joinToString(" · ")
                val baja = i.decision == DecisionRecepcion.BAJA
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${i.codigo} · ${nombreSucursal(i.sucursalId)}", fontSize = 14.sp, color = GTextoSuave)
                        Text(fechaHora(i.fecha), fontSize = 13.sp, color = GTextoSuave)
                    }
                    Text(i.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GTexto)
                    Text(detalle, fontSize = 14.sp, color = GTexto)
                    Text(
                        textoDecision(i.decision), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (i.decision == null) GNaranjaTexto else if (baja) GRojoTexto else GAzulTexto,
                        modifier = Modifier
                            .background(
                                if (i.decision == null) GNaranjaFondo else if (baja) GRojoFondo else GAzulFondo,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

    if (pidiendoRango) {
        DialogoRango(
            desdeInicial = rangoDesde?.let { formatoFecha.format(Date(it)) } ?: "",
            hastaInicial = rangoHasta?.let { formatoFecha.format(Date(it)) } ?: "",
            onCerrar = { pidiendoRango = false },
            onAplicar = { d, h -> rangoDesde = d; rangoHasta = h; fecha = "Rango"; pidiendoRango = false }
        )
    }

    if (exportando) {
        ModalBottomSheet(
            onDismissRequest = { exportando = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exportar reporte", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                    Box(
                        modifier = Modifier.size(40.dp).background(GFondo, CircleShape).clickable { exportando = false },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = GTexto) }
                }
                Text("Filtros aplicados", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GTexto)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filtros = listOfNotNull(
                        if (sucursalId == null) "Todas las tiendas" else textoTienda,
                        textoFecha,
                        if (tipo == "Todos") "Todos los tipos" else tipo,
                        productoId?.let { nombreProducto(it) }
                    )
                    filtros.forEach {
                        Text(
                            it, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GTexto,
                            modifier = Modifier.background(GFondo, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                Text(
                    "$total ${if (esCajas) "incidencias" else "movimientos"} se incluirán",
                    fontSize = 15.sp, color = GTextoSuave
                )
                Text("Formato", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GTexto)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GAzulFondo, RoundedCornerShape(18.dp))
                        .border(BorderStroke(2.dp, Azul), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PDF", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = GRojoTexto,
                        modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 16.dp)
                    )
                    Column {
                        Text("Reporte PDF", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                        Text("Tabla de ${if (esCajas) "incidencias" else "movimientos"}, fácil de leer en el celular", fontSize = 14.sp, color = GTextoSuave)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(if (total > 0) Azul else Color(0xFFCBD5E1), RoundedCornerShape(27.dp))
                        .clickable(enabled = total > 0) {
                            val subtitulo = "$total registros · $textoTienda · $textoFecha"
                            val archivo = if (!esCajas) {
                                ReportePdf.generar(
                                    contexto, "historial_movimientos", "Historial de movimientos", subtitulo,
                                    listOf("Fecha", "Tipo", "Producto", "Cant.", "Sucursal", "Motivo"),
                                    movVisibles.map {
                                        listOf(fechaHora(it.fechaHora), it.tipo.name, nombreProducto(it.productoId), "${it.cantidad}", nombreSucursal(it.sucursalId), it.motivo.orEmpty())
                                    },
                                    listOf(2f, 1.6f, 4f, 1f, 2.6f, 4f)
                                )
                            } else {
                                ReportePdf.generar(
                                    contexto, "incidencias_cajas", "Incidencias de cajas", subtitulo,
                                    listOf("Caja", "Sucursal", "Producto", "Falta", "Sobra", "Dañ.", "Decisión"),
                                    incVisibles.map {
                                        listOf(
                                            it.codigo, nombreSucursal(it.sucursalId), it.nombre,
                                            "${(it.esperado - it.recibido).coerceAtLeast(0)}", "${(it.recibido - it.esperado).coerceAtLeast(0)}",
                                            "${it.danado}", textoDecision(it.decision)
                                        )
                                    },
                                    listOf(2f, 2.6f, 4f, 1f, 1f, 1f, 3.6f)
                                )
                            }
                            exportando = false
                            ReportePdf.compartir(contexto, archivo)
                        },
                    contentAlignment = Alignment.Center
                ) { Text("Generar y compartir", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** Caja de filtro del mockup (etiqueta + valor ▾) que abre un menú con las opciones. */
@Composable
private fun FiltroCaja(
    titulo: String,
    valor: String,
    modifier: Modifier,
    opciones: @Composable (cerrar: () -> Unit) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                .clickable { abierto = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(titulo, fontSize = 14.sp, color = GTextoSuave)
            Text("$valor ▾", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GTexto, maxLines = 1)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones { abierto = false }
        }
    }
}

/** Rango de fechas dd/MM/aaaa; no deja aplicar si el formato es inválido o "desde" es mayor que "hasta". */
@Composable
private fun DialogoRango(
    desdeInicial: String,
    hastaInicial: String,
    onCerrar: () -> Unit,
    onAplicar: (Long, Long) -> Unit
) {
    var desde by remember { mutableStateOf(desdeInicial) }
    var hasta by remember { mutableStateOf(hastaInicial) }
    val d = parsearFecha(desde)
    val h = parsearFecha(hasta)
    val error = when {
        desde.isNotBlank() && d == null -> "Fecha desde inválida (usa dd/mm/aaaa)"
        hasta.isNotBlank() && h == null -> "Fecha hasta inválida (usa dd/mm/aaaa)"
        d != null && h != null && d > h -> "La fecha desde no puede ser mayor que la fecha hasta"
        else -> null
    }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Rango de fechas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(desde, { desde = it.take(10) }, label = { Text("Desde (dd/mm/aaaa)") }, singleLine = true)
                OutlinedTextField(hasta, { hasta = it.take(10) }, label = { Text("Hasta (dd/mm/aaaa)") }, singleLine = true)
                if (error != null) Text(error, color = GRojoTexto, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(enabled = d != null && h != null && error == null, onClick = { onAplicar(d!!, h!!) }) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
