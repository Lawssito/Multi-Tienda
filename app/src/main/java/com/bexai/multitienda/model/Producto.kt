package com.bexai.multitienda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Producto: representa un artículo del inventario.
 *
 * Está marcado como @Entity igual que los modelos del repo (por ejemplo Usuario).
 * El id se genera automáticamente cuando se guarde en Room; por eso tiene
 * valor por defecto 0 al crear un producto nuevo.
 *
 * Puedes agregar más campos según lo que necesite el proyecto
 * (por ejemplo: categoría, código de barras, descripción, etc.).
 */
@Entity
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,    // Identificador único del producto
    val nombre: String, // Nombre del producto
    val precio: Double, // Precio unitario
    val stock: Int      // Unidades disponibles
)
