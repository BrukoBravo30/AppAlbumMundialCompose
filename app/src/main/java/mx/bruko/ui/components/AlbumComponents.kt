package mx.bruko.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mx.bruko.data.Player
import mx.bruko.ui.theme.ShieldShape

@Composable
fun PlayerCard(player: Player) {
    // 1. Definimos los colores base para las rarezas estándar
    val (colorBase, colorFuerte) = when (player.rareza) {
        "Diamante" -> Pair(Color(0xFF81D4FA), Color(0xFF01579B))
        "Oro" -> Pair(Color(0xFFFFF59D), Color(0xFFF57F17))
        "Plata" -> Pair(Color(0xFFEEEEEE), Color(0xFF616161))
        "Bronce" -> Pair(Color(0xFFFFCC80), Color(0xFFE65100))
        "unico" -> Pair(Color(0xFF000000), Color(0xFF6200EA)) // Base para el estado 'unico'
        else -> Pair(Color(0xFFE0E0E0), Color(0xFF9E9E9E))
    }

    // 2. Definimos el Brush (Degradado) especial para el Top 10
    val fondoBrush = if (player.rareza == "unico") {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F0C29), // Negro Azulado
                Color(0xFF302B63), // Violeta Profundo
                Color(0xFF24243E), // Azul Medianoche
                Color(0xFF00FFFF), // Destello Cian (El toque "único")
                Color(0xFF0F0C29)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(colorBase, colorFuerte, colorBase)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),

        elevation = CardDefaults.cardElevation(if (player.rareza == "unico") 16.dp else 8.dp)

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (player.pegado) Color.White else Color(0xFFE0E0E0))
        ) {
            if (player.pegado) {
                // --- CAPA 1: Fondo Degradado (Aquí aplicamos el Brush dinámico) ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fondoBrush)
                )

                // --- CAPA 2: Abreviatura del País ---
                val codigoPais = player.pais.take(3).uppercase()
                Text(
                    text = codigoPais,
                    fontSize = 85.sp,
                    fontWeight = FontWeight.Black,
                    // Si es único, la marca de agua brilla un poco más en azul
                    color = if (player.rareza == "unico") Color(0xFF00E5FF).copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.25f),
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-15).dp)
                        .rotate(-90f)
                )

                // --- CAPA 3: Foto del Jugador ---
                AsyncImage(
                    model = player.foto_url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.85f)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.Fit
                )

                // --- CAPA 4: Nombre y Bandera ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    // Si es único, el degradado inferior es violeta oscuro, no negro
                                    if (player.rareza == "unico") Color(0xFF311B92).copy(alpha = 0.8f)
                                    else Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = obtenerUrlBandera(player.pais),
                        contentDescription = "Bandera",
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = player.nombre.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        // Un pequeño efecto de brillo para el nombre en los únicos
                        letterSpacing = if (player.rareza == "unico") 1.sp else 0.sp
                    )
                }

            } else {
                // --- ESTADO: NO OBTENIDA ---
                Column(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFCCCCCC), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "?", color = Color.Gray, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = player.posicion, color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// Función auxiliar con el diccionario completo de tus 48 selecciones
fun obtenerUrlBandera(pais: String): String {
    val codigo = when (pais.lowercase()) {
        "inglaterra" -> "gb-eng"
        "francia" -> "fr"
        "españa" -> "es"
        "portugal" -> "pt"
        "brasil" -> "br"
        "alemania" -> "de"
        "países bajos", "paises bajos" -> "nl"
        "argentina" -> "ar"
        "bélgica", "belgica" -> "be"
        "noruega" -> "no"
        "senegal" -> "sn"
        "marruecos" -> "ma"
        "turquía", "turquia" -> "tr"
        "costa de marfil" -> "ci"
        "ecuador" -> "ec"
        "suecia" -> "se"
        "uruguay" -> "uy"
        "estados unidos" -> "us"
        "suiza" -> "ch"
        "colombia" -> "co"
        "croacia" -> "hr"
        "japón", "japon" -> "jp"
        "austria" -> "at"
        "argelia" -> "dz"
        "ghana" -> "gh"
        "escocia" -> "gb-sct"
        "chequia" -> "cz"
        "república democrática del congo", "republica democratica del congo" -> "cd"
        "corea del sur" -> "kr"
        "paraguay" -> "py"
        "canadá", "canada" -> "ca"
        "bosnia-herzegovina", "bosnia y herzegovina" -> "ba"
        "egipto" -> "eg"
        "méxico", "mexico" -> "mx"
        "uzbekistán", "uzbekistan" -> "uz"
        "haití", "haiti" -> "ht"
        "túnez", "tunez" -> "tn"
        "australia" -> "au"
        "cabo verde" -> "cv"
        "sudáfrica", "sudafrica" -> "za"
        "panamá", "panama" -> "pa"
        "curazao" -> "cw"
        "arabia saudita" -> "sa"
        "irán", "iran" -> "ir"
        "nueva zelanda" -> "nz"
        "irak" -> "iq"
        "catar", "qatar" -> "qa"
        "jordania" -> "jo"
        else -> "un" // Default (Naciones Unidas) en caso de que haya algún error de dedo en la BD
    }
    return "https://flagcdn.com/w80/$codigo.png"
}