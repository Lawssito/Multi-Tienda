package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.ui.components.ValidatedTextField
import com.bexai.multitienda.ui.theme.*
import com.bexai.multitienda.viewmodel.GerenciaViewModel
import kotlinx.coroutines.launch

/**
 * GestionTiendasView: administración de tiendas y sucursales habilitadas en la red.
 */
@Composable
fun GestionTiendasView(
    navController: NavHostController,
    viewModel: GerenciaViewModel = viewModel()
) {
    val tiendas by viewModel.tiendas.collectAsState()
    val stocks by viewModel.stockSucursal.collectAsState()

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    val form by viewModel.formTienda.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(GFondo)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Color.White, CircleShape).clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = GTexto)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gerencia", fontSize = 14.sp, color = GTextoSuave)
                        Text("Tiendas habilitadas", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                    }
                    Text(
                        "+ Nueva", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier
                            .background(Azul, RoundedCornerShape(20.dp))
                            .clickable { mostrarDialogoCrear = true }
                            .padding(horizontal = 18.dp, vertical = 11.dp)
                    )
                }
            }
            item {
                Text(
                    "Una tienda deshabilitada no aparece al elegir sucursal ni recibe movimientos.",
                    fontSize = 14.sp, color = GTextoSuave
                )
            }
            if (tiendas.isEmpty()) {
                item { Text("No hay tiendas registradas", color = GTextoSuave) }
            }
            items(tiendas, key = { it.id }) { tienda ->
                val propias = stocks.filter { it.sucursalId == tienda.id && it.cantidad > 0 }
                TiendaCard(
                    tienda = tienda,
                    productos = propias.size,
                    unidades = propias.sumOf { it.cantidad },
                    onToggleActiva = { nuevaActiva ->
                        viewModel.cambiarEstadoTienda(tienda.id, nuevaActiva)
                        scope.launch {
                            val estado = if (nuevaActiva) "habilitada" else "deshabilitada"
                            snackbarHostState.showSnackbar("${tienda.nombre} $estado")
                        }
                    },
                )
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    // Modal Crear Nueva Tienda: los campos y errores vienen del ViewModel
    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCrear = false; viewModel.limpiarFormTienda() },
            title = { Text("Registrar Nueva Sucursal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ValidatedTextField(
                        value = form.nombre,
                        onValueChange = viewModel::onNombreTiendaChange,
                        label = "Nombre de la tienda",
                        isError = form.nombreError != null,
                        supportingText = form.nombreError
                    )
                    ValidatedTextField(
                        value = form.direccion,
                        onValueChange = viewModel::onDireccionTiendaChange,
                        label = "Dirección completa",
                        isError = form.direccionError != null,
                        supportingText = form.direccionError
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nombre = form.nombre.trim()
                        viewModel.agregarTienda {
                            scope.launch { snackbarHostState.showSnackbar("Sucursal '$nombre' agregada exitosamente") }
                            mostrarDialogoCrear = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text("Crear Sucursal")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrear = false; viewModel.limpiarFormTienda() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TiendaCard(
    tienda: Sucursal,
    productos: Int,
    unidades: Int,
    onToggleActiva: (Boolean) -> Unit
) {
    val color = if (tienda.activa) GTexto else GTextoSuave
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tienda.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
                Text("${tienda.direccion} · ID ${tienda.id}", fontSize = 14.sp, color = GTextoSuave)
            }
            Switch(checked = tienda.activa, onCheckedChange = onToggleActiva)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$productos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(" productos", fontSize = 14.sp, color = GTextoSuave)
            Spacer(Modifier.width(16.dp))
            Text("$unidades", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(" unidades", fontSize = 14.sp, color = GTextoSuave)
            Spacer(Modifier.weight(1f))
            Text(
                if (tienda.activa) "Activa" else "Inactiva: no aparece al operador",
                fontSize = 13.sp, color = GTextoSuave
            )
        }
    }
}
