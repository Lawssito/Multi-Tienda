package com.bexai.multitienda.model

/**
 * ProductoStock: fila que se muestra en la pantalla de productos.
 *
 * Une el catálogo ([Producto]) con la cantidad que ese producto tiene en
 * la sucursal seleccionada ([StockUbicacion]).
 */
data class ProductoStock(
    val id: Int,
    val nombre: String,
    val stock: Int
)
