package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/** Pantallas del operador con su barra inferior (Inicio, Escanear al centro y Cuenta). */
@Composable
fun ConBarraOperador(
    navController: NavHostController,
    contenido: @Composable (PaddingValues) -> Unit
) = ConBarraRol(
    navController = navController,
    items = itemsOperador,
    rutaInicio = Ruta.OperadorInicio.ruta,
    central = centralOperador,
    contenido = contenido
)
