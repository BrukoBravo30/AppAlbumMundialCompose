package mx.bruko.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import mx.bruko.viewModel.AlbumViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamesHubScreen(
    viewModel: AlbumViewModel,
    onPlayWordle: () -> Unit,
    onPlayPlinko: () -> Unit,
    onPlayPenales: () -> Unit
) {
    // Animaciones de Entrada
    var isVisible by remember { mutableStateOf(false) }

    // Animación de conteo de monedas
    val animatedCoins by animateIntAsState(
        targetValue = if (isVisible) viewModel.monedas else 0,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "coinCount"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) { // FONDO TRANSPARENTE

        // 1. PARTICULAS DE CASINO FLOTANTES
        CasinoParticles()

        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER DEL CASINO (Animado y Premium) ---
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn(tween(800))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 110.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ZONA ARCADE",
                            color = Color(0xFF00E5FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "CASINO VIP",
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00))),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                shadow = Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 15f)
                            )
                        )
                    }

                    // Saldo animado con Shimmer
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF131C2D).copy(alpha = 0.8f))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoinPulseIcon()
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$animatedCoins",
                            color = Color(0xFFFFD700),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // --- LISTA DE JUEGOS ---
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Juego 1: Wordle
                item {
                    GameCard(
                        title = "Wordle Futbolero",
                        description = "Adivina el jugador oculto con pistas en 6 intentos.",
                        icon = Icons.Filled.SportsSoccer,
                        accentColor = Color(0xFF00FF87), // Neón Green
                        multiplier = "x10",
                        isLocked = false,
                        isVisible = isVisible,
                        delayAnim = 100,
                        onClick = onPlayWordle
                    )
                }

                // Juego 2: Plinko
                item {
                    GameCard(
                        title = "Plinko Stake",
                        description = "La pirámide del destino. ¡Deja caer la bola a la suerte!",
                        icon = Icons.Filled.Casino,
                        accentColor = Color(0xFF00E5FF), // Cyan
                        multiplier = "x130",
                        isLocked = false,
                        isVisible = isVisible,
                        delayAnim = 250,
                        onClick = onPlayPlinko
                    )
                }

                // Juego 3: Penales
                item {
                    GameCard(
                        title = "Penales",
                        description = "Tira a puerta, intentando engañar al portero rival.",
                        icon = Icons.Filled.Games,
                        accentColor = Color(0xFFFF3366), // Pink Neon
                        multiplier = "x2",
                        isLocked = false,
                        isVisible = isVisible,
                        delayAnim = 400,
                        onClick = onPlayPenales
                    )
                }

                // Juego 4: Ruleta (Bloqueado)
                item {
                    GameCard(
                        title = "Ruleta",
                        description = "Gira la ruleta y multiplica tus ganancias al instante.",
                        icon = Icons.Filled.Money,
                        accentColor = Color(0xFFFFD700), // Gold
                        multiplier = "x50",
                        isLocked = true,
                        isVisible = isVisible,
                        delayAnim = 550,
                        onClick = { }
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENTE: TARJETA DE JUEGO PREMIUM
// ==========================================
@Composable
fun GameCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    multiplier: String,
    isLocked: Boolean,
    isVisible: Boolean,
    delayAnim: Int,
    onClick: () -> Unit
) {
    // Animación de entrada en cascada
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { 100 },
            animationSpec = tween(600, delayMillis = delayAnim, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(600, delayMillis = delayAnim))
    ) {
        // Animación Idle del Ícono (Pulse)
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val iconScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isLocked) 1f else 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, delayMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "iconScale"
        )

        val cardColor = if (isLocked) Color.Gray else accentColor

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // Un poco más altas para que respire el contenido
                .graphicsLayer {
                    if (!isLocked) {
                        shadowElevation = 25f
                        ambientShadowColor = cardColor
                        spotShadowColor = cardColor
                    }
                }
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = !isLocked, onClick = onClick)
                // Gradiente Interno (Del color del juego a Slate Dark)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            if (isLocked) Color(0xFF2C2C2C) else cardColor.copy(alpha = 0.25f),
                            Color(0xFF0F172A).copy(alpha = 0.8f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isLocked) Color.DarkGray else cardColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Brillo de borde lateral
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, cardColor, Color.Transparent)
                            )
                        )
                )

                // Contenido
                Row(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono del juego Animado
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(iconScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(cardColor.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                            .border(1.dp, cardColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.Lock else icon,
                            contentDescription = null,
                            tint = if (isLocked) Color.DarkGray else cardColor,
                            modifier = Modifier.size(32.dp).graphicsLayer {
                                shadowElevation = if (!isLocked) 10f else 0f
                                ambientShadowColor = cardColor
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Textos
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                color = if (isLocked) Color.Gray else Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            color = if (isLocked) Color.DarkGray else Color(0xFF94A3B8), // Slate 400
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (isLocked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PRÓXIMAMENTE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // EL MULTIPLICADOR (Dopamina pura)
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.width(70.dp)
                    ) {
                        Text(
                            text = multiplier,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                ),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                shadow = Shadow(color = Color(0xFFFFD700).copy(alpha = 0.4f), blurRadius = 15f)
                            ),
                            modifier = Modifier.graphicsLayer {
                                alpha = if (isLocked) 0.2f else 1f
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ANIMACIONES SECUNDARIAS
// ==========================================
@Composable
fun CoinPulseIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "coinPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coinScale"
    )
    Text(
        text = "🪙",
        fontSize = 18.sp,
        modifier = Modifier.scale(scale)
    )
}

@Composable
fun CasinoParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleMove"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dibujamos algunas estrellas/destellos que suben lentamente
        for (i in 0..20) {
            // Posiciones pseudoaleatorias estables
            val x = (width * ((i * 137) % 100) / 100f)
            val yOffset = (height * ((i * 193) % 100) / 100f)
            var y = (yOffset + offsetY) % height
            if (y < 0) y += height

            val alpha = 0.2f + (0.3f * sin((i + offsetY/50f))).coerceIn(0f, 1f)

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha), // Dorado brillante
                radius = if (i % 3 == 0) 4f else 2f,
                center = Offset(x, y)
            )
        }
    }
}