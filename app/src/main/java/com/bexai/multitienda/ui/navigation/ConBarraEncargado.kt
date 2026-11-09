package com.bexai.multitienda.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.viewmodel.EncargadoViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bexai.multitienda.ui.components.RoleBottomBar

@Composable
fun ConBarraEncargado(
    navController: NavHostController,
    contenido: @Composable (PaddingValues) -> Unit
) {
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val vm: EncargadoViewModel = viewModel()
    val totalAlertas by vm.totalAlertas.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            RoleBottomBar(
                items = itemsEncargado.map { if (it.ruta == Ruta.EncargadoAlertas.ruta) it.copy(badge = totalAlertas) else it },
                rutaActual = rutaActual,
                onItemClick = { item ->
                    navController.navigate(item.ruta) {
                        popUpTo(Ruta.EncargadoInicio.ruta) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding -> contenido(padding) }
}
