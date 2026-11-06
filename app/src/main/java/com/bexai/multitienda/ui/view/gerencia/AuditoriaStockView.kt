package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.*
import com.bexai.multitienda.viewmodel.GerenciaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AuditoriaStockView: auditar existencias globales, revisar quiebres e historial de movimientos.
 */
@Composable
fun AuditoriaStockView(
    navController: NavHostController,
    viewModel: GerenciaViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val quiebres by viewModel.productosQuiebre.collectAsState()
    val bajoStock by viewModel.productosStockBajo.collectAsState()
    val historial by viewModel.historialMovimientos.collectAsState()

    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var busquedaGlobal by remember { mutableStateOf("") }

    val productosFiltrados = remember(productos, busquedaGlobal) {
        if (busquedaGlobal.isBlank()) productos
        else productos.filter {
            it.nombre.contains(busquedaGlobal, ignoreCase = true) ||
            it.categoria.contains(busquedaGlobal, ignoreCase = true) ||
            it.codigoBarras.contains(busquedaGlobal)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Auditoría de Stock",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Text(
                    text = "Existencias globales y trazabilidad de cambios",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }

            IconButton(
                onClick = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = Peligro
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pestañas
        TabRow(
            selectedTabIndex = tabSeleccionado,
            containerColor = Superficie,
            contentColor = Azul
        ) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 },
                text = { Text("Quiebres (${quiebres.size + bajoStock.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = { Text("Existencias (${productos.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = tabSeleccionado == 2,
                onClick = { tabSeleccionado = 2 },
                text = { Text("Historial (${historial.size})", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tabSeleccionado) {
            0 -> {
                // Alertas de Quiebre y Stock Crítico
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Text(
                            text = "Productos en Quiebre Total (0 uds): ${quiebres.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Peligro
                        )
                    }
                    items(quiebres, key = { "q_${it.id}" }) { prod ->
                        ItemAlertaQuiebre(producto = prod, esQuiebreTotal = true)
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Productos con Stock Crítico (<10 uds): ${bajoStock.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Advertencia
                        )
                    }
                    items(bajoStock, key = { "b_${it.id}" }) { prod ->
                        ItemAlertaQuiebre(producto = prod, esQuiebreTotal = false)
                    }
                }
            }
            1 -> {
                // Existencias Globales
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = busquedaGlobal,
                        onValueChange = { busquedaGlobal = it },
                        placeholder = { Text("Buscar en existencias globales...") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(productosFiltrados, key = { it.id }) { prod ->
                            ItemExistenciaCard(prod)
                        }
                    }
                }
            }
            2 -> {
                // Historial de Modificaciones
                if (historial.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay movimientos registrados", color = TextoSecundario)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(historial, key = { it.id }) { mov ->
                            val prod = productos.find { it.id == mov.productoId }
                            ItemHistorialCard(mov, prod)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemAlertaQuiebre(producto: Producto, esQuiebreTotal: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (esQuiebreTotal) PeligroSuave.copy(alpha = 0.5f) else AdvertenciaSuave.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Categoría: ${producto.categoria} • Cód: ${producto.codigoBarras}", fontSize = 12.sp, color = TextoSecundario)
            }
            Box(
                modifier = Modifier
                    .background(if (esQuiebreTotal) Peligro else Advertencia, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (esQuiebreTotal) "QUIEBRE (0)" else "BAJO (${producto.stock})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ItemExistenciaCard(producto: Producto) {
    val (fondo, texto, label) = when {
        producto.stock == 0 -> Triple(PeligroSuave, Peligro, "0 uds")
        producto.stock < 10 -> Triple(AdvertenciaSuave, Advertencia, "${producto.stock} uds")
        else -> Triple(ExitoSuave, Exito, "${producto.stock} uds")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Superficie),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${producto.categoria} • Cód: ${producto.codigoBarras}", fontSize = 12.sp, color = TextoSecundario)
            }

            Box(
                modifier = Modifier
                    .background(fondo, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(label, color = texto, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ItemHistorialCard(mov: Movimiento, prod: Producto?) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaStr = dateFormat.format(Date(mov.fechaHora))

    val (colorTipo, iconoTipo) = when (mov.tipo) {
        TipoMovimiento.ENTRADA -> Pair(Exito, Icons.Outlined.ArrowDownward)
        TipoMovimiento.SALIDA -> Pair(Peligro, Icons.Outlined.ArrowUpward)
        TipoMovimiento.AJUSTE -> Pair(Azul, Icons.Outlined.Tune)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Superficie),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(colorTipo.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoTipo, contentDescription = null, tint = colorTipo, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = prod?.nombre ?: "Producto #${mov.productoId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${mov.tipo} (${mov.cantidad})",
                        color = colorTipo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = mov.motivo ?: "Sin motivo especificado",
                    fontSize = 12.sp,
                    color = TextoPrincipal
                )

                Text(
                    text = "Fecha: $fechaStr • Sucursal #${mov.sucursalId}",
                    fontSize = 11.sp,
                    color = TextoSecundario
                )
            }
        }
    }
}
