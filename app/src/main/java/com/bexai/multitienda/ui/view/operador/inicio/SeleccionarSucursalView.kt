package com.bexai.multitienda.ui.view.operador.inicio

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.FondoPantalla
import com.bexai.multitienda.ui.theme.GrisFondo
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario

private val FondoTarjeta = Color(0xFFF3F3F3)
private val TextoSubtitulo = Color(0xFF334155)

// Cada sucursal usa un color de ícono distinto: azul, naranja, morado
private val ColoresIcono = listOf(
    Color(0xFFE8F1FE) to Color(0xFF3B82F6),
    Color(0xFFFFF1E6) to Color(0xFFF97316),
    Color(0xFFF1EBFE) to Color(0xFF7C3AED)
)

/**
 * Pantalla donde el operador elige la sucursal en la que va a trabajar (mockup "Elegir sucursal").
 * Las sucursales inactivas salen apagadas con un candado y no se pueden elegir.
 * [onVolver] es null cuando no hay pantalla anterior (primer ingreso): ahí no se muestra la flecha.
 */
@Composable
fun SeleccionarSucursalView(
    sucursales: List<Sucursal>,
    sucursalActualId: Int?,
    onElegir: (Sucursal) -> Unit,
    onVolver: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    val visibles = sucursales.filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Barra superior: volver, título y avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onVolver != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .clickable(onClick = onVolver),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver",
                        tint = TextoPrincipal, modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(Modifier.size(40.dp))
            }
            Text("Elegir sucursal", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextoPrincipal)
            Box(Modifier.size(40.dp))
        }

        Text(
            text = "¿Dónde trabajarás hoy?",
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextoPrincipal
        )
        Text(
            text = "Tus movimientos quedarán registrados en esta sucursal.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = TextoSubtitulo
        )

        CampoBusqueda(valor = busqueda, onCambio = { busqueda = it })

        Text(
            text = "SUCURSALES",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = TextoPrincipal
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(visibles, key = { _, s -> s.id }) { indice, sucursal ->
                TarjetaSucursal(
                    sucursal = sucursal,
                    colores = ColoresIcono[indice % ColoresIcono.size],
                    esActual = sucursal.id == sucursalActualId,
                    onClick = { onElegir(sucursal) }
                )
            }
        }
    }
}

@Composable
private fun CampoBusqueda(valor: String, onCambio: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (valor.isEmpty()) {
                Text("Buscar sucursal", fontSize = 15.sp, color = TextoSecundario)
            }
            BasicTextField(
                value = valor,
                onValueChange = onCambio,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = TextoPrincipal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TarjetaSucursal(
    sucursal: Sucursal,
    colores: Pair<Color, Color>,
    esActual: Boolean,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(18.dp)
    val activa = sucursal.activa
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (activa) FondoTarjeta else GrisFondo, forma)
            .then(if (esActual) Modifier.border(BorderStroke(2.dp, Azul), forma) else Modifier)
            .clickable(enabled = activa, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(if (activa) colores.first else Color(0xFFE2E7EF), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (activa) Icons.Outlined.Storefront else Icons.Outlined.Lock,
                contentDescription = null,
                tint = if (activa) colores.second else Color(0xFF94A3B8),
                modifier = Modifier.size(26.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                sucursal.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = if (activa) TextoPrincipal else TextoSecundario
            )
            Text(
                text = if (activa) sucursal.direccion else "Inactiva · no disponible",
                fontSize = 13.sp,
                color = TextoSecundario
            )
        }
        if (activa) {
            Text(
                text = if (esActual) "Actual" else "Operativa",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (esActual) Color(0xFF1D4ED8) else Color(0xFF15803D),
                modifier = Modifier
                    .background(
                        if (esActual) Color(0xFFDBEAFE) else Color(0xFFE7F7EF),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
