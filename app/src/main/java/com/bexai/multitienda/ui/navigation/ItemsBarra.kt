package com.bexai.multitienda.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import com.bexai.multitienda.ui.components.ItemNav

val itemsOperador = listOf(
    ItemNav(Ruta.OperadorInicio.ruta, "Inicio", Icons.Outlined.Home),
    ItemNav(Ruta.OperadorCuenta.ruta, "Cuenta", Icons.Outlined.Person)
)
val centralOperador = ItemNav(
    Ruta.OperadorEscaner.ruta, "Escanear", Icons.Outlined.QrCodeScanner
)

val itemsEncargado = listOf(
    ItemNav(Ruta.EncargadoInicio.ruta, "Stock", Icons.Outlined.Inventory2),
    ItemNav(Ruta.EncargadoDiferencias.ruta, "Diferencias", Icons.Outlined.CompareArrows),
    ItemNav(Ruta.EncargadoAlertas.ruta, "Alertas", Icons.Outlined.Notifications),
    ItemNav(Ruta.EncargadoCuenta.ruta, "Cuenta", Icons.Outlined.Person)
)

val itemsGerencia = listOf(
    ItemNav(Ruta.GerenciaInicio.ruta, "Panel", Icons.Outlined.Dashboard),
    ItemNav(Ruta.GerenciaStock.ruta, "Stock", Icons.Outlined.Inventory2),
    ItemNav(Ruta.GerenciaAlertas.ruta, "Alertas", Icons.Outlined.Notifications),
    ItemNav(Ruta.GerenciaHistorial.ruta, "Historial", Icons.Outlined.History),
    ItemNav(Ruta.GerenciaCuenta.ruta, "Cuenta", Icons.Outlined.Person)
)
