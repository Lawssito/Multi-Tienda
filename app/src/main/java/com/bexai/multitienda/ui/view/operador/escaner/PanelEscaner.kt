package com.bexai.multitienda.ui.view.operador.escaner

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.theme.Advertencia
import com.bexai.multitienda.ui.theme.AdvertenciaSuave
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.BarraFondo
import com.bexai.multitienda.ui.theme.Borde
import com.bexai.multitienda.ui.theme.Celeste
import com.bexai.multitienda.ui.theme.Exito
import com.bexai.multitienda.ui.theme.ExitoSuave
import com.bexai.multitienda.ui.theme.GrisBarra
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.SuperficieVariante
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.operador.EscanerUiState
import com.bexai.multitienda.viewmodel.operador.UltimaLectura

// =====================================================================
// Controles del escáner: barra superior (sucursal y tipo de movimiento) y panel inferior
// (última lectura, Código manual, disparo y flash). Los usa EscanerView.
// =====================================================================

@Composable
internal fun BarraSuperiorEscaner(
    sucursal: String,
    tipo: TipoMovimiento,
    esConteo: Boolean,
    esRecepcion: Boolean,
    esCaja: Boolean,
    onTipoChange: (TipoMovimiento, Boolean) -> Unit,
    onCajaElegida: () -> Unit,
    onCerrar: () -> Unit,
    onListo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCerrar,
            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sucursal, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            ChipTipo(tipo, esConteo, esRecepcion, esCaja, onTipoChange, onCajaElegida)
        }

        Button(
            onClick = onListo,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextoPrincipal)
        ) { Text("Listo", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ChipTipo(
    tipo: TipoMovimiento,
    esConteo: Boolean,
    esRecepcion: Boolean,
    esCaja: Boolean,
    onTipoChange: (TipoMovimiento, Boolean) -> Unit,
    onCajaElegida: () -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val (texto, color, fondo) = when {
        esRecepcion || esCaja -> Triple("Entrada · caja", Exito, ExitoSuave)
        esConteo -> Triple("Conteo", TextoPrincipal, SuperficieVariante)
        tipo == TipoMovimiento.ENTRADA -> Triple("Entrada", AzulOscuro, AzulSuave)
        tipo == TipoMovimiento.SALIDA -> Triple("Salida", Peligro, PeligroSuave)
        else -> Triple("Ajuste", Advertencia, AdvertenciaSuave)
    }
    Box {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(fondo)
                .clickable(enabled = !esRecepcion) { abierto = true }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(texto, color = color, fontWeight = FontWeight.Bold)
            if (!esRecepcion) {
                Icon(Icons.Default.KeyboardArrowDown, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        // Conteo se guarda como Ajuste (tipo AJUSTE) pero con el modo conteo activado
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            listOf(
                Triple(TipoMovimiento.ENTRADA, false, "Entrada"),
                Triple(TipoMovimiento.ENTRADA, true, "Caja"),
                Triple(TipoMovimiento.SALIDA, false, "Salida"),
                Triple(TipoMovimiento.AJUSTE, false, "Ajuste"),
                Triple(TipoMovimiento.AJUSTE, true, "Conteo")
            ).forEach { (opcionTipo, marca, nombre) ->
                // La marca es "es Conteo", salvo en la opción Caja (Entrada con marca true)
                val esOpcionCaja = nombre == "Caja"
                DropdownMenuItem(
                    text = { Text(nombre) },
                    onClick = {
                        abierto = false
                        if (esOpcionCaja) {
                            if (!esCaja) onCajaElegida()
                        } else if (opcionTipo != tipo || marca != esConteo || esCaja) {
                            onTipoChange(opcionTipo, marca)
                        }
                    }
                )
            }
        }
    }
}

@Composable
internal fun PanelEscaner(
    estado: EscanerUiState,
    buscando: Boolean,
    linterna: Boolean,
    onManual: () -> Unit,
    onDisparo: () -> Unit,
    onFlash: () -> Unit,
    onCambiarCantidad: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            // Mismo margen inferior que la barra del operador, para que los botones queden a su altura
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (estado.ultima == null) "Escanear código" else "Último escaneado",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal
                )
                Text(
                    when (estado.lecturas) {
                        0 -> "Sin lecturas"
                        1 -> "1 lectura"
                        else -> "${estado.lecturas} lecturas"
                    },
                    color = TextoSecundario
                )
            }

            estado.ultima?.let { TarjetaLectura(it, onCambiarCantidad) }

            if (buscando) {
                Text("Buscando código…", color = TextoSecundario)
            } else if (estado.mensaje != null) {
                Text(estado.mensaje, color = Peligro, fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AccionPanel(
                    etiqueta = "Código manual",
                    icono = Icons.Outlined.Keyboard,
                    colorIcono = Color(0xFF7C3AED),
                    fondoIcono = Color(0xFFEDE9FE),
                    onClick = onManual,
                    modifier = Modifier.weight(1f)
                )
                BotonDisparo(onClick = onDisparo, modifier = Modifier.weight(1f))
                AccionPanel(
                    etiqueta = "Flash",
                    icono = if (linterna) Icons.Filled.FlashOn else Icons.Outlined.FlashOn,
                    colorIcono = if (linterna) Color.White else TextoPrincipal,
                    fondoIcono = if (linterna) Azul else SuperficieVariante,
                    onClick = onFlash,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TarjetaLectura(lectura: UltimaLectura, onClick: () -> Unit) {
    val esAjuste = lectura.tipo == TipoMovimiento.AJUSTE && !lectura.esConteo
    val detalle = if (lectura.detalleRecepcion != null) lectura.detalleRecepcion else if (lectura.esConteo) "Conteo · sistema ${lectura.stockAntes}" else when (lectura.tipo) {
        TipoMovimiento.ENTRADA -> "Entrada +${lectura.cantidad} · stock ${lectura.stockAntes} → ${lectura.stockDespues}"
        TipoMovimiento.SALIDA -> "Salida −${lectura.cantidad} · stock ${lectura.stockAntes} → ${lectura.stockDespues}"
        TipoMovimiento.AJUSTE -> "Ajuste · stock ${lectura.stockAntes} → ${lectura.stockDespues}"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Borde, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                lectura.nombre,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(detalle, color = TextoSecundario)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Edit, null, tint = Azul, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (esAjuste) "Toca para editar el ajuste" else "Toca para cambiar la cantidad",
                    color = Azul,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (!esAjuste) {
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier.background(AzulSuave, CircleShape).padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("×${lectura.cantidad}", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun AccionPanel(
    etiqueta: String,
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(52.dp).background(fondoIcono, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icono, null, tint = colorIcono) }
        Text(etiqueta, fontWeight = FontWeight.Medium, color = TextoPrincipal)
    }
}

@Composable
private fun BotonDisparo(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .border(6.dp, GrisBarra, CircleShape)
                .padding(6.dp)
                .background(Celeste, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CropFree, null, tint = BarraFondo, modifier = Modifier.size(32.dp))
        }
    }
}
