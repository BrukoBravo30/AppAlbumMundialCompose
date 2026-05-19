package mx.bruko.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.bruko.viewModel.AlbumViewModel
import mx.bruko.viewModel.BolaPlinko
import mx.bruko.viewModel.PlinkoViewModel
import androidx.compose.runtime.key
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
@Composable
fun PlinkoScreen(
    onBack: () -> Unit,
    viewModel: PlinkoViewModel = viewModel(), // Nace y muere con la vista
    albumViewModel: AlbumViewModel
) {
    BackHandler { onBack() }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))

    Column(
        modifier = Modifier.fillMaxSize().background(bgGradient)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLINKO CASINO", color = Color(0xFF00FF87), fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("🪙 Saldo: ${albumViewModel.monedas}", color = Color(0xFFFFD700), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.size(48.dp))
        }

        // --- ZONA DEL TABLERO (CANVAS) ---
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            TableroPlinkoCanvas(multiplicadores = viewModel.multiplicadores)

            // Renderizamos y animamos cada bola activa
            viewModel.bolasActivas.forEach { bola ->
                // Esto ancla la animación exclusivamente a esta bola, evitando que se mezclen
                key(bola.id) {
                    BolaPlinkoAnimada(
                        bola = bola,
                        onLlegoAlFondo = {
                            viewModel.bolaLlegoAlFondo(bola) { premio ->
                                albumViewModel.sumarMonedas(premio)
                            }
                        }
                    )
                }
            }
        }

        // --- CONTROLES DE APUESTA ---
        ControlesPlinko(
            apuestaActual = viewModel.apuestaSeleccionada,
            precios = viewModel.preciosDisponibles,
            botonHabilitado = viewModel.botonHabilitado, // <-- PASAMOS EL ESTADO DEL COOLDOWN
            onApuestaCambiada = { viewModel.cambiarApuesta(it) },
            onApostar = {
                viewModel.comprarBola(
                    saldoActual = albumViewModel.monedas,
                    onCobrar = { costo -> albumViewModel.restarMonedas(costo) }
                )
            }
        )
    }
}

// ==========================================
// EL DIBUJO ESTÁTICO (CLAVOS Y CAJAS CON TEXTO)
// ==========================================
@Composable
fun TableroPlinkoCanvas(multiplicadores: List<Double>) {
    // Herramienta nativa para medir el tamaño de la letra antes de pintarla
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val filas = 16
        val radioClavo = 4.dp.toPx()
        val anchoTotal = size.width
        val altoTotal = size.height

        val altoPiramide = altoTotal * 0.85f
        val separacionY = altoPiramide / filas

        // 1. Dibujar los clavos
        for (fila in 0 until filas) {
        val clavosEnFila = fila + 3
        val separacionX = anchoTotal / (filas + 4)
        val anchoFila = (clavosEnFila - 1) * separacionX
        val inicioX = (anchoTotal - anchoFila) / 2
        val posY = fila * separacionY

        for (col in 0 until clavosEnFila) {
        val posX = inicioX + (col * separacionX)
        drawCircle(
            color = Color.White,
            radius = radioClavo,
            center = Offset(posX, posY)
        )
    }
    }

        // 2. Dibujar las cajas y SUS TEXTOS
        val separacionCajas = anchoTotal / multiplicadores.size
        val altoCaja = altoTotal * 0.1f
        val posYcaja = altoPiramide + (altoTotal * 0.02f)

        multiplicadores.forEachIndexed { index, mult ->
            val colorCaja = when {
                mult >= 29.0 -> Color(0xFFFF003C) // Rojo Extremo
                mult >= 2.0 -> Color(0xFFFF5E00)  // Naranja
                mult >= 1.0 -> Color(0xFFFFB300)  // Amarillo
                else -> Color(0xFF4CAF50)         // Verde
            }

            val posX = (index * separacionCajas)

            // Pintamos el rectángulo
            drawRoundRect(
                color = colorCaja,
                topLeft = Offset(posX + 1.dp.toPx(), posYcaja),
                size = Size(separacionCajas - 2.dp.toPx(), altoCaja),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Pintamos el Texto
            // Si es un entero (ej 100.0) le quitamos el decimal, si no, lo dejamos (0.7)
            val textoMult = if (mult % 1.0 == 0.0) "${mult.toInt()}x" else "${mult}x"

            val textLayout = textMeasurer.measure(
                text = textoMult,
                style = TextStyle(color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            )

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = posX + (separacionCajas / 2f) - (textLayout.size.width / 2f),
                    y = posYcaja + (altoCaja / 2f) - (textLayout.size.height / 2f)
                )
            )
        }
    }
}

// ==========================================
// LA ANIMACIÓN DE LA BOLA (INTERPOLACIÓN)
// ==========================================
@Composable
fun BolaPlinkoAnimada(bola: BolaPlinko, onLlegoAlFondo: () -> Unit) {
    // Animamos un valor de 0 a 16 (las filas)
    val progreso = remember { Animatable(0f) }

    LaunchedEffect(bola.id) {
        // Tarda 2.5 segundos en caer, moviéndose linealmente fila por fila
        progreso.animateTo(
            targetValue = 16f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
        onLlegoAlFondo()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val filas = 16
        val anchoTotal = size.width
        val altoPiramide = size.height * 0.85f
        val separacionY = altoPiramide / filas
        val separacionX = anchoTotal / (filas + 4)

        // Calculamos la posición X actual basada en la ruta
        // Empezamos en el centro superior
        var posXObjetivo = anchoTotal / 2f

        // Recorremos la ruta precalculada según el progreso de la animación
        val filasCompletadas = progreso.value.toInt()
        val fraccion = progreso.value - filasCompletadas

        for (i in 0 until filasCompletadas) {
            val rebotaDerecha = bola.ruta[i]
            posXObjetivo += if (rebotaDerecha) separacionX / 2f else -(separacionX / 2f)
        }

        // Interpolación para hacer el movimiento fluido a la siguiente fila
        var posXSiguiente = posXObjetivo
        if (filasCompletadas < 16) {
            val rebotaDerecha = bola.ruta[filasCompletadas]
            posXSiguiente += if (rebotaDerecha) separacionX / 2f else -(separacionX / 2f)
        }

        val posXActual = androidx.compose.ui.util.lerp(posXObjetivo, posXSiguiente, fraccion)
        val posYActual = progreso.value * separacionY

        drawCircle(
            color = Color(0xFF00FF87), // Bola Verde Neón
            radius = 8.dp.toPx(),
            center = Offset(posXActual, posYActual)
        )
    }
}

// ==========================================
// CONTROLES INFERIORES
// ==========================================
// ==========================================
// CONTROLES INFERIORES (CON SOPORTE DE COOLDOWN)
// ==========================================
@Composable
fun ControlesPlinko(
    apuestaActual: Int,
    precios: List<Int>,
    botonHabilitado: Boolean,
    onApuestaCambiada: (Int) -> Unit,
    onApostar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2B32))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            precios.forEach { precio ->
                Button(
                    onClick = { onApuestaCambiada(precio) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (apuestaActual == precio) Color(0xFF00FF87) else Color(0xFF2C3E50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(precio.toString(), color = if (apuestaActual == precio) Color.Black else Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onApostar,
            enabled = botonHabilitado, // <-- SI ES FALSE, SE DESACTIVA LA UI Y PREVIENE EL SPAM
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF87),
                disabledContainerColor = Color(0xFF2C3E50) // Color grisáceo cuando está bloqueado
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (botonHabilitado) "JUGAR" else "CARGANDO...",
                color = if (botonHabilitado) Color.Black else Color.Gray,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}