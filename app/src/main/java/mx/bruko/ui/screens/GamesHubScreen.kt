package mx.bruko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.bruko.viewModel.AlbumViewModel

@Composable
fun GamesHubScreen(viewModel: AlbumViewModel,
                   onPlayWordle: () -> Unit,
                   onPlayPlinko: () -> Unit) {
    // Fondo de Casino (Tonos púrpuras y negros muy oscuros)
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF120024), Color(0xFF000000)))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // --- HEADER DEL CASINO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp), // Margen top alto por la música
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ZONA ARCADE", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("CASINO VIP", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            // Saldo actual
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🪙 ${viewModel.monedas}",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // --- LISTA DE JUEGOS ---
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Juego 1: Wordle Futbolero (El que vamos a programar)
            item {
                GameCard(
                    title = "Wordle Futbolero",
                    description = "Adivina el jugador oculto con pistas. ¡Apuesta y multiplica tus monedas hasta x100!",
                    icon = Icons.Filled.SportsSoccer,
                    accentColor = Color(0xFF4CAF50),
                    isLocked = false,
                    onClick = {
                        onPlayWordle()
                    }
                )
            }

            // Juego 2: Plinko
            item {
                GameCard(
                    title = "Plinko Stake",
                    description = "La pirámide del destino. ¡Deja caer la bola y busca el ansiado x100!",
                    icon = Icons.Filled.Casino, // Asegúrate de importar el ícono o usa uno que prefieras
                    accentColor = Color(0xFF00FF87),
                    isLocked = false,
                    onClick = { onPlayPlinko() } // <- CONEXIÓN AL JUEGO
                )
            }

            // Juego 3: Tragamonedas (Placeholder)
            item {
                GameCard(
                    title = "Tragamonedas 777",
                    description = "Haz coincidir 3 escudos del mismo equipo para llevarte el Jackpot.",
                    icon = Icons.Filled.Casino,
                    accentColor = Color(0xFFFFC107),
                    isLocked = true,
                    onClick = { }
                )
            }
        }
    }
}

// Componente visual para cada juego del Hub
@Composable
fun GameCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Barra lateral de color
            Box(modifier = Modifier.fillMaxHeight().width(8.dp).background(if (isLocked) Color.DarkGray else accentColor))

            // Contenido
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono del juego
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLocked) Color(0xFF2C2C2C) else accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Filled.Lock else icon,
                        contentDescription = null,
                        tint = if (isLocked) Color.Gray else accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Textos
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = if (isLocked) Color.Gray else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isLocked) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = Color.DarkGray) { Text("Próximamente", color = Color.LightGray, fontSize = 10.sp) }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}