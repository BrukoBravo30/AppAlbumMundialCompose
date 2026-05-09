package mx.bruko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.bruko.ui.components.PlayerCard
import mx.bruko.viewModel.AlbumViewModel

@Composable
fun InventoryScreen(viewModel: AlbumViewModel) {
    val inventario = viewModel.inventario
    // AGRUPACIÓN: Juntamos las cartas idénticas
    val inventarioAgrupado = inventario.groupBy { it.nombre }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MI ALMACÉN", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("🪙 ${viewModel.monedas}", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Total de cartas: ${inventario.size} | Jugadores únicos: ${inventarioAgrupado.size}",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (inventarioAgrupado.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Almacén vacío.\n¡Abre sobres para conseguir jugadores!", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Iteramos sobre los grupos, no sobre las cartas individuales
                items(inventarioAgrupado.keys.toList()) { nombreJugador ->
                    val listaCopias = inventarioAgrupado[nombreJugador]!!
                    val jugadorModelo = listaCopias.first() // Usamos la primera copia como modelo visual
                    val cantidad = listaCopias.size

                    // Verificamos si ya está pegado en el álbum
                    val yaPegado = viewModel.albumByCountry[jugadorModelo.pais]?.find { it.nombre == nombreJugador }?.pegado == true

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                        Column {
                            Box {
                                PlayerCard(player = jugadorModelo) // Muestra la carta

                                // BADGE DE CANTIDAD (Burbuja indicadora)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(Color(0xFF00E5FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("x$cantidad", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }

                            // Botones de acción
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                // Siempre puedes vender
                                Button(
                                    onClick = { viewModel.venderCarta(jugadorModelo) }, // Elimina solo una copia de la lista original
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Vender\n🪙${viewModel.obtenerPrecioVenta(jugadorModelo.rareza)}", fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }

                                Spacer(Modifier.width(8.dp))

                                // Solo puedes pegar si NO está ya en el álbum
                                Button(
                                    onClick = {
                                        if (!yaPegado) viewModel.pegarEnAlbum(jugadorModelo)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (yaPegado) Color.DarkGray else Color(0xFF4CAF50),
                                        contentColor = if (yaPegado) Color.LightGray else Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    enabled = !yaPegado
                                ) {
                                    Text(if (yaPegado) "Ya\nPegado" else "Pegar al\nÁlbum", fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}