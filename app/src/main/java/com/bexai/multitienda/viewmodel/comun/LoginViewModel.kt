package com.bexai.multitienda.viewmodel.comun

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.bexai.multitienda.data.repository.UsuarioRepository
import com.bexai.multitienda.model.LoginModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * LoginViewModel: valida el formulario (formato de correo y contraseña de 8+ caracteres) y
 * autentica con UsuarioRepository. La pantalla solo muestra [uiState].
 */
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
                    it.copy(errorGeneral = "Correo o contraseña incorrectos")
                }else{
                    it.copy(errorGeneral = null, usuarioAutenticado = usuario)
                }
            }
        }
    }

}