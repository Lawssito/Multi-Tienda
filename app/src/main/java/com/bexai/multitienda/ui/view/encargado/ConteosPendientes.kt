package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bexai.multitienda.model.Conteo
import com.bexai.multitienda.model.LineaConteo
import com.bexai.multitienda.model.RecepcionPendiente
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.Exito
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.Superficie
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.EncargadoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta de un conteo masivo enviado por el operador: muestra faltantes y sobrantes
 * y permite aprobarlo (el stock queda igual a lo contado) o rechazarlo.
 */
@Composable
fun ConteoPendienteCard(
    conteo: Conteo,
    viewModel: EncargadoViewModel,
    onMensaje: (String) -> Unit
) {
    val diferencias by remember(conteo.id) { viewModel.diferenciasDe(conteo.id) }
        .collectAsState(initial = emptyList<LineaConteo>())
    val progreso by remember(conteo.id) { viewModel.progresoDe(conteo.id) }
        .collectAsState(initial = 0 to 0)
    val fecha = conteo.fechaEnvio?.let {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "—"

    Card(
        colors = CardDefaults.cardColors(containerColor = Superficie),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Conteo enviado $fecha",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal
            )
            Text(
                text = "Contó ${progreso.first} de ${progreso.second} productos" +
                    if (progreso.second > progreso.first) " · ${progreso.second - progreso.first} sin contar (no se modifican)" else "",
                color = TextoSecundario
            )
            if (diferencias.isEmpty()) {
                Text("Sin diferencias: lo contado coincide con el sistema.", color = TextoSecundario)
            } else {
                diferencias.forEach { linea ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(linea.nombre, color = TextoPrincipal, fontWeight = FontWeight.SemiBold)
                            Text("Sistema ${linea.sistema} · Contado ${linea.contado}", color = TextoSecundario)
                        }
                        val signo = if (linea.diferencia > 0) "+" else ""
                        Text(
                            text = "$signo${linea.diferencia}",
                            color = if (linea.diferencia > 0) Azul else Peligro,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { viewModel.rechazarConteo(conteo.id, onMensaje) },
                    modifier = Modifier.weight(1f)
                ) { Text("Rechazar", color = Peligro) }
                Button(
                    onClick = { viewModel.aprobarConteo(conteo.id, onMensaje) },
                    colors = ButtonDefaults.buttonColors(containerColor = Exito),
                    modifier = Modifier.weight(1f)
                ) { Text("Aprobar") }
            }
        }
    }
}

/**
 * Aviso de una caja recibida por el operador que espera revisión (mockup "1 recepción por
 * revisar"). Al tocarlo se abre la revisión por línea.
 */
@Composable
fun RecepcionPorRevisarCard(recepcion: RecepcionPendiente, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, Color(0xFFB9F0CF)), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Recepción por revisar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextoPrincipal
        )
        Text(
            text = "${recepcion.codigo} · ${recepcion.proveedor} · guía ${recepcion.guia}",
            color = TextoSecundario
        )
    }
}
