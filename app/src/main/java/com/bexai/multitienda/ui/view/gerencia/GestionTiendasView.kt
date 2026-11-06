package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
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
import com.bexai.multitienda.model.Ubicacion
import com.bexai.multitienda.ui.navigation.Ruta
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
    val activasCount = tiendas.count { it.activa }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var nombreNuevaTienda by remember { mutableStateOf("") }
    var direccionNuevaTienda by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = Azul,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Nueva Tienda") }
            )
        }
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
                        text = "Gestión de Tiendas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal
                    )
                    Text(
                        text = "Sucursales y bodegas de la red",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Resumen de estado de tiendas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Superficie),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Locales", fontSize = 11.sp, color = TextoSecundario)
                        Text("${tiendas.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = ExitoSuave),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Habilitadas", fontSize = 11.sp, color = Exito)
                        Text("$activasCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Exito)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = PeligroSuave),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Deshabilitadas", fontSize = 11.sp, color = Peligro)
                        Text("${tiendas.size - activasCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Peligro)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Listado de tiendas
            if (tiendas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay tiendas registradas", color = TextoSecundario)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(tiendas, key = { it.id }) { tienda ->
                        TiendaCard(
                            tienda = tienda,
                            onToggleActiva = { nuevaActiva ->
                                viewModel.cambiarEstadoTienda(tienda.id, nuevaActiva)
                                scope.launch {
                                    val estado = if (nuevaActiva) "habilitada" else "deshabilitada"
                                    snackbarHostState.showSnackbar("${tienda.nombre} $estado")
                                }
                            },
                            onEliminar = {
                                viewModel.eliminarTienda(tienda.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tienda eliminada")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Crear Nueva Tienda
    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCrear = false },
            title = { Text("Registrar Nueva Sucursal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nombreNuevaTienda,
                        onValueChange = { nombreNuevaTienda = it },
                        label = { Text("Nombre de la tienda") },
                        placeholder = { Text("Ej. Sucursal Oriente - Las Condes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = direccionNuevaTienda,
                        onValueChange = { direccionNuevaTienda = it },
                        label = { Text("Dirección completa") },
                        placeholder = { Text("Ej. Av. Apoquindo 4000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreNuevaTienda.isNotBlank() && direccionNuevaTienda.isNotBlank()) {
                            viewModel.agregarTienda(nombreNuevaTienda, direccionNuevaTienda) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Sucursal '$nombreNuevaTienda' agregada exitosamente")
                                }
                                nombreNuevaTienda = ""
                                direccionNuevaTienda = ""
                                mostrarDialogoCrear = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text("Crear Sucursal")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrear = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TiendaCard(
    tienda: Ubicacion,
    onToggleActiva: (Boolean) -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Superficie),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (tienda.activa) AzulSuave else GrisBarra.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Store,
                    contentDescription = null,
                    tint = if (tienda.activa) Azul else TextoSecundario
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tienda.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextoPrincipal
                )
                Text(
                    text = tienda.direccion,
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (tienda.activa) "● Habilitada" else "○ Deshabilitada",
                    color = if (tienda.activa) Exito else TextoSecundario,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = tienda.activa,
                    onCheckedChange = onToggleActiva
                )
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = Peligro.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
