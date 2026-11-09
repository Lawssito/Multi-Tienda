package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/** Pantallas de Gerencia con su barra inferior (Panel, Stock, Alertas, Historial y Cuenta). */
@Composable
fun ConBarraGerencia(
    navController: NavHostController,
    contenido: @Composable (PaddingValues) -> Unit
) = ConBarraRol(
    navController = navController,
    items = itemsGerencia,
    rutaInicio = Ruta.GerenciaInicio.ruta,
    contenido = contenido
)
