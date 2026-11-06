package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Undo
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
import kotlinx.coroutines.launch

/**
 * DiferenciasStockView: permite comparar el stock en sistema con el stock físico contado
 * y registrar ajustes de conciliación con soporte de deshacer (Undo).
 */
@Composable
fun DiferenciasStockView(
    navController: NavHostController,
    viewModel: EncargadoViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val conteoFisico by viewModel.conteoFisico.collectAsState()
    val ultimoAjuste by viewModel.ultimoAjuste.collectAsState()
    val conteosEnviados by viewModel.conteosEnviados.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var productoParaAjustar by remember { mutableStateOf<Producto?>(null) }
    var stockContadoParaAjustar by remember { mutableIntStateOf(0) }
    var motivoTexto by remember { mutableStateOf("") }

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
                        text = "Diferencias de Stock",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal
                    )
                    Text(
                        text = "Inventario registrado vs conteo físico",
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

            // Banner de Deshacer (Undo) si hay un ajuste previo
            if (ultimoAjuste != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AzulSuave),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ajuste aplicado a ${ultimoAjuste?.nombre}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AzulOscuro
                            )
                            Text(
                                text = "Anterior: ${ultimoAjuste?.stockAnterior} uds ➔ Nuevo: ${ultimoAjuste?.stockNuevo} uds",
                                fontSize = 12.sp,
                                color = AzulOscuro
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.deshacerUltimoAjuste { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Azul),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deshacer", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Lista de productos con comparador de stock
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(conteosEnviados, key = { "conteo-${it.id}" }) { conteo ->
                    ConteoPendienteCard(
                        conteo = conteo,
                        viewModel = viewModel,
                        onMensaje = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                    )
                }
                items(productos, key = { it.id }) { producto ->
                    val stockContado = conteoFisico[producto.id] ?: producto.stock
                    val diferencia = stockContado - producto.stock

                    ItemDiferenciaCard(
                        producto = producto,
                        stockContado = stockContado,
                        diferencia = diferencia,
                        onSumarContado = {
                            viewModel.actualizarStockContado(producto.id, stockContado + 1)
                        },
                        onRestarContado = {
                            viewModel.actualizarStockContado(producto.id, (stockContado - 1).coerceAtLeast(0))
                        },
                        onAplicarAjuste = {
                            productoParaAjustar = producto
                            stockContadoParaAjustar = stockContado
                            motivoTexto = if (diferencia != 0) "Conteo físico: diferencia de $diferencia uds" else "Confirmación de inventario"
                        }
                    )
                }
            }
        }
    }

    // Diálogo de Confirmación de Ajuste
    if (productoParaAjustar != null) {
        val prod = productoParaAjustar!!
        val dif = stockContadoParaAjustar - prod.stock

        AlertDialog(
            onDismissRequest = { productoParaAjustar = null },
            title = { Text("Confirmar Ajuste de Stock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: ${prod.nombre}", fontWeight = FontWeight.SemiBold)
                    Text("Stock en Sistema: ${prod.stock} uds")
                    Text("Stock Contado (Físico): $stockContadoParaAjustar uds", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (dif == 0) "Sin variación" else if (dif > 0) "Sobrante de +$dif uds" else "Faltante de $dif uds",
                        color = if (dif == 0) Exito else if (dif > 0) Azul else Peligro,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = motivoTexto,
                        onValueChange = { motivoTexto = it },
                        label = { Text("Motivo del ajuste") },
                        placeholder = { Text("Ej. Merma, Error de conteo, etc.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.aplicarAjuste(
                            producto = prod,
                            stockContado = stockContadoParaAjustar,
                            motivo = motivoTexto,
                            onExito = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Stock de ${prod.nombre} actualizado a $stockContadoParaAjustar uds")
                                }
                                productoParaAjustar = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text("Guardar Ajuste")
                }
            },
            dismissButton = {
                TextButton(onClick = { productoParaAjustar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ItemDiferenciaCard(
    producto: Producto,
    stockContado: Int,
    diferencia: Int,
    onSumarContado: () -> Unit,
    onRestarContado: () -> Unit,
    onAplicarAjuste: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Superficie),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Nombre y badge de diferencia
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
                    Text(
                        text = "Cat: ${producto.categoria} • Cód: ${producto.codigoBarras.ifBlank { "N/A" }}",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }

                val (fondoDif, textoDif, labelDif) = when {
                    diferencia == 0 -> Triple(ExitoSuave, Exito, "OK (0)")
                    diferencia > 0 -> Triple(AzulSuave, AzulOscuro, "+$diferencia Sobrante")
                    else -> Triple(PeligroSuave, Peligro, "$diferencia Faltante")
                }

                Box(
                    modifier = Modifier
                        .background(fondoDif, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = labelDif,
                        color = textoDif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filas de Stock Sistema vs Stock Contado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Sistema
                Column {
                    Text(text = "Sistema", fontSize = 11.sp, color = TextoSecundario)
                    Text(
                        text = "${producto.stock} uds",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoPrincipal
                    )
                }

                // Control contador físico
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onRestarContado,
                        modifier = Modifier
                            .size(34.dp)
                            .background(GrisBarra.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Menos", modifier = Modifier.size(18.dp))
                    }

                    Box(
                        modifier = Modifier
                            .widthIn(min = 44.dp)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$stockContado",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (diferencia != 0) Azul else TextoPrincipal
                        )
                    }

                    IconButton(
                        onClick = onSumarContado,
                        modifier = Modifier
                            .size(34.dp)
                            .background(GrisBarra.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Más", modifier = Modifier.size(18.dp))
                    }
                }

                // Botón Conciliar
                Button(
                    onClick = onAplicarAjuste,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (diferencia != 0) Azul else GrisBarra.copy(alpha = 0.6f)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Ajustar", fontSize = 12.sp)
                }
            }
        }
    }
}
