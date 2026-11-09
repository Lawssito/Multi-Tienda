package com.bexai.multitienda.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.bexai.multitienda.ui.view.CuentaView
import com.bexai.multitienda.ui.view.gerencia.GerenciaAlertasView
import com.bexai.multitienda.ui.view.gerencia.GerenciaHistorialView
import com.bexai.multitienda.ui.view.gerencia.GerenciaStockView
import com.bexai.multitienda.ui.view.gerencia.GerenciaPanelView
import com.bexai.multitienda.ui.view.gerencia.GestionTiendasView

fun NavGraphBuilder.gerenciaNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.GerenciaInicio.ruta,
        route = Ruta.GRAFO_GERENCIA
    ) {
        composable(Ruta.GerenciaInicio.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaPanelView(navController, Modifier.padding(padding))
            }
        }
        composable(Ruta.GerenciaStock.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaStockView(Modifier.padding(padding))
            }
        }
        composable(Ruta.GerenciaAlertas.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaAlertasView(navController, Modifier.padding(padding))
            }
        }
        composable(Ruta.GerenciaHistorial.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaHistorialView(Modifier.padding(padding))
            }
        }
        composable(Ruta.GerenciaCuenta.ruta) {
            ConBarraGerencia(navController) { padding ->
                CuentaView(
                    iniciales = "GE",
                    rol = "Gerencia",
                    sucursal = null,
                    onCerrarSesion = {
                        navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable(Ruta.GerenciaGestionTiendas.ruta) {
            ConBarraGerencia(navController) {
                GestionTiendasView(navController)
            }
        }
    }
}
