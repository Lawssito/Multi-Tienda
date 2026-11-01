package com.bexai.multitienda.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bexai.multitienda.ui.theme.MultiTiendaTheme

/**
 * ConfirmDialog: diálogo reutilizable para confirmar una acción importante
 * (por ejemplo, eliminar un producto o descartar cambios).
 *
 * Muestra:
 * - Un título y un mensaje con la pregunta.
 * - Botón "Aceptar" que ejecuta [onConfirm].
 * - Botón "Cancelar" que ejecuta [onDismiss].
 */
@Composable
fun ConfirmDialog(
    title: String,          // Título del diálogo
    message: String,        // Pregunta o explicación de la acción
    onConfirm: () -> Unit,  // Acción al presionar "Aceptar"
    onDismiss: () -> Unit   // Acción al presionar "Cancelar" o fuera del diálogo
) {
    AlertDialog(
        // Cerrar el diálogo tocando fuera o con el botón atrás
        onDismissRequest = onDismiss,
        // Título del diálogo
        title = { Text(text = title) },
        // Mensaje que explica qué se va a confirmar
        text = { Text(text = message) },
        // Botón que confirma la acción
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Aceptar")
            }
        },
        // Botón que cancela la acción
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

/**
 * Vista previa de ConfirmDialog con un mensaje de ejemplo.
 */
@Preview(showBackground = true)
@Composable
fun ConfirmDialogPreview() {
    MultiTiendaTheme {
        ConfirmDialog(
            title = "Eliminar producto",
            message = "¿Seguro que deseas eliminar este producto?",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
