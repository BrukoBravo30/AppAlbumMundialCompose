package mx.bruko.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.bruko.games.EstadoLetra
import mx.bruko.games.CeldaWordle
import mx.bruko.viewModel.WordleViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import mx.bruko.viewModel.AlbumViewModel
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun WordleScreen(
    onBack: () -> Unit,
    viewModel: WordleViewModel = viewModel(),
    albumViewModel: AlbumViewModel
) {
    BackHandler { onBack() }
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF120024), Color(0xFF000000)))



    LaunchedEffect(Unit) {
        viewModel.validarEstadoAlEntrar()
    }

    // 3. AUTO-REANUDAR (Si acaban de apostar)
    LaunchedEffect(viewModel.apuestaPagada) {
        if (viewModel.apuestaPagada && !viewModel.juegoTerminado) {
            viewModel.iniciarTimer()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {

        if (viewModel.cargandoJugador) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Buscando futbolista en los servidores...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "WORDLE FUTBOLERO",
                            color = Color(0xFF00E5FF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        // --- EL RELOJ VISUAL ---
                        if (viewModel.apuestaPagada && !viewModel.juegoTerminado) {
                            val colorReloj = if (viewModel.tiempoRestante <= 10) Color(0xFFFF003C) else Color(0xFF00FF87)
                            Text(
                                text = "⏳ 00:${viewModel.tiempoRestante.toString().padStart(2, '0')}",
                                color = colorReloj,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = "Nacionalidad: ${viewModel.pistaPais} | Posición: ${viewModel.pistaPosicion}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "Apuesta actual: ${viewModel.apuestaActual}",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(48.dp))
                }

                // --- TABLERO (INTELIGENTE Y RESPONSIVO) ---
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1.8f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Matemáticas para encoger las celdas si la palabra es muy larga
                    val maxCellWidth = 50.dp
                    val spacing = 6.dp
                    val letras = if (viewModel.longitudPalabra > 0) viewModel.longitudPalabra else 1
                    val totalSpacing = spacing * (letras - 1)

                    // Calculamos el tamaño: El espacio disponible dividido entre las letras (con un tope de 50.dp)
                    val cellWidth = minOf(maxCellWidth, (maxWidth - totalSpacing) / letras)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        viewModel.tablero.forEach { fila ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                fila.forEach { celda ->
                                    WordleBox(celda = celda, modifier = Modifier.size(cellWidth))
                                }
                            }
                        }
                    }
                }

                // --- TECLADO ---
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    WordleKeyboard(viewModel = viewModel)
                }
            }
        }

        // --- VENTANA DE APUESTAS (AHORA DEPENDE DE LA BASE DE DATOS LOCAL) ---
        if (!viewModel.apuestaPagada && !viewModel.juegoTerminado) {
            BettingDialog(
                monedasDisponibles = albumViewModel.monedas,
                onBetConfirmed = { monto ->
                    albumViewModel.restarMonedas(monto) // 1. Quita el dinero global
                    viewModel.registrarPago(monto)      // 2. Registra el pago en disco
                    viewModel.iniciarTimer()            // 3. Empieza el reloj
                },
                onCancel = onBack
            )
        }

        // --- DIÁLOGO DE RESULTADO ---
        if (viewModel.juegoTerminado) {
            GameResultDialog(
                viewModel = viewModel,
                onConfirm = {
                    if (viewModel.jugadorGano && !viewModel.premioEntregado) {
                        val premio = viewModel.apuestaActual * viewModel.multiplicadorGanado
                        albumViewModel.sumarMonedas(premio)
                        viewModel.entregarPremio() // Esto limpia la apuestaPagada del disco
                    }
                    viewModel.iniciarNuevoJuego()
                }
            )
        }
    }
}

// ==========================================
// COMPONENTES DE INTERFAZ MANTENIDOS
// ==========================================
@Composable
fun WordleBox(celda: CeldaWordle, modifier: Modifier = Modifier) {
    val bgColor by animateColorAsState(
        targetValue = when (celda.estado) {
            EstadoLetra.CORRECTA -> Color(0xFF00E5FF)
            EstadoLetra.PRESENTE -> Color(0xFFFFD700)
            EstadoLetra.AUSENTE -> Color(0xFF2C2C2C)
            EstadoLetra.LLENA, EstadoLetra.VACIA -> Color.Transparent
        },
        animationSpec = tween(500),
        label = "ColorBox"
    )

    val borderColor = when (celda.estado) {
        EstadoLetra.VACIA -> Color(0xFF333333)
        EstadoLetra.LLENA -> Color.Gray
        else -> Color.Transparent
    }

    val textColor = if (celda.estado == EstadoLetra.VACIA || celda.estado == EstadoLetra.LLENA) {
        Color.White
    } else {
        Color.Black
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = celda.char.toString(),
            color = textColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun WordleKeyboard(viewModel: WordleViewModel) {
    val row1 = listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P')
    val row2 = listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L')
    val row3 = listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row1.forEach { letra -> KeyButton(letra.toString()) { viewModel.ingresarLetra(letra) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row2.forEach { letra -> KeyButton(letra.toString()) { viewModel.ingresarLetra(letra) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionKeyButton(text = "ENT") { viewModel.enviarIntento() }
            row3.forEach { letra -> KeyButton(letra.toString()) { viewModel.ingresarLetra(letra) } }
            ActionKeyButton(icon = Icons.Filled.Backspace) { viewModel.borrarLetra() }
        }
    }
}

@Composable
fun KeyButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF333333))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun ActionKeyButton(text: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF555555))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        } else if (icon != null) {
            Icon(icon, contentDescription = "Borrar", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun GameResultDialog(
    viewModel: WordleViewModel,
    onConfirm: () -> Unit
) {
    val premioReal = viewModel.apuestaActual * viewModel.multiplicadorGanado

    AlertDialog(
        onDismissRequest = { },
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                // Aquí usamos el mensaje temático que creamos en el ViewModel (Ej: "⏱️ ¡Pito final!")
                text = viewModel.mensajeResultado.ifEmpty { if (viewModel.jugadorGano) "¡DESCUBIERTO!" else "FIN DEL JUEGO" },
                color = if (viewModel.jugadorGano) Color(0xFF00E5FF) else Color(0xFFE91E63),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("El jugador oculto era:", color = Color.Gray)
                Text(
                    text = viewModel.palabraSecreta,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (viewModel.jugadorGano && premioReal > 0) {
                    Text("¡Multiplicador: x${viewModel.multiplicadorGanado}!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Premio total: 🪙 $premioReal", color = Color.White, fontSize = 16.sp)
                } else if (viewModel.jugadorGano && premioReal == 0) {
                    Text("Adivinaste tarde...", color = Color.LightGray)
                    Text("¡La casa se queda tu apuesta!", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                } else {
                    Text("Has perdido tu apuesta.", color = Color.LightGray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("Continuar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun BettingDialog(
    monedasDisponibles: Int,
    onBetConfirmed: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var montoApuesta by remember { mutableStateOf(100) }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("¿CUÁNTO DESEAS APOSTAR?", color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪙 Disponibles: $monedasDisponibles", color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1000, 5000, 10000).forEach { valor ->
                        Button(
                            onClick = { if (monedasDisponibles >= valor) montoApuesta = valor },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (montoApuesta == valor) Color(0xFF00E5FF) else Color(0xFF333333)
                            )
                        ) {
                            Text(valor.toString(), color = if (montoApuesta == valor) Color.Black else Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onBetConfirmed(montoApuesta) },
                enabled = monedasDisponibles >= montoApuesta,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("APUESTAR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}