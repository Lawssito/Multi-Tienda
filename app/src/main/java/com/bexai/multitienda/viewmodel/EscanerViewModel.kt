package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.data.ExistenciasRepository
import com.bexai.multitienda.data.RecepcionRepository
import com.bexai.multitienda.data.SesionSucursal
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

class EscanerViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val sucursalId: Int get() = SesionSucursal.id
    private val existencias = ExistenciasRepository(db)   // la que eligió el operador
    private val recepcionRepo = RecepcionRepository(db)

    private val _uiState = MutableStateFlow(EscanerUiState())
    val uiState: StateFlow<EscanerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { sembrarProductosDemo() }
    }

    // ---------- Código manual: escribir el código en vez de escanearlo ----------

    fun abrirManual() = _uiState.update { it.copy(manualAbierto = true, errorManual = null) }

    fun cerrarManual() = _uiState.update { it.copy(manualAbierto = false, errorManual = null) }

    fun limpiarErrorManual() = _uiState.update { it.copy(errorManual = null) }

    /**
     * Si el producto no existe, el error queda en el campo y la hoja sigue abierta.
     * Si existe, se cierra la hoja y el código se procesa igual que uno escaneado.
     */
    fun buscarCodigoManual(codigo: String, tipo: TipoMovimiento, esConteo: Boolean, recepcionId: Int = 0, soloCaja: Boolean = false) {
        viewModelScope.launch {
            // En Entrada (fuera de una recepción) también se acepta el código de un despacho
            val esDespacho = recepcionId == 0 && !esConteo && tipo == TipoMovimiento.ENTRADA &&
                db.despachoDao().buscarPorCodigo(codigo.trim()) != null
            if (!esDespacho && soloCaja) {
                _uiState.update { it.copy(errorManual = "No existe una caja con ese código") }
                return@launch
            }
            if (!esDespacho && db.productoDao().buscarPorCodigo(codigo) == null) {
                _uiState.update {
                    it.copy(
                        errorManual = if (codigo.trim().startsWith("DSP-", ignoreCase = true)) {
                            "No existe una caja con ese código"
                        } else {
                            "No existe un producto con ese código"
                        }
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(manualAbierto = false, errorManual = null) }
            procesarCodigo(codigo, tipo, esConteo, recepcionId, soloCaja)
        }
    }

    // ---------- Entrada por caja: el QR de un despacho abre "Caja detectada" ----------

    fun cerrarCaja() = _uiState.update { it.copy(caja = null) }

    private suspend fun detectarCaja(codigo: String): Boolean {
        val despacho = db.despachoDao().buscarPorCodigo(codigo.trim())
        if (despacho == null) {
            // Un código con forma de despacho que no existe es un QR desconocido
            if (codigo.trim().startsWith("DSP-", ignoreCase = true)) {
                avisar("QR de caja desconocido: ${codigo.trim()}")
                return true
            }
            return false
        }
        val resumen = db.despachoDao().resumen(despacho.id)
        _uiState.update {
            it.copy(
                mensaje = null,
                caja = CajaDetectada(
                    despachoId = despacho.id,
                    codigo = despacho.codigo,
                    proveedor = despacho.proveedor,
                    guia = despacho.guia,
                    productos = resumen.productos,
                    unidades = resumen.unidades
                )
            )
        }
        return true
    }

    // ---------- Recepción de caja: cada lectura suma 1 recibida, NO toca el stock ----------

    private fun recibirProducto(codigo: String, recepcionId: Int) {
        viewModelScope.launch {
            val producto = db.productoDao().buscarPorCodigo(codigo)
            if (producto == null) {
                avisar("Producto no encontrado: $codigo")
                return@launch
            }
            val lectura = recepcionRepo.recibir(recepcionId, producto)
            if (lectura == null) {
                avisar("Esta recepción ya fue enviada")
                return@launch
            }
            val linea = lectura.linea
            // Una "tanda" son las lecturas seguidas del mismo producto. Descartar deshace solo la tanda.
            val anterior = _uiState.value.ultima
            val base = if (anterior != null && anterior.detalleRecepcion != null && anterior.productoId == producto.id) {
                anterior.baseConteo
            } else {
                linea.recibido - 1
            }
            _uiState.update {
                it.copy(
                    lecturas = it.lecturas + 1,
                    ultima = UltimaLectura(
                        productoId = producto.id,
                        nombre = producto.nombre,
                        tipo = TipoMovimiento.ENTRADA,
                        cantidad = linea.recibido,
                        stockAntes = linea.esperado,   // en recepción, "antes" = lo esperado
                        stockDespues = linea.recibido,
                        movimientoId = 0,
                        baseConteo = base,
                        recepcionId = recepcionId,
                        detalleRecepcion = textoRecepcion(linea.recibido, linea.esperado)
                    ),
                    mensaje = if (lectura.noEsperado) "${producto.nombre} no venía en esta caja" else null
                )
            }
        }
    }

    private fun textoRecepcion(recibido: Int, esperado: Int) =
        if (esperado == 0) "Fuera del despacho · cuenta como sobrante" else "Recibidas $recibido de $esperado"

    private suspend fun guardarRecepcion(hoja: HojaCantidad, recibido: Int) {
        if (!recepcionRepo.fijarRecibido(hoja.recepcionId, hoja.productoId, recibido)) {
            avisar("Esta recepción ya fue enviada")
            _uiState.update { it.copy(hoja = null) }
            return
        }
        _uiState.update {
            it.copy(
                ultima = it.ultima?.copy(
                    cantidad = recibido,
                    stockDespues = recibido,
                    detalleRecepcion = textoRecepcion(recibido, hoja.stockBase)
                ),
                hoja = null
            )
        }
    }

    private suspend fun descartarRecepcion(hoja: HojaCantidad) {
        recepcionRepo.fijarRecibido(hoja.recepcionId, hoja.productoId, hoja.baseConteo)
        _uiState.update {
            it.copy(
                lecturas = (it.lecturas - (hoja.cantidadInicial - hoja.baseConteo).coerceAtLeast(0)).coerceAtLeast(0),
                ultima = null,
                hoja = null
            )
        }
    }

    fun procesarCodigo(codigo: String, tipo: TipoMovimiento, esConteo: Boolean, recepcionId: Int = 0, soloCaja: Boolean = false) {
        if (_uiState.value.hoja != null || _uiState.value.caja != null) return   // con una hoja abierta no se leen códigos
        if (recepcionId > 0) {
            recibirProducto(codigo, recepcionId)
            return
        }
        if (esConteo) {
            contarProducto(codigo)
            return
        }

        viewModelScope.launch {
            if (tipo == TipoMovimiento.ENTRADA && detectarCaja(codigo)) return@launch
            if (soloCaja) {
                avisar("Modo Caja: escanea el QR de la caja (DSP-0042)")
                return@launch
            }

            val producto = db.productoDao().buscarPorCodigo(codigo)
            if (producto == null) {
                avisar("Producto no encontrado: $codigo")
                return@launch
            }

            // Stock de ESTA sucursal (el total global vive en Producto.stock)
            val stockSucursal = existencias.cantidad(producto.id, sucursalId)

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
                            stockBase = stockSucursal,
                            cantidadInicial = stockSucursal
                        )
                    )
                }
                return@launch
            }

            // Entrada / Salida: cada lectura mueve 1 unidad
            val delta = if (tipo == TipoMovimiento.ENTRADA) 1 else -1
            if (stockSucursal + delta < 0) {
                avisar("Sin stock de ${producto.nombre}")
                return@launch
            }
            val stockNuevo = stockSucursal + delta

            val previa = _uiState.value.ultima
            val ultima = if (previa != null && previa.productoId == producto.id && previa.tipo == tipo) {
                // Mismo producto: se suma al movimiento que ya existe
                val cantidad = previa.cantidad + 1
                db.withTransaction {
                    db.movimientoDao().actualizarCantidad(previa.movimientoId, cantidad)
                    existencias.sumar(producto.id, sucursalId, delta)
                }
                previa.copy(cantidad = cantidad, stockDespues = stockNuevo)
            } else {
                val movimientoId = db.withTransaction {
                    val id = db.movimientoDao().insertar(
                        Movimiento(
                            productoId = producto.id,
                            sucursalId = sucursalId,
                            tipo = tipo,
                            cantidad = 1,
                            fechaHora = System.currentTimeMillis()
                        )
                    ).toInt()
                    existencias.sumar(producto.id, sucursalId, delta)
                    id
                }
                UltimaLectura(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    tipo = tipo,
                    cantidad = 1,
                    stockAntes = stockSucursal,
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
                    baseConteo = ultima.baseConteo,
                    esRecepcion = ultima.detalleRecepcion != null,
                    recepcionId = ultima.recepcionId
                )
            )
        }
    }

    fun cerrarHoja() = _uiState.update { it.copy(hoja = null) }

    fun guardarHoja(cantidad: Int, motivo: String) {
        val hoja = _uiState.value.hoja ?: return
        viewModelScope.launch {
            when {
                hoja.esRecepcion -> guardarRecepcion(hoja, cantidad)
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
            if (hoja.esRecepcion) {
                descartarRecepcion(hoja)
                return@launch
            }
            if (hoja.esConteo) {
                descartarLineaConteo(hoja)
                return@launch
            }
            val movimientoId = hoja.movimientoId
            if (movimientoId != null) {
                db.withTransaction {
                    db.movimientoDao().eliminar(movimientoId)
                    existencias.fijar(hoja.productoId, sucursalId, hoja.stockBase)
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
            val conteo = conteoDao.enCurso(sucursalId)
            if (conteo == null) {
                avisar("No hay un conteo en curso. Vuelve e inicia el conteo.")
                return@launch
            }

            // El stock del sistema se congeló al iniciar el conteo. Si el producto es nuevo
            // (se creó después de iniciar), se toma su stock actual.
            val previo = conteoDao.buscarDetalle(conteo.id, producto.id)
            // Si la línea está marcada para recontar, el primer escaneo reemplaza el valor anterior
            val recontando = previo?.actualizado == ConteoDetalle.RECONTAR
            val contado = (if (recontando) 0 else previo?.contado ?: 0) + 1
            val sistema = previo?.sistema ?: existencias.cantidad(producto.id, sucursalId)
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
        val conteo = conteoDao.enCurso(sucursalId) ?: return
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
        val conteo = conteoDao.enCurso(sucursalId)
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
                lecturas = (it.lecturas - (hoja.cantidadInicial - hoja.baseConteo).coerceAtLeast(0)).coerceAtLeast(0),
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
            existencias.fijar(hoja.productoId, sucursalId, stockNuevo)
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
                        sucursalId = sucursalId,
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
            existencias.fijar(hoja.productoId, sucursalId, total)
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
