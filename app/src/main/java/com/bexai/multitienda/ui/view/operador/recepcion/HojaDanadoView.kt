package com.bexai.multitienda.ui.view.operador.recepcion

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.FondoPantalla
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario

private val MOTIVOS = listOf("Roto", "Mojado", "Vencido", "Otro")
private const val MAXIMO_NOTA = 200

/**
 * Hoja "Marcar como dañado" (mockup 9c). Unidades de 0 a lo recibido, motivo obligatorio
 * (Roto, Mojado, Vencido u Otro) y nota opcional de hasta 200 caracteres.
 * Con 0 unidades se quita la marca de dañado.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HojaDanadoView(
    linea: LineaRecepcion,
    codigoDespacho: String,
    error: String?,
    onCerrar: () -> Unit,
    onGuardar: (cantidad: Int, motivo: String, nota: String) -> Unit
) {
    var cantidad by remember { mutableIntStateOf(if (linea.danado > 0) linea.danado else 1.coerceAtMost(linea.recibido)) }
    var motivo by remember { mutableStateOf(linea.motivoDanado.orEmpty()) }
    var nota by remember { mutableStateOf(linea.nota.orEmpty()) }
    var intentoGuardar by remember { mutableStateOf(false) }
    val falta = intentoGuardar && cantidad > 0 && motivo.isBlank()

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Marcar como dañado", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipal)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(FondoPantalla, androidx.compose.foundation.shape.CircleShape)
                        .clickable(onClick = onCerrar),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextoPrincipal) }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FondoPantalla, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(linea.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                Text("Recibidas ${linea.recibido} · despacho $codigoDespacho", fontSize = 14.sp, color = TextoSecundario)
            }

            Text("Unidades dañadas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotonCantidad("–", FondoPantalla, TextoPrincipal) { if (cantidad > 0) cantidad-- }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(BorderStroke(2.dp, Azul), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$cantidad", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextoPrincipal, textAlign = TextAlign.Center)
                }
                BotonCantidad("+", Azul, Color.White) { if (cantidad < linea.recibido) cantidad++ }
            }
            Text("Máximo ${linea.recibido} (las recibidas)", fontSize = 14.sp, color = TextoSecundario)

            Row {
                Text("Motivo ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                Text("*", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Peligro)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MOTIVOS.forEach { opcion ->
                    val elegido = motivo == opcion
                    Text(
                        text = opcion,
                        color = if (elegido) Color.White else TextoPrincipal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .background(if (elegido) TextoPrincipal else Color.White, RoundedCornerShape(24.dp))
                            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
                            .clickable { motivo = opcion }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
            if (falta) Text("Elige un motivo", color = Peligro, fontSize = 14.sp)

            Text("Nota (opcional)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
            OutlinedTextField(
                value = nota,
                onValueChange = { if (it.length <= MAXIMO_NOTA) nota = it },
                placeholder = { Text("Ej. Tapas dobladas, venían al fondo de la caja") },
                supportingText = { Text("${nota.length}/$MAXIMO_NOTA") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            if (error != null) Text(error, color = Peligro, fontSize = 14.sp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(if (cantidad > 0) Color(0xFFDC2626) else TextoPrincipal, RoundedCornerShape(28.dp))
                    .clickable {
                        intentoGuardar = true
                        if (cantidad == 0 || motivo.isNotBlank()) onGuardar(cantidad, motivo, nota)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (cantidad) {
                        0 -> "Quitar marca de dañado"
                        1 -> "Guardar 1 como dañado"
                        else -> "Guardar $cantidad como dañados"
                    },
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BotonCantidad(texto: String, fondo: Color, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(fondo, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(texto, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color) }
}
