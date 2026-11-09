package com.bexai.multitienda.ui.view.operador.escaner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.theme.BarraFondo
import com.bexai.multitienda.viewmodel.operador.EscanerUiState
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Pantalla Escáner del operador. Pide el permiso de cámara, muestra la cámara con la barra
 * superior y el panel inferior, y abre las hojas (Código manual, Caja detectada y Cambiar
 * cantidad/Ajuste). Toda la lógica vive en EscanerViewModel; aquí solo se dibuja el estado.
 * Las piezas visuales están en CamaraEscaner.kt y PanelEscaner.kt.
 */
@Composable
fun EscanerView(
    tipo: TipoMovimiento,
    esConteo: Boolean,
    esRecepcion: Boolean,
    esCaja: Boolean,
    sucursal: String,
    estado: EscanerUiState,
    onTipoChange: (TipoMovimiento, Boolean) -> Unit,
    onCajaElegida: () -> Unit,
    onCodigo: (String) -> Unit,
    onSinCodigo: () -> Unit,
    onNuevaBusqueda: () -> Unit,
    onAbrirManual: () -> Unit,
    onCerrarManual: () -> Unit,
    onBuscarManual: (String) -> Unit,
    onEditarManual: () -> Unit,
    onAbrirHoja: () -> Unit,
    onCerrarHoja: () -> Unit,
    onDescartarHoja: () -> Unit,
    onGuardarHoja: (Int, String) -> Unit,
    onComenzarCaja: (Int) -> Unit,
    onCerrarCaja: () -> Unit,
    onCerrar: () -> Unit,
    onListo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var rechazado by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        rechazado = !concedido
    }
    LaunchedEffect(Unit) {
        if (!permisoConcedido) launcher.launch(Manifest.permission.CAMERA)
    }

    // Al volver de Ajustes, revisa si el usuario dio el permiso
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    val abrirAjustes: () -> Unit = {
        try {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
            )
        } catch (e: ActivityNotFoundException) {
            // Algunos equipos no tienen la pantalla de ajustes de la app
            Log.w("EscanerView", "No se pudo abrir Ajustes", e)
        }
    }

    var linterna by remember { mutableStateOf(false) }

    // Lectura de códigos
    val pedirLectura = remember { AtomicBoolean(false) }
    var buscando by remember { mutableStateOf(false) }

    // Límite de búsqueda: si en 5 segundos no hay código, se detiene y se avisa
    LaunchedEffect(buscando) {
        if (buscando) {
            delay(5_000)
            if (pedirLectura.compareAndSet(true, false)) onSinCodigo()
            buscando = false
        }
    }

    Column(modifier.fillMaxSize().background(BarraFondo)) {

        // Zona de cámara: ocupa todo lo que sobra arriba del panel
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (permisoConcedido) {
                VistaCamara(
                    linterna = linterna,
                    pedirLectura = pedirLectura,
                    onCodigo = { codigo ->
                        buscando = false
                        onCodigo(codigo)
                    },
                    modifier = Modifier.fillMaxSize()
                )
                MarcoVisor(Modifier.align(Alignment.Center))
            } else {
                SinPermiso(
                    rechazado = rechazado,
                    onReintentar = { launcher.launch(Manifest.permission.CAMERA) },
                    onAbrirAjustes = abrirAjustes
                )
            }

            BarraSuperiorEscaner(
                sucursal = sucursal,
                tipo = tipo,
                esConteo = esConteo,
                esRecepcion = esRecepcion,
                esCaja = esCaja,
                onTipoChange = onTipoChange,
                onCajaElegida = onCajaElegida,
                onCerrar = onCerrar,
                onListo = onListo,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Panel: solo si hay cámara
        if (permisoConcedido) {
            PanelEscaner(
                estado = estado,
                buscando = buscando,
                linterna = linterna,
                onManual = onAbrirManual,
                onDisparo = {
                    onNuevaBusqueda()
                    pedirLectura.set(true)
                    buscando = true
                },
                onFlash = { linterna = !linterna },
                onCambiarCantidad = onAbrirHoja
            )
        }
    }

    // Hoja inferior: Código manual
    if (estado.manualAbierto) {
        HojaCodigoManualView(
            error = estado.errorManual,
            permiteCaja = (tipo == TipoMovimiento.ENTRADA || esCaja) && !esConteo && !esRecepcion,
            onCerrar = onCerrarManual,
            onBuscar = onBuscarManual,
            onEditar = onEditarManual
        )
    }

    // Hoja inferior: Caja detectada (QR de un despacho leído en Entrada)
    estado.caja?.let { caja ->
        HojaCajaDetectadaView(
            caja = caja,
            onComenzar = { onComenzarCaja(caja.despachoId) },
            onCerrar = onCerrarCaja
        )
    }

    // Hoja inferior: Cambiar cantidad o Ajuste
    estado.hoja?.let { hoja ->
        HojaCantidadView(
            hoja = hoja,
            onCerrar = onCerrarHoja,
            onDescartar = onDescartarHoja,
            onGuardar = onGuardarHoja
        )
    }
}
