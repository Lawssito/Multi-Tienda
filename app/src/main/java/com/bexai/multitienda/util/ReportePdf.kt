package com.bexai.multitienda.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ReportePdf: genera un PDF simple (título, fecha y una tabla) con [PdfDocument], que trae
 * Android, y lo comparte. Es el único formato de exportación de Gerencia.
 *
 * El archivo se guarda en la caché de la app (carpeta "reportes") y se comparte con
 * FileProvider (ver AndroidManifest.xml y res/xml/file_paths.xml).
 */
object ReportePdf {

    private const val ANCHO = 595      // A4 en puntos
    private const val ALTO = 842
    private const val MARGEN = 36f

    /**
     * @param pesos ancho relativo de cada columna (mismo largo que [encabezados])
     * @return el archivo PDF generado
     * @throws java.io.IOException si no se pudo escribir el archivo en la caché
     */
    fun generar(
        contexto: Context,
        nombreArchivo: String,
        titulo: String,
        subtitulo: String,
        encabezados: List<String>,
        filas: List<List<String>>,
        pesos: List<Float>
    ): File {
        val carpeta = File(contexto.cacheDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "$nombreArchivo.pdf")

        val pTitulo = Paint().apply { textSize = 18f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val pSub = Paint().apply { textSize = 10f; color = 0xFF5B6474.toInt(); isAntiAlias = true }
        val pEnc = Paint().apply { textSize = 10f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val pTexto = Paint().apply { textSize = 10f; isAntiAlias = true }
        val pLinea = Paint().apply { color = 0xFFE2E8F0.toInt(); strokeWidth = 0.8f }

        val anchoUtil = ANCHO - 2 * MARGEN
        val sumaPesos = pesos.sum().coerceAtLeast(0.01f)
        val anchos = pesos.map { anchoUtil * it / sumaPesos }
        val altoFila = 18f

        val pdf = PdfDocument()
        // try/finally: el PdfDocument se cierra aunque falle la escritura (libera la memoria nativa)
        try {
            var numero = 1
            var pagina = pdf.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, numero).create())
            var lienzo = pagina.canvas
            var y = MARGEN + 16f

            fun cabeceraTabla() {
                var x = MARGEN
                encabezados.forEachIndexed { i, texto ->
                    lienzo.drawText(recortar(texto, pEnc, anchos[i] - 6f), x, y, pEnc)
                    x += anchos[i]
                }
                y += 6f
                lienzo.drawLine(MARGEN, y, ANCHO - MARGEN, y, pLinea)
                y += 14f
            }

            lienzo.drawText(titulo, MARGEN, y, pTitulo)
            y += 16f
            val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            lienzo.drawText("$subtitulo · Generado $ahora", MARGEN, y, pSub)
            y += 24f
            cabeceraTabla()

            filas.forEach { fila ->
                if (y + altoFila > ALTO - MARGEN) {
                    pdf.finishPage(pagina)
                    numero++
                    pagina = pdf.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, numero).create())
                    lienzo = pagina.canvas
                    y = MARGEN + 16f
                    cabeceraTabla()
                }
                var x = MARGEN
                fila.forEachIndexed { i, texto ->
                    if (i < anchos.size) {
                        lienzo.drawText(recortar(texto, pTexto, anchos[i] - 6f), x, y, pTexto)
                        x += anchos[i]
                    }
                }
                y += altoFila
            }
            if (filas.isEmpty()) lienzo.drawText("Sin datos para mostrar.", MARGEN, y, pTexto)

            pdf.finishPage(pagina)
            archivo.outputStream().use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
        return archivo
    }

    /** Abre el selector de Android para compartir o guardar el PDF. */
    fun compartir(contexto: Context, archivo: File) {
        try {
            val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            contexto.startActivity(Intent.createChooser(intent, "Exportar PDF"))
        } catch (e: ActivityNotFoundException) {
            avisarError(contexto, "No hay una app para compartir el PDF", e)
        } catch (e: IllegalArgumentException) {
            // FileProvider rechaza la ruta si no coincide con res/xml/file_paths.xml
            avisarError(contexto, "No se pudo compartir el PDF", e)
        }
    }

    /**
     * Genera el PDF y lo comparte. Si algo falla (disco lleno, sin app para compartir…) muestra
     * un aviso en vez de cerrar la app. Es lo que usan las pantallas de Gerencia.
     */
    fun exportar(contexto: Context, generarArchivo: () -> File) {
        val archivo = try {
            generarArchivo()
        } catch (e: Exception) {
            avisarError(contexto, "No se pudo generar el PDF", e)
            return
        }
        compartir(contexto, archivo)
    }

    private fun avisarError(contexto: Context, mensaje: String, error: Exception) {
        Log.e("ReportePdf", mensaje, error)
        Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show()
    }

    /** Acorta el texto con "…" para que entre en [ancho]. */
    private fun recortar(texto: String, paint: Paint, ancho: Float): String {
        if (paint.measureText(texto) <= ancho) return texto
        val cabe = paint.breakText(texto, true, ancho - paint.measureText("…"), null)
        return texto.take(cabe).trimEnd() + "…"
    }
}
