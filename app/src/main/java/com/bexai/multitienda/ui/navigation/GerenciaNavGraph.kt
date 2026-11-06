package com.bexai.multitienda.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.ui.view.gerencia.AuditoriaStockView
import com.bexai.multitienda.ui.view.gerencia.GerenciaDashboardView
import com.bexai.multitienda.ui.view.gerencia.GestionTiendasView
import com.bexai.multitienda.ui.view.gerencia.ReportesView

fun NavGraphBuilder.gerenciaNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.GerenciaInicio.ruta,
        route = Ruta.GRAFO_GERENCIA
    ) {
        composable(Ruta.GerenciaInicio.ruta) {
            ConBarraGerencia(navController) {
                GerenciaDashboardView(navController)
            }
        }
        composable(Ruta.GerenciaAuditoria.ruta) {
            ConBarraGerencia(navController) {
                AuditoriaStockView(navController)
            }
        }
        composable(Ruta.GerenciaGestionTiendas.ruta) {
            ConBarraGerencia(navController) {
                GestionTiendasView(navController)
            }
        }
        composable(Ruta.GerenciaReportes.ruta) {
            ConBarraGerencia(navController) {
                ReportesView(navController)
            }
        }
    }
}
