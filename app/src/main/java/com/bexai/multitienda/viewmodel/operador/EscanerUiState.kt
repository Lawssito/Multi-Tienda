package com.bexai.multitienda.viewmodel.operador

import com.bexai.multitienda.model.TipoMovimiento

// =====================================================================
// Estado de la pantalla Escáner (lo produce EscanerViewModel y lo dibuja EscanerView).
// Se separó del ViewModel para que el archivo de lógica quede más corto y fácil de leer.
// =====================================================================

/**
 * Lo último escaneado. En Entrada/Salida el mismo producto repetido acumula la
 * cantidad (x N) sobre un único Movimiento. En Ajuste, [cantidad] es el total real.
 */
data class UltimaLectura(
    val productoId: Int,
    val nombre: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val stockAntes: Int,
    val stockDespues: Int,
    val movimientoId: Int,          // en modo Conteo no se usa (0)
    val motivo: String? = null,
    val esConteo: Boolean = false,  // en conteo, cantidad = contado y stockAntes = sistema
    val baseConteo: Int = 0,        // contado que había antes de esta tanda de lecturas
    val detalleRecepcion: String? = null, // en recepción de caja: "Recibidas 24 de 24"
    val recepcionId: Int = 0              // recepción en curso (solo en recepción de caja)
)

/** Datos de la hoja inferior (Cambiar cantidad o Ajuste). */
data class HojaCantidad(
    val esAjuste: Boolean,
    val productoId: Int,
    val nombre: String,
    val tipo: TipoMovimiento,
    val stockBase: Int,            // stock antes de este movimiento
    val cantidadInicial: Int,
    val motivoInicial: String = "",
    val movimientoId: Int? = null, // si ya existe, la hoja lo edita en vez de crear otro
    val esConteo: Boolean = false, // edita lo contado de un producto en el conteo en curso
    val baseConteo: Int = 0,       // a esto vuelve lo contado (o lo recibido) si se descarta
    val esRecepcion: Boolean = false, // edita lo recibido de un producto en la recepción de una caja
    val recepcionId: Int = 0
)

/** Caja (despacho) detectada al leer su QR en Entrada. */
data class CajaDetectada(
    val despachoId: Int,
    val codigo: String,
    val proveedor: String,
    val guia: String,
    val productos: Int,
    val unidades: Int
)

data class EscanerUiState(
    val lecturas: Int = 0,
    val ultima: UltimaLectura? = null,
    val mensaje: String? = null,
    val hoja: HojaCantidad? = null,
    val manualAbierto: Boolean = false,   // hoja "Código manual"
    val errorManual: String? = null,
    val caja: CajaDetectada? = null       // hoja "Caja detectada"
)
