package com.bexai.multitienda.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.ui.view.LoginView
import com.bexai.multitienda.ui.view.SeleccionSucursalView
import com.bexai.multitienda.viewmodel.SesionViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sesionViewModel: SesionViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta,
        modifier = modifier
    ) {
        // ---------- LOGIN ----------
        composable(Ruta.Login.ruta) {
            // Al volver al login (por ejemplo, después de cerrar sesión)
            // se limpia la sesión anterior.
            LaunchedEffect(Unit) {
                sesionViewModel.cerrarSesion()
            }

            LoginView(
                onLoginExitoso = { usuario ->
                    sesionViewModel.iniciarSesion(usuario)
                    navController.navigate(Ruta.SeleccionSucursal.ruta) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ---------- ELECCIÓN DE SUCURSAL ----------
        composable(Ruta.SeleccionSucursal.ruta) {
            SeleccionSucursalView(
                onContinuar = { rol ->
                    val destino = when (rol) {
                        Rol.OPERADOR -> Ruta.GRAFO_OPERADOR
                        Rol.ENCARGADO -> Ruta.GRAFO_ENCARGADO
                        Rol.GERENCIA -> Ruta.GRAFO_GERENCIA
                    }
                    navController.navigate(destino) {
                        // El login y la elección de sucursal ya no se necesitan
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVolverLogin = { navController.popBackStack() },
                viewModel = sesionViewModel
            )
        }

        operadorNavGraph(navController, sesionViewModel)
        encargadoNavGraph(navController, sesionViewModel)
        gerenciaNavGraph(navController, sesionViewModel)
    }
}
