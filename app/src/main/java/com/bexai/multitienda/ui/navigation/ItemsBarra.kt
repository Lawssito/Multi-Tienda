package com.bexai.multitienda.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import com.bexai.multitienda.ui.components.ItemNav

val itemsOperador = listOf(
    ItemNav(Ruta.OperadorInicio.ruta, "Inicio", Icons.Outlined.Home),
    ItemNav(Ruta.OperadorCuenta.ruta, "Cuenta", Icons.Outlined.Person)
)
val centralOperador = ItemNav(
    Ruta.OperadorEscaner.ruta, "Escanear", Icons.Outlined.QrCodeScanner)