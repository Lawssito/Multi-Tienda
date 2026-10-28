package com.bexai.multitienda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.bexai.multitienda.ui.theme.MultiTiendaTheme
import com.bexai.multitienda.ui.view.ProductosView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTiendaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pantalla principal de la app
                    ProductosView(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}