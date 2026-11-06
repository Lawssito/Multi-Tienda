package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.*
import com.bexai.multitienda.viewmodel.GerenciaViewModel

/**
 * GerenciaDashboardView: tablero analítico interactivo con métricas globales,
 * salud del inventario y resumen operativo del día.
 */
@Composable
fun GerenciaDashboardView(
    navController: NavHostController,
    viewModel: GerenciaViewModel = viewModel()
) {
    val totalInventario by viewModel.totalInventario.collectAsState()
    val totalProductos by viewModel.totalProductos.collectAsState()
    val quiebres by viewModel.productosQuiebre.collectAsState()
    val bajoStock by viewModel.productosStockBajo.collectAsState()
    val normales by viewModel.productosNormales.collectAsState()
    val tiendasActivas by viewModel.totalTiendasActivas.collectAsState()
    val tiendas by viewModel.tiendas.collectAsState()
    val resumenDia by viewModel.resumenDia.collectAsState()

    val totalProdFloat = totalProductos.coerceAtLeast(1).toFloat()
    val pctNormal = (normales.size / totalProdFloat) * 100f
    val pctBajo = (bajoStock.size / totalProdFloat) * 100f
    val pctQuiebre = (quiebres.size / totalProdFloat) * 100f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 80.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tablero Analítico",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Text(
                    text = "Monitoreo global de existencias y red",
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

        Spacer(modifier = Modifier.height(14.dp))

        // Fila 1 KPIs: Unidades e Items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardKpi(
                titulo = "Total Unidades",
                valor = "$totalInventario",
                icono = Icons.Outlined.Inventory2,
                colorFondo = AzulSuave,
                colorIcono = Azul,
                modifier = Modifier.weight(1f)
            )
            CardKpi(
                titulo = "Variedad Catálogo",
                valor = "$totalProductos",
                icono = Icons.Outlined.Category,
                colorFondo = GrisBarra.copy(alpha = 0.2f),
                colorIcono = TextoPrincipal,
                modifier = Modifier.weight(1f)
            )
            CardKpi(
                titulo = "Tiendas Activas",
                valor = "$tiendasActivas / ${tiendas.size}",
                icono = Icons.Outlined.Store,
                colorFondo = ExitoSuave,
                colorIcono = Exito,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fila 2 KPIs: Alertas de Stock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardKpi(
                titulo = "Quiebres de Stock",
                valor = "${quiebres.size}",
                icono = Icons.Outlined.WarningAmber,
                colorFondo = PeligroSuave,
                colorIcono = Peligro,
                subtitulo = "0 unidades",
                modifier = Modifier.weight(1f)
            )
            CardKpi(
                titulo = "Stock Crítico",
                valor = "${bajoStock.size}",
                icono = Icons.Outlined.HourglassBottom,
                colorFondo = AdvertenciaSuave,
                colorIcono = Advertencia,
                subtitulo = "< 10 unidades",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta Salud del Inventario (Barra segmentada)
        Card(
            colors = CardDefaults.cardColors(containerColor = Superficie),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Salud del Inventario",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextoPrincipal
                )
                Text(
                    text = "Proporción de disponibilidad de productos",
                    fontSize = 12.sp,
                    color = TextoSecundario
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Barra segmentada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(GrisBarra.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                ) {
                    if (pctNormal > 0) {
                        Box(
                            modifier = Modifier
                                .weight(pctNormal)
                                .fillMaxHeight()
                                .background(Exito, RoundedCornerShape(6.dp))
                        )
                    }
                    if (pctBajo > 0) {
                        Box(
                            modifier = Modifier
                                .weight(pctBajo)
                                .fillMaxHeight()
                                .background(Advertencia)
                        )
                    }
                    if (pctQuiebre > 0) {
                        Box(
                            modifier = Modifier
                                .weight(pctQuiebre)
                                .fillMaxHeight()
                                .background(Peligro, RoundedCornerShape(6.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Leyenda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LeyendaItem(color = Exito, label = "Normal (${normales.size})")
                    LeyendaItem(color = Advertencia, label = "Bajo (${bajoStock.size})")
                    LeyendaItem(color = Peligro, label = "Quiebre (${quiebres.size})")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resumen Operativo del Día
        Card(
            colors = CardDefaults.cardColors(containerColor = Superficie),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Operaciones de la Red (Hoy)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextoPrincipal
                    )
                    Icon(Icons.Outlined.History, contentDescription = null, tint = Azul)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OperacionItem(
                        label = "Entradas",
                        valor = "+${resumenDia.entradas}",
                        color = Exito
                    )
                    OperacionItem(
                        label = "Salidas",
                        valor = "-${resumenDia.salidas}",
                        color = Peligro
                    )
                    OperacionItem(
                        label = "Movimientos",
                        valor = "${resumenDia.movimientos}",
                        color = Azul
                    )
                    OperacionItem(
                        label = "Ajustes",
                        valor = "${resumenDia.contados}",
                        color = Advertencia
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accesos Directos a Módulos
        Text(
            text = "Accesos Directos",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextoPrincipal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { navController.navigate(Ruta.GerenciaAuditoria.ruta) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul),
                modifier = Modifier.weight(1f)
            ) {
                Text("Auditoría", fontSize = 13.sp)
            }

            Button(
                onClick = { navController.navigate(Ruta.GerenciaGestionTiendas.ruta) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul),
                modifier = Modifier.weight(1f)
            ) {
                Text("Tiendas", fontSize = 13.sp)
            }

            Button(
                onClick = { navController.navigate(Ruta.GerenciaReportes.ruta) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul),
                modifier = Modifier.weight(1f)
            ) {
                Text("Reportes", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CardKpi(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorFondo: Color,
    colorIcono: Color,
    subtitulo: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = TextoPrincipal
            )
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = TextoSecundario
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    fontSize = 10.sp,
                    color = colorIcono,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LeyendaItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = TextoSecundario)
    }
}

@Composable
private fun OperacionItem(label: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextoSecundario)
    }
}
