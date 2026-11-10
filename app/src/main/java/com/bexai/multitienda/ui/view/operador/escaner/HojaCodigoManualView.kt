package com.bexai.multitienda.ui.view.operador.escaner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.SuperficieVariante
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario

private const val MINIMO_DIGITOS = 8
private const val MAXIMO_DIGITOS = 13
private const val MAXIMO_CAJA = 16

/**
 * Hoja para escribir un código de barras a mano.
 * Validación: solo dígitos, de 8 a 13 caracteres. Si el producto no existe,
 * [error] llega desde el ViewModel y se muestra en el mismo campo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaCodigoManualView(
    error: String?,
    permiteCaja: Boolean,   // en Entrada también se acepta el código de una caja (DSP-0042)
    onCerrar: () -> Unit,
    onBuscar: (String) -> Unit,
    onEditar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val esCodigoCaja = permiteCaja && Regex("[A-Za-z]{2,5}-?[0-9]{2,10}").matches(texto)
    val valido = texto.length in MINIMO_DIGITOS..MAXIMO_DIGITOS && texto.all { it.isDigit() } || esCodigoCaja

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Código manual",
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

            Text(
                if (permiteCaja) "Escribe el código de barras del producto o el código de la caja (DSP-0042)."
                else "Escribe el código de barras del producto.",
                color = TextoSecundario
            )

            OutlinedTextField(
                value = texto,
                onValueChange = {
                    texto = if (permiteCaja) {
                        it.filter { c -> c.isLetterOrDigit() || c == '-' }.take(MAXIMO_CAJA)
                    } else {
                        it.filter { c -> c.isDigit() }.take(MAXIMO_DIGITOS)
                    }
                    onEditar()   // al escribir, desaparece el error del producto no encontrado
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null,
                placeholder = { Text(if (permiteCaja) "Ej.: 7800000000016 o DSP-0042" else "Ej.: 7800000000016") },
                supportingText = {
                    Text(
                        error ?: if (permiteCaja) {
                            "Producto: $MINIMO_DIGITOS a $MAXIMO_DIGITOS dígitos. Caja: letras y número, como DSP-0042."
                        } else {
                            "Solo dígitos, de $MINIMO_DIGITOS a $MAXIMO_DIGITOS caracteres (${texto.length}/$MAXIMO_DIGITOS)"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = if (permiteCaja) KeyboardType.Text else KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (valido) onBuscar(texto) }),
                shape = RoundedCornerShape(20.dp)
            )

            Button(
                onClick = { onBuscar(texto) },
                enabled = valido,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul)
            ) { Text(if (esCodigoCaja) "Buscar caja" else "Buscar producto", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
