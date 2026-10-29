package com.bexai.multitienda.data

import com.bexai.multitienda.model.Producto

/**
 * StockRepository: único punto de acceso a los datos de productos.
 *
 * Igual que UsuarioRepository en la rama Login, por ahora guarda los datos
 * en memoria. Cuando se conecte Room, esta clase será la que hable con el
 * ProductoDAO y la vista no tendrá que cambiar.
 */
class StockRepository {

    // Lista de productos (datos de ejemplo para iniciar)
    private val productos = mutableListOf(
        Producto(id = 1, nombre = "Leche entera 1 L", precio = 4500.0, stock = 24),
        Producto(id = 2, nombre = "Pan tajado", precio = 6200.0, stock = 0)
    )

    /**
     * Devuelve una copia de la lista para que nadie modifique los datos
     * del repositorio por fuera.
     */
    fun listar(): List<Producto> = productos.toList()

    /**
     * Agrega un producto a la lista.
     */
    fun agregar(producto: Producto) {
        productos.add(producto)
    }

    /**
     * Elimina el producto que tenga el mismo id.
     */
    fun eliminar(producto: Producto) {
        productos.removeAll { it.id == producto.id }
    }

    /**
     * Calcula el id que le corresponde al siguiente producto.
     */
    fun siguienteId(): Int {
        return (productos.maxOfOrNull { it.id } ?: 0) + 1
    }
}
