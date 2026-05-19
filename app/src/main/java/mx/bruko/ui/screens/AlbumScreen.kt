package mx.bruko.ui.screens

// --- Compose Foundation ---
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Compose Material 3 ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

// --- Compose UI ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Librerías Externas ---
import coil.compose.AsyncImage

import mx.bruko.ui.components.PlayerCard
import mx.bruko.ui.components.obtenerUrlBandera
import mx.bruko.viewModel.AlbumViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(viewModel: AlbumViewModel) {
    val albumData = viewModel.albumByCountry
    val countries = albumData.keys.toList()
    val pagerState = rememberPagerState(pageCount = { countries.size })

    // ==========================================
    // CÁLCULOS DE PROGRESO
    // ==========================================
    val totalJugadores = albumData.values.flatten().size
    val jugadoresPegados = albumData.values.flatten().count { it.pegado }
    val porcentajeActual = if (totalJugadores > 0) jugadoresPegados.toFloat() / totalJugadores else 0f

    val progresoAnimado by animateFloatAsState(
        targetValue = porcentajeActual,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "BarraDopamina"
    )

    // ==========================================
    // ESTILOS DE FONDO AMBIENTAL (Glow de fondo)
    // ==========================================
    val backgroundBase = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF020617)) // Slate 900 a Slate 950
    )
    val ambientGlow = Brush.radialGradient(
        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), Color.Transparent),
        radius = 1200f
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundBase)) {
        // Capa de brillo ambiental de fondo
        Box(modifier = Modifier.fillMaxSize().background(ambientGlow))

        Column(modifier = Modifier.fillMaxSize()) {

            // ==========================================
            // HEADER: BARRA DE PROGRESO NEÓN PREMIUM
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 8.dp) // Reducido el top padding
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "COLECCIÓN",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "$jugadoresPegados / $totalJugadores",
                        color = Color(0xFF00E5FF), // Cian Neón
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Contenedor de la barra de dopamina (Efecto profundidad)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF1E293B)) // Slate 800
                        .border(1.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(7.dp))
                ) {
                    // Relleno animado (Degradado Neón)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progresoAnimado)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00FF87), Color(0xFF00E5FF))
                                )
                            )
                    )
                }
                Text(
                    text = "${(progresoAnimado * 100).toInt()}% COMPLETADO",
                    color = Color(0xFF94A3B8), // Slate 400
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.End).padding(top = 6.dp)
                )
            }

            // ==========================================
            // PÁGINAS DEL ÁLBUM (GLASSMORPHISM)
            // ==========================================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp) // Tarjetas un poco más anchas
            ) { page ->
                val country = countries[page]
                val players = albumData[country] ?: emptyList()

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .graphicsLayer {
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val absOffset = kotlin.math.abs(pageOffset)

                            val scale = 1f - (absOffset * 0.08f) // Escala más suave
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - (absOffset * 0.5f)
                        },
                    shape = RoundedCornerShape(16.dp),
                    // Fondo Glassmorphism Dark Premium
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D).copy(alpha = 0.75f)),
                    // Borde de luz muy fino para el efecto cristal
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp) // Quitamos sombra nativa para mejor rendimiento con alpha
                ) {
                    Column {
                        // Encabezado del País (Gradiente suave en lugar de color sólido)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0B101A).copy(alpha = 0.8f), Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = obtenerUrlBandera(country),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape) // Borde en bandera
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = country.uppercase(),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        // Cuadrícula de Estampas
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(players) { player ->
                                // Aquí suponemos que PlayerCard ya tiene un diseño premium,
                                // al estar sobre un fondo oscuro, resaltará automáticamente.
                                if (player.pegado) {
                                    Box(modifier = Modifier.graphicsLayer {
                                        shadowElevation = 15f
                                        shape = RoundedCornerShape(10.dp)
                                        ambientShadowColor = Color(0xFF00E5FF) // Sombra cian para las cartas pegadas
                                    }) {
                                        PlayerCard(player = player)
                                    }
                                } else {
                                    HuecoVacioHolograma(nombre = player.nombre)
                                }
                            }
                        }
                    }
                }
            }

            // Indicador inferior
            Text(
                text = "${pagerState.currentPage + 1} / ${countries.size}",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp, top = 8.dp),
                color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
        }
    }
}

// ==========================================
// COMPONENTE: HUECO VACÍO (Estilo Holograma/Placeholder Gaming)
// ==========================================
@Composable
fun HuecoVacioHolograma(nombre: String) {
    Box(
        modifier = Modifier
            .aspectRatio(0.7f) // Proporción clásica
            .clip(RoundedCornerShape(10.dp))
            // Fondo oscuro muy sutilmente tintado de cian
            .background(Color(0xFF00E5FF).copy(alpha = 0.03f))
            // Borde luminoso tenue
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PersonOutline,
                contentDescription = "Falta",
                tint = Color(0xFF00E5FF).copy(alpha = 0.3f), // Icono fantasma
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nombre.uppercase(),
                color = Color(0xFF94A3B8).copy(alpha = 0.7f), // Texto gris-azulado sutil
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}