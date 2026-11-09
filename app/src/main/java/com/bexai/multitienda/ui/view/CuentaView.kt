package com.bexai.multitienda.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla "Cuenta" del operador, el encargado y gerencia (mockups CuentaEncargado y CuentaGerencia):
 * avatar con iniciales, rol, sucursal asignada y cerrar sesión.
 */
@Composable
fun CuentaView(
    iniciales: String,
    rol: String,
    sucursal: String?,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    onCambiarSucursal: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FA))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cuenta", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(52.dp).background(Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                }
                Column {
                    Text(rol, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("StockMulti", fontSize = 14.sp, color = Color(0xFF5B6474))
                }
            }
            if (sucursal != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sucursal asignada", fontSize = 15.sp, color = Color(0xFF5B6474))
                    Text(sucursal, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }
            }
        }

        if (onCambiarSucursal != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(28.dp))
                    .clickable(onClick = onCambiarSucursal),
                contentAlignment = Alignment.Center
            ) {
                Text("Cambiar de sucursal", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), textAlign = TextAlign.Center)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFFEE2E2), RoundedCornerShape(28.dp))
                .border(BorderStroke(1.dp, Color(0xFFFCA5A5)), RoundedCornerShape(28.dp))
                .clickable(onClick = onCerrarSesion),
            contentAlignment = Alignment.Center
        ) {
            Text("Cerrar sesión", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), textAlign = TextAlign.Center)
        }
    }
}
