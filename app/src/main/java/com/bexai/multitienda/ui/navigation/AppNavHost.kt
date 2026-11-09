package com.bexai.multitienda.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import com.bexai.multitienda.ui.view.comun.LoginView

/**
 * AppNavHost: punto de entrada de la navegación. Empieza en Login y, según el rol, entra al
 * grafo del Operador, del Encargado o de Gerencia (cada uno en su propio archivo).
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

        operadorNavGraph(navController)
        encargadoNavGraph(navController)
        gerenciaNavGraph(navController)
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
