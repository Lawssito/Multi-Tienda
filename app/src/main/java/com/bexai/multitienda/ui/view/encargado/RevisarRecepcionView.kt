package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.data.DecisionRecepcion
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.RecepcionPendiente
import com.bexai.multitienda.model.totales
import com.bexai.multitienda.ui.theme.Azul
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores del mockup v6 (RevisarRecepcion.dc.html)
private val Fondo = Color(0xFFF4F6FA)
private val Texto = Color(0xFF0F172A)
private val TextoSuave = Color(0xFF5B6474)
private val VerdeTexto = Color(0xFF15803D)
private val VerdeFondo = Color(0xFFE7F7EF)
private val NaranjaTexto = Color(0xFFB45309)
private val NaranjaFondo = Color(0xFFFFEBD2)
private val AzulTexto = Color(0xFF1D4ED8)
private val AzulFondo = Color(0xFFDBEAFE)
private val RojoTexto = Color(0xFFB91C1C)
private val RojoFondo = Color(0xFFFEE2E2)

private fun etiqueta(decision: String) = when (decision) {
    DecisionRecepcion.RECLAMAR -> "Reclamar faltante"
    DecisionRecepcion.ESPERAR -> "Esperar 2º envío"
    DecisionRecepcion.DEVOLVER -> "Devolver"
    DecisionRecepcion.BAJA -> "Dar de baja"
    DecisionRecepcion.LIBERAR -> "Liberar"
    else -> "Aceptar"
}

/** Opciones según el problema principal de la línea: dañados, faltantes o sobrante. */
private fun opciones(linea: LineaRecepcion) = when {
    linea.danado > 0 -> listOf(DecisionRecepcion.DEVOLVER, DecisionRecepcion.BAJA, DecisionRecepcion.LIBERAR)
    linea.faltan > 0 -> listOf(DecisionRecepcion.RECLAMAR, DecisionRecepcion.ESPERAR)
    else -> listOf(DecisionRecepcion.ACEPTAR, DecisionRecepcion.DEVOLVER)
}

/**
 * El encargado revisa una recepción enviada (mockup "Encargado: revisar recepción").
 * Decide por línea con diferencia y marca la recepción revisada cuando todas tienen decisión.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevisarRecepcionView(
    recepcion: RecepcionPendiente?,
    lineas: List<LineaRecepcion>,
    onVolver: () -> Unit,
    onDecidir: (detalleId: Int, decision: String) -> Unit,
    onMarcarRevisada: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conProblema = lineas.filter { it.conProblema }
    val faltaDecidir = conProblema.count { it.decision == null }
    val t = lineas.totales()
    val hora = recepcion?.fechaEnvio?.let { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(it)) } ?: "—"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(44.dp).background(Color.White, CircleShape).clickable(onClick = onVolver),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = Texto) }
            Column {
                Text("Recepción ${recepcion?.codigo ?: ""}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Texto)
                Text("${recepcion?.proveedor ?: ""} · enviada $hora", fontSize = 14.sp, color = TextoSuave)
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiqueta("${t.entran} ingresadas", VerdeTexto, VerdeFondo)
            Etiqueta("Faltan ${t.faltan}", NaranjaTexto, NaranjaFondo)
            Etiqueta("Sobra ${t.sobran}", AzulTexto, AzulFondo)
            Etiqueta("Dañados ${t.danadas}", RojoTexto, RojoFondo)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (conProblema.isEmpty()) {
                Text("Llegó todo completo y en buen estado. No hay nada que decidir.", fontSize = 15.sp, color = TextoSuave)
            }
            conProblema.forEach { linea ->
                TarjetaRevision(linea, onDecidir = { onDecidir(linea.detalleId, it) })
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(if (faltaDecidir == 0) Azul else Color(0xFF93B4F5), RoundedCornerShape(28.dp))
                .clickable(enabled = faltaDecidir == 0, onClick = onMarcarRevisada),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (faltaDecidir == 0) "Marcar revisada" else "Falta decidir $faltaDecidir",
                color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Etiqueta(texto: String, color: Color, fondo: Color) {
    Text(
        text = texto, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.background(fondo, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaRevision(linea: LineaRecepcion, onDecidir: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(linea.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Texto, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (linea.faltan > 0) Etiqueta("▼ -${linea.faltan}", NaranjaTexto, NaranjaFondo)
                if (linea.sobran > 0) Etiqueta("▲ +${linea.sobran}", AzulTexto, AzulFondo)
                if (linea.danado > 0) Etiqueta("! ${linea.danado} dañados", RojoTexto, RojoFondo)
            }
        }
        if (linea.faltan > 0 || linea.sobran > 0) {
            Text("Recibido ${linea.recibido} · esperado ${linea.esperado}", fontSize = 14.sp, color = TextoSuave)
        }
        if (linea.danado > 0) {
            Text("Motivo: ${linea.motivoDanado.orEmpty().lowercase()} · retenidos, no suman stock", fontSize = 14.sp, color = TextoSuave)
            if (!linea.nota.isNullOrBlank()) Text("\"${linea.nota}\"", fontSize = 14.sp, color = TextoSuave)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            opciones(linea).forEach { opcion ->
                val elegida = linea.decision == opcion
                Text(
                    text = etiqueta(opcion),
                    color = if (elegida) Color.White else Texto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(if (elegida) Texto else Color.White, RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
                        .clickable { onDecidir(opcion) }
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }
}
