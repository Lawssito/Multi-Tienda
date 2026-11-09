package com.bexai.multitienda.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.bexai.multitienda.ui.view.CuentaView
import com.bexai.multitienda.data.SesionSucursal
import com.bexai.multitienda.ui.view.operador.SeleccionarSucursalView
import com.bexai.multitienda.viewmodel.SeleccionarSucursalViewModel
import com.bexai.multitienda.ui.view.encargado.AlertasEncargadoView
import com.bexai.multitienda.ui.view.encargado.DiferenciasStockView
import com.bexai.multitienda.ui.view.encargado.EncargadoHomeView
import com.bexai.multitienda.ui.view.encargado.RevisarRecepcionView
import com.bexai.multitienda.viewmodel.EncargadoViewModel

fun NavGraphBuilder.encargadoNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.EncargadoSucursal.ruta,
        route = Ruta.GRAFO_ENCARGADO
    ) {
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
        composable(Ruta.EncargadoInicio.ruta) {
            ConBarraEncargado(navController) { padding ->
                EncargadoHomeView(
                    modifier = Modifier.padding(padding),
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) }
                )
            }
        }
        composable(Ruta.EncargadoStock.ruta) {
            ConBarraEncargado(navController) { padding ->
                EncargadoHomeView(modifier = Modifier.padding(padding))
            }
        }
        composable(Ruta.EncargadoDiferencias.ruta) {
            ConBarraEncargado(navController) { padding ->
                DiferenciasStockView(
                    modifier = Modifier.padding(padding),
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) }
                )
            }
        }
        composable(
            route = Ruta.EncargadoRevisarRecepcion.patron,
            arguments = listOf(navArgument("recepcionId") { type = NavType.IntType })
        ) { entrada ->
            val recepcionId = entrada.arguments?.getInt("recepcionId") ?: 0
            val viewModel: EncargadoViewModel = viewModel()
            val pendientes by viewModel.recepcionesPendientes.collectAsState()
            val lineas by viewModel.lineasRecepcion(recepcionId).collectAsState(initial = emptyList())
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
        composable(Ruta.EncargadoAlertas.ruta) {
            ConBarraEncargado(navController) { padding ->
                AlertasEncargadoView(
                    navController,
                    Modifier.padding(padding),
                    onCambiarSucursal = { navController.navigate(Ruta.EncargadoSucursal.ruta) }
                )
            }
        }
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
