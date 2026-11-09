package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.ui.view.comun.CuentaView
import com.bexai.multitienda.ui.view.gerencia.GerenciaAlertasView
import com.bexai.multitienda.ui.view.gerencia.GerenciaHistorialView
import com.bexai.multitienda.ui.view.gerencia.GerenciaPanelView
import com.bexai.multitienda.ui.view.gerencia.GerenciaStockView
import com.bexai.multitienda.ui.view.gerencia.GestionTiendasView

/**
 * Grafo de navegación de Gerencia: Panel, Stock, Alertas, Historial, Cuenta y Tiendas
 * habilitadas. Todas las pantallas van dentro de ConBarraGerencia.
 */
fun NavGraphBuilder.gerenciaNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.GerenciaInicio.ruta,
        route = Ruta.GRAFO_GERENCIA
    ) {
        // ---------- Panel ----------
        composable(Ruta.GerenciaInicio.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaPanelView(navController, Modifier.padding(padding))
            }
        }
        // ---------- Stock global ----------
        composable(Ruta.GerenciaStock.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaStockView(Modifier.padding(padding))
            }
        }
        // ---------- Alertas ----------
        composable(Ruta.GerenciaAlertas.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaAlertasView(navController, Modifier.padding(padding))
            }
        }
        // ---------- Historial ----------
        composable(Ruta.GerenciaHistorial.ruta) {
            ConBarraGerencia(navController) { padding ->
                GerenciaHistorialView(Modifier.padding(padding))
            }
        }
        // ---------- Cuenta ----------
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
        // ---------- Tiendas habilitadas ----------
        composable(Ruta.GerenciaGestionTiendas.ruta) {
            ConBarraGerencia(navController) {
                GestionTiendasView(navController)
            }
        }
    }
}
