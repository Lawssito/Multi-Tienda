package com.bexai.multitienda.ui.view.encargado

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavHostController
import com.bexai.multitienda.model.EstadoReposicion
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.ui.components.PastillaSucursal
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.FondoPantalla
import com.bexai.multitienda.ui.theme.NaranjaFondo
import com.bexai.multitienda.ui.theme.NaranjaTexto
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.encargado.EncargadoViewModel
import kotlinx.coroutines.launch


/**
 * Alertas del encargado: bandeja de lo que requiere una acción suya.
 * Cajas por revisar, conteos por aprobar, productos bajo el mínimo sin reposición declarada
 * y, como estado, las reposiciones ya enviadas a gerencia.
 */
@Composable
fun AlertasEncargadoView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onCambiarSucursal: () -> Unit = {},
    viewModel: EncargadoViewModel = viewModel()
) {
    val recepciones by viewModel.recepcionesPendientes.collectAsState()
    val conteos by viewModel.conteosEnviados.collectAsState()
    val bajoMinimo by viewModel.bajoMinimoSinReposicion.collectAsState()
    val enviadas by viewModel.reposicionesEnviadas.collectAsState()
    var aEliminar by remember { mutableStateOf<Int?>(null) }
    val ajustes by viewModel.ajustesRecientes.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val sucursales by viewModel.sucursales.collectAsState()
    val sucursalId by viewModel.sucursalSeleccionadaId.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var reponer by remember { mutableStateOf<Producto?>(null) }
    val nombreSucursal = sucursales.firstOrNull { it.id == (sucursalId ?: 1) }?.nombre ?: ""
    val total = recepciones.size + conteos.size + bajoMinimo.size

    Box(modifier = modifier.fillMaxSize().background(FondoPantalla)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PastillaSucursal(nombreSucursal, onClick = onCambiarSucursal)
                    Text(
                        if (total == 0) "Sin pendientes" else "$total por resolver",
                        fontSize = 15.sp, color = TextoSecundario
                    )
                    Text("Alertas", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipal)
                }
            }

            if (recepciones.isNotEmpty()) {
                item { Seccion("Cajas por revisar", recepciones.size) }
                items(recepciones, key = { "recepcion-${it.id}" }) { recepcion ->
                    // Al resolverse, la fila se desvanece y las de abajo suben a ocupar su lugar
                    Box(Modifier.animateItem(fadeInSpec = tween(400), placementSpec = tween(500), fadeOutSpec = tween(500))) {
                        RecepcionPorRevisarCard(
                            recepcion = recepcion,
                            onClick = { navController.navigate(Ruta.EncargadoRevisarRecepcion.conRecepcion(recepcion.id)) }
                        )
                    }
                }
            }

            if (conteos.isNotEmpty()) {
                item { Seccion("Conteos por aprobar", conteos.size) }
                items(conteos, key = { "conteo-${it.id}" }) { conteo ->
                    // Al resolverse, la fila se desvanece y las de abajo suben a ocupar su lugar
                    Box(Modifier.animateItem(fadeInSpec = tween(400), placementSpec = tween(500), fadeOutSpec = tween(500))) {
                        ConteoPendienteCard(
                            conteo = conteo,
                            viewModel = viewModel,
                            onMensaje = { msg -> scope.launch { snackbar.showSnackbar(msg) } }
                        )
                    }
                }
            }

            if (bajoMinimo.isNotEmpty()) {
                item { Seccion("Bajo el mínimo sin reposición", bajoMinimo.size) }
                items(bajoMinimo, key = { "bajo-${it.id}" }) { p ->
                    // Al resolverse, la fila se desvanece y las de abajo suben a ocupar su lugar
                    Box(Modifier.animateItem(fadeInSpec = tween(400), placementSpec = tween(500), fadeOutSpec = tween(500))) {
                        TarjetaBajoMinimo(p, onReponer = { reponer = p })
                    }
                }
            }

            if (ajustes.isNotEmpty()) {
                item { Seccion("Ajustes del operador (24 h)", ajustes.size) }
                items(ajustes, key = { "ajuste-${it.id}" }) { a ->
                    val nombre = productos.firstOrNull { it.id == a.productoId }?.nombre ?: "Producto ${a.productoId}"
                    val hora = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(a.fechaHora))
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                        Text("Nuevo total ${a.cantidad} uds · ${a.motivo ?: "sin motivo"}", fontSize = 14.sp, color = TextoSecundario)
                        Text(hora, fontSize = 13.sp, color = TextoSecundario)
                    }
                }
            }

            if (enviadas.isNotEmpty()) {
                item { Seccion("Mis reposiciones", enviadas.size) }
                items(enviadas, key = { "rep-${it.id}" }) { r ->
                    // Al resolverse, la fila se desvanece y las de abajo suben a ocupar su lugar
                    Box(Modifier.animateItem(fadeInSpec = tween(400), placementSpec = tween(500), fadeOutSpec = tween(500))) {
                        val nombre = productos.firstOrNull { it.id == r.productoId }?.nombre ?: "Producto ${r.productoId}"
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp))
                                .let { m -> if (r.estado != EstadoReposicion.PENDIENTE) m.clickable { aEliminar = r.id } else m }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                                Text(
                                    "${r.cantidad} uds" + (if (r.urgente) " · urgente" else "") +
                                        when (r.estado) {
                                            EstadoReposicion.APROBADA -> " · despacho ${r.despachoCodigo ?: ""}"
                                            EstadoReposicion.RECHAZADA -> " · ${r.motivoRechazo ?: "sin motivo"}"
                                            else -> ""
                                        },
                                    fontSize = 14.sp, color = TextoSecundario
                                )
                            }
                            val (etiqueta, colorTexto, colorFondo) = when (r.estado) {
                                EstadoReposicion.APROBADA -> Triple("Aprobada", Color(0xFF15803D), Color(0xFFDCFCE7))
                                EstadoReposicion.RECHAZADA -> Triple("Rechazada", Peligro, PeligroSuave)
                                else -> Triple("Enviada", NaranjaTexto, NaranjaFondo)
                            }
                            Text(
                                etiqueta, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorTexto,
                                modifier = Modifier.background(colorFondo, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (total == 0 && enviadas.isEmpty() && ajustes.isEmpty()) {
                item { Text("No hay nada pendiente por ahora.", fontSize = 15.sp, color = TextoSecundario) }
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    aEliminar?.let { id ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("Eliminar reposición") },
            text = { Text("Se quita de tu lista. Gerencia conserva su registro de la decisión.") },
            confirmButton = { TextButton(onClick = { viewModel.eliminarReposicion(id); aEliminar = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { aEliminar = null }) { Text("Cancelar") } }
        )
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
private fun Seccion(titulo: String, cantidad: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipal)
        Text(
            "$cantidad", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoSecundario,
            modifier = Modifier.background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TarjetaBajoMinimo(p: Producto, onReponer: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(p.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal, modifier = Modifier.weight(1f))
            Text(
                "Stock ${p.stock} / mín. ${p.stockMinimo}",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Peligro,
                modifier = Modifier.background(PeligroSuave, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp).background(Azul, RoundedCornerShape(24.dp)).clickable(onClick = onReponer),
            contentAlignment = Alignment.Center
        ) { Text("Declarar reposición", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}
