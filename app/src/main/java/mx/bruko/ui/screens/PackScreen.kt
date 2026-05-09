package mx.bruko.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// Modelo visual de los sobres en la tienda
data class SobreUI(val tipo: TipoSobre, val nombre: String, val colorUrl: String)

@Composable
fun PackScreen(viewModel: AlbumViewModel) {
    var state by remember { mutableStateOf(PackState.TIENDA) }
    var sobreSeleccionado by remember { mutableStateOf<SobreUI?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var nuevasCartas by remember { mutableStateOf<List<Player>>(emptyList()) }

    // Fondo oscuro premium para la tienda
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF0A0A0A)))

    // Catálogo de sobres (Reemplaza estas URLs por tus propios PNGs en GitHub si lo deseas)
    val catalogoSobres = listOf(
        SobreUI(TipoSobre.NORMAL, "Sobre de Oro", "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/sobre_normal.png"),
        SobreUI(TipoSobre.PREMIUM, "Sobre Jumbo Premium", "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/sobre_premium.png"),
        SobreUI(TipoSobre.ULTIMATE, "Sobre Ultimate", "https://raw.githubusercontent.com/BrukoBravo30/AlbumMundialImages/main/sobre_unico.png")
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {

        // --- HEADER GLOBAL DE MONEDAS ---
        // Siempre visible en la parte superior derecha
        Text(
            text = "🪙 ${viewModel.monedas}",
            color = Color(0xFFFFD700),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // ==========================================
        // ESTADO 1: LA TIENDA (Cascada Vertical)
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.TIENDA,
            exit = fadeOut(tween(300))
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 80.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "TIENDA OFICIAL",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(catalogoSobres) { sobre ->
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(180.dp)
                            .clickable {
                                sobreSeleccionado = sobre
                                showDialog = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Imagen del Sobre a la izquierda
                            AsyncImage(
                                model = sobre.colorUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Fit
                            )

                            // Detalles a la derecha
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .padding(end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = sobre.nombre,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Botón de precio
                                Button(
                                    onClick = {
                                        sobreSeleccionado = sobre
                                        showDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(8.dp)
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

        // ==========================================
        // POPUP DE CONFIRMACIÓN DE COMPRA
        // ==========================================
        if (showDialog && sobreSeleccionado != null) {
            val puedeComprar = viewModel.monedas >= sobreSeleccionado!!.tipo.precio

            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color(0xFF2C2C2C),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = {
                    Text("Confirmar Compra", fontWeight = FontWeight.Bold)
                },
                text = {
                    if (puedeComprar) {
                        Text("¿Deseas gastar ${sobreSeleccionado!!.tipo.precio} monedas por el ${sobreSeleccionado!!.nombre}?")
                    } else {
                        Text("No tienes suficientes monedas para este sobre. Vende algunas repetidas en el almacén.")
                    }
                },
                confirmButton = {
                    if (puedeComprar) {
                        Button(
                            onClick = {
                                showDialog = false
                                state = PackState.ABRIENDO
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("Comprar", color = Color.Black, fontWeight = FontWeight.Bold)
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
        // ESTADO 2: ANIMACIÓN DE APERTURA (Temblor)
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.ABRIENDO && sobreSeleccionado != null,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(300))
        ) {
            val offsetX = remember { Animatable(0f) }
            val scale by animateFloatAsState(
                targetValue = if (state == PackState.ABRIENDO) 1.2f else 1f,
                animationSpec = tween(1500)
            )

            // Secuencia automática de animación y cobro
            LaunchedEffect(Unit) {
                // 1. Efecto de temblor
                for (i in 0..15) {
                    offsetX.animateTo(if (i % 2 == 0) 25f else -25f, tween(50))
                }
                offsetX.animateTo(0f)

                // 2. Ejecutar la compra en el ViewModel
                val resultado = viewModel.abrirSobre(sobreSeleccionado!!.tipo)
                if (resultado != null) {
                    nuevasCartas = resultado
                    delay(300) // Breve pausa dramática
                    state = PackState.ABIERTO
                } else {
                    // Fallback por si hay un error de fondos de último milisegundo
                    state = PackState.TIENDA
                }
            }

            // Imagen del sobre en el centro vibrando
            AsyncImage(
                model = sobreSeleccionado?.colorUrl,
                contentDescription = "Abriendo Sobre",
                modifier = Modifier
                    .width(250.dp)
                    .height(350.dp)
                    .graphicsLayer {
                        translationX = offsetX.value
                        scaleX = scale
                        scaleY = scale
                    },
                contentScale = ContentScale.Fit
            )
        }

        // ==========================================
        // ESTADO 3: CARTAS REVELADAS
        // ==========================================
        AnimatedVisibility(
            visible = state == PackState.ABIERTO,
            enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.5f),
            exit = fadeOut(tween(500))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "¡NUEVAS CARTAS!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Fila deslizable con las 5 cartas
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(nuevasCartas) { jugador ->
                        Box(modifier = Modifier.width(220.dp)) {
                            PlayerCard(player = jugador)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Las cartas se han enviado a tu Almacén",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        sobreSeleccionado = null
                        state = PackState.TIENDA // Reiniciamos el ciclo
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp).width(200.dp)
                ) {
                    Text("Continuar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}