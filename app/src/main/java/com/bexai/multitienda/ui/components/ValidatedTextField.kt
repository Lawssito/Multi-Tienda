package com.bexai.multitienda.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bexai.multitienda.ui.theme.MultiTiendaTheme

/**
 * ValidatedTextField: campo de texto reutilizable para formularios.
 *
 * Sirve para pedir un dato y avisar al usuario cuando ese dato es inválido:
 * - Muestra una etiqueta con el nombre del campo.
 * - Si [isError] es true, pinta el campo en rojo y muestra un ícono de error.
 * - Si [supportingText] tiene texto, lo muestra debajo del campo
 *   (normalmente es el mensaje de error).
 */
@Composable
fun ValidatedTextField(
    value: String,                  // Texto que el usuario está escribiendo
    onValueChange: (String) -> Unit, // Se ejecuta cada vez que el usuario escribe
    label: String,                  // Nombre del campo (por ejemplo "Nombre del producto")
    isError: Boolean = false,       // true = el dato no es válido
    supportingText: String? = null  // Mensaje de ayuda o de error debajo del campo
) {
    OutlinedTextField(
        // Valor actual del campo
        value = value,
        // Avisa hacia afuera cada cambio que hace el usuario
        onValueChange = onValueChange,
        // Etiqueta flotante que indica qué dato se debe escribir
        label = { Text(text = label) },
        // Activa el borde rojo y el color de error del campo
        isError = isError,
        // Muestra el mensaje de apoyo/error solo si existe
        supportingText = { if (supportingText != null) Text(text = supportingText) },
        // Muestra el ícono de error únicamente cuando hay error
        trailingIcon = {
            if (isError) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = "Error"
                )
            }
        },
        // Hace que el campo ocupe todo el ancho disponible
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Vista previa de ValidatedTextField en estado de error:
 * se ven la etiqueta, el ícono de error y el mensaje debajo del campo.
 */
@Preview(showBackground = true)
@Composable
fun ValidatedTextFieldPreview() {
    MultiTiendaTheme {
        ValidatedTextField(
            value = "",
            onValueChange = {},
            label = "Nombre del producto",
            isError = true,
            supportingText = "El nombre debe tener mínimo 5 caracteres"
        )
    }
}
