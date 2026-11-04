package com.bexai.multitienda.ui.navigation

import androidx.navigation.NavHostController
import com.bexai.multitienda.viewmodel.SesionViewModel

/**
 * Cierra la sesión y vuelve al login eliminando el menú del rol del back stack.
 * Se usa desde todos los grafos para que "cerrar sesión" funcione igual.
 */
internal fun cerrarSesionYVolverAlLogin(
    navController: NavHostController,
    sesionViewModel: SesionViewModel
) {
    sesionViewModel.cerrarSesion()
    navController.navigate(Ruta.Login.ruta) {
        popUpTo(navController.graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
