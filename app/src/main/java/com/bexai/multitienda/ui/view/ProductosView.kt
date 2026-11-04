package com.bexai.multitienda.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.model.FormularioModel
import com.bexai.multitienda.model.ProductoStock
import com.bexai.multitienda.ui.components.ConfirmDialog
import com.bexai.multitienda.ui.components.ProductRow
import com.bexai.multitienda.ui.components.SearchField
import com.bexai.multitienda.ui.components.ValidatedTextField
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import com.bexai.multitienda.viewmodel.StockViewModel

/**
 * ProductosView: pantalla funcional de productos.
 *
 * La vista solo muestra información y avisa al usuario; la lógica y el estado
 * viven en [StockViewModel]. El stock que se muestra es el de la sucursal
 * elegida en la sesión.
 *
 * Permite:
 * - Agregar productos con validaciones (nombre y stock).
 * - Buscar productos por nombre con [SearchField].
 * - Ver la lista con [ProductRow].
 * - Eliminar un producto manteniéndolo presionado y confirmando con
 *   [ConfirmDialog].
 */
@Composable
fun ProductosView(
    ubicacionId: Int,
    nombreSucursal: String? = null,
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = viewModel(),
    onVolver: (() -> Unit)? = null
) {
    // Estado del formulario y lista de productos que vienen del ViewModel
    val estado by viewModel.uiState.collectAsState()
    val productos by viewModel.productos.collectAsState()

    // Conecta el ViewModel con la sucursal elegida en la sesión
    LaunchedEffect(ubicacionId) {
        viewModel.seleccionarUbicacion(ubicacionId)
    }

    ProductosContenido(
        estado = estado,
        productos = productos,
        nombreSucursal = nombreSucursal,
        onNombreChange = viewModel::onNombreChange,
        onStockChange = viewModel::onStockChange,
        onAgregar = viewModel::agregar,
        onEliminar = viewModel::eliminar,
        onVolver = onVolver,
        modifier = modifier
    )
}

/**
 * Contenido sin ViewModel para que la vista previa pueda usar datos de ejemplo.
 */
@Composable
private fun ProductosContenido(
    estado: FormularioModel,
    productos: List<ProductoStock>,
    nombreSucursal: String?,
    onNombreChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onAgregar: () -> Unit,
    onEliminar: (ProductoStock) -> Unit,
    onVolver: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Texto del buscador (solo afecta a esta pantalla, por eso vive aquí)
    var busqueda by remember { mutableStateOf("") }

    // Producto que se quiere eliminar (null = no se muestra el diálogo)
    var productoAEliminar by remember { mutableStateOf<ProductoStock?>(null) }

    // Lista filtrada según lo que se escriba en el buscador
    val productosFiltrados = productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (onVolver != null) {
            TextButton(onClick = onVolver) {
                Text(text = "← Volver")
            }
        }

        if (nombreSucursal != null) {
            Text(
                text = "Sucursal: $nombreSucursal",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Text(
            text = "Agregar producto",
            style = MaterialTheme.typography.titleLarge
        )

        // Campo para el nombre, con validación
        ValidatedTextField(
            value = estado.nombre,
            onValueChange = onNombreChange,
            label = "Nombre del producto",
            isError = estado.nombreError != null,
            supportingText = estado.nombreError
        )

        // Campo para el stock, con validación
        ValidatedTextField(
            value = estado.stock,
            onValueChange = onStockChange,
            label = "Stock inicial en esta sucursal",
            isError = estado.stockError != null,
            supportingText = estado.stockError
        )

        Button(
            onClick = onAgregar,
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.guardando
        ) {
            Text(text = if (estado.guardando) "Guardando..." else "Agregar")
        }

        Text(
            text = "Listado de productos",
            style = MaterialTheme.typography.titleLarge
        )

        // Buscador: filtra la lista por nombre mientras se escribe
        SearchField(
            query = busqueda,
            onQueryChange = { busqueda = it },
            placeholder = "Buscar producto..."
        )

        Text(
            text = "${productosFiltrados.size} producto(s) encontrado(s)",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Mantén presionado un producto para eliminarlo",
            style = MaterialTheme.typography.bodySmall
        )

        // Lista de productos: al mantener presionado uno, se pregunta si se elimina
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productosFiltrados, key = { it.id }) { producto ->
                ProductRow(
                    name = producto.nombre,
                    stock = producto.stock,
                    onLongPress = { productoAEliminar = producto }
                )
            }
        }
    }

    // Se guarda en una variable local para que Kotlin sepa que no es null
    val producto = productoAEliminar
    if (producto != null) {
        // Diálogo de confirmación antes de eliminar
        ConfirmDialog(
            title = "Eliminar producto",
            message = "¿Seguro que deseas eliminar ${producto.nombre}?",
            onConfirm = {
                onEliminar(producto)
                productoAEliminar = null
            },
            onDismiss = {
                productoAEliminar = null
            }
        )
    }
}

/**
 * Vista previa de la pantalla con datos de ejemplo.
 */
@Preview(showBackground = true)
@Composable
fun ProductosViewPreview() {
    MultiTiendaTheme {
        ProductosContenido(
            estado = FormularioModel(nombre = "Leche entera 1 L", stock = "24"),
            productos = listOf(
                ProductoStock(id = 1, nombre = "Leche entera 1 L", stock = 24),
                ProductoStock(id = 2, nombre = "Pan tajado", stock = 0)
            ),
            nombreSucursal = "Sucursal Centro",
            onNombreChange = {},
            onStockChange = {},
            onAgregar = {},
            onEliminar = {},
            onVolver = null
        )
    }
}
