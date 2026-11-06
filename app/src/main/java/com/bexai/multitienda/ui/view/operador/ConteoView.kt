package com.bexai.multitienda.ui.view.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.LineaConteo
import com.bexai.multitienda.ui.theme.Advertencia
import com.bexai.multitienda.ui.theme.AdvertenciaSuave
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.BarraFondo
import com.bexai.multitienda.ui.theme.Exito
import com.bexai.multitienda.ui.theme.ExitoSuave
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.SuperficieVariante
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.ConteoUiState

@Composable
fun ConteoView(
    estado: ConteoUiState,
    onVolver: () -> Unit,
    onEscanear: () -> Unit,
    onFinalizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier.background(Color.White, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoPrincipal)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Conteo masivo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                Text(
                    if (estado.inicio.isEmpty()) estado.sucursal else "${estado.sucursal} · iniciado ${estado.inicio}",
                    color = TextoSecundario
                )
            }
        }

        // Progreso
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${estado.contados} de ${estado.total} productos",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal
                )
                Text("${estado.porcentaje}%", color = Azul, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { if (estado.total == 0) 0f else estado.contados.toFloat() / estado.total },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Azul,
                trackColor = SuperficieVariante
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EtiquetaConteo("Sin diferencia ${estado.sinDiferencia}", Exito, ExitoSuave)
                EtiquetaConteo("Con diferencia ${estado.conDiferencia}", Peligro, PeligroSuave)
            }
        }

        // Escanear siguiente
        Button(
            onClick = onEscanear,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Azul)
        ) {
            Icon(Icons.Outlined.CropFree, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Escanear siguiente", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // Lista
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Últimos contados", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
            Text("Contado / Sistema", color = TextoSecundario)
        }

        if (estado.lineas.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no has contado productos.\nPulsa Escanear siguiente.",
                    color = TextoSecundario
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estado.lineas, key = { it.productoId }) { linea -> FilaConteo(linea) }
            }
        }

        // Finalizar
        Button(
            onClick = onFinalizar,
            enabled = estado.lineas.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BarraFondo)
        ) { Text("Finalizar y revisar", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun EtiquetaConteo(texto: String, color: Color, fondo: Color) {
    Text(
        texto,
        color = color,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun FilaConteo(linea: LineaConteo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).background(AzulSuave, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Outlined.Inventory2, null, tint = AzulOscuro) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                linea.nombre, fontWeight = FontWeight.Bold, color = TextoPrincipal,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(linea.codigoBarras, color = TextoSecundario, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("${linea.contado} / ${linea.sistema}", fontWeight = FontWeight.Bold, color = TextoPrincipal)
        Spacer(Modifier.width(8.dp))
        InsigniaDiferencia(linea.diferencia)
    }
}

@Composable
internal fun InsigniaDiferencia(diferencia: Int) {
    val (texto, color, fondo) = when {
        diferencia == 0 -> Triple("✓ OK", Exito, ExitoSuave)
        diferencia < 0 -> Triple("▼ −${-diferencia}", Advertencia, AdvertenciaSuave)
        else -> Triple("▲ +$diferencia", AzulOscuro, AzulSuave)
    }
    Text(
        texto,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
