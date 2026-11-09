package com.bexai.multitienda.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.data.SesionSucursal
import com.bexai.multitienda.model.LineaRecepcion
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.view.CuentaView
import com.bexai.multitienda.ui.components.BannerConfirmacion
import com.bexai.multitienda.ui.view.operador.ConteoView
import com.bexai.multitienda.ui.view.operador.EscanerView
import com.bexai.multitienda.ui.view.operador.InicioOperadorView
import com.bexai.multitienda.ui.view.operador.HojaDanadoView
import com.bexai.multitienda.ui.view.operador.RecepcionView
import com.bexai.multitienda.ui.view.operador.ResumenConteoView
import com.bexai.multitienda.ui.view.operador.ResumenRecepcionView
import com.bexai.multitienda.ui.view.operador.SeleccionarSucursalView
import com.bexai.multitienda.viewmodel.ConteoViewModel
import com.bexai.multitienda.viewmodel.EscanerViewModel
import com.bexai.multitienda.viewmodel.InicioOperadorViewModel
import com.bexai.multitienda.viewmodel.RecepcionViewModel
import com.bexai.multitienda.viewmodel.SeleccionarSucursalViewModel

fun NavGraphBuilder.operadorNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.OperadorSucursal.ruta,
        route = Ruta.GRAFO_OPERADOR
    ) {
        composable(Ruta.OperadorSucursal.ruta) {
            val viewModel: SeleccionarSucursalViewModel = viewModel()
            val sucursales by viewModel.sucursales.collectAsState()
            val actual by SesionSucursal.actual.collectAsState()
            SeleccionarSucursalView(
                sucursales = sucursales,
                sucursalActualId = actual.id,
                onVolver = if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else null,
                onElegir = { sucursal ->
                    viewModel.elegir(sucursal)
                    navController.navigate(Ruta.OperadorInicio.ruta) {
                        popUpTo(Ruta.OperadorSucursal.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Ruta.OperadorInicio.ruta) { entrada ->
            val viewModel : InicioOperadorViewModel = viewModel()
            val estado by viewModel.uiState.collectAsState()
            // Lo deja el Resumen del conteo al enviar: cuántas diferencias tenía
            val enviado by entrada.savedStateHandle
                .getStateFlow<Int?>("conteoEnviado", null)
                .collectAsState()
            // Lo deja el Resumen de recepción al enviar: cuántas alertas generó
            val recepcionEnviada by entrada.savedStateHandle
                .getStateFlow<Int?>("recepcionEnviada", null)
                .collectAsState()
            ConBarraOperador(navController) { padding ->
                Box(Modifier.padding(padding)) {
                    InicioOperadorView(
                        sucursal = estado.sucursal,
                        fecha = estado.fecha,
                        resumen = estado.resumen,
                        onMovimiento = { tipo -> navController.navigate(Ruta.OperadorEscaner.conTipo(tipo)) },
                        onConteo = { navController.navigate(Ruta.OperadorConteo.ruta) },
                        onCambiarSucursal = { navController.navigate(Ruta.OperadorSucursal.ruta) }
                    )
                    recepcionEnviada?.let { alertas ->
                        BannerConfirmacion(
                            texto = when (alertas) {
                                0 -> "Recepción enviada · sin alertas"
                                1 -> "Recepción enviada · 1 alerta al encargado"
                                else -> "Recepción enviada · $alertas alertas al encargado"
                            },
                            onCerrar = { entrada.savedStateHandle["recepcionEnviada"] = null },
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                        )
                    }
                    enviado?.let { diferencias ->
                        BannerConfirmacion(
                            texto = when (diferencias) {
                                0 -> "Conteo enviado · sin diferencias"
                                1 -> "Conteo enviado · 1 diferencia"
                                else -> "Conteo enviado · $diferencias diferencias"
                            },
                            onCerrar = { entrada.savedStateHandle["conteoEnviado"] = null },
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                        )
                    }
                }
            }
        }
        composable(Ruta.OperadorConteo.ruta) {
            val conteoViewModel: ConteoViewModel = viewModel()
            val estado by conteoViewModel.uiState.collectAsState()
            ConteoView(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onEscanear = { navController.navigate(Ruta.OperadorEscaner.conTipo(TipoMovimiento.AJUSTE, conteo = true)) },
                onFinalizar = { navController.navigate(Ruta.OperadorResumenConteo.ruta) }
            )
        }
        composable(Ruta.OperadorResumenConteo.ruta) { entrada ->
            // Comparte el ViewModel de la pantalla Conteo (sigue en la pila debajo de esta)
            val conteoEntrada = remember(entrada) { navController.getBackStackEntry(Ruta.OperadorConteo.ruta) }
            val conteoViewModel: ConteoViewModel = viewModel(conteoEntrada)
            val estado by conteoViewModel.uiState.collectAsState()
            ResumenConteoView(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onRecontar = { productoId ->
                    conteoViewModel.recontar(productoId)
                    // Abre el escáner de conteo sin cerrar el Resumen: al terminar vuelves a él
                    navController.navigate(Ruta.OperadorEscaner.conTipo(TipoMovimiento.AJUSTE, conteo = true))
                },
                onEnviar = {
                    conteoViewModel.enviar { diferencias ->
                        navController.getBackStackEntry(Ruta.OperadorInicio.ruta)
                            .savedStateHandle["conteoEnviado"] = diferencias
                        navController.popBackStack(Ruta.OperadorInicio.ruta, inclusive = false)
                    }
                }
            )
        }
        composable(
            route = Ruta.OperadorRecepcion.patron,
            arguments = listOf(navArgument("despachoId") { type = NavType.IntType })
        ) { entrada ->
            val despachoId = entrada.arguments?.getInt("despachoId") ?: 0
            val recepcionViewModel: RecepcionViewModel = viewModel()
            val estado by recepcionViewModel.uiState.collectAsState()
            var lineaDanado by remember { mutableStateOf<LineaRecepcion?>(null) }
            var errorDanado by remember { mutableStateOf<String?>(null) }
            RecepcionView(
                estado = estado,
                onVolver = { navController.popBackStack(Ruta.OperadorInicio.ruta, inclusive = false) },
                onEscanear = { navController.navigate(Ruta.OperadorEscaner.paraRecepcion(estado.recepcionId)) },
                onDanado = { linea -> errorDanado = null; lineaDanado = linea },
                onTerminar = { navController.navigate(Ruta.OperadorResumenRecepcion.conDespacho(despachoId)) }
            )
            lineaDanado?.let { linea ->
                // Se vuelve a buscar la línea para ver sus valores al día
                val actual = estado.lineas.firstOrNull { it.detalleId == linea.detalleId } ?: linea
                HojaDanadoView(
                    linea = actual,
                    codigoDespacho = estado.codigo,
                    error = errorDanado,
                    onCerrar = { lineaDanado = null },
                    onGuardar = { cantidad, motivo, nota ->
                        recepcionViewModel.marcarDanado(actual.detalleId, cantidad, motivo, nota) { error ->
                            errorDanado = error
                            if (error == null) lineaDanado = null
                        }
                    }
                )
            }
        }
        composable(
            route = Ruta.OperadorResumenRecepcion.patron,
            arguments = listOf(navArgument("despachoId") { type = NavType.IntType })
        ) {
            val recepcionViewModel: RecepcionViewModel = viewModel()
            val estado by recepcionViewModel.uiState.collectAsState()
            ResumenRecepcionView(
                estado = estado,
                onVolver = { navController.popBackStack() },
                onEnviar = {
                    recepcionViewModel.enviar { alertas ->
                        navController.getBackStackEntry(Ruta.OperadorInicio.ruta)
                            .savedStateHandle["recepcionEnviada"] = alertas
                        navController.popBackStack(Ruta.OperadorInicio.ruta, inclusive = false)
                    }
                }
            )
        }
        composable(Ruta.OperadorCuenta.ruta) {
            ConBarraOperador(navController) { padding ->
                val sucursalCuenta by SesionSucursal.actual.collectAsState()
                CuentaView(
                    iniciales = "OP",
                    rol = "Operador",
                    sucursal = sucursalCuenta.nombre.ifBlank { null },
                    onCambiarSucursal = { navController.navigate(Ruta.OperadorSucursal.ruta) },
                    onCerrarSesion = {
                        navController.navigate(Ruta.Login.ruta) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable(
            route = Ruta.OperadorEscaner.patron,
            arguments = listOf(
                navArgument("tipo") {
                    type = NavType.StringType
                    defaultValue = TipoMovimiento.ENTRADA.name
                },
                navArgument("conteo") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("recepcion") {
                    type = NavType.IntType
                    defaultValue = 0   // id de la recepción en curso cuando se escanean los productos de una caja
                }
            )
        ) { entrada ->
            val args = entrada.arguments
            val tipoInicial = TipoMovimiento.valueOf(args?.getString("tipo") ?: TipoMovimiento.ENTRADA.name)
            val conteoInicial = args?.getBoolean("conteo") ?: false
            val recepcionId = args?.getInt("recepcion") ?: 0
            val sucursalActiva by SesionSucursal.actual.collectAsState()
            // El tipo y el modo Conteo pueden cambiarse desde el chip, por eso son estados locales
            var tipo by rememberSaveable(tipoInicial) { mutableStateOf(tipoInicial) }
            var esConteo by rememberSaveable(conteoInicial) { mutableStateOf(conteoInicial) }
            // Modo Caja: Entrada donde solo se acepta el QR de una caja (despacho)
            var esCaja by rememberSaveable { mutableStateOf(false) }
            val escanerViewModel: EscanerViewModel = viewModel()
            val estado by escanerViewModel.uiState.collectAsState()
            EscanerView(
                tipo = tipo,
                esConteo = esConteo,
                esRecepcion = recepcionId > 0,
                esCaja = esCaja,
                sucursal = sucursalActiva.nombre,
                estado = estado,
                onTipoChange = { nuevo, conteo ->
                    tipo = nuevo
                    esConteo = conteo
                    esCaja = false
                    escanerViewModel.reiniciarUltima()
                },
                onCajaElegida = {
                    tipo = TipoMovimiento.ENTRADA
                    esConteo = false
                    esCaja = true
                    escanerViewModel.reiniciarUltima()
                },
                onCodigo = { codigo -> escanerViewModel.procesarCodigo(codigo, tipo, esConteo, recepcionId, esCaja) },
                onSinCodigo = { escanerViewModel.avisarSinCodigo() },
                onNuevaBusqueda = { escanerViewModel.limpiarMensaje() },
                onAbrirManual = { escanerViewModel.abrirManual() },
                onCerrarManual = { escanerViewModel.cerrarManual() },
                onBuscarManual = { codigo -> escanerViewModel.buscarCodigoManual(codigo, tipo, esConteo, recepcionId, esCaja) },
                onEditarManual = { escanerViewModel.limpiarErrorManual() },
                onAbrirHoja = { escanerViewModel.abrirHojaCantidad() },
                onCerrarHoja = { escanerViewModel.cerrarHoja() },
                onDescartarHoja = { escanerViewModel.descartarHoja() },
                onGuardarHoja = { cantidad, motivo -> escanerViewModel.guardarHoja(cantidad, motivo) },
                onComenzarCaja = { despachoId ->
                    escanerViewModel.cerrarCaja()
                    // La recepción reemplaza al escáner: al volver desde ella se llega a Inicio
                    navController.navigate(Ruta.OperadorRecepcion.conDespacho(despachoId)) {
                        popUpTo(Ruta.OperadorEscaner.patron) { inclusive = true }
                    }
                },
                onCerrarCaja = { escanerViewModel.cerrarCaja() },
                onCerrar = { navController.popBackStack() },
                onListo = { navController.popBackStack() }
            )
        }
    }
}
