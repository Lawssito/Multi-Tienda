package com.bexai.multitienda.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia

fun NavGraphBuilder.gerenciaNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.GerenciaInicio.ruta,
        route = Ruta.GRAFO_GERENCIA
    ) {
        composable(Ruta.GerenciaInicio.ruta) {
            PantallaVacia(
                titulo = "Inicio gestión",
                descripcion = "Grafo de gestión. Pantalla vacía de prueba.",
                acciones = listOf(
                    AccionPantalla(
                        texto = "Ir a reportes",
                        alHacerClic = {
                            navController.navigate(Ruta.GerenciaInicio.ruta)
                        }
                    ),
                    AccionPantalla(
                        texto = "Volver al login",
                        alHacerClic = {
                            navController.navigate(Ruta.Login.ruta) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                )
            )
        }
        composable(Ruta.GerenciaReportes.ruta) {
            PantallaVacia(
                titulo = "Reportes",
                descripcion = "Pantalla vacía de reportes para probar la navegación.",
                acciones = listOf(
                    AccionPantalla(
                        texto = "Volver",
                        alHacerClic = { navController.popBackStack() }
                    )
                )
            )
        }
    }
}
