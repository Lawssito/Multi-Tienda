package com.bexai.multitienda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bexai.multitienda.ui.theme.BarraFondo
import com.bexai.multitienda.ui.theme.ExitoSuave
import kotlinx.coroutines.delay

/** Aviso oscuro con una palomita. Se cierra al tocarlo o solo a los 4 segundos. */
@Composable
fun BannerConfirmacion(
    texto: String,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(texto) {
        delay(4_000)
        onCerrar()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BarraFondo, RoundedCornerShape(20.dp))
            .clickable(onClick = onCerrar)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = ExitoSuave)
        Spacer(Modifier.width(14.dp))
        Text(texto, color = Color.White)
    }
}
