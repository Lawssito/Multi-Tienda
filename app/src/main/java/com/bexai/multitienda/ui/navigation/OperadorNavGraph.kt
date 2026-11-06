package com.bexai.multitienda.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.ConteoRepository
import com.bexai.multitienda.model.ResumenDia
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.view.AccionPantalla
import com.bexai.multitienda.ui.view.PantallaVacia
import com.bexai.multitienda.ui.components.BannerConfirmacion
import com.bexai.multitienda.ui.view.operador.ConteoView
import com.bexai.multitienda.ui.view.operador.EscanerView
import com.bexai.multitienda.ui.view.operador.InicioOperadorView
import com.bexai.multitienda.ui.view.operador.ResumenConteoView
import com.bexai.multitienda.viewmodel.ConteoViewModel
import com.bexai.multitienda.viewmodel.EscanerViewModel
import com.bexai.multitienda.viewmodel.InicioOperadorViewModel
import kotlinx.coroutines.launch

fun NavGraphBuilder.operadorNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Ruta.OperadorInicio.ruta,
        route = Ruta.GRAFO_OPERADOR
    ) {
        composable(Ruta.OperadorInicio.ruta) { entrada ->
            val viewModel : InicioOperadorViewModel = viewModel()
            val estado by viewModel.uiState.collectAsState()
            // Lo deja el Resumen del conteo al enviar: cuántas diferencias tenía
            val enviado by entrada.savedStateHandle
                .getStateFlow<Int?>("conteoEnviado", null)
                .collectAsState()
            ConBarraOperador(navController) { padding ->
                Box(Modifier.padding(padding)) {
                    InicioOperadorView(
                        sucursal = estado.sucursal,
                        fecha = estado.fecha,
                        resumen = estado.resumen,
                        onMovimiento = { tipo -> navController.navigate(Ruta.OperadorEscaner.conTipo(tipo)) },
                        onConteo = { navController.navigate(Ruta.OperadorConteo.ruta) }
                    )
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
        composable(Ruta.OperadorCuenta.ruta) {
            // TEMPORAL: probar la revisión del conteo sin la pantalla del encargado
            val contexto = LocalContext.current
            val scope = rememberCoroutineScope()
            val conteoRepo = remember { ConteoRepository(AppDatabase.obtener(contexto)) }
            fun revisarUltimoConteo(aprobar: Boolean) {
                scope.launch {
                    val conteo = conteoRepo.ultimoEnviado(1)
                    val ok = conteo != null &&
                        if (aprobar) conteoRepo.aprobar(conteo.id) else conteoRepo.rechazar(conteo.id)
                    val mensaje = when {
                        !ok -> "No hay conteos enviados"
                        aprobar -> "Conteo aprobado"
                        else -> "Conteo rechazado"
                    }
                    Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show()
                }
            }
            ConBarraOperador(navController) { padding ->
                PantallaVacia(
                    titulo = "Cuenta",
                    descripcion = "Datos del usuario y cerrar sesión.",
                    acciones = listOf(
                        AccionPantalla("Aprobar último conteo (prueba)") { revisarUltimoConteo(true) },
                        AccionPantalla("Rechazar último conteo (prueba)") { revisarUltimoConteo(false) },
                        AccionPantalla("Cerrar sesión") {
                            navController.navigate(Ruta.Login.ruta) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    ),
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
                }
            )
        ) { entrada ->
            val args = entrada.arguments
            val tipoInicial = TipoMovimiento.valueOf(args?.getString("tipo") ?: TipoMovimiento.ENTRADA.name)
            val conteoInicial = args?.getBoolean("conteo") ?: false
            // El tipo y el modo Conteo pueden cambiarse desde el chip, por eso son estados locales
            var tipo by rememberSaveable(tipoInicial) { mutableStateOf(tipoInicial) }
            var esConteo by rememberSaveable(conteoInicial) { mutableStateOf(conteoInicial) }
            val escanerViewModel: EscanerViewModel = viewModel()
            val estado by escanerViewModel.uiState.collectAsState()
            EscanerView(
                tipo = tipo,
                esConteo = esConteo,
                sucursal = "Tienda Centro",
                estado = estado,
                onTipoChange = { nuevo, conteo ->
                    tipo = nuevo
                    esConteo = conteo
                    escanerViewModel.reiniciarUltima()
                },
                onCodigo = { codigo -> escanerViewModel.procesarCodigo(codigo, tipo, esConteo) },
                onSinCodigo = { escanerViewModel.avisarSinCodigo() },
                onNuevaBusqueda = { escanerViewModel.limpiarMensaje() },
                onAbrirManual = { escanerViewModel.abrirManual() },
                onCerrarManual = { escanerViewModel.cerrarManual() },
                onBuscarManual = { codigo -> escanerViewModel.buscarCodigoManual(codigo, tipo, esConteo) },
                onEditarManual = { escanerViewModel.limpiarErrorManual() },
                onAbrirHoja = { escanerViewModel.abrirHojaCantidad() },
                onCerrarHoja = { escanerViewModel.cerrarHoja() },
                onDescartarHoja = { escanerViewModel.descartarHoja() },
                onGuardarHoja = { cantidad, motivo -> escanerViewModel.guardarHoja(cantidad, motivo) },
                onCerrar = { navController.popBackStack() },
                onListo = { navController.popBackStack() }
            )
        }
    }
}