package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.data.HistorialFiltro
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.viewmodel.AlertaItem
import com.bexai.multitienda.viewmodel.GerenciaViewModel
import com.bexai.multitienda.viewmodel.TipoAlerta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VioletaTexto = Color(0xFF6D28D9)
private val VioletaFondo = Color(0xFFEDE9FE)

private fun etiqueta(t: TipoAlerta) = when (t) {
    TipoAlerta.MINIMO -> "! STOCK MÍNIMO"
    TipoAlerta.DESCALCE -> "▼ DESCALCE"
    TipoAlerta.REPOSICION -> "↻ REPOSICIÓN"
    TipoAlerta.CAJA -> "▣ CAJA"
}

private fun colores(t: TipoAlerta): Pair<Color, Color> = when (t) {
    TipoAlerta.MINIMO -> GRojoTexto to GRojoFondo
    TipoAlerta.DESCALCE -> GNaranjaTexto to GNaranjaFondo
    TipoAlerta.REPOSICION -> GAzulTexto to GAzulFondo
    TipoAlerta.CAJA -> VioletaTexto to VioletaFondo
}

private val nombresTipo = listOf(
    TipoAlerta.MINIMO to "Mínimo",
    TipoAlerta.DESCALCE to "Descalce",
    TipoAlerta.REPOSICION to "Reposición",
    TipoAlerta.CAJA to "Caja"
)

/** Acción que Gerencia está haciendo sobre una alerta (abre el diálogo correspondiente). */
private sealed class Accion(val alerta: AlertaItem) {
    class Aprobar(a: AlertaItem) : Accion(a)
    class Rechazar(a: AlertaItem) : Accion(a)
    class Despacho(a: AlertaItem) : Accion(a)
    class Revisar(a: AlertaItem) : Accion(a)
}

/**
 * Alertas de Gerencia (mockup "Alertas"): pestañas Activas y Revisadas y chips por tipo.
 * Cada alerta se resuelve con una decisión:
 * - Reposición: Aprobar (genera el despacho que recibirá el operador) o Rechazar con motivo.
 * - Mínimo: Crear despacho, o Marcar revisada con una nota.
 * - Descalce y Caja: Marcar revisada con una nota.
 */
@Composable
fun GerenciaAlertasView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: GerenciaViewModel = viewModel()
) {
    val alertas by viewModel.alertas.collectAsState()
    var verRevisadas by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf<TipoAlerta?>(null) }
    var accion by remember { mutableStateOf<Accion?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val activas = alertas.filter { !it.revisada }
    val revisadas = alertas.filter { it.revisada }
    val base = if (verRevisadas) revisadas else activas
    val visibles = base.filter { tipo == null || it.tipo == tipo }

    Box(modifier = modifier.fillMaxSize().background(GFondo)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Text("Alertas", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = GTexto) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFEAEFF5), RoundedCornerShape(22.dp)).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Pestana("Activas (${activas.size})", !verRevisadas, Modifier.weight(1f)) { verRevisadas = false }
                    Pestana("Revisadas (${revisadas.size})", verRevisadas, Modifier.weight(1f)) { verRevisadas = true }
                }
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    nombresTipo.forEach { (t, nombre) ->
                        GChip("$nombre ${base.count { it.tipo == t }}", tipo == t) { tipo = if (tipo == t) null else t }
                    }
                }
            }

            if (visibles.isEmpty()) {
                item {
                    Text(
                        if (verRevisadas) "Todavía no hay alertas revisadas." else "No hay alertas activas.",
                        fontSize = 15.sp, color = GTextoSuave
                    )
                }
            }
            items(visibles, key = { it.clave }) { a ->
                // Al marcar revisada, la fila se desvanece y las demás suben a ocupar su lugar
                Box(
                    Modifier.animateItem(
                        fadeInSpec = tween(400),
                        placementSpec = tween(500),
                        fadeOutSpec = tween(500)
                    )
                ) {
                TarjetaAlerta(
                    alerta = a,
                    onVerHistorial = {
                        HistorialFiltro.pendiente.value = a.productoId to a.sucursalId
                        navController.navigate(Ruta.GerenciaHistorial.ruta) {
                            popUpTo(Ruta.GerenciaInicio.ruta) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onAprobar = { accion = Accion.Aprobar(a) },
                    onRechazar = { accion = Accion.Rechazar(a) },
                    onDespacho = { accion = Accion.Despacho(a) },
                    onRevisada = { accion = Accion.Revisar(a) }
                )
                }
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    when (val ac = accion) {
        is Accion.Aprobar -> DialogoDespacho(
            titulo = "Aprobar reposición",
            alerta = ac.alerta,
            confirmar = "Aprobar y crear despacho",
            onCerrar = { accion = null },
            onConfirmar = { cantidad, proveedor ->
                accion = null
                viewModel.aprobarReposicion(ac.alerta, cantidad, proveedor) { codigo ->
                    scope.launch { snackbar.showSnackbar("Despacho $codigo creado para ${ac.alerta.detalle.substringBefore(" ·")}") }
                }
            }
        )
        is Accion.Despacho -> DialogoDespacho(
            titulo = "Crear despacho",
            alerta = ac.alerta,
            confirmar = "Crear despacho",
            onCerrar = { accion = null },
            onConfirmar = { cantidad, proveedor ->
                accion = null
                viewModel.crearDespachoDesdeAlerta(ac.alerta, cantidad, proveedor) { codigo ->
                    scope.launch { snackbar.showSnackbar("Despacho $codigo creado") }
                }
            }
        )
        is Accion.Rechazar -> DialogoTexto(
            titulo = "Rechazar reposición",
            etiqueta = "Motivo (el encargado lo verá)",
            obligatorio = true,
            confirmar = "Rechazar",
            onCerrar = { accion = null },
            onConfirmar = { texto ->
                accion = null
                viewModel.rechazarReposicion(ac.alerta, texto)
            }
        )
        is Accion.Revisar -> DialogoTexto(
            titulo = "Marcar revisada",
            etiqueta = "Nota (opcional)",
            obligatorio = false,
            confirmar = "Marcar revisada",
            onCerrar = { accion = null },
            onConfirmar = { texto ->
                accion = null
                viewModel.marcarAlertaRevisada(ac.alerta, texto)
            }
        )
        null -> Unit
    }
}

@Composable
private fun Pestana(texto: String, activa: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(if (activa) Color.White else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (activa) GTexto else GTextoSuave)
    }
}

@Composable
private fun Boton(texto: String, relleno: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .then(
                if (relleno) Modifier.background(GTexto, RoundedCornerShape(24.dp))
                else Modifier.border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (relleno) Color.White else GTexto)
    }
}

