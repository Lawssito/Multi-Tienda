package com.bexai.multitienda.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import kotlinx.coroutines.withTimeoutOrNull

/**
 * ProductRow: fila reutilizable para mostrar un producto dentro de una lista.
 *
 * Muestra:
 * - En la izquierda: el nombre del producto y su precio.
 * - En la derecha: las existencias o "Sin stock" si no hay unidades.
 * - Si el usuario mantiene presionada la fila durante 3 segundos,
 *   se ejecuta [onLongPress] (por ejemplo, para preguntar si se elimina).
 */
@Composable
fun ProductRow(
    name: String,              // Nombre del producto
    price: Double,             // Precio del producto
    stock: Int,                // Cantidad de unidades disponibles
    onLongPress: () -> Unit = {} // Acción al mantener presionado 3 segundos
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                // detectTapGestures permite saber cuándo se presiona y suelta
                detectTapGestures(
                    onPress = {
                        // Espera máximo 3 segundos a que el usuario suelte
                        val resultado = withTimeoutOrNull(1000L) {
                            tryAwaitRelease()
                        }
                        // Si el tiempo se agotó (resultado null), mantuvo presionado
                        if (resultado == null) {
                            onLongPress()
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Espacio interno de la tarjeta
            // Centra los textos y el stock verticalmente
            verticalAlignment = Alignment.CenterVertically,
            // Separa el bloque de textos del stock a los extremos
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Columna izquierda: nombre y precio del producto
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Precio: $price",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Texto derecho: existencias disponibles o aviso de que no hay
            Text(
                text = if (stock > 0) "Stock: $stock" else "Sin stock",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Vista previa de ProductRow con un producto de ejemplo.
 */
@Preview(showBackground = true)
@Composable
fun ProductRowPreview() {
    MultiTiendaTheme {
        ProductRow(
            name = "Leche entera 1 L",
            price = 4500.0,
            stock = 24
        )
    }
}
