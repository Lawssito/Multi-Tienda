package com.bexai.multitienda.ui.view.operador

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bexai.multitienda.model.TipoMovimiento
import com.bexai.multitienda.ui.theme.Advertencia
import com.bexai.multitienda.ui.theme.AdvertenciaSuave
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.AzulOscuro
import com.bexai.multitienda.ui.theme.AzulSuave
import com.bexai.multitienda.ui.theme.BarraFondo
import com.bexai.multitienda.ui.theme.Celeste
import com.bexai.multitienda.ui.theme.GrisBarra
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.PeligroSuave
import com.bexai.multitienda.ui.theme.SuperficieVariante
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.ui.theme.TextoSecundario

@Composable
fun EscanerView(
    tipo: TipoMovimiento,
    sucursal: String,
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

    val abrirAjustes = {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        )
    }

    var linterna by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().background(BarraFondo)) {

        // Zona de cámara: ocupa todo lo que sobra arriba del panel
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (permisoConcedido) {
                VistaCamara(linterna = linterna, modifier = Modifier.fillMaxSize())
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
                onCerrar = onCerrar,
                onListo = onListo,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Panel: solo si hay cámara
        if (permisoConcedido) {
            PanelEscaner(
                lecturas = 0,
                linterna = linterna,
                onManual = { /* pendiente */ },
                onDisparo = { /* pendiente: ML Kit */ },
                onFlash = { linterna = !linterna }
            )
        }
    }
}

@Composable
private fun VistaCamara(linterna: Boolean, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(camera, linterna) {
        camera?.cameraControl?.enableTorch(linterna)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val futuro = ProcessCameraProvider.getInstance(ctx)
            futuro.addListener({
                val provider = futuro.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@Composable
private fun MarcoVisor(modifier: Modifier = Modifier) {
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

@Composable
private fun BarraSuperiorEscaner(
    sucursal: String,
    tipo: TipoMovimiento,
    onCerrar: () -> Unit,
    onListo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCerrar,
            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sucursal, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            ChipTipo(tipo)
        }

        Button(
            onClick = onListo,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextoPrincipal)
        ) { Text("Listo", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ChipTipo(tipo: TipoMovimiento) {
    val (texto, color, fondo) = when (tipo) {
        TipoMovimiento.ENTRADA -> Triple("Entrada", AzulOscuro, AzulSuave)
        TipoMovimiento.SALIDA -> Triple("Salida", Peligro, PeligroSuave)
        TipoMovimiento.AJUSTE -> Triple("Ajuste", Advertencia, AdvertenciaSuave)
    }
    Row(
        modifier = Modifier
            .background(fondo, CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = color, fontWeight = FontWeight.Bold)
        Icon(Icons.Default.KeyboardArrowDown, null, tint = color, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PanelEscaner(
    lecturas: Int,
    linterna: Boolean,
    onManual: () -> Unit,
    onDisparo: () -> Unit,
    onFlash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            // Mismo margen inferior que la barra del operador, para que los botones queden a su altura
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Escanear código", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                Text(
                    if (lecturas == 0) "Sin lecturas" else "$lecturas lecturas",
                    color = TextoSecundario
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AccionPanel(
                    etiqueta = "Código manual",
                    icono = Icons.Outlined.Keyboard,
                    colorIcono = Color(0xFF7C3AED),
                    fondoIcono = Color(0xFFEDE9FE),
                    onClick = onManual,
                    modifier = Modifier.weight(1f)
                )
                BotonDisparo(onClick = onDisparo, modifier = Modifier.weight(1f))
                AccionPanel(
                    etiqueta = "Flash",
                    icono = if (linterna) Icons.Filled.FlashOn else Icons.Outlined.FlashOn,
                    colorIcono = if (linterna) Color.White else TextoPrincipal,
                    fondoIcono = if (linterna) Azul else SuperficieVariante,
                    onClick = onFlash,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AccionPanel(
    etiqueta: String,
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(52.dp).background(fondoIcono, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icono, null, tint = colorIcono) }
        Text(etiqueta, fontWeight = FontWeight.Medium, color = TextoPrincipal)
    }
}

@Composable
private fun BotonDisparo(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .border(6.dp, GrisBarra, CircleShape)
                .padding(6.dp)
                .background(Celeste, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CropFree, null, tint = BarraFondo, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun SinPermiso(
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
