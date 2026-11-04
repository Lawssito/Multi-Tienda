package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.FormularioModel
import com.bexai.multitienda.model.ProductoStock
import com.bexai.multitienda.repository.ProductoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * StockViewModel: contiene la lógica y el estado de la pantalla de productos.
 *
 * - [uiState] guarda lo que el usuario escribe y los errores del formulario.
 * - [productos] es la lista del catálogo con el stock de la sucursal actual.
 * - [seleccionarUbicacion] conecta la pantalla con la sucursal de la sesión.
 * - [agregar] valida los datos y guarda producto + stock en Room.
 * - [eliminar] quita el producto y sus cantidades por sucursal.
 */
class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.getDatabase(application)
    private val repository = ProductoRepository(baseDeDatos)

    // Catálogo con el stock de la sucursal seleccionada
    private val _productos = MutableStateFlow<List<ProductoStock>>(emptyList())
    val productos: StateFlow<List<ProductoStock>> = _productos.asStateFlow()

    // Estado del formulario (campos + errores)
    private val _uiState = MutableStateFlow(FormularioModel())
    val uiState: StateFlow<FormularioModel> = _uiState.asStateFlow()

    private var ubicacionActual: Int? = null
    private var trabajoProductos: Job? = null

    /**
     * Indica qué sucursal se está mostrando. La pantalla la recibe de la
     * sesión y la llama cada vez que cambia.
     */
    fun seleccionarUbicacion(ubicacionId: Int) {
        if (ubicacionActual == ubicacionId && trabajoProductos?.isActive == true) return

        ubicacionActual = ubicacionId
        trabajoProductos?.cancel()
        trabajoProductos = viewModelScope.launch {
            repository.listarConStock(ubicacionId).collect { lista ->
                _productos.value = lista
            }
        }

        // Solo crea los productos de ejemplo si el catálogo está vacío
        viewModelScope.launch {
            repository.asegurarProductosDemo(ubicacionId)
        }
    }

    /**
     * Se ejecuta cada vez que el usuario escribe el nombre.
     * También borra el error anterior de ese campo.
     */
    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, nombreError = null) }
    }

    /**
     * Se ejecuta cada vez que el usuario escribe el stock.
     */
    fun onStockChange(stock: String) {
        _uiState.update { it.copy(stock = stock, stockError = null) }
    }

    /**
     * Valida el formulario y agrega el producto si no hay errores.
     *
     * Reglas:
     * - Nombre: mínimo 5 caracteres.
     * - Stock: número entero de 0 o más.
     * - Debe existir una sucursal seleccionada.
     */
    fun agregar() {
        val ubicacionId = ubicacionActual
        if (ubicacionId == null) {
            _uiState.update { it.copy(stockError = "Selecciona una sucursal para continuar") }
            return
        }

        val estado = _uiState.value
        if (estado.guardando) return

        val nombreValido = estado.nombre.trim().length >= 5
        val stockNumero = estado.stock.toIntOrNull()
        val stockValido = stockNumero != null && stockNumero >= 0

        _uiState.update {
            it.copy(
                nombreError = if (nombreValido) null else "El nombre debe tener mínimo 5 caracteres",
                stockError = if (stockValido) null else "Ingresa un stock de 0 o más"
            )
        }

        if (!nombreValido || !stockValido) return

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            runCatching {
                repository.agregar(
                    nombre = estado.nombre.trim(),
                    cantidad = stockNumero!!,
                    ubicacionId = ubicacionId
                )
            }.onSuccess {
                _uiState.value = FormularioModel()
            }.onFailure {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        stockError = "No se pudo guardar el producto"
                    )
                }
            }
        }
    }

    /**
     * Elimina el producto recibido por medio del repositorio.
     */
    fun eliminar(producto: ProductoStock) {
        viewModelScope.launch {
            repository.eliminar(producto.id)
        }
    }
}
