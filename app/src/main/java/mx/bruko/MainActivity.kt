package mx.bruko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import mx.bruko.ui.components.MusicPlayerWidget
import mx.bruko.ui.screens.*
import mx.bruko.ui.theme.AlbumMundialTheme
import mx.bruko.viewModel.AlbumViewModel

// Definimos los destinos de nuestra app
enum class RutasApp {
    ALBUM,
    SOBRES,
    ALMACEN,
    JUEGOS,
    WORDLE,
    PLINKO,
    PENALES
}

class MainActivity : ComponentActivity() {
    private val viewModel: AlbumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            AlbumMundialTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// ESTRUCTURA DE LOS BOTONES
// ==========================================
data class NavItemMenu(
    val ruta: RutasApp,
    val titulo: String,
    val icono: ImageVector,
    val colorActivo: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AlbumViewModel) {
    var pantallaActual by remember { mutableStateOf(RutasApp.ALBUM) }

    // Ocultamos el Nav automáticamente si estamos dentro de un minijuego
    val mostrarBottomNav = pantallaActual in listOf(RutasApp.ALBUM, RutasApp.SOBRES, RutasApp.ALMACEN, RutasApp.JUEGOS)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black, // Fondo base oscuro
        topBar = {
            MusicPlayerWidget()
        },
        bottomBar = {
            // Animamos la entrada y salida de la barra inferior
            AnimatedVisibility(
                visible = mostrarBottomNav,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                DopamineBottomNav(pantallaActual = pantallaActual) { nuevaRuta ->
                    pantallaActual = nuevaRuta
                }
            }
        }
    ) { paddingValues ->
        // Si el bottom nav está oculto, el padding inferior debe ser 0 para usar toda la pantalla
        val paddingReal = if (mostrarBottomNav) paddingValues else PaddingValues(top = paddingValues.calculateTopPadding())

        Box(modifier = Modifier.padding(paddingReal).fillMaxSize()) {
            when (pantallaActual) {
                RutasApp.ALBUM -> AlbumScreen(viewModel = viewModel)
                RutasApp.SOBRES -> PackScreen(viewModel = viewModel)
                RutasApp.ALMACEN -> InventoryScreen(viewModel = viewModel)
                RutasApp.JUEGOS -> GamesHubScreen(
                    viewModel = viewModel,
                    onPlayWordle = { pantallaActual = RutasApp.WORDLE },
                    onPlayPlinko = { pantallaActual = RutasApp.PLINKO },
                    onPlayPenales = { pantallaActual = RutasApp.PENALES }
                )
                RutasApp.WORDLE -> WordleScreen(
                    onBack = { pantallaActual = RutasApp.JUEGOS },
                    albumViewModel = viewModel
                )
                RutasApp.PLINKO -> PlinkoScreen(
                    onBack = { pantallaActual = RutasApp.JUEGOS },
                    albumViewModel = viewModel
                )
                RutasApp.PENALES -> PenalesScreen(
                    onBack = { pantallaActual = RutasApp.JUEGOS },
                    albumViewModel = viewModel
                )
            }
        }
    }
}

// ==========================================
// EL NUEVO NAVBAR FLOTANTE (ESTILO CLASH ROYALE)
// ==========================================
@Composable
fun DopamineBottomNav(
    pantallaActual: RutasApp,
    onNavigate: (RutasApp) -> Unit
) {
    val items = listOf(
        NavItemMenu(RutasApp.ALBUM, "Álbum", Icons.Filled.MenuBook, Color(0xFF00E5FF)), // Cian
        NavItemMenu(RutasApp.SOBRES, "Sobres", Icons.Filled.Star, Color(0xFFFFD700)),   // Oro
        NavItemMenu(RutasApp.ALMACEN, "Cartas", Icons.Filled.Inventory, Color(0xFFFF007F)), // Magenta Neón
        NavItemMenu(RutasApp.JUEGOS, "Juegos", Icons.Filled.PlayArrow, Color(0xFF00FF87))  // Verde Veneno
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp), // Separado de los bordes (Flotante)
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color.Black)
                .clip(CircleShape)
                .background(Color(0xFF1E1E2C)) // Azul muy oscuro, casi negro
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                DopamineNavItem(
                    item = item,
                    isSelected = pantallaActual == item.ruta,
                    onClick = { onNavigate(item.ruta) }
                )
            }
        }
    }
}

// ==========================================
// EL BOTÓN ANIMADO INDIVIDUAL
// ==========================================
@Composable
fun DopamineNavItem(
    item: NavItemMenu,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 1. Animamos el color de fondo (Transparente -> Color brillante al 20% de opacidad)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) item.colorActivo.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(300)
    )

    // 2. Animamos el tamaño del icono para que "palpite"
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    // 3. Quitamos el efecto "Ripple" feo de Android clásico
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Esto quita el círculo gris feo al tocar
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icono,
                contentDescription = item.titulo,
                tint = if (isSelected) item.colorActivo else Color.Gray,
                modifier = Modifier.scale(iconScale)
            )

            // Animamos la expansión del texto. Solo el seleccionado muestra sus letras
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(animationSpec = tween(300)) + fadeIn(tween(300)),
                exit = shrinkHorizontally(animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                Text(
                    text = item.titulo,
                    color = item.colorActivo,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp) // Espacio entre el icono y el texto
                )
            }
        }
    }
}