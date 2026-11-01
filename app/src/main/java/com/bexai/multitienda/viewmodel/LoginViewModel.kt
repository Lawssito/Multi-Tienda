package com.bexai.multitienda.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.bexai.multitienda.model.LoginModel
import com.bexai.multitienda.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginModel())
    val uiState : StateFlow<LoginModel> = _uiState.asStateFlow()
    private val repository = UsuarioRepository()

    fun onEmailChange(emailNuevo : String) {
        _uiState.update { it.copy(email = emailNuevo, emailError =  null, errorGeneral  = null) }
    }

    fun onPasswordChange(passwordNuevo : String) {
        _uiState.update { it.copy(password = passwordNuevo, passwordError =  null, errorGeneral  = null) }
    }

    fun onLoginClick() {
        val estado = _uiState.value

        val emailInvalido = when {
            estado.email.isBlank() -> "Ingrese correo"
            !Patterns.EMAIL_ADDRESS.matcher(estado.email).matches() -> "Correo invalido"
            else -> null
        }

        val passInvalido = when {
            estado.password.isBlank() -> "Ingrese contraseña"
            estado.password.length < 8 -> "Contraseña debe tener al menos 8 caracteres"
            else -> null
        }

        _uiState.update { it.copy(emailError = emailInvalido, passwordError = passInvalido) }

        if (emailInvalido == null && passInvalido == null) {
            val usuario = repository.autenticar(estado.email, estado.password)

            _uiState.update {
                if (usuario == null) {
                    it.copy(errorGeneral = "Correo o Contraseña incorrectos")
                }else{
                    it.copy(errorGeneral = null, usuarioAutenticado = usuario)
                }
            }
        }
    }

}