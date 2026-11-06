package com.bexai.multitienda.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.ui.view.encargado.AlertasReposicionView
import com.bexai.multitienda.ui.view.encargado.DiferenciasStockView
import com.bexai.multitienda.ui.view.encargado.EncargadoHomeView

fun NavGraphBuilder.encargadoNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.EncargadoInicio.ruta,
        route = Ruta.GRAFO_ENCARGADO
    ) {
        composable(Ruta.EncargadoInicio.ruta) {
            ConBarraEncargado(navController) {
                EncargadoHomeView(navController)
            }
        }
        composable(Ruta.EncargadoStock.ruta) {
            ConBarraEncargado(navController) {
                EncargadoHomeView(navController)
            }
        }
        composable(Ruta.EncargadoDiferencias.ruta) {
            ConBarraEncargado(navController) {
                DiferenciasStockView(navController)
            }
        }
        composable(Ruta.EncargadoAlertasReposicion.ruta) {
            ConBarraEncargado(navController) {
                AlertasReposicionView(navController)
            }
        }
    }
}
