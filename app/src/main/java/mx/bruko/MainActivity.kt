package mx.bruko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
    ALBUM, SOBRES, ALMACEN, JUEGOS, WORDLE, PLINKO, PENALES
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
// CONTROLADOR DE PANTALLA DE CARGA
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AlbumViewModel) {
    // Estado que controla si estamos en la pantalla de carga
    var cargando by remember { mutableStateOf(true) }

    // Transición cinematográfica entre el Splash y la App
    Crossfade(
        targetState = cargando,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "AppTransition"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(onFinished = { cargando = false })
        } else {
            // El contenido real de la aplicación
            AppPrincipalContent(viewModel = viewModel)
        }
    }
}

// ==========================================
// CONTENIDO PRINCIPAL DE LA APP
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipalContent(viewModel: AlbumViewModel) {
    var pantallaActual by remember { mutableStateOf(RutasApp.ALBUM) }
    val mostrarBottomNav = pantallaActual in listOf(RutasApp.ALBUM, RutasApp.SOBRES, RutasApp.ALMACEN, RutasApp.JUEGOS)

    // Envolvemos el Scaffold en nuestro fondo profundo
    GlobalGamingBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                MusicPlayerWidget()
            },
            bottomBar = {
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
}

// ==========================================
// FONDO GLOBAL PROFUNDO
// ==========================================
@Composable
fun GlobalGamingBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF040B16), Color(0xFF010205))
                    )
                )

                val step = 80.dp.toPx()
                val width = size.width
                val height = size.height
                val gridAlpha = 0.03f
                val gridColor = Color.White.copy(alpha = gridAlpha)

                for (i in -height.toInt()..(width * 2).toInt() step step.toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(i.toFloat(), 0f),
                        end = Offset(i.toFloat() - height, height),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(i.toFloat(), 0f),
                        end = Offset(i.toFloat() + height, height),
                        strokeWidth = 3f
                    )
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), Color.Transparent),
                        radius = width * 1.2f
                    ),
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE100FF).copy(alpha = 0.08f), Color.Transparent),
                        radius = width
                    ),
                    center = Offset(width, height)
                )
            }
    ) {
        content()
    }
}
// ==========================================
// ESTRUCTURA DE LOS BOTONES
// ==========================================
data class NavItemMenu(
    val ruta: RutasApp,
    val titulo: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val colorActivo: androidx.compose.ui.graphics.Color
)
// ==========================================
// EL NAVBAR FLOTANTE
// ==========================================
@Composable
fun DopamineBottomNav(
    pantallaActual: RutasApp,
    onNavigate: (RutasApp) -> Unit
) {
    val items = listOf(
        NavItemMenu(RutasApp.ALBUM, "Álbum", Icons.Filled.MenuBook, Color(0xFF00E5FF)),
        NavItemMenu(RutasApp.SOBRES, "Sobres", Icons.Filled.Star, Color(0xFFFFD700)),
        NavItemMenu(RutasApp.ALMACEN, "Cartas", Icons.Filled.Inventory, Color(0xFFFF007F)),
        NavItemMenu(RutasApp.JUEGOS, "Juegos", Icons.Filled.PlayArrow, Color(0xFF00FF87))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 24.dp, shape = CircleShape, spotColor = Color(0xFF00E5FF), ambientColor = Color.Black)
                .clip(CircleShape)
                .background(Color(0xFF0B101A).copy(alpha = 0.85f))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
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
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) item.colorActivo.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(300), label = ""
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
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
                tint = if (isSelected) item.colorActivo else Color(0xFF64748B),
                modifier = Modifier.scale(iconScale)
            )

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
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}