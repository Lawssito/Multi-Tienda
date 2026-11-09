package com.bexai.multitienda.ui.view.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.viewmodel.RecepcionUiState

/**
 * Resumen de recepción (mockup 9e): unidades que entran al stock, faltan, sobran y dañadas,
 * más los productos con diferencia. Al enviar entran al stock solo las unidades buenas.
 */
@Composable
fun ResumenRecepcionView(
    estado: RecepcionUiState,
    onVolver: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = estado.totales
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoRecepcion)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BotonVolver(onVolver)
            Column {
                Text("Resumen de recepción", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextoRecepcion)
                Text("${estado.codigo} · ${t.recibidas} de ${t.esperadas} unidades", fontSize = 14.sp, color = TextoSuaveRecepcion)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Total("${t.entran}", "✓ Entran al stock", Color(0xFFE5F5EC), VerdeTexto, Modifier.weight(1f))
                Total("${t.faltan}", "▼ Faltan", NaranjaFondo, NaranjaTexto, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Total("${t.sobran}", "▲ Sobra", AzulFondo, AzulTexto, Modifier.weight(1f))
                Total("${t.danadas}", "! Dañados", RojoFondo, RojoTexto, Modifier.weight(1f))
            }

            Text("Productos con diferencia", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoRecepcion)
            val difs = estado.conDiferencia
            if (difs.isEmpty()) {
                Text("Sin diferencias: llegó todo completo y en buen estado.", fontSize = 14.sp, color = TextoSuaveRecepcion)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(22.dp))
                ) {
                    difs.forEach { FilaDiferencia(it) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = TextoSuaveRecepcion)
                Text(
                    "Al enviar, entran al stock solo las ${t.entran} unidades buenas. " +
                        "Faltantes, sobrante y dañados quedan como alerta para el encargado.",
                    fontSize = 14.sp, color = TextoSuaveRecepcion
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(28.dp))
                    .clickable(onClick = onVolver),
                contentAlignment = Alignment.Center
            ) { Text("Recontar", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoRecepcion) }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp)
                    .background(Azul, RoundedCornerShape(28.dp))
                    .clickable(onClick = onEnviar),
                contentAlignment = Alignment.Center
            ) { Text("Enviar recepción", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
private fun Total(valor: String, etiqueta: String, fondo: Color, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(fondo, RoundedCornerShape(22.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(valor, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(etiqueta, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
internal fun FilaDiferencia(linea: LineaRecepcion) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(linea.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoRecepcion)
            Text(detalleDiferencia(linea), fontSize = 14.sp, color = TextoSuaveRecepcion)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (linea.faltan > 0) ChipEstado("▼ -${linea.faltan}", NaranjaTexto, NaranjaFondo)
            if (linea.sobran > 0) ChipEstado("▲ +${linea.sobran}", AzulTexto, AzulFondo)
            if (linea.danado > 0) ChipEstado("! ${linea.danado}", RojoTexto, RojoFondo)
        }
    }
}

internal fun detalleDiferencia(linea: LineaRecepcion): String {
    val partes = mutableListOf<String>()
    if (linea.faltan > 0 || linea.sobran > 0) partes += "Recibido ${linea.recibido} · esperado ${linea.esperado}"
    if (linea.danado > 0) partes += "${linea.danado} dañados · ${linea.motivoDanado.orEmpty().lowercase()}"
    return partes.joinToString(" · ")
}
