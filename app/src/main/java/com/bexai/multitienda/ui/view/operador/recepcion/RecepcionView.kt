package com.bexai.multitienda.ui.view.operador.recepcion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.Exito
import com.bexai.multitienda.ui.theme.FondoPantalla
import com.bexai.multitienda.ui.theme.GrisFondo
import com.bexai.multitienda.ui.theme.NaranjaFondo
import com.bexai.multitienda.ui.theme.NaranjaTexto
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.ui.theme.VerdeFondo
import com.bexai.multitienda.viewmodel.operador.RecepcionUiState

private val ColoresMiniatura = listOf(
    Color(0xFFE8F1FE), Color(0xFFFFF6D6), Color(0xFFF1EBFE), Color(0xFFFFF1E6), Color(0xFFFCE7F3)
)

/** Etiqueta redondeada de color (OK, Faltan 1, Sobra 1…). */
@Composable
internal fun ChipEstado(texto: String, color: Color, fondo: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(fondo, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** Círculo blanco con la flecha de volver (mismo de las pantallas del mockup). */
@Composable
internal fun BotonVolver(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = TextoPrincipal)
    }
}

/**
 * Recepción de una caja (mockup "Recepción DSP-0042"): progreso, lista de productos con
 * recibido / esperado y acceso a Marcar dañado tocando una fila.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecepcionView(
    estado: RecepcionUiState,
    onVolver: () -> Unit,
    onEscanear: () -> Unit,
    onDanado: (LineaRecepcion) -> Unit,
    onTerminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totales = estado.totales
    val completos = estado.lineas.count { it.esperado > 0 && it.recibido == it.esperado }
    val faltan = estado.lineas.count { it.recibido in 1 until it.esperado }
    val sobran = estado.lineas.count { it.sobran > 0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BotonVolver(onVolver)
            Column {
                Text("Recepción ${estado.codigo}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipal)
                Text("${estado.proveedor} · Guía ${estado.guia}", fontSize = 14.sp, color = TextoSecundario)
            }
        }

        // Progreso
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${totales.recibidas} de ${totales.esperadas} unidades",
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal
                )
                Text("${estado.porcentaje}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Azul)
            }
            LinearProgressIndicator(
                progress = { estado.porcentaje / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFFE2E8F0)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipEstado("Completos $completos", Exito, VerdeFondo)
                ChipEstado("Faltan $faltan", NaranjaTexto, NaranjaFondo)
                ChipEstado("Sobra $sobran", AzulOscuro, AzulSuave)
                ChipEstado("Dañados ${totales.danadas}", Peligro, PeligroSuave)
            }
        }

        // Escanear siguiente
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Azul, RoundedCornerShape(28.dp))
                .clickable(enabled = estado.cargado, onClick = onEscanear),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Color.White)
            Text(
                "  Escanear siguiente",
                color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Productos de la caja", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
            Text("Recibido / Esperado", fontSize = 13.sp, color = TextoSecundario)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(22.dp))
        ) {
            itemsIndexed(estado.lineas, key = { _, l -> l.detalleId }) { indice, linea ->
                FilaProducto(
                    linea = linea,
                    color = ColoresMiniatura[indice % ColoresMiniatura.size],
                    onClick = { onDanado(linea) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(TextoPrincipal, RoundedCornerShape(28.dp))
                .clickable(enabled = estado.cargado, onClick = onTerminar),
            contentAlignment = Alignment.Center
        ) {
            Text("Terminar caja y revisar", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilaProducto(linea: LineaRecepcion, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = linea.recibido > 0, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(color, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Azul)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                linea.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = TextoPrincipal, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(linea.codigoBarras, fontSize = 12.sp, color = TextoSecundario)
            if (linea.danado > 0) {
                ChipEstado("${linea.danado} dañados · ${linea.motivoDanado.orEmpty().lowercase()}", Peligro, PeligroSuave)
            }
        }
        Text(
            "${linea.recibido} / ${linea.esperado}",
            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal
        )
        when {
            linea.recibido == 0 -> ChipEstado("Pend.", TextoSecundario, GrisFondo)
            linea.sobran > 0 -> ChipEstado("▲ +${linea.sobran}", AzulOscuro, AzulSuave)
            linea.faltan > 0 -> ChipEstado("▼ -${linea.faltan}", NaranjaTexto, NaranjaFondo)
            else -> ChipEstado("✓ OK", Exito, VerdeFondo)
        }
    }
}
