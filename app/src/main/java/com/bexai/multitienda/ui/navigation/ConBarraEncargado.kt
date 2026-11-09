package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bexai.multitienda.viewmodel.encargado.EncargadoViewModel

/**
 * Pantallas del encargado con su barra inferior (Stock, Diferencias, Alertas y Cuenta).
 * La pestaña Alertas muestra un contador con las tareas pendientes de la sucursal.
 */
@Composable
fun ConBarraEncargado(
    navController: NavHostController,
    contenido: @Composable (PaddingValues) -> Unit
) {
    val vm: EncargadoViewModel = viewModel()
    val totalAlertas by vm.totalAlertas.collectAsState()

    ConBarraRol(
        navController = navController,
        items = itemsEncargado.map { if (it.ruta == Ruta.EncargadoAlertas.ruta) it.copy(badge = totalAlertas) else it },
        rutaInicio = Ruta.EncargadoInicio.ruta,
        contenido = contenido
    )
}
