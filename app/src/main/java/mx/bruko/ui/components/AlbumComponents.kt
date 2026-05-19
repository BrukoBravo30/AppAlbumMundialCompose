package mx.bruko.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mx.bruko.data.Player

// --- 1. DEFINICIÓN DE RAREZAS PREMIUM ---
data class RarityConfig(
    val bgColors: List<Color>,
    val borderColor: Color,
    val textColor: Color,
    val glowColor: Color,
    val ratingString: String
)

fun getPremiumRarityConfig(rareza: String): RarityConfig {
    return when (rareza) {
        "unico" -> RarityConfig(
            bgColors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF020617)),
            borderColor = Color(0xFF00E5FF),
            textColor = Color(0xFFE0FFFF),
            glowColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
            ratingString = "99"
        )
        "Diamante" -> RarityConfig(
            bgColors = listOf(Color(0xFF2E1065), Color(0xFF4C1D95), Color(0xFF170830)),
            borderColor = Color(0xFFD8B4FE),
            textColor = Color(0xFFF3E8FF),
            glowColor = Color(0xFF9333EA).copy(alpha = 0.4f),
            ratingString = "92"
        )
        "Oro" -> RarityConfig(
            bgColors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF260E04)),
            borderColor = Color(0xFFFCD34D),
            textColor = Color(0xFFFEF3C7),
            glowColor = Color(0xFFF59E0B).copy(alpha = 0.3f),
            ratingString = "86"
        )
        "Plata" -> RarityConfig(
            bgColors = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF0F172A)),
            borderColor = Color(0xFFCBD5E1),
            textColor = Color(0xFFF8FAFC),
            glowColor = Color(0xFF94A3B8).copy(alpha = 0.2f),
            ratingString = "78"
        )
        else -> RarityConfig(
            bgColors = listOf(Color(0xFF3B2F2F), Color(0xFF5C4033), Color(0xFF261C1C)),
            borderColor = Color(0xFFCD7F32),
            textColor = Color(0xFFFFDAB9),
            glowColor = Color(0xFF8B4513).copy(alpha = 0.1f),
            ratingString = "65"
        )
    }
}

@Composable
fun PlayerCard(player: Player) {
    val config = getPremiumRarityConfig(player.rareza)

    // Animación continua para el brillo holográfico
    val infiniteTransition = rememberInfiniteTransition(label = "FoilAnimation")
    val foilOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FoilOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f) // Ligeramente más alta, estilo trading card real
            .padding(4.dp)
            // Sombra exterior tintada con el color de la rareza
            .graphicsLayer {
                shadowElevation = 24f
                ambientShadowColor = config.glowColor
                spotShadowColor = config.glowColor
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)) // Negro puro de base
    ) {
        if (player.pegado) {
            Box(modifier = Modifier.fillMaxSize()) {

                // --- CAPA 1: FONDO RADIAL & TEXTURA FUTURISTA ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.radialGradient(config.bgColors))
                )

                // Patrón de líneas diagonales para dar textura de eSports
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            val numLines = 40
                            val strokeWidth = 1.dp.toPx()
                            val spacing = size.height / numLines
                            for (i in -numLines..numLines * 2) {
                                val startX = -size.width
                                val startY = i * spacing
                                val endX = size.width * 2
                                val endY = startY + (size.width * 3) // Ángulo diagonal
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.15f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = strokeWidth,
                                    blendMode = BlendMode.Overlay
                                )
                            }
                        }
                )

                // --- CAPA 2: MARCO INTERIOR (Physical Card Feel) ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .border(1.dp, config.borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                )

                // --- CAPA 3: BADGES (Rating & Posición) ---
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.ratingString,
                        color = config.textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(color = Color.Black, blurRadius = 8f, offset = Offset(2f, 2f))
                        )
                    )
                    Text(
                        text = player.posicion,
                        color = config.borderColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(color = Color.Black, blurRadius = 4f, offset = Offset(1f, 1f))
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AsyncImage(
                        model = obtenerUrlBandera(player.pais),
                        contentDescription = "Bandera",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.dp, config.borderColor.copy(alpha = 0.8f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // --- CAPA 4: RENDER DEL JUGADOR ---
                // Sombra suave proyectada por el jugador sobre el fondo
                AsyncImage(
                    model = player.foto_url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.82f)
                        .align(Alignment.BottomEnd) // Alinear a la derecha para dar espacio a los badges
                        .graphicsLayer {
                            shadowElevation = 8f
                            ambientShadowColor = Color.Black
                        },
                    contentScale = ContentScale.Fit
                )

                // --- CAPA 5: PANEL DE NOMBRE INFERIOR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(55.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                            )
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Línea divisoria brillante
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, config.borderColor, Color.Transparent)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = player.nombre.uppercase(),
                            color = config.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(color = config.glowColor, blurRadius = 12f)
                            )
                        )
                    }
                }

                // --- CAPA 6: DESTELLO HOLOGRÁFICO SUPERPUESTO ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = if (player.rareza == "unico" || player.rareza == "Diamante") 0.4f else 0.15f
                        }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.1f),
                                    config.borderColor.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, foilOffset * 1000f),
                                end = Offset(1000f, (foilOffset + 1f) * 1000f)
                            )
                        )
                )

            }
        } else {
            // --- ESTADO: NO OBTENIDA (Silueta Elegante) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)), // Gris pálido/Azulado
                contentAlignment = Alignment.Center
            ) {
                // Marco interior tenue
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Person,
                        contentDescription = "Falta",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = player.posicion,
                        color = Color(0xFF475569),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

fun obtenerUrlBandera(pais: String): String {
    // Mantengo tu función intacta
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
        else -> "un"
    }
    return "https://flagcdn.com/w80/$codigo.png"
}