package com.bexai.multitienda.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.model.Ubicacion
import com.bexai.multitienda.model.Usuario
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import com.bexai.multitienda.viewmodel.SesionViewModel
import java.util.Date

/**
 * SeleccionSucursalView: pantalla para elegir la sucursal (tienda) en la
 * que trabajará el usuario después de iniciar sesión.
 *
 * La lista viene desde Room por medio de [SesionViewModel.tiendas].
 * Las sucursales con activa = false se muestran deshabilitadas.
 */
@Composable
fun SeleccionSucursalView(
    onContinuar: (Rol) -> Unit,
    onVolverLogin: (() -> Unit)? = null,
    viewModel: SesionViewModel = viewModel()
) {
    val sesion by viewModel.uiState.collectAsState()
    val tiendas by viewModel.tiendas.collectAsState()

    SeleccionSucursalContenido(
        usuario = sesion.usuario,
        rol = sesion.rol,
        tiendas = tiendas,
        tiendaSeleccionada = sesion.tienda,
        error = sesion.error,
        onSeleccionar = viewModel::seleccionarTienda,
        onContinuar = {
            if (viewModel.confirmarSesion()) {
                viewModel.obtenerRol()?.let(onContinuar)
            }
        },
        onVolverLogin = onVolverLogin
    )
}

/**
 * Contenido sin ViewModel para poder previsualizarlo y probarlo con datos
 * de ejemplo.
 */
@Composable
fun SeleccionSucursalContenido(
    usuario: Usuario?,
    rol: Rol?,
    tiendas: List<Ubicacion>,
    tiendaSeleccionada: Ubicacion?,
    error: String?,
    onSeleccionar: (Ubicacion) -> Unit,
    onContinuar: () -> Unit,
    onVolverLogin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onVolverLogin != null) {
            TextButton(onClick = onVolverLogin) {
                Text(text = "← Volver al login")
            }
        }

        Text(
            text = "Elegir sucursal",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Usuario: ${usuario?.nombre ?: "Sin sesión"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Rol: ${rol?.name ?: "Sin rol"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sucursales disponibles",
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tiendas.isEmpty()) {
                item {
                    Text(
                        text = "No hay sucursales registradas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(tiendas, key = { it.id }) { tienda ->
                TarjetaSucursal(
                    ubicacion = tienda,
                    seleccionada = tiendaSeleccionada?.id == tienda.id,
                    onSeleccionar = onSeleccionar
                )
            }
        }

        error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onContinuar,
            enabled = tiendaSeleccionada != null && tiendaSeleccionada.activa,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Continuar")
        }
    }
}

/**
 * Tarjeta de una sucursal. Si está inactiva, se ve más transparente y no
 * se puede seleccionar.
 */
@Composable
private fun TarjetaSucursal(
    ubicacion: Ubicacion,
    seleccionada: Boolean,
    onSeleccionar: (Ubicacion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (ubicacion.activa) 1f else 0.55f)
            .clickable(enabled = ubicacion.activa) { onSeleccionar(ubicacion) },
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = seleccionada,
                onClick = null,
                enabled = ubicacion.activa
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ubicacion.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = ubicacion.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!ubicacion.activa) {
                    Text(
                        text = "Inactiva",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeleccionSucursalPreview() {
    MultiTiendaTheme {
        SeleccionSucursalContenido(
            usuario = Usuario(
                nombre = "Operador Demo",
                email = "operador@stockmulti.com",
                password = "opedemo123",
                rol = Rol.OPERADOR
            ),
            rol = Rol.OPERADOR,
            tiendas = listOf(
                Ubicacion(
                    id = 1,
                    nombre = "Sucursal Centro",
                    direccion = "Av. Libertador 123",
                    activa = true,
                    fechaCreacion = Date(0)
                ),
                Ubicacion(
                    id = 2,
                    nombre = "Sucursal Ñuñoa",
                    direccion = "Av. Irarrázaval 789",
                    activa = false,
                    fechaCreacion = Date(0)
                )
            ),
            tiendaSeleccionada = null,
            error = null,
            onSeleccionar = {},
            onContinuar = {},
            onVolverLogin = {}
        )
    }
}
