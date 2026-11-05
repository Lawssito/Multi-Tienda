package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.ui.view.operador.EscanerView
import com.bexai.multitienda.ui.view.operador.InicioOperadorView
import com.bexai.multitienda.viewmodel.EscanerViewModel
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
                    onMovimiento = { tipo -> navController.navigate(Ruta.OperadorEscaner.conTipo(tipo)) },
                    onConteo = { navController.navigate(Ruta.OperadorEscaner.conTipo(TipoMovimiento.AJUSTE, conteo = true)) },
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
        composable(
            route = Ruta.OperadorEscaner.patron,
            arguments = listOf(
                navArgument("tipo") {
                    type = NavType.StringType
                    defaultValue = TipoMovimiento.ENTRADA.name
                },
                navArgument("conteo") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entrada ->
            val args = entrada.arguments
            val tipo = TipoMovimiento.valueOf(args?.getString("tipo") ?: TipoMovimiento.ENTRADA.name)
            val esConteo = args?.getBoolean("conteo") ?: false
            val escanerViewModel: EscanerViewModel = viewModel()
            val estado by escanerViewModel.uiState.collectAsState()
            EscanerView(
                tipo = tipo,
                esConteo = esConteo,
                sucursal = "Tienda Centro",
                estado = estado,
                onCodigo = { codigo -> escanerViewModel.procesarCodigo(codigo, tipo) },
                onSinCodigo = { escanerViewModel.avisarSinCodigo() },
                onNuevaBusqueda = { escanerViewModel.limpiarMensaje() },
                onCerrar = { navController.popBackStack() },
                onListo = { navController.popBackStack() }
            )
        }
    }
}