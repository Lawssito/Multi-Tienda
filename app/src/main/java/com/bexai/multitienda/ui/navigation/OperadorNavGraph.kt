package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.ui.view.ProductosView
import com.bexai.multitienda.viewmodel.SesionViewModel

fun NavGraphBuilder.operadorNavGraph(
    navController: NavHostController,
    sesionViewModel: SesionViewModel
) {
    navigation(
        startDestination = Ruta.OperadorInicio.ruta,
        route = Ruta.GRAFO_OPERADOR
    ) {
        composable(Ruta.OperadorInicio.ruta) {
            val sesion by sesionViewModel.uiState.collectAsState()

            ConBarraOperador(navController) { padding ->
                PantallaVacia(
                    titulo = "Hola, ${sesion.usuario?.nombre ?: "operador"}",
                    descripcion = "Sucursal: ${sesion.tienda?.nombre ?: "Sin sucursal"}\n" +
                        "Rol: ${sesion.rol?.name ?: "Sin rol"}",
                    acciones = listOf(
                        AccionPantalla("Ir a productos") {
                            navController.navigate(Ruta.OperadorProductos.ruta) {
                                launchSingleTop = true
                            }
                        }
                    ),
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Ruta.OperadorProductos.ruta) {
            val sesion by sesionViewModel.uiState.collectAsState()
            val tienda = sesion.tienda

            ConBarraOperador(navController) { padding ->
                if (tienda == null) {
                    PantallaVacia(
                        titulo = "Productos",
                        descripcion = "No hay una sucursal seleccionada. Vuelve a iniciar sesión.",
                        acciones = listOf(
                            AccionPantalla("Volver al login") {
                                cerrarSesionYVolverAlLogin(navController, sesionViewModel)
                            }
                        ),
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    ProductosView(
                        ubicacionId = tienda.id,
                        nombreSucursal = tienda.nombre,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        composable(Ruta.OperadorCuenta.ruta) {
            val sesion by sesionViewModel.uiState.collectAsState()

            ConBarraOperador(navController) { padding ->
                PantallaVacia(
                    titulo = "Cuenta",
                    descripcion = "Usuario: ${sesion.usuario?.nombre ?: "Sin usuario"}\n" +
                        "Correo: ${sesion.usuario?.email ?: "-"}\n" +
                        "Rol: ${sesion.rol?.name ?: "Sin rol"}\n" +
                        "Sucursal: ${sesion.tienda?.nombre ?: "Sin sucursal"}",
                    acciones = listOf(
                        AccionPantalla("Cerrar sesión") {
                            cerrarSesionYVolverAlLogin(navController, sesionViewModel)
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
