package com.bexai.multitienda.ui.view.operador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.SuperficieVariante
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario
import com.bexai.multitienda.viewmodel.HojaCantidad

private const val MAXIMO_MOTIVO = 120
private const val OTRO = "Otro"

/** Motivos de ajuste de un toque. "Otro" pide escribir el motivo. */
private val MOTIVOS_AJUSTE = listOf("Merma", "Dañado", "Contado mal", OTRO)

/**
 * Hoja inferior para escribir una cantidad.
 * - Cambiar cantidad (Entrada/Salida): la cantidad es cuántas unidades se mueven.
 * - Ajuste: la cantidad es el total real y además exige un motivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaCantidadView(
    hoja: HojaCantidad,
    onCerrar: () -> Unit,
    onDescartar: () -> Unit,
    onGuardar: (cantidad: Int, motivo: String) -> Unit
) {
    var texto by remember(hoja) { mutableStateOf(hoja.cantidadInicial.toString()) }
    // Si el motivo guardado es uno de los botones, se marca; si no, era "Otro" con texto
    var opcion by remember(hoja) {
        mutableStateOf(
            when {
                hoja.motivoInicial.isBlank() -> null
                hoja.motivoInicial in MOTIVOS_AJUSTE -> hoja.motivoInicial
                else -> OTRO
            }
        )
    }
    var motivoOtro by remember(hoja) {
        mutableStateOf(if (hoja.motivoInicial in MOTIVOS_AJUSTE) "" else hoja.motivoInicial)
    }
    val motivo = if (opcion == OTRO) motivoOtro.trim() else opcion.orEmpty()

    val cantidad = texto.toIntOrNull() ?: 0
    val signo = if (hoja.tipo == TipoMovimiento.ENTRADA) 1 else -1
    val esSalida = !hoja.esAjuste && hoja.tipo == TipoMovimiento.SALIDA
    val minimo = if (hoja.esAjuste || hoja.esConteo) 0 else 1
    val maximo = if (esSalida) hoja.stockBase else 9999
    val cantidadValida = texto.isNotEmpty() && cantidad in minimo..maximo
    val motivoValido = !hoja.esAjuste || motivo.isNotEmpty()
    val resultado = if (hoja.esAjuste) cantidad else hoja.stockBase + signo * cantidad
    val diferencia = cantidad - hoja.stockBase   // solo para Conteo
    val textoDiferencia = when {
        diferencia == 0 -> "sin diferencia"
        diferencia > 0 -> "+$diferencia"
        else -> "−${-diferencia}"
    }

    val titulo = when {
        hoja.esConteo -> "Conteo: escribe lo contado"
        hoja.esAjuste -> "Ajuste: escribe el total real"
        hoja.tipo == TipoMovimiento.ENTRADA -> "Cambiar cantidad · Entrada"
        else -> "Cambiar cantidad · Salida"
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título y botón cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    titulo,
                    modifier = Modifier.weight(1f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier.background(SuperficieVariante, CircleShape)
                ) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextoPrincipal) }
            }

            // Producto
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).background(AzulSuave, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Inventory2, null, tint = AzulOscuro) }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(hoja.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    Text(
                        when {
                            hoja.esConteo -> "Sistema ${hoja.stockBase}"
                            hoja.esAjuste -> "Stock actual ${hoja.stockBase}"
                            else -> "Stock inicial ${hoja.stockBase}"
                        },
                        color = TextoSecundario
                    )
                }
            }

            // Contador − [cantidad] +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotonContador(Icons.Default.Remove, "Restar", SuperficieVariante, TextoPrincipal) {
                    texto = (cantidad - 1).coerceIn(minimo, maximo).toString()
                }
                OutlinedTextField(
                    value = texto,
                    onValueChange = { nuevo -> texto = nuevo.filter { it.isDigit() }.take(4) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = TextoPrincipal
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(20.dp)
                )
                BotonContador(Icons.Default.Add, "Sumar", Azul, Color.White) {
                    texto = (cantidad + 1).coerceIn(minimo, maximo).toString()
                }
            }

            // Atajos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(6, 12, 24, 48).forEach { atajo ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SuperficieVariante, RoundedCornerShape(20.dp))
                            .clickable { texto = atajo.coerceAtMost(maximo).toString() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("$atajo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoPrincipal) }
                }
            }

            // Resultado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulSuave.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    when {
                        hoja.esConteo -> "Contado"
                        hoja.esAjuste -> "Nuevo total"
                        else -> "Stock resultante"
                    },
                    color = AzulOscuro, fontWeight = FontWeight.Bold
                )
                Text(
                    if (hoja.esConteo) "${hoja.stockBase} → $cantidad ($textoDiferencia)"
                    else "${hoja.stockBase} → $resultado",
                    color = AzulOscuro, fontWeight = FontWeight.Bold
                )
            }

            if (!cantidadValida) {
                Text(
                    if (esSalida) "Máximo $maximo (stock disponible)" else "Ingresa una cantidad entre $minimo y $maximo",
                    color = Peligro
                )
            }

            // Motivo (solo ajuste)
            if (hoja.esAjuste) {
                Row {
                    Text("¿Por qué ajustas? ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoPrincipal)
                    Text("*", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Peligro)
                }
                // Botones de un toque, en 2 filas de 2
                MOTIVOS_AJUSTE.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        fila.forEach { op ->
                            BotonMotivo(
                                texto = op,
                                seleccionado = opcion == op,
                                onClick = { opcion = op },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                if (opcion == OTRO) {
                    OutlinedTextField(
                        value = motivoOtro,
                        onValueChange = { motivoOtro = it.take(MAXIMO_MOTIVO) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escribe el motivo") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                    Text(
                        "Obligatorio · ${motivoOtro.length}/$MAXIMO_MOTIVO caracteres",
                        color = TextoSecundario
                    )
                }
            }

            // Descartar (rojo) y Guardar, lado a lado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDescartar,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Peligro),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PeligroSuave,
                        contentColor = Peligro
                    )
                ) { Text("Descartar", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { onGuardar(cantidad, motivo) },
                    enabled = cantidadValida && motivoValido,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text(
                        if (hoja.esAjuste) "Ajustar a $cantidad" else "Guardar $cantidad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonContador(
    icono: ImageVector,
    descripcion: String,
    fondo: Color,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 64.dp)
            .background(fondo, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icono, contentDescription = descripcion, tint = colorIcono) }
}

@Composable
private fun BotonMotivo(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (seleccionado) Azul else SuperficieVariante, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontWeight = FontWeight.Bold,
            color = if (seleccionado) Color.White else TextoPrincipal
        )
    }
}
