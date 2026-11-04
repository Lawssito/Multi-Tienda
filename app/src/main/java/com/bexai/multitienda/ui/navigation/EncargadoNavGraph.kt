package com.bexai.multitienda.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.viewmodel.SesionViewModel

fun NavGraphBuilder.encargadoNavGraph(
    navController: NavHostController,
    sesionViewModel: SesionViewModel
) {
    navigation(
        startDestination = Ruta.EncargadoInicio.ruta,
        route = Ruta.GRAFO_ENCARGADO
    ) {
        composable(Ruta.EncargadoInicio.ruta) {
            val sesion by sesionViewModel.uiState.collectAsState()

            PantallaVacia(
                titulo = "Inicio encargado",
                descripcion = "Usuario: ${sesion.usuario?.nombre ?: "Sin usuario"}\n" +
                    "Sucursal: ${sesion.tienda?.nombre ?: "Sin sucursal"}",
                acciones = listOf(
                    AccionPantalla("Cerrar sesión") {
                        cerrarSesionYVolverAlLogin(navController, sesionViewModel)
                    }
                )
            )
        }
    }
}
