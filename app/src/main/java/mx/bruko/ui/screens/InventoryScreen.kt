package mx.bruko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.bruko.ui.components.PlayerCard
import mx.bruko.viewModel.AlbumViewModel

// Paleta de rarezas premium (Tonos más sofisticados y elegantes)
fun getPremiumRarityColor(rareza: String): Color {
    return when (rareza) {
        "unico" -> Color(0xFF00E5FF) // Cian puro
        "Diamante" -> Color(0xFFC084FC) // Púrpura suave
        "Oro" -> Color(0xFFFBBF24) // Champagne / Oro mate
        "Plata" -> Color(0xFF94A3B8) // Slate gris azulado
        else -> Color(0xFFB45309) // Bronce oscuro
    }
}

@Composable
fun InventoryScreen(viewModel: AlbumViewModel) {
    val inventario = viewModel.inventario
    val inventarioAgrupado = inventario.groupBy { it.nombre }

    // ==========================================
    // CONTENEDOR PRINCIPAL TRANSPARENTE
    // ==========================================
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ==========================================
            // HEADER PREMIUM (Minimalista)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ALMACÉN",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: ${inventario.size}   •   Únicas: ${inventarioAgrupado.size}",
                        color = Color(0xFF64748B), // Slate 500
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Saldo elegante
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f)) // Fondo cristal oscuro
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${viewModel.monedas}",
                        color = Color(0xFFFBBF24), // Oro elegante
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ==========================================
            // GRID DE CARTAS
            // ==========================================
            if (inventarioAgrupado.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.LibraryAdd, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Almacén vacío",
                            color = Color(0xFF475569),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(inventarioAgrupado.keys.toList()) { nombreJugador ->
                        val listaCopias = inventarioAgrupado[nombreJugador]!!
                        val jugadorModelo = listaCopias.first()
                        val cantidad = listaCopias.size
                        val yaPegado = viewModel.albumByCountry[jugadorModelo.pais]?.find { it.nombre == nombreJugador }?.pegado == true
                        val rarityColor = getPremiumRarityColor(jugadorModelo.rareza)

                        // CONTENEDOR GLASSMORPHISM PREMIUM (Sin sombras escandalosas)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF151E2E).copy(alpha = 0.7f)) // Azul marino ahumado
                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                // 1. ZONA DE LA CARTA
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Glow ambiental de rareza (Solo detrás de la carta)
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .blur(40.dp)
                                            .background(rarityColor.copy(alpha = 0.15f), CircleShape)
                                    )

                                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                                        PlayerCard(player = jugadorModelo)
                                    }

                                    // BADGE "xCantidad" (Sutil y elegante)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-6).dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF020617).copy(alpha = 0.9f))
                                            .border(0.5.dp, rarityColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "x$cantidad",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // 2. ZONA DE ACCIONES (Botones Fantasma/Glass)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                    // BOTÓN: PEGAR AL ÁLBUM
                                    val actionColor = if (yaPegado) Color(0xFF334155) else Color(0xFF00E5FF)
                                    val bgPegar = if (yaPegado) Color.Transparent else actionColor.copy(alpha = 0.08f)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgPegar)
                                            .border(0.5.dp, actionColor.copy(alpha = if (yaPegado) 0.2f else 0.3f), RoundedCornerShape(8.dp))
                                            .clickable(enabled = !yaPegado) { viewModel.pegarEnAlbum(jugadorModelo) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (yaPegado) Icons.Filled.Check else Icons.Filled.LibraryAdd,
                                                contentDescription = null,
                                                tint = actionColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (yaPegado) "EN ÁLBUM" else "PEGAR",
                                                color = actionColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }

                                    // BOTÓN: VENDER (Carmín sofisticado, no rojo brillante)
                                    val sellColor = Color(0xFFF43F5E) // Rose/Crimson premium
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(sellColor.copy(alpha = 0.08f))
                                            .border(0.5.dp, sellColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.venderCarta(jugadorModelo) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Sell,
                                                contentDescription = null,
                                                tint = sellColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "VENDER",
                                                color = sellColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "🪙${viewModel.obtenerPrecioVenta(jugadorModelo.rareza)}",
                                                color = sellColor.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}