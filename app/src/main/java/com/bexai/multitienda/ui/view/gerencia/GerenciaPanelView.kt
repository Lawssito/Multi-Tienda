package com.bexai.multitienda.ui.view.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.navigation.Ruta
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.viewmodel.GerenciaViewModel
import java.util.Calendar

private val AzulPanel = Color(0xFF2563EB)

/** Inicio de hoy, hace 7 días o hace 30 días, en milisegundos. */
internal fun desdeDias(dias: Int): Long =
    Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -dias)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

/**
 * Panel general (mockup "Panel"): stock total de la red, alertas activas, movimientos del
 * período y stock por ubicación con acceso a Administrar tiendas.
 */
@Composable
fun GerenciaPanelView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: GerenciaViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val tiendas by viewModel.tiendas.collectAsState()
    val alertas by viewModel.alertas.collectAsState()
    val historial by viewModel.historialMovimientos.collectAsState()
    val ubicaciones by viewModel.stockPorUbicacion.collectAsState()
    val stocks by viewModel.stockSucursal.collectAsState()
    val incidencias by viewModel.incidenciasCajas.collectAsState()
    val contexto = LocalContext.current

    var periodo by remember { mutableStateOf("Hoy") }
    var menu by remember { mutableStateOf(false) }

    val desde = when (periodo) {
        "Hoy" -> desdeDias(0)
        "7 días" -> desdeDias(6)
        else -> desdeDias(29)
    }
    val movimientos = historial.filter { it.fechaHora >= desde }
    val activas = alertas.filter { !it.revisada }
    val criticas = activas.count { it.critica }
    val maximo = (ubicaciones.maxOfOrNull { it.total } ?: 1).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GFondo)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Gerencia", fontSize = 15.sp, color = GTextoSuave)
                Text("Panel general", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
            }
            Box {
                Text(
                    "$periodo ▾", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GTexto,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .clickable { menu = true }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    listOf("Hoy", "7 días", "30 días").forEach { op ->
                        DropdownMenuItem(text = { Text(op) }, onClick = { periodo = op; menu = false })
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(AzulPanel, RoundedCornerShape(28.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Stock total en la red", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))
            Text("%,d".format(productos.sumOf { it.stock }).replace(',', '.'), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("unidades · ${tiendas.size} ubicaciones", fontSize = 15.sp, color = Color.White.copy(alpha = 0.85f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier.weight(1f).background(GRojoFondo, RoundedCornerShape(24.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Alertas activas", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GRojoTexto)
                Text("${activas.size}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = GRojo)
                Text("$criticas críticas", fontSize = 15.sp, color = GRojoTexto)
            }
            Column(
                modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(24.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Movimientos ${if (periodo == "Hoy") "hoy" else periodo}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GTextoSuave)
                Text("${movimientos.size}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                Text(
                    "${movimientos.count { it.tipo == TipoMovimiento.ENTRADA }} entradas · ${movimientos.count { it.tipo == TipoMovimiento.SALIDA }} salidas",
                    fontSize = 14.sp, color = GTextoSuave
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stock por ubicación", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
                Text(
                    "Administrar tiendas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Azul,
                    modifier = Modifier.clickable { navController.navigate(Ruta.GerenciaGestionTiendas.ruta) }
                )
            }
            if (ubicaciones.isEmpty()) Text("Sin sucursales.", fontSize = 15.sp, color = GTextoSuave)
            ubicaciones.forEach { u ->
                val inactiva = !u.sucursal.activa
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            u.sucursal.nombre + if (inactiva) " · inactiva" else "",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = if (inactiva) GTextoSuave else GTexto
                        )
                        Text("${u.total}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (inactiva) GTextoSuave else GTexto)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFE9EDF3), RoundedCornerShape(5.dp))) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((u.total.toFloat() / maximo).coerceIn(0.03f, 1f))
                                .height(10.dp)
                                .background(if (inactiva) Color(0xFF94A3B8) else AzulPanel, RoundedCornerShape(5.dp))
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reportes PDF", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = GTexto)
            FilaReporte("Reposición sugerida", "Productos bajo el mínimo y cuánto pedir") {
                exportarReposicionPdf(contexto, productos, stocks, tiendas)
            }
            FilaReporte("Mermas y pérdidas", "Ajustes y dañados · $periodo") {
                exportarMermasPdf(contexto, periodo, desde, historial, incidencias, productos, tiendas)
            }
        }
    }
}


@Composable
private fun FilaReporte(titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GTexto)
            Text(detalle, fontSize = 13.sp, color = GTextoSuave)
        }
        BotonPdf(onClick)
    }
}
