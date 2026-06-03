package mx.bruko.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import mx.bruko.data.Player
import mx.bruko.ui.components.PlayerCard
import mx.bruko.viewModel.AlbumViewModel
import mx.bruko.viewModel.TipoSobre
import kotlin.math.cos
import kotlin.math.sin

// Estados de la pantalla
enum class PackState { TIENDA, ABRIENDO, ABIERTO }

// Modelo visual de los sobres
data class SobreUI(val tipo: TipoSobre, val nombre: String, val colorUrl: String)

fun getRarityColors(tipo: TipoSobre): List<Color> {
    return when(tipo) {
        TipoSobre.NORMAL -> listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
        TipoSobre.PREMIUM -> listOf(Color(0xFFE100FF), Color(0xFF7F00FF))
        TipoSobre.ULTIMATE -> listOf(Color(0xFF00E5FF), Color(0xFF0072FF))
    }
}

@Composable
fun PackScreen(viewModel: AlbumViewModel) {
    var state by remember { mutableStateOf(PackState.TIENDA) }
    var sobreSeleccionado by remember { mutableStateOf<SobreUI?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var nuevasCartas by remember { mutableStateOf<List<Player>>(emptyList()) }

    val catalogoSobres = listOf(
        SobreUI(TipoSobre.NORMAL, "Sobre de Oro", "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/sobre_normal.png"),
        SobreUI(TipoSobre.PREMIUM, "Sobre Jumbo Premium", "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/sobre_premium.png"),
        SobreUI(TipoSobre.ULTIMATE, "Sobre Ultimate", "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/sobre_unico.png")
    )

    // Fondo oscuro de la app para fundir el gradiente radial de apertura
    val darkAppBg = Color(0xFF0A1628)

    // ==========================================
    // CONTENEDOR PRINCIPAL TRANSPARENTE
    // ==========================================
    Box(
        modifier = Modifier.fillMaxSize(), // QUITAMOS EL BACKGROUND AQUÍ
        contentAlignment = Alignment.Center
    ) {

        // ==========================================
        // ESTADO 1: LA TIENDA
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.TIENDA,
            exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.9f)
        ) {
            LazyColumn(
                // Ajustamos padding top para que no choque con la Dynamic Island
                contentPadding = PaddingValues(top = 110.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        // --- TITULO CON GLOW ---
                        Text(
                            text = "TIENDA",
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFE100FF))),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp,
                                shadow = Shadow(color = Color(0xFF00E5FF).copy(alpha = 0.6f), blurRadius = 20f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- CONTADOR DE MONEDAS PREMIUM ---
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131C2D).copy(alpha = 0.8f)) // Cristal oscuro
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${viewModel.monedas}",
                                color = Color(0xFFFFD700), // Dorado vibrante
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                items(catalogoSobres) { sobre ->
                    val rarityColors = getRarityColors(sobre.tipo)
                    val interactionSource = remember { MutableInteractionSource() }

                    // Animación de Shimmer para el botón
                    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                    val shimmerX by infiniteTransition.animateFloat(
                        initialValue = -200f,
                        targetValue = 600f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "shimmer_anim"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                sobreSeleccionado = sobre
                                showDialog = true
                            }
                    ) {
                        // Tarjeta Glassmorphism sobre el fondo del main
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                // Fondo de tarjeta más traslúcido para integrarse
                                .background(Brush.linearGradient(listOf(rarityColors[0].copy(alpha = 0.1f), Color(0xFF131C2D).copy(alpha = 0.5f))))
                                .border(1.5.dp, rarityColors[0].copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // IMAGEN LIMPIA
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = sobre.colorUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // INFO & BOTÓN
                                Column(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .padding(end = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = sobre.nombre.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Botón Precio con Efecto Shimmer
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Brush.horizontalGradient(rarityColors))
                                            .padding(horizontal = 24.dp, vertical = 12.dp)
                                            .drawWithContent {
                                                drawContent()
                                                drawRect(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                                                        start = Offset(shimmerX, 0f),
                                                        end = Offset(shimmerX + 150f, size.height)
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🪙 ${sobre.tipo.precio}",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // POPUP DE CONFIRMACIÓN (Glassmorphism)
        // ==========================================
        if (showDialog && sobreSeleccionado != null) {
            val puedeComprar = viewModel.monedas >= sobreSeleccionado!!.tipo.precio
            val rarezaColors = getRarityColors(sobreSeleccionado!!.tipo)

            AlertDialog(
                onDismissRequest = { showDialog = false },
                // Fondo de popup traslúcido
                containerColor = Color(0xFF131C2D).copy(alpha = 0.9f),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                title = {
                    Text("AUTORIZAR COMPRA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                },
                text = {
                    if (puedeComprar) {
                        Text("Se deducirán 🪙 ${sobreSeleccionado!!.tipo.precio} monedas de tu saldo para adquirir el ${sobreSeleccionado!!.nombre}.")
                    } else {
                        Text("Fondos insuficientes. Necesitas más monedas para adquirir este sobre de rareza superior.")
                    }
                },
                confirmButton = {
                    if (puedeComprar) {
                        Button(
                            onClick = {
                                showDialog = false
                                state = PackState.ABRIENDO
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = rarezaColors[0]),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Adquirir", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(if (puedeComprar) "Cancelar" else "Entendido", color = Color.Gray)
                    }
                }
            )
        }

        // ==========================================
        // ESTADO 2: ANIMACIÓN DE APERTURA
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.ABRIENDO && sobreSeleccionado != null,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(300))
        ) {
            val rarityColors = sobreSeleccionado?.let { getRarityColors(it.tipo) } ?: listOf(Color.White, Color.Gray)
            val offsetX = remember { Animatable(0f) }
            val scale by animateFloatAsState(
                targetValue = if (state == PackState.ABRIENDO) 1.3f else 1f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing),
                label = "PackScale"
            )

            // Pulso ambiental de luz
            val infiniteTransition = rememberInfiniteTransition(label = "Aura")
            val auraAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.8f, // Bajamos un poco para que no tape el fondo del main
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "AuraAlpha"
            )

            // Rotación de las partículas
            val particleRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                label = "ParticleRot"
            )

            LaunchedEffect(Unit) {
                // Temblor dramático
                for (i in 0..20) {
                    offsetX.animateTo(if (i % 2 == 0) 35f else -35f, tween(40))
                }
                offsetX.animateTo(0f)

                val resultado = viewModel.abrirSobre(sobreSeleccionado!!.tipo)
                if (resultado != null) {
                    nuevasCartas = resultado
                    delay(500)
                    state = PackState.ABIERTO
                } else {
                    state = PackState.TIENDA
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Gradiente radial fundido con el fondo oscuro base
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(rarityColors[0].copy(alpha = auraAlpha), Color.Transparent), // Fundir a transparente
                                radius = 1200f
                            )
                        )
                )

                // Partículas flotantes
                Canvas(
                    modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = particleRotation }
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    for (i in 0..12) {
                        val angle = (i * (360f / 12)) * (Math.PI / 180f)
                        val radiusOffset = 300f + (sin((particleRotation + i * 30) * Math.PI / 180f) * 50f).toFloat()
                        val x = center.x + (cos(angle) * radiusOffset).toFloat()
                        val y = center.y + (sin(angle) * radiusOffset).toFloat()

                        drawCircle(
                            color = rarityColors[1].copy(alpha = auraAlpha),
                            radius = 12f,
                            center = Offset(x, y)
                        )
                    }
                }

                // Sobre vibrando flotante
                AsyncImage(
                    model = sobreSeleccionado?.colorUrl,
                    contentDescription = "Abriendo Sobre",
                    modifier = Modifier
                        .width(260.dp)
                        .height(360.dp)
                        .graphicsLayer {
                            translationX = offsetX.value
                            scaleX = scale
                            scaleY = scale
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        // ==========================================
        // ESTADO 3: CARTAS REVELADAS
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.ABIERTO,
            enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.6f, animationSpec = tween(800, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(500))
        ) {
            val rarityColors = sobreSeleccionado?.let { getRarityColors(it.tipo) } ?: listOf(Color.Cyan, Color.Blue)

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Glow trasero sutil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(rarityColors[0].copy(alpha = 0.15f), Color.Transparent)),
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¡NUEVAS ADQUISICIONES!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 40.dp),
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(color = rarityColors[0], blurRadius = 15f)
                        )
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        items(nuevasCartas) { jugador ->
                            Box(modifier = Modifier.width(220.dp)) {
                                PlayerCard(player = jugador)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Botón Continuar Glassmorphism
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush = Brush.horizontalGradient(rarityColors), alpha = 0.8f)                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable {
                                sobreSeleccionado = null
                                state = PackState.TIENDA
                            }
                            .padding(horizontal = 40.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ENVIAR AL ALMACÉN",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}