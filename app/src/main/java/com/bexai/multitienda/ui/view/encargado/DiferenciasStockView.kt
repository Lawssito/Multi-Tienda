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
// ---------- Material 3 en esta pantalla (todo viene de androidx.compose.material3) ----------
// Button            -> botón "Declarar reposición" (TarjetaDiferencia)
// ButtonDefaults    -> configura los colores del Button (no es un componente visible)
// FilterChip        -> filtros "Todas / Faltantes / Sobrantes"
// FilterChipDefaults-> configura los colores del FilterChip (no es un componente visible)
// SnackbarHost      -> lugar donde aparece el aviso inferior (Snackbar)
// SnackbarHostState -> estado que controla cuándo se muestra el Snackbar
// Text              -> todos los textos de la pantalla
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.FondoPantalla
import com.bexai.multitienda.ui.theme.NaranjaFondo
import com.bexai.multitienda.ui.theme.NaranjaTexto
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.encargado.DiferenciaStock
import com.bexai.multitienda.viewmodel.encargado.EncargadoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // MATERIAL 3 · SnackbarHostState: guarda y muestra el aviso inferior (Snackbar)
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
                        fontSize = 15.sp, color = TextoSecundario
                    )
                    Text("Diferencias", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipal)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Resumen("${diferencias.size}", "Con descalce", NaranjaFondo, NaranjaTexto, Modifier.weight(1f))
                    Resumen("$bajoMinimo", "Bajo mínimo", PeligroSuave, Peligro, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Todas", "Faltantes", "Sobrantes").forEach { nombre ->
                        // MATERIAL 3 · FilterChip: chip de filtro. "selected" indica si está elegido
                        // y Material 3 cambia solo su apariencia; FilterChipDefaults pone nuestros colores.
                        FilterChip(
                            selected = filtro == nombre,
                            onClick = { filtro = nombre },
                            label = { Text(nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(24.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = TextoPrincipal,
                                selectedContainerColor = TextoPrincipal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            if (visibles.isEmpty()) {
                item {
                    Text(
                        if (diferencias.isEmpty()) "El último conteo no tiene diferencias." else "Sin diferencias en este filtro.",
                        fontSize = 15.sp, color = TextoSecundario
                    )
                }
            }
            items(visibles, key = { "dif-${it.productoId}" }) { d ->
                TarjetaDiferencia(d, onReponer = { reponer = d })
            }
        }
        // MATERIAL 3 · SnackbarHost: muestra el Snackbar abajo de la pantalla
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
            Text(d.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal, modifier = Modifier.weight(1f))
            Text(
                text = if (falta) "▼ −${-d.diferencia} uds" else "▲ +${d.diferencia} uds",
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = if (falta) NaranjaTexto else AzulOscuro,
                modifier = Modifier
                    .background(if (falta) NaranjaFondo else AzulSuave, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Dato("Registrado", "${d.registrado}", TextoPrincipal, Modifier.weight(1f))
            Dato("Contado", "${d.contado}", TextoPrincipal, Modifier.weight(1f))
            Dato("Mínimo", "${d.minimo}", if (d.contado < d.minimo) Peligro else TextoPrincipal, Modifier.weight(1f))
        }
        if (falta) {
            // MATERIAL 3 · Button: botón con efecto al tocar y accesible;
            // ButtonDefaults.buttonColors le pone el azul de la app.
            Button(
                onClick = onReponer,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul, contentColor = Color.White)
            ) { Text("Declarar reposición", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(etiqueta, fontSize = 14.sp, color = TextoSecundario)
        Text(valor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
