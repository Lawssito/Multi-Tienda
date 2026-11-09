package com.bexai.multitienda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.ui.theme.Azul

/**
 * Pastilla con la sucursal actual. Al tocarla se vuelve a la pantalla de elegir sucursal.
 * La usan las pantallas del encargado (Stock, Diferencias y Alertas).
 */
@Composable
fun PastillaSucursal(nombre: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Azul)
        Text(nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Cambiar sucursal", tint = Color(0xFF5B6474))
    }
}
