package com.bexai.multitienda.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bexai.multitienda.data.AppDatabase
import com.bexai.multitienda.model.LoginModel
import com.bexai.multitienda.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(
        AppDatabase.getDatabase(application).usuarioDao()
    )

    private val _uiState = MutableStateFlow(LoginModel())
    val uiState: StateFlow<LoginModel> = _uiState.asStateFlow()

    fun onEmailChange(emailNuevo: String) {
        _uiState.update {
            it.copy(email = emailNuevo, emailError = null, errorGeneral = null)
        }
    }

    fun onPasswordChange(passwordNuevo: String) {
        _uiState.update {
            it.copy(password = passwordNuevo, passwordError = null, errorGeneral = null)
        }
    }

    fun onLoginClick() {
        val estado = _uiState.value
        if (estado.cargando) return

        val emailInvalido = when {
            estado.email.isBlank() -> "Ingrese correo"
            !Patterns.EMAIL_ADDRESS.matcher(estado.email).matches() -> "Correo inválido"
            else -> null
        }

        val passwordInvalida = when {
            estado.password.isBlank() -> "Ingrese contraseña"
            estado.password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            else -> null
        }

        _uiState.update {
            it.copy(
                emailError = emailInvalido,
                passwordError = passwordInvalida,
                errorGeneral = null
            )
        }

        if (emailInvalido != null || passwordInvalida != null) return

        _uiState.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                // Deja usuarios de prueba si la base está vacía
                repository.asegurarUsuariosDemo()

                val usuario = repository.autenticar(estado.email, estado.password)

                _uiState.update {
                    if (usuario == null) {
                        it.copy(
                            cargando = false,
                            errorGeneral = "Correo o contraseña incorrectos"
                        )
                    } else {
                        it.copy(
                            cargando = false,
                            errorGeneral = null,
                            usuarioAutenticado = usuario
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "No se pudo acceder a la base de datos"
                    )
                }
            }
        }
    }

    /**
     * Limpia el usuario autenticado después de navegar, para que al volver
     * al login no se dispare otra vez la navegación automática.
     */
    fun onLoginConsumido() {
        _uiState.update { it.copy(usuarioAutenticado = null) }
    }
}
