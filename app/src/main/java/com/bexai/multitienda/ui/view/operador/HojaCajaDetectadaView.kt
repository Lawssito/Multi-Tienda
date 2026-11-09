package com.bexai.multitienda.ui.view.operador

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.viewmodel.CajaDetectada

/**
 * Hoja "Caja detectada" (mockup 9a): aparece al leer en Entrada el QR de un despacho.
 * "Comenzar recepción" abre la recepción; el texto de abajo cierra la hoja para escanear otro QR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaCajaDetectadaView(
    caja: CajaDetectada,
    onComenzar: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Caja detectada", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextoRecepcion)
                ChipEstado("✓ QR leído", VerdeTexto, VerdeFondo)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(52.dp).background(Color(0xFFF6E8DA), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color(0xFFB45309))
                    }
                    Column {
                        Text("Despacho ${caja.codigo}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoRecepcion)
                        Text("${caja.proveedor} · Guía ${caja.guia}", fontSize = 14.sp, color = TextoSuaveRecepcion)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DatoCaja(caja.productos.toString(), "productos", Modifier.weight(1f))
                    DatoCaja(caja.unidades.toString(), "unidades esperadas", Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Azul, RoundedCornerShape(28.dp))
                    .clickable(onClick = onComenzar),
                contentAlignment = Alignment.Center
            ) {
                Text("Comenzar recepción", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "¿No es esta caja? Vuelve a escanear el QR",
                modifier = Modifier.fillMaxWidth().clickable(onClick = onCerrar),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = TextoSuaveRecepcion
            )
        }
    }
}

@Composable
private fun DatoCaja(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(FondoRecepcion, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(valor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextoRecepcion)
        Text(etiqueta, fontSize = 14.sp, color = TextoSuaveRecepcion)
    }
}
