package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.ui.view.operador.InicioOperadorView

fun NavGraphBuilder.operadorNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.OperadorInicio.ruta,
        route = Ruta.GRAFO_OPERADOR
    ) {
        composable(Ruta.OperadorInicio.ruta) {
            ConBarraOperador(navController) { padding ->
                InicioOperadorView(
                    sucursal = "Tienda Centro",
                    fecha = "29 sept",
                    resumen = ResumenDia(entradas = 214, salidas = 96, movimientos = 24, contados = 120),
                    onMovimiento = {},
                    onConteoMasivo = {},
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