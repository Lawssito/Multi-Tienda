package com.bexai.multitienda.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bexai.multitienda.ui.theme.MultiTiendaTheme

/**
 * Acción que se muestra como botón en una pantalla.
 * [texto] es lo que se lee y [alHacerClic] es lo que ocurre al presionar.
 */
data class AccionPantalla(
    val texto: String,
    val alHacerClic: () -> Unit
)

/**
 * PantallaVacia: pantalla sencilla para probar la navegación.
 *
 * Muestra un título, una descripción y una lista de botones.
 * Cuando la Persona A complete su parte, estas pantallas se reemplazan por
 * las pantallas reales, pero la navegación ya queda probada.
 */
@Composable
fun PantallaVacia(
    titulo: String,
    descripcion: String,
    acciones: List<AccionPantalla> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Columna centrada con todo el contenido de la pantalla
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyMedium
        )

        // Un botón por cada acción que se quiera probar
        acciones.forEach { accion ->
            Button(
                onClick = accion.alHacerClic,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = accion.texto)
            }
        }
    }
}

/**
 * Vista previa de una pantalla vacía con dos acciones de ejemplo.
 */
@Preview(showBackground = true)
@Composable
fun PantallaVaciaPreview() {
    MultiTiendaTheme {
        PantallaVacia(
            titulo = "Pantalla de prueba",
            descripcion = "Aquí se prueba la navegación entre pantallas.",
            acciones = listOf(
                AccionPantalla(texto = "Ir a otra pantalla", alHacerClic = {}),
                AccionPantalla(texto = "Volver", alHacerClic = {})
            )
        )
    }
}
