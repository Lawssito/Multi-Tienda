package com.bexai.multitienda.ui.view.gerencia

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.StockSucursal
import com.bexai.multitienda.model.Sucursal
import com.bexai.multitienda.ui.theme.Peligro
import com.bexai.multitienda.ui.theme.TextoPrincipal
import com.bexai.multitienda.util.ReportePdf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun GChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Text(
        text = texto,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (seleccionado) Color.White else TextoPrincipal,
        modifier = Modifier
            .background(if (seleccionado) TextoPrincipal else Color.White, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

/** Botón "Exportar PDF" de las pantallas de Gerencia. */
@Composable
internal fun BotonPdf(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("PDF", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Peligro)
        Text("Exportar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoPrincipal)
    }
}

internal fun nombreCorto(s: Sucursal): String = s.nombre.removePrefix("Sucursal ").trim()

internal fun fechaHora(ms: Long?): String =
    if (ms == null) "—" else SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(ms))

/** Stock de un producto en cada sucursal (en el orden de [tiendas]); 0 si no hay registro. */
internal fun stockPorSucursal(
    producto: Producto,
    stocks: List<StockSucursal>,
    tiendas: List<Sucursal>
): List<Int> = tiendas.map { t ->
    stocks.firstOrNull { it.productoId == producto.id && it.sucursalId == t.id }?.cantidad ?: 0
}

/** PDF del stock global: total, mínimo y una columna por sucursal. */
internal fun exportarStockPdf(
    contexto: Context,
    productos: List<Producto>,
    stocks: List<StockSucursal>,
    tiendas: List<Sucursal>
) {
    val encabezados = listOf("Producto", "Total", "Mín.") + tiendas.map { nombreCorto(it) }
    val filas = productos.map { p ->
        listOf(p.nombre, "${p.stock}", "${p.stockMinimo}") +
            stockPorSucursal(p, stocks, tiendas).map { "$it" }
    }
    val pesos = listOf(4f, 1f, 1f) + tiendas.map { 1.4f }
    ReportePdf.exportar(contexto) {
        ReportePdf.generar(
            contexto, "stock_global", "Stock global", "${productos.size} productos · ${tiendas.size} sucursales",
            encabezados, filas, pesos
        )
    }
}
