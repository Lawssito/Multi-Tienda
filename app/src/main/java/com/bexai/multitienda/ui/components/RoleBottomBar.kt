package com.bexai.multitienda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.ui.theme.BarraFondo
import com.bexai.multitienda.ui.theme.Celeste
import com.bexai.multitienda.ui.theme.GrisBarra
import com.bexai.multitienda.ui.theme.MultiTiendaTheme

// =====================================================================
// Barra inferior de navegación, común a los tres roles (estilo del mockup).
// =====================================================================

/** Un botón de la barra: ruta de destino, texto, ícono y contador opcional (badge). */
data class ItemNav(
    val ruta : String,
    val etiqueta : String,
    val icono : ImageVector,
    val badge : Int = 0,
)

@Composable
fun RoleBottomBar(
    items: List<ItemNav>,
    rutaActual: String?,
    onItemClick: (ItemNav) -> Unit,
    modifier: Modifier = Modifier,
    central: ItemNav? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(BarraFondo, RoundedCornerShape(28.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Con botón central, los ítems se reparten a ambos lados
            val mitad = if (central != null) items.size / 2 else items.size
            items.take(mitad).forEach {
                ItemBarra(it, it.ruta == rutaActual, onItemClick)
            }
            if (central != null) Spacer(modifier = Modifier.width(64.dp))
            items.drop(mitad).forEach {
                ItemBarra(it, it.ruta == rutaActual, onItemClick)
            }
        }

        if (central != null) {
            Box(
                modifier = Modifier
                    .offset(y = (-25).dp)
                    .size(80.dp)
                    .background(Celeste, RoundedCornerShape(35.dp))
                    .border(4.dp, Color(0xFFF4F6FA), RoundedCornerShape(35.dp))
                    .clickable{ onItemClick(central) },
                contentAlignment = Alignment.Center
            ) {
                Icon(central.icono, contentDescription = central.etiqueta, tint = BarraFondo)
            }
        }
    }
}

@Composable
private fun ItemBarra(
    item: ItemNav,
    seleccionado: Boolean,
    onClick: (ItemNav) -> Unit
) {
    val color = if (seleccionado) Celeste else GrisBarra
    Column(
        modifier = Modifier
            .clickable { onClick(item) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .widthIn(min = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 30.dp)
                .background(
                    if (seleccionado) Celeste.copy(alpha = 0.18f) else Color.Transparent,
                    RoundedCornerShape(15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icono, contentDescription = null, tint = color)
            if (item.badge > 0) {
                Text(
                    text = if (item.badge > 9) "9+" else "${item.badge}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = (-2).dp)
                        .background(Color(0xFFDC2626), RoundedCornerShape(8.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
        Text(
            text = item.etiqueta,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
fun RoleBottomBarOperadorPreview() {
    MultiTiendaTheme {
        RoleBottomBar(
            items = listOf(
                ItemNav("inicio", "Inicio", Icons.Outlined.Home),
                ItemNav("cuenta", "Cuenta", Icons.Outlined.Person)
            ),
            rutaActual = "inicio",
            onItemClick = {},
            central = ItemNav("escaner", "Escanear", Icons.Outlined.QrCodeScanner)
        )
    }
}
