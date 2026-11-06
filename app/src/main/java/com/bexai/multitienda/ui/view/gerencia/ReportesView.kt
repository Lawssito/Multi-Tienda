package com.bexai.multitienda.ui.view.gerencia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.*
import com.bexai.multitienda.viewmodel.GerenciaViewModel
import kotlinx.coroutines.launch

/**
 * ReportesView: centro de reportes analíticos y exportación a CSV/Compartir.
 */
@Composable
fun ReportesView(
    navController: NavHostController,
    viewModel: GerenciaViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tipoReporte by remember { mutableStateOf("INVENTARIO") }

    val textoResumen = remember(tipoReporte, viewModel) {
        viewModel.generarTextoResumen(tipoReporte)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        text = "Centro de Reportes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal
                    )
                    Text(
                        text = "Exportación de datos de inventario y trazabilidad",
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

            // Selector de Reporte
            Text("Seleccionar Tipo de Reporte:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tipoReporte == "INVENTARIO",
                    onClick = { tipoReporte = "INVENTARIO" },
                    label = { Text("Inventario Global") },
                    leadingIcon = { Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = tipoReporte == "QUIEBRES",
                    onClick = { tipoReporte = "QUIEBRES" },
                    label = { Text("Quiebres de Stock") },
                    leadingIcon = { Icon(Icons.Outlined.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = tipoReporte == "MOVIMIENTOS",
                    onClick = { tipoReporte = "MOVIMIENTOS" },
                    label = { Text("Auditoría / Cambios") },
                    leadingIcon = { Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de Acción de Exportación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Compartir / Exportar CSV
                Button(
                    onClick = {
                        val csvData = viewModel.generarCSV(tipoReporte)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csvData)
                            putExtra(Intent.EXTRA_TITLE, "Reporte_$tipoReporte.csv")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Exportar Reporte CSV")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar CSV", fontSize = 13.sp)
                }

                // Copiar al Portapapeles
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Reporte $tipoReporte", textoResumen)
                        clipboard.setPrimaryClip(clip)
                        scope.launch {
                            snackbarHostState.showSnackbar("Reporte copiado al portapapeles")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Texto", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vista previa del reporte
            Text("Vista Previa del Reporte:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Superficie),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = textoResumen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextoPrincipal
                    )
                }
            }
        }
    }
}