@Composable
private fun TarjetaAlerta(
    alerta: AlertaItem,
    onVerHistorial: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onDespacho: () -> Unit,
    onRevisada: () -> Unit
) {
    val (color, fondo) = colores(alerta.tipo)
    val hora = alerta.hora?.let { ms ->
        if (ms >= desdeDias(0)) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms)) else fechaHora(ms)
    } ?: ""
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                etiqueta(alerta.tipo), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color,
                modifier = Modifier.background(fondo, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
            )
            Text(hora, fontSize = 15.sp, color = GTextoSuave)
        }
        Text(alerta.titulo, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
        Text(alerta.detalle, fontSize = 15.sp, color = GTextoSuave)
        if (!alerta.nota.isNullOrBlank()) {
            Text(
                "Nota: ${alerta.nota}", fontSize = 14.sp, color = GTexto,
                modifier = Modifier.fillMaxWidth().background(GFondo, RoundedCornerShape(12.dp)).padding(10.dp)
            )
        }
        if (alerta.revisada) {
            Boton("Ver historial", false, Modifier.fillMaxWidth(), onVerHistorial)
        } else if (alerta.tipo == TipoAlerta.REPOSICION) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Boton("Rechazar", false, Modifier.weight(1f), onRechazar)
                Boton("Aprobar", true, Modifier.weight(1f), onAprobar)
            }
        } else {
            if (alerta.tipo == TipoAlerta.MINIMO) Boton("Crear despacho", true, Modifier.fillMaxWidth(), onDespacho)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Boton("Ver historial", false, Modifier.weight(1f), onVerHistorial)
                Boton("Marcar revisada", alerta.tipo != TipoAlerta.MINIMO, Modifier.weight(1f), onRevisada)
            }
        }
    }
}

/** Diálogo para crear un despacho: cantidad (entero de 1 a 9999) y proveedor. */
@Composable
private fun DialogoDespacho(
    titulo: String,
    alerta: AlertaItem,
    confirmar: String,
    onCerrar: () -> Unit,
    onConfirmar: (cantidad: Int, proveedor: String) -> Unit
) {
    var cantidadTexto by remember { mutableStateOf(alerta.cantidad.coerceIn(1, 9999).toString()) }
    var proveedor by remember { mutableStateOf("") }
    val cantidad = cantidadTexto.toIntOrNull() ?: 0
    val valida = cantidad in 1..9999
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${alerta.titulo} · ${alerta.detalle.substringBefore(" ·")}", fontSize = 14.sp, color = GTextoSuave)
                OutlinedTextField(
                    value = cantidadTexto,
                    onValueChange = { cantidadTexto = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    isError = !valida,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = proveedor,
                    onValueChange = { proveedor = it.take(60) },
                    label = { Text("Proveedor (opcional)") },
                    singleLine = true
                )
                if (!valida) Text("Ingresa una cantidad entre 1 y 9999", color = GRojoTexto, fontSize = 13.sp)
                else Text("El operador recibirá la caja escaneando su código DSP.", color = GTextoSuave, fontSize = 13.sp)
            }
        },
        confirmButton = { TextButton(enabled = valida, onClick = { onConfirmar(cantidad, proveedor) }) { Text(confirmar) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

/** Diálogo con un texto (motivo u observación), de hasta 200 caracteres. */
@Composable
private fun DialogoTexto(
    titulo: String,
    etiqueta: String,
    obligatorio: Boolean,
    confirmar: String,
    onCerrar: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val valido = !obligatorio || texto.isNotBlank()
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it.take(200) },
                label = { Text(etiqueta) },
                supportingText = { Text("${texto.length}/200") },
                isError = !valido
            )
        },
        confirmButton = { TextButton(enabled = valido, onClick = { onConfirmar(texto) }) { Text(confirmar) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
