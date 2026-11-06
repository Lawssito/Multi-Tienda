package com.bexai.multitienda.ui.view.operador

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.ui.theme.Superficie
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario

/**
 * Pantalla donde el operador elige la sucursal en la que va a trabajar.
 * Las sucursales inactivas se ven deshabilitadas y no se pueden elegir.
 */
@Composable
fun SeleccionarSucursalView(
    sucursales: List<Sucursal>,
    onElegir: (Sucursal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Elige tu sucursal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextoPrincipal
        )
        Text(
            text = "Los movimientos y conteos se registrarán en esta sucursal.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sucursales, key = { it.id }) { sucursal ->
                Card(
                    onClick = { onElegir(sucursal) },
                    enabled = sucursal.activa,
                    colors = CardDefaults.cardColors(containerColor = Superficie),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = sucursal.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal
                        )
                        Text(
                            text = if (sucursal.activa) sucursal.direccion else "Sucursal inactiva",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario
                        )
                    }
                }
            }
        }
    }
}
