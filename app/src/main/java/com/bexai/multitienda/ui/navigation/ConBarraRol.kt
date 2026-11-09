package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bexai.multitienda.ui.components.ItemNav
import com.bexai.multitienda.ui.components.RoleBottomBar

/**
 * ConBarraRol: Scaffold con la barra inferior de un rol (patrón plantilla).
 *
 * Operador, Encargado y Gerencia usaban tres copias casi iguales de este código; ahora las tres
 * (ConBarraOperador, ConBarraEncargado y ConBarraGerencia) delegan aquí y solo indican sus
 * botones y la ruta de inicio de su grafo.
 *
 * @param rutaInicio pantalla raíz del rol: al cambiar de pestaña se vuelve a ella guardando el
 *   estado de cada pestaña (así cada una recuerda su scroll y filtros).
 * @param central botón destacado del centro (solo el operador tiene uno: Escanear).
 */
@Composable
internal fun ConBarraRol(
    navController: NavHostController,
    items: List<ItemNav>,
    rutaInicio: String,
    central: ItemNav? = null,
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
                items = items,
                rutaActual = rutaActual,
                central = central,
                onItemClick = { item ->
                    navController.navigate(item.ruta) {
                        popUpTo(rutaInicio) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding -> contenido(padding) }
}
