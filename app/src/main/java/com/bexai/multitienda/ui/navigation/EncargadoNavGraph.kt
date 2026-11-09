package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.bexai.multitienda.data.sesion.SesionSucursal
import com.bexai.multitienda.ui.view.comun.CuentaView
import com.bexai.multitienda.ui.view.encargado.AlertasEncargadoView
import com.bexai.multitienda.ui.view.encargado.DiferenciasStockView
import com.bexai.multitienda.ui.view.encargado.EncargadoHomeView
import com.bexai.multitienda.ui.view.encargado.RevisarRecepcionView
import com.bexai.multitienda.ui.view.operador.inicio.SeleccionarSucursalView
import com.bexai.multitienda.viewmodel.encargado.EncargadoViewModel
import com.bexai.multitienda.viewmodel.operador.SeleccionarSucursalViewModel

/**
 * Grafo de navegación del Encargado: elegir sucursal, Stock, Diferencias, Alertas, revisar una
 * recepción de caja y Cuenta. Las pestañas principales van dentro de ConBarraEncargado.
 */
fun NavGraphBuilder.encargadoNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.EncargadoSucursal.ruta,
        route = Ruta.GRAFO_ENCARGADO
    ) {
        // ---------- Elegir sucursal ----------
        composable(Ruta.EncargadoSucursal.ruta) {
            val viewModel: SeleccionarSucursalViewModel = viewModel()
            val sucursales by viewModel.sucursales.collectAsState()
            val actual by SesionSucursal.actual.collectAsState()
            SeleccionarSucursalView(
                sucursales = sucursales,
                sucursalActualId = actual.id,
                onVolver = if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else null,
                onElegir = { sucursal ->
                    viewModel.elegir(sucursal)
                    navController.navigate(Ruta.EncargadoInicio.ruta) {
                        popUpTo(Ruta.EncargadoSucursal.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        // ---------- Stock (pantalla de inicio del encargado) ----------
        composable(Ruta.EncargadoInicio.ruta) {
            ConBarraEncargado(navController) { padding ->
                EncargadoHomeView(
                    modifier = Modifier.padding(padding),
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) }
                )
            }
        }
        // ---------- Diferencias del último conteo ----------
        composable(Ruta.EncargadoDiferencias.ruta) {
            ConBarraEncargado(navController) { padding ->
                DiferenciasStockView(
                    modifier = Modifier.padding(padding),
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) }
                )
            }
        }
        // ---------- Revisar una recepción de caja ----------
        composable(
            route = Ruta.EncargadoRevisarRecepcion.patron,
            arguments = listOf(navArgument("recepcionId") { type = NavType.IntType })
        ) { entrada ->
            val recepcionId = entrada.arguments?.getInt("recepcionId") ?: 0
            val viewModel: EncargadoViewModel = viewModel()
            val pendientes by viewModel.recepcionesPendientes.collectAsState()
            // remember: sin él se creaba un Flow nuevo (y una consulta nueva) en cada recomposición
            val lineas by remember(recepcionId) { viewModel.lineasRecepcion(recepcionId) }
                .collectAsState(initial = emptyList())
            RevisarRecepcionView(
                recepcion = pendientes.firstOrNull { it.id == recepcionId },
                lineas = lineas,
                onVolver = { navController.popBackStack() },
                onDecidir = { detalleId, decision -> viewModel.decidirLinea(detalleId, decision) },
                onMarcarRevisada = {
                    viewModel.marcarRecepcionRevisada(recepcionId) { ok -> if (ok) navController.popBackStack() }
                }
            )
        }
        // ---------- Alertas ----------
        composable(Ruta.EncargadoAlertas.ruta) {
            ConBarraEncargado(navController) { padding ->
                AlertasEncargadoView(
                    navController,
                    Modifier.padding(padding),
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) }
                )
            }
        }
        // ---------- Cuenta ----------
        composable(Ruta.EncargadoCuenta.ruta) {
            val viewModel: EncargadoViewModel = viewModel()
            val sucursales by viewModel.sucursales.collectAsState()
            val selId by viewModel.sucursalSeleccionadaId.collectAsState()
            ConBarraEncargado(navController) { padding ->
                CuentaView(
                    iniciales = "EN",
                    rol = "Encargado",
                    sucursal = sucursales.firstOrNull { it.id == selId }?.nombre,
                    onCerrarSesion = {
                        navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                    },
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
