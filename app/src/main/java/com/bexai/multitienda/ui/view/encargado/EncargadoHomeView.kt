package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
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
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.*
import com.bexai.multitienda.viewmodel.EncargadoViewModel

/**
 * EncargadoHomeView: consulta y exploración de stock en bodega en tiempo real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncargadoHomeView(
    navController: NavHostController,
    viewModel: EncargadoViewModel = viewModel()
) {
    val productos by viewModel.productosFiltrados.collectAsState()
    val todosLosProductos by viewModel.productos.collectAsState()
    val sucursales by viewModel.sucursales.collectAsState()
    val sucursalId by viewModel.sucursalSeleccionadaId.collectAsState()

    val query by viewModel.query.collectAsState()
    val categoriaFiltro by viewModel.categoriaFiltro.collectAsState()
    val estadoFiltro by viewModel.estadoFiltro.collectAsState()

    var menuSucursalAbierto by remember { mutableStateOf(false) }
    val sucursalActual = sucursales.find { it.id == sucursalId } ?: sucursales.firstOrNull()

    val quiebresCount = todosLosProductos.count { it.stock == 0 }
    val bajoStockCount = todosLosProductos.count { it.stock in 1..9 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Encabezado superior con Logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Stock de Bodega",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Text(
                    text = "Consulta y supervisión de existencias",
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

        // Selector de Sucursal
        Card(
            colors = CardDefaults.cardColors(containerColor = Superficie),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { menuSucursalAbierto = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Store, contentDescription = null, tint = Azul)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = sucursalActual?.nombre ?: "Seleccionar Sucursal",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = sucursalActual?.direccion ?: "Dirección de bodega",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }
                }
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = menuSucursalAbierto,
                onDismissRequest = { menuSucursalAbierto = false }
            ) {
                sucursales.forEach { suc ->
                    DropdownMenuItem(
                        text = { Text(suc.nombre + if (!suc.activa) " (Inactiva)" else "") },
                        onClick = {
                            viewModel.onSucursalChange(suc.id)
                            menuSucursalAbierto = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjetas métricas rápidas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                titulo = "Total Items",
                valor = "${todosLosProductos.size}",
                colorFondo = AzulSuave,
                colorTexto = AzulOscuro,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                titulo = "En Quiebre",
                valor = "$quiebresCount",
                colorFondo = PeligroSuave,
                colorTexto = Peligro,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                titulo = "Bajo Stock",
                valor = "$bajoStockCount",
                colorFondo = AdvertenciaSuave,
                colorTexto = Advertencia,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barra de Búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Buscar por nombre, código o categoría...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Superficie,
                unfocusedContainerColor = Superficie
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtro por Estado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Todos", "Quiebre", "Bajo Stock", "Normal").forEach { est ->
                FilterChip(
                    selected = estadoFiltro == est,
                    onClick = { viewModel.onEstadoFiltroChange(est) },
                    label = { Text(est, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Lista de Productos
        if (productos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = TextoSecundario,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se encontraron productos coincidentes",
                        color = TextoSecundario,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    ProductoBodegaCard(
                        producto = producto,
                        onReportarDiferencia = {
                            navController.navigate(Ruta.EncargadoDiferencias.ruta)
                        },
                        onDeclararAlerta = {
                            navController.navigate(Ruta.EncargadoAlertasReposicion.ruta)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    titulo: String,
    valor: String,
    colorFondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorTexto)
            Text(text = titulo, fontSize = 11.sp, color = colorTexto)
        }
    }
}

@Composable
fun ProductoBodegaCard(
    producto: Producto,
    onReportarDiferencia: () -> Unit,
    onDeclararAlerta: () -> Unit
) {
    val (colorFondoBadge, colorTextoBadge, estadoLabel) = when {
        producto.stock == 0 -> Triple(PeligroSuave, Peligro, "QUIEBRE")
        producto.stock < 10 -> Triple(AdvertenciaSuave, Advertencia, "BAJO STOCK")
        else -> Triple(ExitoSuave, Exito, "NORMAL")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Superficie),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextoPrincipal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Código: ${producto.codigoBarras.ifBlank { "N/A" }} • Cat: ${producto.categoria}",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }

                // Badge de Stock
                Box(
                    modifier = Modifier
                        .background(colorFondoBadge, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$estadoLabel (${producto.stock})",
                        color = colorTextoBadge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción rápida
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReportarDiferencia,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Diferencia", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDeclararAlerta,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Alerta", fontSize = 12.sp)
                }
            }
        }
    }
}
