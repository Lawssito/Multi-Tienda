package com.bexai.multitienda.ui.view.operador

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

/**
 * Hoja para escribir un código de barras a mano.
 * Validación: solo dígitos, de 8 a 13 caracteres. Si el producto no existe,
 * [error] llega desde el ViewModel y se muestra en el mismo campo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaCodigoManualView(
    error: String?,
    onCerrar: () -> Unit,
    onBuscar: (String) -> Unit,
    onEditar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val valido = texto.length in MINIMO_DIGITOS..MAXIMO_DIGITOS

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

            Text("Escribe el código de barras del producto.", color = TextoSecundario)

            OutlinedTextField(
                value = texto,
                onValueChange = {
                    texto = it.filter { c -> c.isDigit() }.take(MAXIMO_DIGITOS)
                    onEditar()   // al escribir, desaparece el error del producto no encontrado
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null,
                placeholder = { Text("Ej.: 7800000000016") },
                supportingText = {
                    Text(error ?: "Solo dígitos, de $MINIMO_DIGITOS a $MAXIMO_DIGITOS caracteres (${texto.length}/$MAXIMO_DIGITOS)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (valido) onBuscar(texto) }),
                shape = RoundedCornerShape(20.dp)
            )

            Button(
                onClick = { onBuscar(texto) },
                enabled = valido,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul)
            ) { Text("Buscar producto", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
