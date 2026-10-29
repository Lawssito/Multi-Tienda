package com.bexai.multitienda.model

/**
 * Producto: representa un artículo del inventario.
 *
 * Por ahora se usa en memoria. Cuando se conecte Room, esta clase se convierte
 * en @Entity y se agrega su DAO en la carpeta data/.
 *
 * Puedes agregar más campos según lo que necesite el proyecto
 * (por ejemplo: categoría, código de barras, descripción, etc.).
 */
data class Producto(
    val id: Int,        // Identificador único del producto
    val nombre: String, // Nombre del producto
    val precio: Double, // Precio unitario
    val stock: Int      // Unidades disponibles
)
