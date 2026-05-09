package mx.bruko.ui.screens
// --- Compose Foundation (Estructura y Pager) ---
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Compose Material 3 (Componentes Visuales) ---
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text

// --- Compose UI (Modificadores, Gráficos y Texto) ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- Librerías Externas (Coil) ---
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

    // Fondo Minimalista: Gris Grafito a Negro (Hace que las tarjetas resalten)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF2C2C2C), Color(0xFF121212))
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 32.dp) // Muestra el borde de la sig. página
        ) { page ->
            val country = countries[page]
            val players = albumData[country] ?: emptyList()

            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp, horizontal = 8.dp)
                    .graphicsLayer {
                        // LA REGLA DE ORO: Leer los estados de animación SOLO dentro de graphicsLayer.
                        // Así Compose se salta la fase de "Layout" y va directo a "Draw", usando solo la GPU.
                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val absOffset = kotlin.math.abs(pageOffset)

                        // 1. Escala limpia
                        val scale = 1f - (absOffset * 0.1f)
                        scaleX = scale
                        scaleY = scale

                        // 2. Desvanecimiento
                        alpha = 1f - (absOffset * 0.4f)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F2)),
                // Dejamos la elevación fija. Animar sombras colapsa el hilo principal.
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column {
                    // Encabezado Sólido Minimalista
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(Color(0xFF1E1E1E)) // Gris oscuro casi negro
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = obtenerUrlBandera(country),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = country.uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(players) { player ->
                            PlayerCard(player = player)
                        }
                    }
                }
            }
        }

        // Indicador inferior limpio
        Text(
            text = "${pagerState.currentPage + 1} / ${countries.size}",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}