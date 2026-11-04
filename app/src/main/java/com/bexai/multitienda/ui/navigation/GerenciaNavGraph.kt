package com.bexai.multitienda.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.viewmodel.SesionViewModel

fun NavGraphBuilder.gerenciaNavGraph(
    navController: NavHostController,
    sesionViewModel: SesionViewModel
) {
    navigation(
        startDestination = Ruta.GerenciaInicio.ruta,
        route = Ruta.GRAFO_GERENCIA
    ) {
        composable(Ruta.GerenciaInicio.ruta) {
            val sesion by sesionViewModel.uiState.collectAsState()

            PantallaVacia(
                titulo = "Inicio gerencia",
                descripcion = "Usuario: ${sesion.usuario?.nombre ?: "Sin usuario"}\n" +
                    "Sucursal: ${sesion.tienda?.nombre ?: "Sin sucursal"}",
                acciones = listOf(
                    AccionPantalla("Ir a reportes") {
                        navController.navigate(Ruta.GerenciaReportes.ruta) {
                            launchSingleTop = true
                        }
                    },
                    AccionPantalla("Cerrar sesión") {
                        cerrarSesionYVolverAlLogin(navController, sesionViewModel)
                    }
                )
            )
        }
        composable(Ruta.GerenciaReportes.ruta) {
            PantallaVacia(
                titulo = "Reportes",
                descripcion = "Pantalla de reportes.",
                acciones = listOf(
                    AccionPantalla("Volver") { navController.popBackStack() }
                )
            )
        }
    }
}
