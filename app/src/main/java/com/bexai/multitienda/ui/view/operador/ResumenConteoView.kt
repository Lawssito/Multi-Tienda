package com.bexai.multitienda.ui.view.operador

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.bexai.multitienda.ui.theme.Borde
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.ConteoUiState

@Composable
fun ResumenConteoView(
    estado: ConteoUiState,
    onVolver: () -> Unit,
    onRecontar: (productoId: Int) -> Unit,
    onEnviar: () -> Unit,
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
                Text("Resumen del conteo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                Text("${estado.sucursal} · ${estado.contados} productos contados", color = TextoSecundario)
            }
        }

        // Totales
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaTotal("Sin diferencia", estado.sinDiferencia, Azul, AzulSuave, Modifier.weight(1f))
            TarjetaTotal("Faltantes", estado.faltantes, Advertencia, AdvertenciaSuave, Modifier.weight(1f))
            TarjetaTotal("Sobrantes", estado.sobrantes, AzulOscuro, AzulSuave, Modifier.weight(1f))
        }

        Text("Productos con diferencia", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)

        val conDiferencia = estado.conDiferenciaOrdenadas
        if (conDiferencia.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Todo lo contado coincide con el sistema.", color = TextoSecundario)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conDiferencia, key = { it.productoId }) { linea ->
                    FilaDiferencia(linea, onRecontar = { onRecontar(linea.productoId) })
                }
            }
        }

        // Avisos
        Text(
            buildString {
                append("Al enviar, el encargado revisará el conteo. El stock solo cambia cuando lo apruebe.")
                if (estado.sinContar > 0) {
                    append("\n${estado.sinContar} productos sin contar no se ajustarán.")
                }
            },
            color = TextoSecundario
        )

        Button(
            onClick = onEnviar,
            enabled = estado.lineas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Azul)
        ) { Text("Enviar conteo", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun TarjetaTotal(
    etiqueta: String,
    valor: Int,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(fondo, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("$valor", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
        Text(etiqueta, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FilaDiferencia(linea: LineaConteo, onRecontar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                linea.nombre, fontWeight = FontWeight.Bold, color = TextoPrincipal,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text("Contado ${linea.contado} · Sistema ${linea.sistema}", color = TextoSecundario, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))
        InsigniaDiferencia(linea.diferencia)
        Spacer(Modifier.width(8.dp))
        OutlinedButton(
            onClick = onRecontar,
            border = BorderStroke(1.dp, Borde),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Recontar", color = TextoPrincipal, fontWeight = FontWeight.Medium) }
    }
}
