package com.bexai.multitienda.ui.view.operador.escaner

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bexai.multitienda.ui.theme.Azul
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

// =====================================================================
// Cámara del escáner: vista previa con CameraX, lectura de códigos con ML Kit, marco del visor
// y el aviso cuando falta el permiso. La usa EscanerView.
// =====================================================================

/**
 * Vista previa de la cámara trasera con análisis de imagen. Solo entrega un código cuando
 * [pedirLectura] está en true (el operador tocó el botón de disparo).
 */
@Composable
internal fun VistaCamara(
    linterna: Boolean,
    pedirLectura: AtomicBoolean,
    onCodigo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    // El factory solo corre una vez; así siempre llama a la versión más reciente del callback
    val onCodigoActual by rememberUpdatedState(onCodigo)
    // Un solo hilo para analizar imágenes; se cierra al salir de la pantalla (antes quedaba vivo)
    val analizador = remember { Executors.newSingleThreadExecutor() }
    val analisisActivo = remember { AtomicReference<ImageAnalysis?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            // Primero se deja de enviar imágenes al hilo y luego se cierra
            analisisActivo.get()?.clearAnalyzer()
            analizador.shutdown()
        }
    }

    LaunchedEffect(camera, linterna) {
        camera?.cameraControl?.enableTorch(linterna)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val futuro = ProcessCameraProvider.getInstance(ctx)
            futuro.addListener({
                // Si el equipo no tiene cámara trasera o CameraX no inicia, se registra el error
                // y la pantalla sigue funcionando con el Código manual (antes la app se cerraba).
                try {
                    val provider = futuro.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analisis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(analizador, LectorCodigos(pedirLectura) { onCodigoActual(it) })
                        }
                    analisisActivo.set(analisis)
                    provider.unbindAll()
                    camera = provider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis
                    )
                } catch (e: Exception) {
                    Log.e("VistaCamara", "No se pudo iniciar la cámara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

/** Esquinas azules que marcan dónde poner el código. */
@Composable
internal fun MarcoVisor(modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = 300.dp, height = 230.dp)) {
        val largo = 44.dp.toPx()
        val grosor = 4.dp.toPx()
        val w = size.width
        val h = size.height

        fun linea(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(Azul, Offset(x1, y1), Offset(x2, y2), grosor, StrokeCap.Round)

        linea(0f, 0f, largo, 0f);  linea(0f, 0f, 0f, largo)       // arriba izquierda
        linea(w, 0f, w - largo, 0f);  linea(w, 0f, w, largo)      // arriba derecha
        linea(0f, h, largo, h);  linea(0f, h, 0f, h - largo)      // abajo izquierda
        linea(w, h, w - largo, h);  linea(w, h, w, h - largo)     // abajo derecha
    }
}

/** Se muestra en lugar de la cámara cuando falta el permiso. */
@Composable
internal fun SinPermiso(
    rechazado: Boolean,
    onReintentar: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Se necesita la cámara para escanear códigos",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReintentar) { Text("Permitir cámara") }
        if (rechazado) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAbrirAjustes) { Text("Abrir ajustes") }
        }
    }
}
