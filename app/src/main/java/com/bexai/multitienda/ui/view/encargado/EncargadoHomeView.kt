package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.ui.components.PastillaSucursal
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.viewmodel.EncargadoViewModel

// Colores del mockup v6 (Stock.dc.html)
private val FondoPantalla = Color(0xFFF4F6FA)
private val Texto = Color(0xFF0F172A)
private val TextoSuave = Color(0xFF5B6474)
private val Rojo = Color(0xFFDC2626)
private val RojoTexto = Color(0xFFB91C1C)
private val Pista = Color(0xFFE9EDF3)

/**
 * Stock de la sucursal (mockup "Stock"): buscador, filtros Todos, Quiebre, Bajo stock y Normal,
 * y una lista con barra de nivel por producto. Al tocar un producto se abre "Declarar reposición".
 */
@Composable
fun EncargadoHomeView(
    modifier: Modifier = Modifier,
    onCambiarSucursal: () -> Unit = {},
    viewModel: EncargadoViewModel = viewModel()
) {
    val productos by viewModel.productosFiltrados.collectAsState()
    val todos by viewModel.productos.collectAsState()
    val sucursales by viewModel.sucursales.collectAsState()
    val sucursalId by viewModel.sucursalSeleccionadaId.collectAsState()
    val query by viewModel.query.collectAsState()
    val estado by viewModel.estadoFiltro.collectAsState()

    val nombreSucursal = sucursales.firstOrNull { it.id == (sucursalId ?: 1) }?.nombre ?: ""
    val quiebre = todos.count { it.stock <= 0 }
    val bajoStock = todos.count { it.stock > 0 && it.stock < it.stockMinimo }
    val normal = todos.count { it.stock >= it.stockMinimo }
    val maximo = (todos.maxOfOrNull { it.stock } ?: 1).coerceAtLeast(1)
    var reponer by remember { mutableStateOf<com.bexai.multitienda.model.Producto?>(null) }
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PastillaSucursal(nombreSucursal, onClick = onCambiarSucursal)
            Text("Encargado", fontSize = 15.sp, color = TextoSuave)
        }

        Text("Stock", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Texto)

        // Buscador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.5.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = TextoSuave, modifier = Modifier.size(20.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Buscar producto o código", fontSize = 15.sp, color = TextoSuave)
                BasicTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = Texto),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Filtros
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Filtro("Todos ${todos.size}", seleccionado = estado == "Todos") {
                viewModel.onEstadoFiltroChange("Todos")
            }
            Filtro("Quiebre $quiebre", seleccionado = estado == "Quiebre") {
                viewModel.onEstadoFiltroChange(if (estado == "Quiebre") "Todos" else "Quiebre")
            }
            Filtro("Bajo stock $bajoStock", seleccionado = estado == "Bajo stock") {
                viewModel.onEstadoFiltroChange(if (estado == "Bajo stock") "Todos" else "Bajo stock")
            }
            Filtro("Normal $normal", seleccionado = estado == "Normal") {
                viewModel.onEstadoFiltroChange(if (estado == "Normal") "Todos" else "Normal")
            }
        }

        if (productos.isEmpty()) {
            Text("No hay productos que coincidan", fontSize = 15.sp, color = TextoSuave)
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
        ) {
            items(productos, key = { it.id }) { p ->
                val bajo = p.stock < p.stockMinimo
                val sinStock = p.stock <= 0
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reponer = p }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Texto, modifier = Modifier.weight(1f))
                        Text("${p.stock}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (bajo) Rojo else Texto)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Pista, RoundedCornerShape(4.dp))) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((p.stock.toFloat() / maximo).coerceIn(0.03f, 1f))
                                .height(8.dp)
                                .background(if (bajo) Rojo else Azul, RoundedCornerShape(4.dp))
                        )
                    }
                    Text(
                        text = if (sinStock) "! Quiebre · mínimo ${p.stockMinimo}"
                        else if (bajo) "! Bajo stock · mínimo ${p.stockMinimo}"
                        else "${p.categoria.ifBlank { "Sin categoría" }} · mínimo ${p.stockMinimo}",
                        fontSize = 14.sp,
                        color = if (bajo) RojoTexto else TextoSuave
                    )
                }
            }
        }
        androidx.compose.material3.SnackbarHost(snackbar)
    }

    reponer?.let { p ->
        HojaReposicionView(
            nombre = p.nombre,
            sucursal = nombreSucursal,
            stock = p.stock,
            minimo = p.stockMinimo,
            onCerrar = { reponer = null },
            onEnviar = { cantidad, urgente, nota ->
                viewModel.declararReposicion(p.id, cantidad, urgente, nota) {
                    reponer = null
                    scope.launch { snackbar.showSnackbar("Reposición enviada a gerencia · $cantidad uds") }
                }
            }
        )
    }
}

@Composable
private fun Filtro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Text(
        text = texto,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (seleccionado) Color.White else Texto,
        modifier = Modifier
            .background(if (seleccionado) Texto else Color.White, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    )
}
