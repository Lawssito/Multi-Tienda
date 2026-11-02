package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.LoginView
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.ui.view.ProductosView

/**
 * AppNavHost: contiene el grafo de navegación de toda la aplicación.
 *
 * Tiene tres partes:
 * 1. Login: pantalla inicial (la completa la Persona A).
 * 2. Grafo de operador: pantallas del rol operador (las completa la Persona A).
 * 3. Grafo de gestión: pantallas del rol encargado/gerencia.
 *
 * Por ahora las pantallas son vacías (PantallaVacia) para poder probar que
 * la navegación funciona. Después se reemplazan por las pantallas reales.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta,
        modifier = modifier
    ) {
        // ---------- LOGIN ----------
        composable(Ruta.Login.ruta) {
            LoginView(
                onLoginExitoso = { rol ->
                    val destino = when (rol) {
                        Rol.OPERADOR -> Ruta.GRAFO_OPERADOR
                        Rol.ENCARGADO -> Ruta.GRAFO_ENCARGADO
                        Rol.GERENCIA -> Ruta.GRAFO_GERENCIA
                    }
                    navController.navigate(destino){
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ---------- GRAFO DE OPERADOR ----------
        // Este grafo lo completa la Persona A.
        navigation(
            startDestination = Ruta.OperadorInicio.ruta,
            route = Ruta.GRAFO_OPERADOR
        ) {
            composable(Ruta.OperadorInicio.ruta) {
                PantallaVacia(
                    titulo = "Inicio operador",
                    descripcion = "Grafo de operador. Lo completa la Persona A.",
                    acciones = listOf(
                        AccionPantalla(
                            texto = "Ir a productos",
                            alHacerClic = {
                                navController.navigate(Ruta.OperadorProductos.ruta)
                            }
                        ),
                        AccionPantalla(
                            texto = "Volver al login",
                            alHacerClic = {
                                navController.navigate(Ruta.Login.ruta) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    )
                )
            }

            // Aquí ya se usa la pantalla real de productos como ejemplo funcional
            composable(Ruta.OperadorProductos.ruta) {
                ProductosView(
                    modifier = Modifier.fillMaxSize(),
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---------- GRAFO DE ENCARGADO ----------
        navigation(
            startDestination = Ruta.EncargadoInicio.ruta,
            route = Ruta.GRAFO_ENCARGADO
        )
        {
            composable(Ruta.EncargadoInicio.ruta)
            {
                PantallaVacia(
                    titulo = "Inicio Encargado",
                    descripcion = "Grafo de encargado.",
                    acciones = listOf(
                        AccionPantalla(
                            texto = "Volever al login",
                            alHacerClic = {
                                navController.navigate(Ruta.Login.ruta) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    )
                )
            }
        }

        // ---------- GRAFO DE GERENCIA ----------
        navigation(
            startDestination = Ruta.GerenciaInicio.ruta,
            route = Ruta.GRAFO_GERENCIA
        ) {
            composable(Ruta.GerenciaInicio.ruta) {
                PantallaVacia(
                    titulo = "Inicio gestión",
                    descripcion = "Grafo de gestión. Pantalla vacía de prueba.",
                    acciones = listOf(
                        AccionPantalla(
                            texto = "Ir a reportes",
                            alHacerClic = {
                                navController.navigate(Ruta.GerenciaInicio.ruta)
                            }
                        ),
                        AccionPantalla(
                            texto = "Volver al login",
                            alHacerClic = {
                                navController.navigate(Ruta.Login.ruta) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    )
                )
            }

            composable(Ruta.GerenciaReportes.ruta) {
                PantallaVacia(
                    titulo = "Reportes",
                    descripcion = "Pantalla vacía de reportes para probar la navegación.",
                    acciones = listOf(
                        AccionPantalla(
                            texto = "Volver",
                            alHacerClic = { navController.popBackStack() }
                        )
                    )
                )
            }
        }
    }
}

/**
 * Vista previa del grafo completo. Muestra la pantalla inicial (login).
 */
@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    MultiTiendaTheme {
        AppNavHost()
    }
}
