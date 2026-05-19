package mx.bruko.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.bruko.viewModel.AlbumViewModel
import mx.bruko.viewModel.PenalesViewModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.getValue
@Composable
fun PenalesScreen(
    onBack: () -> Unit,
    viewModel: PenalesViewModel = viewModel(),
    albumViewModel: AlbumViewModel
) {
    BackHandler { onBack() }
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF0B192C), Color(0xFF1A365D)))

    Column(modifier = Modifier.fillMaxSize().background(bgGradient)) {

        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TANDA DE PENALES", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("🪙 Saldo: ${albumViewModel.monedas}", color = Color(0xFFFFD700), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.size(48.dp))
        }

        // --- ZONA DE RACHA Y PREMIO ACUMULADO ---
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Racha Actual: ${viewModel.rachaGoles} ⚽", color = Color(0xFF00FF87), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (viewModel.juegoActivo && viewModel.rachaGoles > 0) {
                    Text("Premio Asegurado: 🪙 ${viewModel.premioAcumulado}", color = Color(0xFFFFD700), fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        //PORTERÍA Y ZONAS INTERACTIVAS
        Box(
            modifier = Modifier
                .weight(1f) // Mantiene todo empujado
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // EL CONTENEDOR RECTANGULAR DE LA PORTERÍA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f), // <-- PROPORCIÓN RECTANGULAR 16:9 (Como una portería real)
                contentAlignment = Alignment.Center
            ) {
                val urlBaseGithub = "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/"

                // 1. IMAGEN DE FONDO
                AsyncImage(
                    model = "${urlBaseGithub}porteria.png",
                    contentDescription = "Portería",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                // 2. ANIMACIÓN DEL PORTERO GIGANTE
                // Calculamos cuánto se mueve en los ejes X y Y
                val targetOffsetX = when (viewModel.zonaPortero) {
                    1, 4 -> (-60).dp // Se lanza a la izquierda de la pantalla
                    2, 5 -> 60.dp    // Se lanza a la derecha de la pantalla
                    else -> 0.dp     // Se queda al centro
                }
                val targetOffsetY = when (viewModel.zonaPortero) {
                    1, 2 -> (-30).dp // Salta hacia arriba
                    4, 5 -> 30.dp    // Se tira al pasto
                    else -> 50.dp
                }

                // Estos delegados hacen que el movimiento sea un deslizamiento suave, no instantáneo
                val animOffsetX by animateDpAsState(targetValue = targetOffsetX)
                val animOffsetY by animateDpAsState(targetValue = targetOffsetY)

                val urlPortero = when (if (viewModel.zonaPortero == 0) 3 else viewModel.zonaPortero) {
                    1 -> "${urlBaseGithub}penal_arriba_izquierda.png"
                    2 -> "${urlBaseGithub}penal_arriba_derecha.png"
                    3 -> "${urlBaseGithub}penal_centro.png"
                    4 -> "${urlBaseGithub}penal_abajo_izquierda.png"
                    5 -> "${urlBaseGithub}penal_abajo_derecha.png"
                    else -> "${urlBaseGithub}penal_centro.png"
                }

                AsyncImage(
                    model = urlPortero,
                    contentDescription = "Portero",
                    modifier = Modifier
                        .size(220.dp) // <-- PORTERO MUCHO MÁS IMPONENTE
                        .offset(x = animOffsetX, y = animOffsetY) // <-- SE DESLIZA A LA ESQUINA
                )

                // 3. EL BALÓN
                if (viewModel.zonaBalon != 0) {
                    AsyncImage(
                        model = "${urlBaseGithub}balon.png",
                        contentDescription = "Balón",
                        modifier = Modifier
                            .size(40.dp)
                            .align(obtenerAlineacion(viewModel.zonaBalon))
                    )
                }

                // 4. ZONAS CLICKEABLES
                if (viewModel.juegoActivo && !viewModel.animandoTiro) {
                    val zonas = listOf(1, 2, 3, 4, 5)

                    zonas.forEach { zona ->
                        Box(
                            modifier = Modifier
                                .align(obtenerAlineacion(zona))
                                .padding(12.dp)
                                .size(55.dp) // Círculos ligeramente más grandes
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .clickable { viewModel.tirarPenal(zona) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                }
            }

            // 5. OVERLAY DE MENSAJE GOL/ATAJADO
            androidx.compose.animation.AnimatedVisibility(
                visible = viewModel.mensajeOverlay.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = viewModel.mensajeOverlay,
                    color = Color(viewModel.colorOverlay),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 15f)
                    )
                )
            }
        }

        //CONTROLES INFERIORES
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1E2B32)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!viewModel.juegoActivo) {
                // Estado 1: Elegir apuesta e iniciar
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(100, 500, 1000,5000).forEach { monto ->
                        Button(
                            onClick = { viewModel.apuestaInicial = monto },
                            colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.apuestaInicial == monto) Color(0xFF00FF87) else Color(0xFF2C3E50))
                        ) {
                            Text(monto.toString(), color = if (viewModel.apuestaInicial == monto) Color.Black else Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.iniciarJuego(albumViewModel.monedas) { costo -> albumViewModel.restarMonedas(costo) }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("INICIAR TANDA", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            } else {
                // Estado 2: En pleno juego, opción de retirarse
                Button(
                    onClick = {
                        viewModel.retirarseConGanancias { premio -> albumViewModel.sumarMonedas(premio)}
                    },
                    enabled = viewModel.rachaGoles > 0 && !viewModel.animandoTiro,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color.DarkGray
                    )
                ) {
                    Text(
                        text = if (viewModel.rachaGoles > 0) "RETIRARSE CON 🪙 ${viewModel.premioAcumulado}" else "SELECCIONA UNA ZONA",
                        color = if (viewModel.rachaGoles > 0 && !viewModel.animandoTiro) Color.Black else Color.LightGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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