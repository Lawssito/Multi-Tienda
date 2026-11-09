package com.bexai.multitienda.ui.view.operador.escaner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Analiza los cuadros de la cámara, pero solo cuando [pedirLectura] está en true
 * (es decir, después de pulsar el botón de escaneo). Al encontrar un código lo
 * entrega en [onCodigo] y vuelve a dejar [pedirLectura] en false.
 */
class LectorCodigos(
    private val pedirLectura: AtomicBoolean,
    private val onCodigo: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val escaner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE
            )
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val imagenCamara = imageProxy.image
        if (!pedirLectura.get() || imagenCamara == null) {
            imageProxy.close()
            return
        }

        val imagen = InputImage.fromMediaImage(imagenCamara, imageProxy.imageInfo.rotationDegrees)
        escaner.process(imagen)
            .addOnSuccessListener { codigos ->
                val valor = codigos.firstNotNullOfOrNull { it.rawValue }
                if (valor != null && pedirLectura.compareAndSet(true, false)) {
                    onCodigo(valor)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
