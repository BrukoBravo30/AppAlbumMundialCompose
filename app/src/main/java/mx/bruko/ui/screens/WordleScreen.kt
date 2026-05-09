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
import androidx.compose.foundation.layout.fillMaxHeight

// Agrega estas importaciones
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import mx.bruko.viewModel.AlbumViewModel
@Composable
fun WordleScreen(
    onBack: () -> Unit,
    viewModel: WordleViewModel = viewModel(),
    albumViewModel: AlbumViewModel // Lo necesitamos para las monedas
) {
    // ESTO CORRIGE EL GESTO DE SALIR:
    // Cuando el usuario deslice desde el borde, ejecutará onBack()
    BackHandler {
        onBack()
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF120024), Color(0xFF000000)))
    var showBetDialog by remember { mutableStateOf(true) } // Ventana de apuestas al iniciar

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER CON BOTÓN DE SALIR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WORDLE FUTBOLERO", color = Color(0xFF00E5FF), fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("🪙 Apuesta actual: ${viewModel.apuestaActual}", color = Color(0xFFFFD700), fontSize = 12.sp)
                }
                // Espacio para equilibrar el botón de la izquierda
                Spacer(modifier = Modifier.size(48.dp))
            }

            // --- TABLERO (CON PESO DINÁMICO) ---
            // Usamos fillMaxHeight(0.6f) o weight para que no pelee con el teclado
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.tablero.forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fila.forEach { celda ->
                                WordleBox(celda = celda, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // --- TECLADO (SIEMPRE ABAJO) ---
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                WordleKeyboard(viewModel = viewModel)
            }
        }

        // --- VENTANA DE APUESTAS (OVERLAY) ---
        if (showBetDialog) {
            BettingDialog(
                monedasDisponibles = albumViewModel.monedas,
                onBetConfirmed = { monto ->
                    albumViewModel.monedas -= monto // Restamos de la cartera global
                    viewModel.apuestaActual = monto
                    showBetDialog = false
                },
                onCancel = onBack
            )
        }

        // --- DIÁLOGO DE RESULTADO ---
        if (viewModel.juegoTerminado) {
            GameResultDialog(
                viewModel = viewModel,
                onConfirm = {
                    // Si ganó, le sumamos el premio al AlbumViewModel
                    if (viewModel.jugadorGano) {
                        val premio = viewModel.apuestaActual * viewModel.multiplicadorGanado
                        albumViewModel.monedas += premio
                    }
                    onBack() // Regresamos al hub para refrescar
                }
            )
        }
    }
}
// ==========================================
// COMPONENTE: CELDA INDIVIDUAL (Animada)
// ==========================================
@Composable
fun WordleBox(celda: CeldaWordle, modifier: Modifier = Modifier) {
    // Animación de color suave cuando cambia el estado de la letra
    val bgColor by animateColorAsState(
        targetValue = when (celda.estado) {
            EstadoLetra.CORRECTA -> Color(0xFF00E5FF) // Cyan Neón
            EstadoLetra.PRESENTE -> Color(0xFFFFD700) // Oro Casino
            EstadoLetra.AUSENTE -> Color(0xFF2C2C2C)  // Gris Oscuro
            EstadoLetra.LLENA, EstadoLetra.VACIA -> Color.Transparent
        },
        animationSpec = tween(500)
    )

    val borderColor = when (celda.estado) {
        EstadoLetra.VACIA -> Color(0xFF333333)
        EstadoLetra.LLENA -> Color.Gray
        else -> Color.Transparent
    }

    val textColor = if (celda.estado == EstadoLetra.VACIA || celda.estado == EstadoLetra.LLENA) {
        Color.White
    } else {
        Color.Black // Texto negro para contrastar con el fondo cyan/oro
    }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Mantiene la forma cuadrada
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

// ==========================================
// COMPONENTE: TECLADO QWERTY
// ==========================================
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
        // Fila 1
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row1.forEach { letra -> KeyButton(letra.toString()) { viewModel.ingresarLetra(letra) } }
        }
        // Fila 2
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row2.forEach { letra -> KeyButton(letra.toString()) { viewModel.ingresarLetra(letra) } }
        }
        // Fila 3 (Con ENTER y BORRAR)
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

// ==========================================
// COMPONENTE: POPUP RESULTADOS
// ==========================================
@Composable
fun GameResultDialog(viewModel: WordleViewModel) {
    AlertDialog(
        onDismissRequest = { /* Forzamos a que toque un botón */ },
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = if (viewModel.jugadorGano) "¡VICTORIA!" else "FIN DEL JUEGO",
                color = if (viewModel.jugadorGano) Color(0xFF00E5FF) else Color(0xFFE91E63),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                if (viewModel.jugadorGano) {
                    Text("¡Multiplicador ganado: x${viewModel.multiplicadorGanado}!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                } else {
                    Text("Has perdido tu apuesta.", color = Color.LightGray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.iniciarNuevoJuego("NEYMAR") /* Para probar otra después de ganar */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("Jugar de nuevo", color = Color.Black, fontWeight = FontWeight.Bold)
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

                // Botones de apuesta rápida
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(100, 500, 1000).forEach { valor ->
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
                Text("APUESTA TOTAL", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}