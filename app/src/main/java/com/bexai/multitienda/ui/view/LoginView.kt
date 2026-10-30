package com.bexai.multitienda.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bexai.multitienda.ui.theme.Azul
import com.bexai.multitienda.ui.theme.Borde
import com.bexai.multitienda.ui.theme.Superficie
import com.bexai.multitienda.viewmodel.LoginViewModel

@Composable
fun LoginView(viewModel: LoginViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsState()

    val colorCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Superficie,
        unfocusedContainerColor = Superficie,
        unfocusedBorderColor = Borde,
        focusedBorderColor = Azul
    )
    // Fondo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
    )
    // Franja Azul
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Azul)
                .statusBarsPadding()
                .padding(start = 28.dp, end = 28.dp, top = 40.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        { // Caja blanca Icono e Icono
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            )
            {
                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Azul)
            }
            Text(
                "StockMulti",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Tu inventario, siempre bajo control.",
                color = Color.White,
                fontSize = 16.sp,
            )
        }
        // Formulario
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Usuario", modifier = Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = estado.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("Ingrese Correo") },

                isError = estado.emailError != null,
                supportingText = { estado.emailError?.let { Text(it) } },
                singleLine = true,

                shape = MaterialTheme.shapes.large,
                colors = colorCampo,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Contraseña", modifier = Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = estado.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("Ingrese contraseña") },

                isError = estado.passwordError != null,
                supportingText = { estado.passwordError?.let { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,

                shape = MaterialTheme.shapes.large,
                colors = colorCampo,
                modifier = Modifier.fillMaxWidth()
            )

            estado.errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = viewModel::onLoginClick,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp)) {
                Text("Iniciar sesión", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            estado.usuarioAutenticado?.let {
                Text("Bienvenido ${it.nombre} [${it.rol}]")
            }
        }
    }
}