package com.bexai.multitienda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.Rol
import com.bexai.multitienda.model.SesionModel
import com.bexai.multitienda.model.Ubicacion
import com.bexai.multitienda.model.Usuario
import com.bexai.multitienda.repository.UbicacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SesionViewModel: mantiene la sesión del usuario.
 *
 * Guarda el usuario autenticado, su rol y la sucursal (tienda) elegida.
 * Las sucursales se leen desde Room con un Flow; por eso, si se agrega o
 * modifica una sucursal, la pantalla se actualiza sola.
 */
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.getDatabase(application)
    private val ubicacionRepository = UbicacionRepository(baseDeDatos.ubicacionDao())

    // Usuario, rol y tienda de la sesión actual
    private val _uiState = MutableStateFlow(SesionModel())
    val uiState: StateFlow<SesionModel> = _uiState.asStateFlow()

    // Lista de sucursales que viene desde Room
    val tiendas: StateFlow<List<Ubicacion>> = ubicacionRepository.listar()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        // Crea sucursales de ejemplo si la base de datos está vacía
        viewModelScope.launch {
            ubicacionRepository.asegurarUbicacionesDemo()
        }
    }

    /**
     * Guarda el usuario y su rol cuando el login fue correcto.
     */
    fun iniciarSesion(usuario: Usuario) {
        _uiState.update {
            it.copy(
                usuario = usuario,
                rol = usuario.rol,
                tienda = null,
                error = null
            )
        }
    }

    /**
     * Selecciona la sucursal donde trabajará el usuario.
     * Ignora las sucursales inactivas.
     */
    fun seleccionarTienda(ubicacion: Ubicacion) {
        if (!ubicacion.activa) {
            _uiState.update {
                it.copy(error = "La sucursal ${ubicacion.nombre} está inactiva")
            }
            return
        }

        _uiState.update {
            it.copy(tienda = ubicacion, error = null)
        }
    }

    /**
     * Revisa que la sesión esté completa antes de continuar.
     * Devuelve true si se puede entrar al menú del rol.
     */
    fun confirmarSesion(): Boolean {
        val estado = _uiState.value

        if (estado.usuario == null || estado.rol == null) {
            _uiState.update { it.copy(error = "No hay un usuario autenticado") }
            return false
        }

        val tienda = estado.tienda
        if (tienda == null) {
            _uiState.update { it.copy(error = "Selecciona una sucursal para continuar") }
            return false
        }

        if (!tienda.activa) {
            _uiState.update { it.copy(error = "La sucursal seleccionada está inactiva") }
            return false
        }

        _uiState.update { it.copy(error = null) }
        return true
    }

    fun obtenerRol(): Rol? = _uiState.value.rol

    fun cerrarSesion() {
        _uiState.value = SesionModel()
    }
}
