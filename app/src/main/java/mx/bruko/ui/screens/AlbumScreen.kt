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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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

// Helper para colores de borde según rareza (Premium Blue/Neon)
fun getBorderRarityColors(rareza: String): List<Color> {
    return when (rareza) {
        "unico" -> listOf(Color(0xFF00E5FF), Color(0xFF0072FF)) // Cian a Azul Profundo
        "Diamante" -> listOf(Color(0xFFD8B4FE), Color(0xFF7E22CE)) // Lavanda a Púrpura
        "Oro" -> listOf(Color(0xFFFCD34D), Color(0xFFB45309)) // Oro brillante a oscuro
        "Plata" -> listOf(Color(0xFFE2E8F0), Color(0xFF64748B)) // Plata a Slate
        else -> listOf(Color(0xFFD97706), Color(0xFF78350F)) // Bronce
    }
}

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
    // CONTENEDOR PRINCIPAL TRANSPARENTE
    // ==========================================
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ==========================================
            // HEADER: BARRA DE PROGRESO NEÓN PREMIUM
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
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
                        color = Color(0xFF00E5FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF0B1B3D).copy(alpha = 0.8f)) // Transparencia sutil
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(7.dp))
                ) {
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
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.End).padding(top = 6.dp)
                )
            }

            // ==========================================
            // PÁGINAS DEL ÁLBUM
            // ==========================================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp)
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

                            val scale = 1f - (absOffset * 0.08f)
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - (absOffset * 0.5f)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    // Fondo Glassmorphism Diagonal (Ajustado para que flote sobre el fondo global)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF131C2D).copy(alpha = 0.65f), // Más cristalino
                                        Color(0xFF08101E).copy(alpha = 0.85f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    ) {
                        Column {
                            // Encabezado del País
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF040B16).copy(alpha = 0.7f), Color.Transparent)
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
                                            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
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
                                    if (player.pegado) {
                                        val isHighRating = player.rareza == "unico" || player.rareza == "Diamante"
                                        val borderColors = getBorderRarityColors(player.rareza)

                                        // CONTENEDOR DE CARTA
                                        Box(modifier = Modifier
                                            .graphicsLayer {
                                                shape = RoundedCornerShape(12.dp)
                                                shadowElevation = if (isHighRating) 30f else 8f
                                                ambientShadowColor = borderColors[0]
                                                spotShadowColor = borderColors[0]
                                            }
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = if (isHighRating) 2.dp else 1.dp,
                                                brush = Brush.linearGradient(borderColors),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        ) {
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
// COMPONENTE: HUECO VACÍO
// ==========================================
@Composable
fun HuecoVacioHolograma(nombre: String) {
    Box(
        modifier = Modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0B1426).copy(alpha = 0.5f), Color(0xFF060B14).copy(alpha = 0.5f)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
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
                tint = Color(0xFF00E5FF).copy(alpha = 0.3f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nombre.uppercase(),
                color = Color(0xFF94A3B8).copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}