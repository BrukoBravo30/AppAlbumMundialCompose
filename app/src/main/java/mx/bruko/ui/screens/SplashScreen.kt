package mx.bruko.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import mx.bruko.R
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Animación de la barra de progreso (0% a 100%)
    val progress = remember { Animatable(0f) }

    // Animación de escala para Lamine Yamal (Efecto zoom suave)
    val scaleAnim = rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Lógica de carga funcional (Simulamos 3.5 segundos)
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
        delay(200) // Pequeña pausa al final para satisfacción visual
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A1931), Color(0xFF010205))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. RESPLANDOR TRASERO (Glow de Poder)
        Box(
            modifier = Modifier
                .size(400.dp)
                .blur(80.dp)
                .scale(scaleAnim.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2.Imagen del Hijo de Messi
            AsyncImage(
                model = R.drawable.portugal_cristiano_ronaldo,
                contentDescription = "Cristiano Ronaldo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(450.dp)
                    .scale(scaleAnim.value)
                    .graphicsLayer {
                        // Sombra sutil para despegarlo del fondo
                        shadowElevation = 20f
                        spotShadowColor = Color.Black
                    },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. TÍTULO CON DOPAMINA (Dorado y Brillante)
            Text(
                text = "ÁLBUM",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00))),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 12.sp,
                    shadow = Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 25f)
                )
            )

            Text(
                text = "DE ESTRELLAS",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // 4. BARRA DE CARGA GAMING
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                ) {
                    // El progreso real
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.value)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00FF87), Color(0xFF00E5FF))
                                )
                            )
                    )
                }

                Text(
                    text = "CARGANDO COLECCIÓN...",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

