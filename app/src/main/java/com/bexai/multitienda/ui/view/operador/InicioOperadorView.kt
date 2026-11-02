package com.bexai.multitienda.ui.view.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Tune
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
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.theme.Advertencia
import com.bexai.multitienda.ui.theme.AdvertenciaSuave
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.Exito
import com.bexai.multitienda.ui.theme.ExitoSuave
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.TextoSecundario

@Composable
fun InicioOperadorView (
    sucursal: String,
    fecha: String,
    resumen: ResumenDia,
    onMovimiento: (TipoMovimiento) -> Unit,
    onConteo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EncabezadoSucursal(sucursal)
        TarjetaResumen(fecha, resumen)

        Text(
            text = "Movimientos",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonAccion(
                etiqueta = "Entrada", icono = Icons.Outlined.ArrowDownward,
                colorIcono = Exito, fondoIcono = ExitoSuave,
                onClick = { onMovimiento(TipoMovimiento.ENTRADA) },
                modifier = Modifier.weight(1f)
            )
            BotonAccion(
                etiqueta = "Salida", icono = Icons.Outlined.ArrowUpward,
                colorIcono = Peligro, fondoIcono = PeligroSuave,
                onClick = { onMovimiento(TipoMovimiento.SALIDA) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonAccion(
                etiqueta = "Ajuste", icono = Icons.Outlined.Tune,
                colorIcono = Advertencia, fondoIcono = AdvertenciaSuave,
                onClick = { onMovimiento(TipoMovimiento.AJUSTE) },
                modifier = Modifier.weight(1f)
            )
            BotonAccion(
                etiqueta = "Conteo", icono = Icons.Outlined.Checklist,
                colorIcono = AzulOscuro, fondoIcono = AzulSuave,
                onClick = onConteo,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EncabezadoSucursal(sucursal: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = AzulOscuro)
        Column {
            Text("Sucursal", fontSize = 13.sp, color = TextoSecundario)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sucursal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Cambiar Sucursal")
            }
        }
    }
}
@Composable
private fun TarjetaResumen(fecha: String, resumen: ResumenDia) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Azul, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Hoy $fecha", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Tu actividad", fontSize = 13.sp, color = AzulSuave)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DatoResumen("Entradas", resumen.entradas, Modifier.weight(1f))
            DatoResumen("Salidas", resumen.salidas, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DatoResumen("Movimientos", resumen.movimientos, Modifier.weight(1f))
            DatoResumen("Contados", resumen.contados, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(etiqueta, fontSize = 13.sp, color = Color(0xFFEFF6FF))
        Text(valor.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun BotonAccion(
    etiqueta: String,
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF3F3F3), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(fondoIcono, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(etiqueta, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Icon(
                Icons.Outlined.NorthEast, contentDescription = null,
                tint = TextoSecundario, modifier = Modifier.size(18.dp)
            )
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun InicioOperadorPreview() {
    MultiTiendaTheme {
        InicioOperadorView(
            sucursal = "Tienda Centro",
            fecha = "29 sep",
            resumen = ResumenDia(entradas = 214, salidas = 96, movimientos = 24, contados = 120),
            onMovimiento = {},
            onConteo = {}
        )
    }
}