package com.bexai.multitienda.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia

fun NavGraphBuilder.encargadoNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.EncargadoInicio.ruta,
        route = Ruta.GRAFO_ENCARGADO
    )
    {
        composable(Ruta.EncargadoInicio.ruta)
        {
            PantallaVacia(
                titulo = "Inicio Encargado",
                descripcion = "Grafo de encargado.",
                acciones = listOf(
                    AccionPantalla(
                        texto = "Volever al login",
                        alHacerClic = {
                            navController.navigate(Ruta.Login.ruta) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                )
            )
        }
    }
}