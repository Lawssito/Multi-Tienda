package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.ConteoDetalle
import com.bexai.multitienda.model.Movimiento
import com.bexai.multitienda.model.Producto
import com.bexai.multitienda.model.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val baseConteo: Int = 0         // contado que había antes de esta tanda de lecturas
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
    val baseConteo: Int = 0        // a esto vuelve lo contado si se descarta
)

data class EscanerUiState(
    val lecturas: Int = 0,
    val ultima: UltimaLectura? = null,
    val mensaje: String? = null,
    val hoja: HojaCantidad? = null
)

class EscanerViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val ubicacionId = 1   // temporal: luego saldrá del usuario logueado

    private val _uiState = MutableStateFlow(EscanerUiState())
    val uiState: StateFlow<EscanerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { sembrarProductosDemo() }
    }

    fun procesarCodigo(codigo: String, tipo: TipoMovimiento, esConteo: Boolean) {
        if (_uiState.value.hoja != null) return   // con la hoja abierta no se leen códigos
        if (esConteo) {
            contarProducto(codigo)
            return
        }

        viewModelScope.launch {
            val producto = db.productoDao().buscarPorCodigo(codigo)
            if (producto == null) {
                avisar("Producto no encontrado: $codigo")
                return@launch
            }

            // Ajuste: se abre la hoja para escribir el total real y el motivo
            if (tipo == TipoMovimiento.AJUSTE) {
                _uiState.update {
                    it.copy(
                        mensaje = null,
                        hoja = HojaCantidad(
                            esAjuste = true,
                            productoId = producto.id,
                            nombre = producto.nombre,
                            tipo = tipo,
                            stockBase = producto.stock,
                            cantidadInicial = producto.stock
                        )
                    )
                }
                return@launch
            }

            // Entrada / Salida: cada lectura mueve 1 unidad
            val delta = if (tipo == TipoMovimiento.ENTRADA) 1 else -1
            if (producto.stock + delta < 0) {
                avisar("Sin stock de ${producto.nombre}")
                return@launch
            }
            val stockNuevo = producto.stock + delta

            val previa = _uiState.value.ultima
            val ultima = if (previa != null && previa.productoId == producto.id && previa.tipo == tipo) {
                // Mismo producto: se suma al movimiento que ya existe
                val cantidad = previa.cantidad + 1
                db.withTransaction {
                    db.movimientoDao().actualizarCantidad(previa.movimientoId, cantidad)
                    db.productoDao().sumarStock(producto.id, delta)
                }
                previa.copy(cantidad = cantidad, stockDespues = stockNuevo)
            } else {
                val movimientoId = db.withTransaction {
                    val id = db.movimientoDao().insertar(
                        Movimiento(
                            productoId = producto.id,
                            ubicacionId = ubicacionId,
                            tipo = tipo,
                            cantidad = 1,
                            fechaHora = System.currentTimeMillis()
                        )
                    ).toInt()
                    db.productoDao().sumarStock(producto.id, delta)
                    id
                }
                UltimaLectura(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    tipo = tipo,
                    cantidad = 1,
                    stockAntes = producto.stock,
                    stockDespues = stockNuevo,
                    movimientoId = movimientoId
                )
            }

            _uiState.update {
                it.copy(lecturas = it.lecturas + 1, ultima = ultima, mensaje = null)
            }
        }
    }

    /** Al tocar la tarjeta: abre la hoja para editar lo último escaneado. */
    fun abrirHojaCantidad() {
        val ultima = _uiState.value.ultima ?: return
        _uiState.update {
            it.copy(
                hoja = HojaCantidad(
                    esAjuste = ultima.tipo == TipoMovimiento.AJUSTE && !ultima.esConteo,
                    productoId = ultima.productoId,
                    nombre = ultima.nombre,
                    tipo = ultima.tipo,
                    stockBase = ultima.stockAntes,
                    cantidadInicial = ultima.cantidad,
                    motivoInicial = ultima.motivo ?: "",
                    movimientoId = ultima.movimientoId,
                    esConteo = ultima.esConteo,
                    baseConteo = ultima.baseConteo
                )
            )
        }
    }

    fun cerrarHoja() = _uiState.update { it.copy(hoja = null) }

    fun guardarHoja(cantidad: Int, motivo: String) {
        val hoja = _uiState.value.hoja ?: return
        viewModelScope.launch {
            when {
                hoja.esConteo -> guardarConteo(hoja, cantidad)
                hoja.esAjuste -> guardarAjuste(hoja, cantidad, motivo.trim())
                else -> guardarCambioCantidad(hoja, cantidad)
            }
        }
    }

    /**
     * Botón "Descartar" de la hoja: deshace lo escaneado.
     * Borra el movimiento (si ya existía) y devuelve el stock al valor de antes.
     */
    fun descartarHoja() {
        val hoja = _uiState.value.hoja ?: return
        viewModelScope.launch {
            if (hoja.esConteo) {
                descartarLineaConteo(hoja)
                return@launch
            }
            val movimientoId = hoja.movimientoId
            if (movimientoId != null) {
                db.withTransaction {
                    db.movimientoDao().eliminar(movimientoId)
                    db.productoDao().fijarStock(hoja.productoId, hoja.stockBase)
                }
            }
            _uiState.update {
                // Entrada/Salida suman 1 lectura por escaneo; un ajuste guardado cuenta 1
                val quitar = when {
                    movimientoId == null -> 0
                    hoja.esAjuste -> 1
                    else -> it.ultima?.cantidad ?: 0
                }
                it.copy(
                    lecturas = (it.lecturas - quitar).coerceAtLeast(0),
                    ultima = if (movimientoId != null) null else it.ultima,
                    hoja = null
                )
            }
        }
    }

    // ---------- Conteo: suma a la lista del conteo en curso, NO toca el stock ----------

    private fun contarProducto(codigo: String) {
        viewModelScope.launch {
            val producto = db.productoDao().buscarPorCodigo(codigo)
            if (producto == null) {
                avisar("Producto no encontrado: $codigo")
                return@launch
            }
            val conteoDao = db.conteoDao()
            val conteo = conteoDao.enCurso(ubicacionId)
            if (conteo == null) {
                avisar("No hay un conteo en curso. Vuelve e inicia el conteo.")
                return@launch
            }

            // El stock del sistema se congeló al iniciar el conteo. Si el producto es nuevo
            // (se creó después de iniciar), se toma su stock actual.
            val previo = conteoDao.buscarDetalle(conteo.id, producto.id)
            val contado = (previo?.contado ?: 0) + 1
            val sistema = previo?.sistema ?: producto.stock
            // Una "tanda" son las lecturas seguidas del mismo producto. Descartar deshace solo la tanda.
            val anterior = _uiState.value.ultima
            val base = if (anterior != null && anterior.esConteo && anterior.productoId == producto.id) {
                anterior.baseConteo
            } else {
                previo?.contado ?: 0
            }
            conteoDao.guardarDetalle(
                ConteoDetalle(
                    id = previo?.id ?: 0,
                    conteoId = conteo.id,
                    productoId = producto.id,
                    contado = contado,
                    sistema = sistema,
                    actualizado = System.currentTimeMillis()
                )
            )

            _uiState.update {
                it.copy(
                    lecturas = it.lecturas + 1,
                    ultima = UltimaLectura(
                        productoId = producto.id,
                        nombre = producto.nombre,
                        tipo = TipoMovimiento.AJUSTE,
                        cantidad = contado,
                        stockAntes = sistema,
                        stockDespues = contado,
                        movimientoId = 0,
                        esConteo = true,
                        baseConteo = base
                    ),
                    mensaje = null
                )
            }
        }
    }

    private suspend fun guardarConteo(hoja: HojaCantidad, contado: Int) {
        if (contado < 0) return
        val conteoDao = db.conteoDao()
        val conteo = conteoDao.enCurso(ubicacionId) ?: return
        val detalle = conteoDao.buscarDetalle(conteo.id, hoja.productoId) ?: return
        conteoDao.guardarDetalle(detalle.copy(contado = contado, actualizado = System.currentTimeMillis()))
        _uiState.update {
            it.copy(
                ultima = it.ultima?.copy(cantidad = contado, stockDespues = contado),
                hoja = null
            )
        }
    }

    private suspend fun descartarLineaConteo(hoja: HojaCantidad) {
        val conteoDao = db.conteoDao()
        val conteo = conteoDao.enCurso(ubicacionId)
        if (conteo != null) {
            if (hoja.baseConteo > 0) {
                // Ya estaba contado antes de esta tanda: vuelve a ese valor
                val detalle = conteoDao.buscarDetalle(conteo.id, hoja.productoId)
                if (detalle != null) {
                    conteoDao.guardarDetalle(
                        detalle.copy(contado = hoja.baseConteo, actualizado = System.currentTimeMillis())
                    )
                }
            } else {
                conteoDao.descontar(conteo.id, hoja.productoId)
            }
        }
        _uiState.update {
            it.copy(
                lecturas = (it.lecturas - (hoja.cantidadInicial - hoja.baseConteo)).coerceAtLeast(0),
                ultima = null,
                hoja = null
            )
        }
    }

    private suspend fun guardarCambioCantidad(hoja: HojaCantidad, cantidad: Int) {
        val movimientoId = hoja.movimientoId ?: return
        val signo = if (hoja.tipo == TipoMovimiento.ENTRADA) 1 else -1
        val stockNuevo = hoja.stockBase + signo * cantidad
        if (cantidad < 1 || stockNuevo < 0) return

        db.withTransaction {
            db.movimientoDao().actualizarCantidad(movimientoId, cantidad)
            db.productoDao().fijarStock(hoja.productoId, stockNuevo)
        }
        _uiState.update {
            it.copy(
                ultima = it.ultima?.copy(cantidad = cantidad, stockDespues = stockNuevo),
                hoja = null
            )
        }
    }

    private suspend fun guardarAjuste(hoja: HojaCantidad, total: Int, motivo: String) {
        if (total < 0 || motivo.isEmpty() || motivo.length > 120) return

        val movimientoId = db.withTransaction {
            val id = if (hoja.movimientoId == null) {
                db.movimientoDao().insertar(
                    Movimiento(
                        productoId = hoja.productoId,
                        ubicacionId = ubicacionId,
                        tipo = TipoMovimiento.AJUSTE,
                        cantidad = total,          // el ajuste guarda la nueva cantidad
                        motivo = motivo,
                        fechaHora = System.currentTimeMillis()
                    )
                ).toInt()
            } else {
                db.movimientoDao().actualizarAjuste(hoja.movimientoId, total, motivo)
                hoja.movimientoId
            }
            db.productoDao().fijarStock(hoja.productoId, total)
            id
        }
        _uiState.update {
            it.copy(
                lecturas = if (hoja.movimientoId == null) it.lecturas + 1 else it.lecturas,
                ultima = UltimaLectura(
                    productoId = hoja.productoId,
                    nombre = hoja.nombre,
                    tipo = TipoMovimiento.AJUSTE,
                    cantidad = total,
                    stockAntes = hoja.stockBase,
                    stockDespues = total,
                    movimientoId = movimientoId,
                    motivo = motivo
                ),
                hoja = null
            )
        }
    }

    /** Al cambiar el tipo en el chip, lo último escaneado deja de aplicar. */
    fun reiniciarUltima() = _uiState.update { it.copy(ultima = null, mensaje = null, hoja = null) }

    fun avisarSinCodigo() = avisar("No se encontró código, intenta de nuevo")

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    private fun avisar(texto: String) = _uiState.update { it.copy(mensaje = texto) }

    // TEMPORAL: productos de prueba (códigos EAN-13 válidos) hasta tener el catálogo real.
    private suspend fun sembrarProductosDemo() {
        val demo = listOf(
            Producto(nombre = "Cuaderno universitario 100 hojas", stock = 48, codigoBarras = "7800000000016"),
            Producto(nombre = "Lápiz grafito HB", stock = 120, codigoBarras = "7800000000023"),
            Producto(nombre = "Resma de papel carta 500 hojas", stock = 30, codigoBarras = "7800000000030")
        )
        demo.forEach { p ->
            if (db.productoDao().buscarPorCodigo(p.codigoBarras) == null) {
                db.productoDao().insertar(p)
            }
        }
    }
}
