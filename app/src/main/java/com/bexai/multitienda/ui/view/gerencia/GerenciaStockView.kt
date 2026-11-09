package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.viewmodel.GerenciaViewModel

/**
 * Stock global (Gerencia): todos los productos con su total y el stock de cada sucursal.
 * Filtros Todos, Quiebre (total 0), Bajo stock (alguna sucursal bajo su mínimo) y Normal.
 * Al tocar un producto se ve el detalle por sucursal y se edita el stock mínimo.
 */
@Composable
fun GerenciaStockView(
    modifier: Modifier = Modifier,
    viewModel: GerenciaViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val stocks by viewModel.stockSucursal.collectAsState()
    val tiendas by viewModel.tiendas.collectAsState()
    val contexto = LocalContext.current

    var query by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("Todos") }
    var detalle by remember { mutableStateOf<Producto?>(null) }

    fun sucursalesBajo(p: Producto) = stockPorSucursal(p, stocks, tiendas).count { it < p.stockMinimo }
    val quiebre = productos.count { it.stock <= 0 }
    val bajo = productos.count { it.stock > 0 && sucursalesBajo(it) > 0 }
    val normal = productos.count { sucursalesBajo(it) == 0 }

    val visibles = productos.filter { p ->
        val coincide = query.isBlank() ||
            p.nombre.contains(query, ignoreCase = true) ||
            p.codigoBarras.contains(query) ||
            p.categoria.contains(query, ignoreCase = true)
        val estado = when (filtro) {
            "Quiebre" -> p.stock <= 0
            "Bajo stock" -> p.stock > 0 && sucursalesBajo(p) > 0
            "Normal" -> sucursalesBajo(p) == 0
            else -> true
        }
        coincide && estado
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GFondo)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${productos.sumOf { it.stock }} uds en ${tiendas.size} sucursales", fontSize = 15.sp, color = GTextoSuave)
                Text("Stock global", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
            }
            BotonPdf(onClick = { exportarStockPdf(contexto, visibles, stocks, tiendas) })
        }

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
            Icon(Icons.Outlined.Search, contentDescription = null, tint = GTextoSuave, modifier = Modifier.size(20.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Buscar producto o código", fontSize = 15.sp, color = GTextoSuave)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = GTexto),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GChip("Todos ${productos.size}", filtro == "Todos") { filtro = "Todos" }
            GChip("Quiebre $quiebre", filtro == "Quiebre") { filtro = if (filtro == "Quiebre") "Todos" else "Quiebre" }
            GChip("Bajo stock $bajo", filtro == "Bajo stock") { filtro = if (filtro == "Bajo stock") "Todos" else "Bajo stock" }
            GChip("Normal $normal", filtro == "Normal") { filtro = if (filtro == "Normal") "Todos" else "Normal" }
        }

        if (visibles.isEmpty()) {
            Text("No hay productos que coincidan", fontSize = 15.sp, color = GTextoSuave)
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(22.dp))
        ) {
            items(visibles, key = { it.id }) { p ->
                val porSucursal = stockPorSucursal(p, stocks, tiendas)
                val hayBajo = porSucursal.any { it < p.stockMinimo }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { detalle = p }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GTexto, modifier = Modifier.weight(1f))
                        Text(
                            "${p.stock}", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            color = if (p.stock <= 0) GRojo else GTexto
                        )
                    }
                    Text(
                        tiendas.mapIndexed { i, t -> "${nombreCorto(t)} ${porSucursal.getOrElse(i) { 0 }}" }.joinToString(" · "),
                        fontSize = 14.sp,
                        color = if (hayBajo) GRojoTexto else GTextoSuave
                    )
                    Text(
                        "${p.categoria.ifBlank { "Sin categoría" }} · mínimo ${p.stockMinimo}",
                        fontSize = 13.sp, color = GTextoSuave
                    )
                }
            }
        }
    }

    detalle?.let { p ->
        val actual = productos.firstOrNull { it.id == p.id } ?: p
        HojaStockGlobal(
            producto = actual,
            filas = tiendas.map { nombreCorto(it) } .zip(stockPorSucursal(actual, stocks, tiendas)),
            onCerrar = { detalle = null },
            onGuardarMinimo = { minimo ->
                viewModel.actualizarMinimo(actual.id, minimo) { detalle = null }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaStockGlobal(
    producto: Producto,
    filas: List<Pair<String, Int>>,
    onCerrar: () -> Unit,
    onGuardarMinimo: (Int) -> Unit
) {
    var texto by remember { mutableStateOf(producto.stockMinimo.toString()) }
    val minimo = texto.toIntOrNull()
    val valido = minimo != null && minimo in 0..9999

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(producto.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                    Text("${producto.categoria.ifBlank { "Sin categoría" }} · total ${producto.stock} uds", fontSize = 14.sp, color = GTextoSuave)
                }
                Box(
                    modifier = Modifier.size(40.dp).background(GFondo, CircleShape).clickable(onClick = onCerrar),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = GTexto) }
            }

            Text("Por sucursal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GTexto)
            Column(
                modifier = Modifier.fillMaxWidth().background(GFondo, RoundedCornerShape(16.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filas.forEach { (nombre, cantidad) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(nombre, fontSize = 15.sp, color = GTexto)
                        Text(
                            "$cantidad uds", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            color = if (cantidad < producto.stockMinimo) GRojoTexto else GTexto
                        )
                    }
                }
            }

            Text("Stock mínimo (por sucursal)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GTexto)
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it.filter { c -> c.isDigit() }.take(4) },
                singleLine = true,
                isError = !valido,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Text(
                if (valido) "Cada sucursal se alerta cuando baja de este número." else "Ingresa un número entre 0 y 9999",
                fontSize = 14.sp, color = if (valido) GTextoSuave else GRojoTexto
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(if (valido) Azul else Color(0xFFCBD5E1), RoundedCornerShape(26.dp))
                    .clickable(enabled = valido) { minimo?.let(onGuardarMinimo) },
                contentAlignment = Alignment.Center
            ) { Text("Guardar mínimo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
