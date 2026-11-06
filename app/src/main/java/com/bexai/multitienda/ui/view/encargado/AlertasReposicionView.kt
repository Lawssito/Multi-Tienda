package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.model.AlertaReposicion
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.*
import com.bexai.multitienda.viewmodel.EncargadoViewModel
import kotlinx.coroutines.launch

/**
 * AlertasReposicionView: declarar y gestionar alertas de reposición de bodega.
 */
@Composable
fun AlertasReposicionView(
    navController: NavHostController,
    viewModel: EncargadoViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val alertas by viewModel.alertasDeclaradas.collectAsState()

    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Campos formulario de nueva alerta
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var menuProductosAbierto by remember { mutableStateOf(false) }
    var cantidadMinimaTexto by remember { mutableStateOf("20") }
    var motivoTexto by remember { mutableStateOf("") }
    var urgenciaSeleccionada by remember { mutableStateOf("ALTA") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        text = "Alertas de Reposición",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal
                    )
                    Text(
                        text = "Declaración y monitoreo de reabastecimiento",
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

            // Tabs superiores
            TabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = Superficie,
                contentColor = Azul
            ) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    text = { Text("Activas (${alertas.size})", fontSize = 13.sp) }
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    text = { Text("Declarar Alerta", fontSize = 13.sp) }
                )
                Tab(
                    selected = tabSeleccionado == 2,
                    onClick = { tabSeleccionado = 2 },
                    text = { Text("Detección Crítica", fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (tabSeleccionado) {
                0 -> {
                    // Listado de alertas declaradas
                    if (alertas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay alertas activas pendientes", color = TextoSecundario)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(alertas, key = { it.id }) { alerta ->
                                AlertaCard(
                                    alerta = alerta,
                                    onResolver = {
                                        viewModel.resolverAlerta(alerta.id)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Alerta de ${alerta.nombreProducto} resuelta")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Formulario de emisión de alerta
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Declarar Necesidad de Reposición",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        // Selector de producto
                        Box {
                            OutlinedTextField(
                                value = productoSeleccionado?.let { "${it.nombre} (Stock: ${it.stock})" } ?: "Seleccionar Producto",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Producto a reponer") },
                                trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuProductosAbierto = true }
                            )

                            DropdownMenu(
                                expanded = menuProductosAbierto,
                                onDismissRequest = { menuProductosAbierto = false }
                            ) {
                                productos.forEach { prod ->
                                    DropdownMenuItem(
                                        text = { Text("${prod.nombre} (Stock: ${prod.stock} uds)") },
                                        onClick = {
                                            productoSeleccionado = prod
                                            menuProductosAbierto = false
                                        }
                                    )
                                }
                            }
                        }

                        // Cantidad mínima requerida
                        OutlinedTextField(
                            value = cantidadMinimaTexto,
                            onValueChange = { cantidadMinimaTexto = it },
                            label = { Text("Cantidad solicitada / Umbral mínimo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selector de Urgencia
                        Text("Nivel de Urgencia:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("ALTA", "MEDIA", "BAJA").forEach { nivel ->
                                FilterChip(
                                    selected = urgenciaSeleccionada == nivel,
                                    onClick = { urgenciaSeleccionada = nivel },
                                    label = { Text(nivel) }
                                )
                            }
                        }

                        // Motivo
                        OutlinedTextField(
                            value = motivoTexto,
                            onValueChange = { motivoTexto = it },
                            label = { Text("Motivo de la alerta") },
                            placeholder = { Text("Ej. Quiebre de stock inminente, alta rotación, etc.") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val prod = productoSeleccionado
                                val cant = cantidadMinimaTexto.toIntOrNull() ?: 10
                                if (prod != null) {
                                    viewModel.declararAlerta(
                                        producto = prod,
                                        cantidadMinima = cant,
                                        motivo = motivoTexto,
                                        urgencia = urgenciaSeleccionada,
                                        onExito = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Alerta declarada para ${prod.nombre}")
                                            }
                                            motivoTexto = ""
                                            tabSeleccionado = 0
                                        }
                                    )
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Seleccione un producto primero")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Azul),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Outlined.NotificationAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emitir Alerta de Reposición", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    // Detección automática de productos en quiebre o bajo stock
                    val criticos = productos.filter { it.stock < 10 }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(criticos, key = { it.id }) { prod ->
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
                                        Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Stock actual: ${prod.stock} uds • Cat: ${prod.categoria}", fontSize = 12.sp, color = TextoSecundario)
                                        Text(
                                            text = if (prod.stock == 0) "¡En Quiebre Total!" else "Stock Crítico",
                                            color = if (prod.stock == 0) Peligro else Advertencia,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            productoSeleccionado = prod
                                            cantidadMinimaTexto = "25"
                                            motivoTexto = if (prod.stock == 0) "Quiebre detectado automáticamente" else "Stock bajo mínimo"
                                            urgenciaSeleccionada = if (prod.stock == 0) "ALTA" else "MEDIA"
                                            tabSeleccionado = 1
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Azul),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Gestionar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertaCard(
    alerta: AlertaReposicion,
    onResolver: () -> Unit
) {
    val (fondoBadge, textoBadge) = when (alerta.urgencia) {
        "ALTA" -> Pair(PeligroSuave, Peligro)
        "MEDIA" -> Pair(AdvertenciaSuave, Advertencia)
        else -> Pair(AzulSuave, Azul)
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
                        text = alerta.nombreProducto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextoPrincipal
                    )
                    Text(
                        text = "Stock Actual: ${alerta.stockActual} uds • Pedir: ${alerta.cantidadMinima} uds",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }

                Box(
                    modifier = Modifier
                        .background(fondoBadge, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = alerta.urgencia,
                        color = textoBadge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Motivo: ${alerta.motivo}",
                fontSize = 13.sp,
                color = TextoPrincipal
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onResolver,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Marcar Atendida", fontSize = 12.sp)
                }
            }
        }
    }
}
