package mx.bruko.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.bruko.data.Player
import mx.bruko.ui.components.PlayerCard
import mx.bruko.viewModel.AlbumViewModel
import mx.bruko.viewModel.TipoSobre

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

    val bgBase = Brush.verticalGradient(listOf(Color(0xFF0A0F1A), Color(0xFF02040A)))
    val ambientLight1 = Brush.radialGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.08f), Color.Transparent), radius = 1000f)
    val ambientLight2 = Brush.radialGradient(listOf(Color(0xFFE100FF).copy(alpha = 0.05f), Color.Transparent), radius = 1200f)

    Box(modifier = Modifier.fillMaxSize().background(bgBase), contentAlignment = Alignment.Center) {

        // Luces de fondo
        Box(modifier = Modifier.fillMaxSize().background(ambientLight1).align(Alignment.TopStart))
        Box(modifier = Modifier.fillMaxSize().background(ambientLight2).align(Alignment.BottomEnd))

        // ==========================================
        // ESTADO 1: LA TIENDA
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.TIENDA,
            exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.9f)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 90.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    // --- TITULO EDICIÓN DOPAMINA TOTAL ---
                    Text(
                        text = "TIENDA",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFE100FF))),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Companion.Black,
                            letterSpacing = 6.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(catalogoSobres) { sobre ->
                    val rarityColors = getRarityColors(sobre.tipo)
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                sobreSeleccionado = sobre
                                showDialog = true
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF131C2D).copy(alpha = 0.8f))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(rarityColors.map { it.copy(alpha = 0.6f) }),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .blur(40.dp)
                                            .background(rarityColors[0].copy(alpha = 0.3f), CircleShape)
                                    )
                                    AsyncImage(
                                        model = sobre.colorUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().shadow(15.dp, spotColor = rarityColors[0]),
                                        contentScale = ContentScale.Fit
                                    )
                                }

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

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Brush.horizontalGradient(rarityColors))
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
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
        // POPUP DE CONFIRMACIÓN
        // ==========================================
        if (showDialog && sobreSeleccionado != null) {
            val puedeComprar = viewModel.monedas >= sobreSeleccionado!!.tipo.precio
            val rarezaColors = getRarityColors(sobreSeleccionado!!.tipo)

            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color(0xFF131C2D),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                shape = RoundedCornerShape(20.dp),
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
                targetValue = if (state == PackState.ABRIENDO) 1.25f else 1f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing),
                label = "PackScale"
            )

            val infiniteTransition = rememberInfiniteTransition(label = "Aura")
            val auraAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "AuraAlpha"
            )

            LaunchedEffect(Unit) {
                for (i in 0..20) {
                    offsetX.animateTo(if (i % 2 == 0) 30f else -30f, tween(40))
                }
                offsetX.animateTo(0f)

                val resultado = viewModel.abrirSobre(sobreSeleccionado!!.tipo)
                if (resultado != null) {
                    nuevasCartas = resultado
                    delay(400)
                    state = PackState.ABIERTO
                } else {
                    state = PackState.TIENDA
                }
            }

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .scale(scale)
                        .blur(60.dp)
                        .background(
                            brush = Brush.radialGradient(rarityColors),
                            shape = CircleShape,
                            alpha = auraAlpha
                        )
                )

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .blur(80.dp)
                        .background(brush = Brush.horizontalGradient(rarityColors), alpha = 0.2f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¡NUEVAS ADQUISICIONES!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        items(nuevasCartas) { jugador ->
                            Box(modifier = Modifier
                                .width(220.dp)
                                .shadow(20.dp, spotColor = rarityColors[0])
                            ) {
                                PlayerCard(player = jugador)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(rarityColors))
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

        // ==========================================
        // SOLUCIÓN Z-INDEX: EL CONTADOR DE MONEDAS AL FINAL
        // ==========================================
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF000000).copy(alpha = 0.6f))
                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🪙", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${viewModel.monedas}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}