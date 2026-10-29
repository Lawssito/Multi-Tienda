package com.bexai.multitienda.viewmodel

import androidx.lifecycle.ViewModel
import com.bexai.multitienda.model.FormularioModel
import com.bexai.multitienda.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * StockViewModel: contiene la lógica y el estado de la pantalla de productos.
 *
 * - [productos] es la lista de productos que se muestra en la vista.
 * - [formulario] guarda lo que el usuario escribe y los errores de validación.
 * - [agregar] valida los datos y, si todo está bien, agrega el producto.
 * - [eliminar] quita un producto de la lista.
 *
 * Por ahora los datos viven en memoria. Más adelante se puede cambiar esta
 * clase para que reciba la información de Room (carpeta data/) sin tener que
 * modificar la vista.
 */
class StockViewModel : ViewModel() {

    // Lista de productos (datos de ejemplo para iniciar)
    private val _productos = MutableStateFlow(
        listOf(
            Producto(id = 1, nombre = "Leche entera 1 L", precio = 4500.0, stock = 24),
            Producto(id = 2, nombre = "Pan tajado", precio = 6200.0, stock = 0)
        )
    )
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Estado del formulario (campos + errores)
    private val _formulario = MutableStateFlow(FormularioModel())
    val formulario: StateFlow<FormularioModel> = _formulario.asStateFlow()

    /**
     * Se ejecuta cada vez que el usuario escribe el nombre.
     * También borra el error anterior de ese campo.
     */
    fun onNombreChange(nombre: String) {
        _formulario.update { it.copy(nombre = nombre, nombreError = null) }
    }

    /**
     * Se ejecuta cada vez que el usuario escribe el precio.
     */
    fun onPrecioChange(precio: String) {
        _formulario.update { it.copy(precio = precio, precioError = null) }
    }

    /**
     * Se ejecuta cada vez que el usuario escribe el stock.
     */
    fun onStockChange(stock: String) {
        _formulario.update { it.copy(stock = stock, stockError = null) }
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
        val actual = _formulario.value

        // Se revisa cada campo
        val nombreValido = actual.nombre.trim().length >= 5
        val precioNumero = actual.precio.toDoubleOrNull()
        val precioValido = precioNumero != null && precioNumero > 0
        val stockNumero = actual.stock.toIntOrNull()
        val stockValido = stockNumero != null && stockNumero >= 0

        // Se guardan los mensajes de error (null si el campo está bien)
        _formulario.value = actual.copy(
            nombreError = if (nombreValido) null else "El nombre debe tener mínimo 5 caracteres",
            precioError = if (precioValido) null else "Ingresa un precio mayor que 0",
            stockError = if (stockValido) null else "Ingresa un stock de 0 o más"
        )

        // Si todo está bien, se agrega el producto y se limpia el formulario
        if (nombreValido && precioValido && stockValido) {
            val nuevoId = (_productos.value.maxOfOrNull { it.id } ?: 0) + 1
            _productos.update { lista ->
                lista + Producto(
                    id = nuevoId,
                    nombre = actual.nombre.trim(),
                    precio = precioNumero!!,
                    stock = stockNumero!!
                )
            }
            _formulario.value = FormularioModel()
        }
    }

    /**
     * Elimina de la lista el producto recibido.
     */
    fun eliminar(producto: Producto) {
        _productos.update { lista ->
            lista.filter { it.id != producto.id }
        }
    }
}
