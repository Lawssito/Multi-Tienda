package com.bexai.multitienda.viewmodel

import androidx.lifecycle.ViewModel
import com.bexai.multitienda.data.StockRepository
import com.bexai.multitienda.model.FormularioModel
import com.bexai.multitienda.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * StockViewModel: contiene la lógica y el estado de la pantalla de productos.
 *
 * Sigue el mismo formato de LoginViewModel:
 * - [uiState] guarda lo que el usuario escribe y los errores del formulario.
 * - [productos] es la lista que se muestra en la vista.
 * - [agregar] valida los datos y, si todo está bien, guarda en el repositorio.
 * - [eliminar] quita un producto por medio del repositorio.
 *
 * Los datos se piden a [StockRepository] (carpeta data/), no directamente
 * a la vista. Así, cuando se conecte Room, solo cambia el repositorio.
 */
class StockViewModel : ViewModel() {

    // Único punto de acceso a los datos
    private val repository = StockRepository()

    // Lista de productos que se muestra en la vista
    private val _productos = MutableStateFlow(repository.listar())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Estado del formulario (campos + errores)
    private val _uiState = MutableStateFlow(FormularioModel())
    val uiState: StateFlow<FormularioModel> = _uiState.asStateFlow()

    /**
     * Se ejecuta cada vez que el usuario escribe el nombre.
     * También borra el error anterior de ese campo.
     */
    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, nombreError = null) }
    }

    /**
     * Se ejecuta cada vez que el usuario escribe el precio.
     */
    fun onPrecioChange(precio: String) {
        _uiState.update { it.copy(precio = precio, precioError = null) }
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
     * - Precio: número mayor que 0.
     * - Stock: número entero de 0 o más.
     */
    fun agregar() {
        val estado = _uiState.value

        // Se revisa cada campo
        val nombreValido = estado.nombre.trim().length >= 5
        val precioNumero = estado.precio.toDoubleOrNull()
        val precioValido = precioNumero != null && precioNumero > 0
        val stockNumero = estado.stock.toIntOrNull()
        val stockValido = stockNumero != null && stockNumero >= 0

        // Se guardan los mensajes de error (null si el campo está bien)
        _uiState.update {
            it.copy(
                nombreError = if (nombreValido) null else "El nombre debe tener mínimo 5 caracteres",
                precioError = if (precioValido) null else "Ingresa un precio mayor que 0",
                stockError = if (stockValido) null else "Ingresa un stock de 0 o más"
            )
        }

        // Si todo está bien, se guarda el producto y se limpia el formulario
        if (nombreValido && precioValido && stockValido) {
            repository.agregar(
                Producto(
                    nombre = estado.nombre.trim(),
                    precio = precioNumero!!,
                    stock = stockNumero!!
                )
            )
            _productos.value = repository.listar()
            _uiState.value = FormularioModel()
        }
    }

    /**
     * Elimina el producto recibido por medio del repositorio.
     */
    fun eliminar(producto: Producto) {
        repository.eliminar(producto)
        _productos.value = repository.listar()
    }
}
