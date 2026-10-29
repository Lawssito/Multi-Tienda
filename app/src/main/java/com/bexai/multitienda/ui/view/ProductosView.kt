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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.model.Producto
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
 * viven en [StockViewModel].
 *
 * Permite:
 * - Agregar productos con validaciones (nombre, precio y stock).
 * - Buscar productos por nombre con [SearchField].
 * - Ver la lista con [ProductRow].
 * - Eliminar un producto manteniéndolo presionado 3 segundos y confirmando
 *   con [ConfirmDialog].
 */
@Composable
fun ProductosView(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = viewModel()
) {
    // Estado del formulario y lista de productos que vienen del ViewModel
    val formulario by viewModel.formulario.collectAsState()
    val productos by viewModel.productos.collectAsState()

    // Texto del buscador (solo afecta a esta pantalla, por eso vive aquí)
    var busqueda by remember { mutableStateOf("") }

    // Producto que se quiere eliminar (null = no se muestra el diálogo)
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    // Lista filtrada según lo que se escriba en el buscador
    val productosFiltrados = productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true)
    }

    Column(
        // Ocupa toda la pantalla y deja un margen interno
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        // Separación entre cada elemento
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Agregar producto",
            style = MaterialTheme.typography.titleLarge
        )

        // Campo para el nombre, con validación
        ValidatedTextField(
            value = formulario.nombre,
            onValueChange = { viewModel.onNombreChange(it) },
            label = "Nombre del producto",
            isError = formulario.nombreError != null,
            supportingText = formulario.nombreError
        )

        // Campo para el precio, con validación
        ValidatedTextField(
            value = formulario.precio,
            onValueChange = { viewModel.onPrecioChange(it) },
            label = "Precio",
            isError = formulario.precioError != null,
            supportingText = formulario.precioError
        )

        // Campo para el stock, con validación
        ValidatedTextField(
            value = formulario.stock,
            onValueChange = { viewModel.onStockChange(it) },
            label = "Stock",
            isError = formulario.stockError != null,
            supportingText = formulario.stockError
        )

        Button(
            onClick = { viewModel.agregar() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Agregar")
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

        // Instrucción para que el usuario sepa cómo eliminar
        Text(
            text = "Mantén presionado un producto 3 segundos para eliminarlo",
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
                    price = producto.precio,
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
                viewModel.eliminar(producto)
                productoAEliminar = null
            },
            onDismiss = {
                productoAEliminar = null
            }
        )
    }
}

/**
 * Vista previa de la pantalla completa con los datos de ejemplo.
 */
@Preview(showBackground = true)
@Composable
fun ProductosViewPreview() {
    MultiTiendaTheme {
        ProductosView()
    }
}
