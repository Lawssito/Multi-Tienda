package com.bexai.multitienda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.bexai.multitienda.ui.navigation.AppNavHost
import com.bexai.multitienda.ui.theme.MultiTiendaTheme

/**
 * MainActivity: única Activity de la app. Aplica el tema y carga [AppNavHost], que decide
 * qué pantalla mostrar según el rol con el que se inicia sesión.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTiendaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // La navegación decide qué pantalla se muestra
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
