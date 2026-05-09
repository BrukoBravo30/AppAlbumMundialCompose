package mx.bruko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import mx.bruko.ui.screens.AlbumScreen
import mx.bruko.ui.screens.PackScreen
import mx.bruko.ui.theme.AlbumMundialTheme
import mx.bruko.viewModel.AlbumViewModel

// Definimos los destinos de nuestra app
enum class RutasApp {
    ALBUM,
    SOBRES
}

class MainActivity : ComponentActivity() {

    // Instanciamos el ViewModel aquí. Al pasarlo a ambas pantallas,
    // comparten los mismos datos de Firebase.
    private val viewModel: AlbumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlbumMundialTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AlbumViewModel) {
    // Variable de estado que recuerda en qué pantalla estamos
    var pantallaActual by remember { mutableStateOf(RutasApp.ALBUM) }

    // Scaffold es la estructura base de Material Design para apps
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E1E), // Gris oscuro para combinar con tu tema
                contentColor = Color.White
            ) {
                // Botón 1: El Álbum
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = "Mi Álbum") },
                    label = { Text("Mi Álbum") },
                    selected = pantallaActual == RutasApp.ALBUM,
                    onClick = { pantallaActual = RutasApp.ALBUM },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF), // Tu Cian especial
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Botón 2: Los Sobres
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Abrir Sobres") },
                    label = { Text("Abrir Sobres") },
                    selected = pantallaActual == RutasApp.SOBRES,
                    onClick = { pantallaActual = RutasApp.SOBRES },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        // El contenido principal va aquí, respetando el padding de la barra inferior
        Box(modifier = Modifier.padding(paddingValues)) {
            // Este "when" es el corazón de la navegación
            when (pantallaActual) {
                RutasApp.ALBUM -> AlbumScreen(viewModel = viewModel)
                RutasApp.SOBRES -> PackScreen(viewModel = viewModel)
            }
        }
    }
}