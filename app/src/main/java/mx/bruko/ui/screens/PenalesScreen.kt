package mx.bruko.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.bruko.viewModel.AlbumViewModel
import mx.bruko.viewModel.PenalesViewModel

@Composable
fun PenalesScreen(
    onBack: () -> Unit,
    viewModel: PenalesViewModel = viewModel(),
    albumViewModel: AlbumViewModel
) {
    BackHandler { onBack() }

    val urlBaseGithub = "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/"

    // ==========================================
    // CONTENEDOR PRINCIPAL TRANSPARENTE
    // ==========================================
    Box(modifier = Modifier.fillMaxSize()) { // QUITAMOS EL FONDO MORADO AQUÍ

        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER PREMIUM ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp, start = 16.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A).copy(alpha = 0.6f)) // Glass
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "TANDA DE PENALES",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        style = TextStyle(shadow = Shadow(Color(0xFF00E5FF).copy(alpha = 0.5f), blurRadius = 20f))
                    )
                    Text(
                        text = "🪙 Saldo: ${albumViewModel.monedas}",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- RACHA Y PREMIO CON GLOW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f)) // Glassmorphism
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RACHA ACTUAL: ${viewModel.rachaGoles} ⚽",
                        color = Color(0xFF00FF87),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    if (viewModel.juegoActivo && viewModel.rachaGoles > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PREMIO ACUMULADO: 🪙 ${viewModel.premioAcumulado}",
                            color = Color(0xFFFFD700),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(shadow = Shadow(Color(0xFFFFD700).copy(alpha = 0.4f), blurRadius = 15f))
                        )
                    }
                }
            }

            // ==========================================
            // ESCENARIO DE JUEGO (Portería AAA Drawn by Code)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // EL CONTENEDOR RECTANGULAR DEL ESCENARIO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.6f) // Proporción FIFA Real
                        .graphicsLayer {
                            // Sombra suave en el escenario
                            shadowElevation = 40f
                            ambientShadowColor = Color.Black
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 1. DIBUJAMOS LA PORTERÍA (Sin usar imágenes pegadas)
                    CustomGoalStructure(accentColor = Color.White)

                    // 2. ANIMACIÓN DEL PORTERO GIGANTE (Mantenemos assets)
                    // Reducimos offsets para que no se salga de la nueva portería dibujada
                    val targetOffsetX = when (viewModel.zonaPortero) {
                        1, 4 -> (-60).dp
                        2, 5 -> 60.dp
                        else -> 0.dp
                    }
                    val targetOffsetY = when (viewModel.zonaPortero) {
                        1, 2 -> (-40).dp // Salta hacia arriba
                        4, 5 -> 30.dp    // Se tira al pasto
                        else -> 40.dp
                    }

                    val animOffsetX by animateDpAsState(targetValue = targetOffsetX, label = "X")
                    val animOffsetY by animateDpAsState(targetValue = targetOffsetY, label = "Y")

                    val urlPortero = when (if (viewModel.zonaPortero == 0) 3 else viewModel.zonaPortero) {
                        1 -> "${urlBaseGithub}portero_arriba_izquierda.png"
                        2 -> "${urlBaseGithub}portero_arriba_derecha.png"
                        3 -> "${urlBaseGithub}portero_centro.png"
                        4 -> "${urlBaseGithub}portero_abajo_izquierda.png"
                        5 -> "${urlBaseGithub}portero_abajo_derecha.png"
                        else -> "${urlBaseGithub}portero_centro.png"
                    }

                    AsyncImage(
                        model = urlPortero,
                        contentDescription = "Portero",
                        modifier = Modifier
                            .size(230.dp) // <-- PORTERO MÁS IMPONENTE
                            .offset(x = animOffsetX, y = animOffsetY)
                    )

                    // 3. EL BALÓN (Mantenemos asset)
                    if (viewModel.zonaBalon != 0) {
                        AsyncImage(
                            model = "${urlBaseGithub}balon.png",
                            contentDescription = "Balón",
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    shadowElevation = 10f
                                    ambientShadowColor = Color.Black
                                }
                                .align(obtenerAlineacion(viewModel.zonaBalon))
                                .padding(10.dp) // Pequeño ajuste para centrarlo en la zona
                        )
                    }

                    // 4. ZONAS CLICKEABLES (Rediseñadas Neón)
                    if (viewModel.juegoActivo && !viewModel.animandoTiro) {
                        val zonas = listOf(1, 2, 3, 4, 5)

                        zonas.forEach { zona ->
                            // Interaction Source para quitar el ripple feo
                            val interactionSource = remember { MutableInteractionSource() }

                            Box(
                                modifier = Modifier
                                    .align(obtenerAlineacion(zona))
                                    .padding(8.dp) // Ajuste de posición
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)) // Glass
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        viewModel.tirarPenal(zona)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Punto central Neón
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                    }
                }

                // 5. OVERLAY DE MENSAJE GOL/ATAJADO
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewModel.mensajeOverlay.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut()
                ) {
                    Text(
                        text = viewModel.mensajeOverlay,
                        color = Color(viewModel.colorOverlay),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 20f)
                        )
                    )
                }
            }

            // ==========================================
            // CONTROLES INFERIORES GLASSMORPHISM
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f)) // Glass oscuro Slate
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!viewModel.juegoActivo) {
                    // Estado 1: Elegir apuesta e iniciar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(100, 500, 1000, 5000).forEach { monto ->
                            // Interaction source para disable ripple
                            val interactionSource = remember { MutableInteractionSource() }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (viewModel.apuestaInicial == monto) Color(0xFF00FF87) else Color(0xFF1E293B).copy(alpha = 0.5f))
                                    .border(0.5.dp, if (viewModel.apuestaInicial == monto) Color.White else Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                    .clickable(interactionSource = interactionSource, indication = null) { viewModel.apuestaInicial = monto },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monto.toString(),
                                    color = if (viewModel.apuestaInicial == monto) Color.Black else Color(0xFF94A3B8), // Slate 400
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            viewModel.iniciarJuego(albumViewModel.monedas) { costo -> albumViewModel.restarMonedas(costo) }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("INICIAR TANDA", color = Color.Black, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                } else {
                    // Estado 2: En pleno juego, opción de retirarse
                    Button(
                        onClick = {
                            viewModel.retirarseConGanancias { premio -> albumViewModel.sumarMonedas(premio)}
                        },
                        enabled = viewModel.rachaGoles > 0 && !viewModel.animandoTiro,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            disabledContainerColor = Color(0xFF334155) // Slate 600
                        )
                    ) {
                        Text(
                            text = if (viewModel.rachaGoles > 0) "RETIRARSE CON 🪙 ${viewModel.premioAcumulado}" else "SELECCIONA UNA ZONA",
                            color = if (viewModel.rachaGoles > 0 && !viewModel.animandoTiro) Color.Black else Color(0xFF94A3B8),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// COMPONENTE: PORTERÍA AAA (Drawn by Code)
// ==========================================
@Composable
fun CustomGoalStructure(accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .graphicsLayer {
                // Glow general de la portería
                shadowElevation = 20f
                ambientShadowColor = accentColor
                spotShadowColor = accentColor
            }
    ) {
        // 1. LA RED (Sutil cuadrícula de profundidad)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp)) {
            val netColor = accentColor.copy(alpha = 0.04f)
            val rows = 12
            val cols = 20
            val rowH = size.height / rows
            val colW = size.width / cols

            // Líneas horizontales
            for (i in 1 until rows) {
                drawLine(netColor, Offset(0f, i * rowH), Offset(size.width, i * rowH), 2f)
            }
            // Líneas verticales (¡AQUÍ FALTABA EL 0f EN EL PRIMER OFFSET!)
            for (i in 1 until cols) {
                drawLine(netColor, Offset(i * colW, 0f), Offset(i * colW, size.height), 2f)
            }
        }

        // 2. LOS POSTES Y TRAVESAÑO (Neón Drawn)
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Usamos border grueso para simular los postes físicos
                .border(6.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                // Superponemos el borde Neón brillante
                .border(2.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
        )
    }
}
// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

fun obtenerAlineacion(zona: Int): Alignment {
    return when (zona) {
        1 -> Alignment.TopStart
        2 -> Alignment.TopEnd
        3 -> Alignment.Center
        4 -> Alignment.BottomStart
        5 -> Alignment.BottomEnd
        else -> Alignment.Center
    }
}