package mx.bruko.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.bruko.data.Player
import mx.bruko.ui.components.PlayerCard
import mx.bruko.viewModel.AlbumViewModel

enum class PackState { CERRADO, ABRIENDO, ABIERTO }

@Composable
fun PackScreen(viewModel: AlbumViewModel) {
    var state by remember { mutableStateOf(PackState.CERRADO) }
    var nuevasCartas by remember { mutableStateOf<List<Player>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Fondo minimalista oscuro
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF000000)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {

        // --- ESTADO 1 y 2: EL SOBRE ---
        AnimatedVisibility(
            visible = state != PackState.ABIERTO,
            exit = fadeOut(tween(500))
        ) {
            // Animación de Temblor (Shake)
            val offsetX = remember { Animatable(0f) }
            val scale by animateFloatAsState(
                targetValue = if (state == PackState.ABRIENDO) 1.1f else 1f,
                animationSpec = tween(1500)
            )

            // Diseño del Sobre Físico
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .height(320.dp)
                    .graphicsLayer {
                        translationX = offsetX.value
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(enabled = state == PackState.CERRADO) {
                        state = PackState.ABRIENDO
                        coroutineScope.launch {
                            // Secuencia de temblor
                            for (i in 0..15) {
                                offsetX.animateTo(if (i % 2 == 0) 15f else -15f, tween(50))
                            }
                            offsetX.animateTo(0f)

                            // Extraemos las cartas matemáticamente
                            nuevasCartas = viewModel.abrirSobre()

                            // Explosión/Apertura
                            delay(300)
                            state = PackState.ABIERTO
                        }
                    },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFFB71C1C), Color(0xFF4A148C))))
                ) {
                    Text(
                        text = if (state == PackState.CERRADO) "TOCA PARA ABRIR" else "¡ABRIENDO!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // --- ESTADO 3: CARTAS REVELADAS ---
        AnimatedVisibility(
            visible = state == PackState.ABIERTO,
            enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "¡NUEVOS JUGADORES!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Fila horizontal para deslizar y ver las 5 cartas
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(nuevasCartas) { jugador ->
                        Box(modifier = Modifier.width(200.dp)) {
                            // Reutilizamos tu tarjeta espectacular
                            PlayerCard(player = jugador)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { state = PackState.CERRADO },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Abrir otro sobre", color = Color.White)
                }
            }
        }
    }
}