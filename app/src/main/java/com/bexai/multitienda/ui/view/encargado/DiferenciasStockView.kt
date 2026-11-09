package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.ui.components.PastillaSucursal
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.viewmodel.DiferenciaStock
import com.bexai.multitienda.viewmodel.EncargadoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores del mockup v6 (Diferencias.dc.html)
private val FondoPantalla = Color(0xFFF4F6FA)
private val Texto = Color(0xFF0F172A)
private val TextoSuave = Color(0xFF5B6474)
private val NaranjaTexto = Color(0xFFB45309)
private val NaranjaFondo = Color(0xFFFFEBD2)
private val AzulTexto = Color(0xFF1D4ED8)
private val AzulFondo = Color(0xFFDBEAFE)
private val RojoTexto = Color(0xFFB91C1C)
private val RojoFondo = Color(0xFFFEE2E2)

/**
 * Diferencias de la sucursal (mockup "Diferencias"): solo análisis del último conteo;
 * las tareas pendientes (cajas y conteos por revisar) viven en Alertas.
 * Resumen (con descalce y bajo mínimo), filtros Todas, Faltantes y Sobrantes, y cada
 * diferencia del último conteo con acceso a "Declarar reposición".
 */
@Composable
fun DiferenciasStockView(
    modifier: Modifier = Modifier,
    onCambiarSucursal: () -> Unit = {},
    viewModel: EncargadoViewModel = viewModel()
) {
    val diferencias by viewModel.diferenciasUltimoConteo.collectAsState()
    val fechaConteo by viewModel.fechaUltimoConteo.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val sucursales by viewModel.sucursales.collectAsState()
    val sucursalId by viewModel.sucursalSeleccionadaId.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var filtro by remember { mutableStateOf("Todas") }
    var reponer by remember { mutableStateOf<DiferenciaStock?>(null) }

    val nombreSucursal = sucursales.firstOrNull { it.id == (sucursalId ?: 1) }?.nombre ?: ""
    val hora = fechaConteo?.let { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(it)) }
    val bajoMinimo = productos.count { it.stock < it.stockMinimo }
    val visibles = diferencias.filter {
        when (filtro) {
            "Faltantes" -> it.diferencia < 0
            "Sobrantes" -> it.diferencia > 0
            else -> true
        }
    }

    Box(modifier = modifier.fillMaxSize().background(FondoPantalla)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PastillaSucursal(nombreSucursal, onClick = onCambiarSucursal)
                    Text(
                        if (hora != null) "Último conteo $hora" else "Sin conteos enviados",
                        fontSize = 15.sp, color = TextoSuave
                    )
                    Text("Diferencias", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Texto)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Resumen("${diferencias.size}", "Con descalce", NaranjaFondo, NaranjaTexto, Modifier.weight(1f))
                    Resumen("$bajoMinimo", "Bajo mínimo", RojoFondo, RojoTexto, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Todas", "Faltantes", "Sobrantes").forEach { nombre ->
                        val elegido = filtro == nombre
                        Text(
                            text = nombre,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (elegido) Color.White else Texto,
                            modifier = Modifier
                                .background(if (elegido) Texto else Color.White, RoundedCornerShape(24.dp))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
                                .clickable { filtro = nombre }
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            if (visibles.isEmpty()) {
                item {
                    Text(
                        if (diferencias.isEmpty()) "El último conteo no tiene diferencias." else "Sin diferencias en este filtro.",
                        fontSize = 15.sp, color = TextoSuave
                    )
                }
            }
            items(visibles, key = { "dif-${it.productoId}" }) { d ->
                TarjetaDiferencia(d, onReponer = { reponer = d })
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    reponer?.let { d ->
        val producto = productos.firstOrNull { it.id == d.productoId }
        HojaReposicionView(
            nombre = d.nombre,
            sucursal = nombreSucursal,
            stock = producto?.stock ?: d.contado,
            minimo = d.minimo,
            onCerrar = { reponer = null },
            onEnviar = { cantidad, urgente, nota ->
                viewModel.declararReposicion(d.productoId, cantidad, urgente, nota) {
                    reponer = null
                    scope.launch { snackbar.showSnackbar("Reposición enviada a gerencia · $cantidad uds") }
                }
            }
        )
    }
}

@Composable
private fun Resumen(valor: String, etiqueta: String, fondo: Color, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.background(fondo, RoundedCornerShape(22.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(valor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(etiqueta, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun TarjetaDiferencia(d: DiferenciaStock, onReponer: () -> Unit) {
    val falta = d.diferencia < 0
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(d.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Texto, modifier = Modifier.weight(1f))
            Text(
                text = if (falta) "▼ −${-d.diferencia} uds" else "▲ +${d.diferencia} uds",
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = if (falta) NaranjaTexto else AzulTexto,
                modifier = Modifier
                    .background(if (falta) NaranjaFondo else AzulFondo, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Dato("Registrado", "${d.registrado}", Texto, Modifier.weight(1f))
            Dato("Contado", "${d.contado}", Texto, Modifier.weight(1f))
            Dato("Mínimo", "${d.minimo}", if (d.contado < d.minimo) RojoTexto else Texto, Modifier.weight(1f))
        }
        if (falta) {
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp).background(Azul, RoundedCornerShape(24.dp)).clickable(onClick = onReponer),
                contentAlignment = Alignment.Center
            ) { Text("Declarar reposición", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(etiqueta, fontSize = 14.sp, color = TextoSuave)
        Text(valor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
