package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.ui.view.operador.InicioOperadorView
import com.bexai.multitienda.viewmodel.InicioOperadorViewModel

fun NavGraphBuilder.operadorNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.OperadorInicio.ruta,
        route = Ruta.GRAFO_OPERADOR
    ) {
        composable(Ruta.OperadorInicio.ruta) {
            val viewModel : InicioOperadorViewModel = viewModel()
            val estado by viewModel.uiState.collectAsState()
            ConBarraOperador(navController) { padding ->
                InicioOperadorView(
                    sucursal = estado.sucursal,
                    fecha = estado.fecha,
                    resumen = estado.resumen,
                    onMovimiento = {tipo -> viewModel.registrarPrueba(tipo)},
                    onConteo = {},
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable(Ruta.OperadorCuenta.ruta) {
            ConBarraOperador(navController) { padding ->
                PantallaVacia(
                    titulo = "Cuenta",
                    descripcion = "Datos del usuario y cerrar sesión.",
                    acciones = listOf(
                        AccionPantalla("Cerrar sesión") {
                            navController.navigate(Ruta.Login.ruta) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    ),
                    modifier = Modifier.padding(padding)
                )
            }
        }
        // El escáner va sin barra: es pantalla completa en el mockup
        composable(Ruta.OperadorEscaner.ruta) {
            PantallaVacia(
                titulo = "Escáner",
                descripcion = "Aquí irá la cámara.",
                acciones = listOf(
                    AccionPantalla("Cerrar") { navController.popBackStack() }
                )
            )
        }
    }
}