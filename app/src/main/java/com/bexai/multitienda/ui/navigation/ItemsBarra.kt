package com.bexai.multitienda.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Store
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
    ItemNav(Ruta.EncargadoAlertasReposicion.ruta, "Alertas", Icons.Outlined.Notifications)
)

val itemsGerencia = listOf(
    ItemNav(Ruta.GerenciaInicio.ruta, "Dashboard", Icons.Outlined.Dashboard),
    ItemNav(Ruta.GerenciaAuditoria.ruta, "Auditoría", Icons.Outlined.FactCheck),
    ItemNav(Ruta.GerenciaGestionTiendas.ruta, "Tiendas", Icons.Outlined.Store),
    ItemNav(Ruta.GerenciaReportes.ruta, "Reportes", Icons.Outlined.Assessment)
)
