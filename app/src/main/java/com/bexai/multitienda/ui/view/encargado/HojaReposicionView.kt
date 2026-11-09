package com.bexai.multitienda.ui.view.encargado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.bexai.multitienda.ui.theme.Azul

private const val MAXIMO_NOTA = 200
private const val MAXIMO_CANTIDAD = 9999

/**
 * Hoja "Declarar reposición" (mockup 12): el encargado pide a gerencia reponer un producto.
 * Cantidad entera de 1 a 9999 (sugerida: lo que falta para duplicar el mínimo), prioridad
 * Normal o Urgente y nota opcional de hasta 200 caracteres.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaReposicionView(
    nombre: String,
    sucursal: String,
    stock: Int,
    minimo: Int,
    onCerrar: () -> Unit,
    onEnviar: (cantidad: Int, urgente: Boolean, nota: String) -> Unit
) {
    val sugerida = (minimo * 2 - stock).coerceIn(1, MAXIMO_CANTIDAD)
    var texto by remember { mutableStateOf(sugerida.toString()) }
    var urgente by remember { mutableStateOf(false) }
    var nota by remember { mutableStateOf("") }
    val cantidad = texto.toIntOrNull() ?: 0
    val valida = cantidad in 1..MAXIMO_CANTIDAD

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
                Text("Declarar reposición", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFF4F6FA), CircleShape).clickable(onClick = onCerrar),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF0F172A)) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F6FA), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(sucursal, fontSize = 14.sp, color = Color(0xFF5B6474))
                }
                Text(
                    "$stock de mín. $minimo",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (stock < minimo) Color(0xFFB91C1C) else Color(0xFF0F172A)
                )
            }

            Text("Cantidad a reponer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Paso("–", Color(0xFFF4F6FA), Color(0xFF0F172A)) {
                    texto = (cantidad - 1).coerceAtLeast(0).toString()
                }
                OutlinedTextField(
                    value = texto,
                    onValueChange = { nuevo -> texto = nuevo.filter { it.isDigit() }.take(4) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    isError = !valida,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                Paso("+", Azul, Color.White) {
                    texto = (cantidad + 1).coerceAtMost(MAXIMO_CANTIDAD).toString()
                }
            }
            Text(
                if (valida) "Sugerido: lo que falta para duplicar el mínimo" else "Ingresa una cantidad entre 1 y $MAXIMO_CANTIDAD",
                fontSize = 14.sp,
                color = if (valida) Color(0xFF5B6474) else Color(0xFFB91C1C)
            )

            Text("Prioridad", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Prioridad("Normal", !urgente, Color(0xFF0F172A), Color.White, Color(0xFFE2E8F0), Modifier.weight(1f)) { urgente = false }
                Prioridad("! Urgente", urgente, Color(0xFFB91C1C), Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f)) { urgente = true }
            }

            Text("Nota (opcional)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            OutlinedTextField(
                value = nota,
                onValueChange = { if (it.length <= MAXIMO_NOTA) nota = it },
                placeholder = { Text("Ej. Faltan 3 unidades tras el conteo de hoy") },
                supportingText = { Text("${nota.length}/$MAXIMO_NOTA") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(if (valida) Azul else Color(0xFF93B4F5), RoundedCornerShape(28.dp))
                    .clickable(enabled = valida) { onEnviar(cantidad, urgente, nota) },
                contentAlignment = Alignment.Center
            ) {
                Text("Enviar a gerencia", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Paso(texto: String, fondo: Color, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(56.dp).background(fondo, RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(texto, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color) }
}

@Composable
private fun Prioridad(
    texto: String,
    elegida: Boolean,
    colorTexto: Color,
    fondoElegida: Color,
    borde: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(if (elegida) fondoElegida else Color.White, RoundedCornerShape(16.dp))
            .border(BorderStroke(if (elegida) 2.dp else 1.dp, if (elegida) borde else Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorTexto) }
}
