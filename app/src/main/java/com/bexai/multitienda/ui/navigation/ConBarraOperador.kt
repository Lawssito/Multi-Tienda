package com.bexai.multitienda.ui.navigation

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
fun ConBarraOperador(
    navController: NavHostController,
    contenido: @Composable (PaddingValues) -> Unit
) {
    // Ruta en la que estamos, para pintar el botón seleccionado
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            RoleBottomBar(
                items = itemsOperador,
                rutaActual = rutaActual,
                central = centralOperador,
                onItemClick = { item ->
                    navController.navigate(item.ruta) {
                        popUpTo(Ruta.OperadorInicio.ruta) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding -> contenido(padding) }
}
