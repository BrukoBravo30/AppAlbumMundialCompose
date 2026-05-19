package mx.bruko.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import mx.bruko.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicPlayerWidget() {
    val context = LocalContext.current

    val playlist = listOf(
        R.raw.careless,
        R.raw.dreaming,
        R.raw.feet,
        R.raw.genius,
        R.raw.kids,
        R.raw.loveme,
        R.raw.type,
        R.raw.yoxti,
        R.raw.dakiti
    )

    val nombresCanciones = listOf(
        "Careless Whisper - George Michael",
        "Dreaming - Smallpools",
        "Feet Don't Fail Me Now - Joy Crookes",
        "Genius - Sia",
        "Kids - MGMT",
        "love me again - John Newman",
        "My type - Saint Motel",
        "Yo x ti, tu x mi - Rosalia",
        "Dakiti - Bad Bunny"
    )

    var currentTrackIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var musicEnabled by remember { mutableStateOf(true) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observador del ciclo de vida
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying && musicEnabled) mediaPlayer?.start()
                Lifecycle.Event.ON_DESTROY -> mediaPlayer?.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mediaPlayer?.release()
        }
    }

    val playSong = {
        if (musicEnabled) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, playlist[currentTrackIndex])
            mediaPlayer?.setOnCompletionListener {
                currentTrackIndex = (currentTrackIndex + 1) % playlist.size
            }
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    LaunchedEffect(currentTrackIndex, musicEnabled) {
        if (musicEnabled) {
            playSong()
        } else {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    // ==========================================
    // UI PREMIUM (Colores y Materiales)
    // ==========================================
    val neonCyan = Color(0xFF00E5FF)
    val glassBg = Color(0xFF0F172A).copy(alpha = 0.85f)
    val glassBorder = Brush.horizontalGradient(
        listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color(0xFFC084FC).copy(alpha = 0.3f))
    )

    // Parseo inteligente del nombre de la canción
    val textoCompleto = nombresCanciones[currentTrackIndex]
    val partesTexto = textoCompleto.split(" - ")
    val songTitle = partesTexto.firstOrNull() ?: textoCompleto
    val artistName = if (partesTexto.size > 1) partesTexto[1] else "Unknown Artist"

    if (musicEnabled) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .shadow(16.dp, RoundedCornerShape(50), ambientColor = neonCyan, spotColor = neonCyan)
                    .clip(RoundedCornerShape(50))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. ÍCONO DE DISCO
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF020617))))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = neonCyan.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. TEXTOS Y ECUALIZADOR
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedEqualizer(isPlaying = isPlaying, color = neonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = songTitle,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, velocity = 30.dp)
                        )
                    }
                    Text(
                        text = artistName,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3. CONTROLES MINIMALISTAS
                val interactionSource = remember { MutableInteractionSource() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Atrás",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                currentTrackIndex = if (currentTrackIndex - 1 < 0) playlist.size - 1 else currentTrackIndex - 1
                            }
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    PulsingPlayButton(
                        isPlaying = isPlaying,
                        color = neonCyan,
                        onClick = {
                            if (isPlaying) mediaPlayer?.pause() else mediaPlayer?.start()
                            isPlaying = !isPlaying
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Siguiente",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                currentTrackIndex = (currentTrackIndex + 1) % playlist.size
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Botón para apagar la música sutil
                    Icon(
                        imageVector = Icons.Filled.VolumeOff,
                        contentDescription = "Apagar",
                        tint = Color(0xFF64748B), // Slate 500
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(interactionSource = interactionSource, indication = null) { musicEnabled = false }
                    )
                }
            }
        }
    } else {
        // ==========================================
        // BOTÓN FLOTANTE: ENCENDER MÚSICA
        // ==========================================
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 36.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), CircleShape)
                    .clickable { musicEnabled = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "Encender", tint = neonCyan, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ==========================================
// MICRO-ANIMACIONES
// ==========================================
@Composable
fun AnimatedEqualizer(isPlaying: Boolean, color: Color) {
    val transition = rememberInfiniteTransition(label = "eq")
    val heights = List(3) { index ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(300 + (index * 150), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "eq_bar"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(12.dp)
    ) {
        heights.forEach { heightRatio ->
            val currentHeight = if (isPlaying) heightRatio.value else 0.3f
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .fillMaxHeight(currentHeight)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
fun PulsingPlayButton(isPlaying: Boolean, color: Color, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .border(1.dp, color.copy(alpha = pulseAlpha), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}