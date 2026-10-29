package com.bexai.multitienda.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bexai.multitienda.ui.theme.MultiTiendaTheme

/**
 * SearchField: campo de búsqueda reutilizable.
 *
 * Sirve para filtrar listas (productos, clientes, etc.):
 * - Muestra un ícono de lupa al inicio del campo.
 * - Muestra un texto guía cuando el campo está vacío.
 * - Si hay texto escrito, muestra un botón (X) para limpiar la búsqueda.
 */
@Composable
fun SearchField(
    query: String,                  // Texto que se está buscando
    onQueryChange: (String) -> Unit, // Se ejecuta cada vez que cambia la búsqueda
    placeholder: String = "Buscar..." // Texto guía cuando el campo está vacío
) {
    OutlinedTextField(
        // Texto actual de la búsqueda
        value = query,
        // Avisa hacia afuera cada cambio en la búsqueda
        onValueChange = onQueryChange,
        // Texto guía que se ve cuando no se ha escrito nada
        placeholder = { Text(text = placeholder) },
        // Ícono de lupa al inicio del campo
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Buscar"
            )
        },
        // Botón para limpiar el texto; solo aparece si hay algo escrito
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        },
        // Evita que el campo crezca a varias líneas
        singleLine = true,
        // Ocupa todo el ancho disponible
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Vista previa de SearchField con texto escrito,
 * para ver la lupa y el botón de limpiar.
 */
@Preview(showBackground = true)
@Composable
fun SearchFieldPreview() {
    MultiTiendaTheme {
        SearchField(
            query = "Leche",
            onQueryChange = {}
        )
    }
}
