package mx.bruko.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import mx.bruko.R

@Composable
fun MusicPlayerWidget() {
    val context = LocalContext.current

    // Aquí pondrás los IDs de tus canciones de la carpeta raw
    // Ejemplo: R.raw.cancion_1, R.raw.cancion_2...
    val playlist = listOf(
        R.raw.careless,
        R.raw.dreaming,
        R.raw.feet,
        R.raw.genius,
        R.raw.kids,
        R.raw.loveme,
        R.raw.type,
        R.raw.yoxti
    )

    val nombresCanciones = listOf(
        "Careless Whisper - George Michael",
        "Dreaming - Smallpools",
        "Feet Don't Fail Me Now - Joy Crookes",
        "Genius - Sia",
        "Kids - MGMT",
        "love me again - John Newman",
        "My type - Saint Motel",
        "Yo x ti, tu x mi - Rosalia"
    )

    var currentTrackIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var musicEnabled by remember { mutableStateOf(true) }

    // Manejador del MediaPlayer
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observador del ciclo de vida del celular
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // Cuando la app se va al fondo o se apaga la pantalla
                Lifecycle.Event.ON_PAUSE -> {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                    }
                }
                // Cuando regresas a la app o prendes la pantalla
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying && musicEnabled) {
                        mediaPlayer?.start()
                    }
                }
                // Cuando cierras la app por completo
                Lifecycle.Event.ON_DESTROY -> {
                    mediaPlayer?.release()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mediaPlayer?.release()
        }
    }

    // Función para reproducir canción
    val playSong = {
        if (musicEnabled) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, playlist[currentTrackIndex])
            mediaPlayer?.setOnCompletionListener {
                // Siguiente canción automática al terminar
                currentTrackIndex = (currentTrackIndex + 1) % playlist.size
            }
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    // Efecto para arrancar la música al inicio o cuando cambias de track
    LaunchedEffect(currentTrackIndex, musicEnabled) {
        if (musicEnabled) {
            playSong()
        } else {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    // Efecto de limpieza (cuando se cierra la app)
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

// UI del Reproductor (Estilo Isla Flotante Premium)
    if (musicEnabled) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // AQUÍ ESTÁ LA MAGIA: top = 32.dp es aprox 1 centímetro hacia abajo
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                .height(56.dp)
                // Le damos bordes completamente redondeados como una cápsula
                .background(Color(0xFF181818), RoundedCornerShape(28.dp))
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ícono y Nombre de la canción
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = nombresCanciones[currentTrackIndex],
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Controles
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    currentTrackIndex = if (currentTrackIndex - 1 < 0) playlist.size - 1 else currentTrackIndex - 1
                }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Atras", tint = Color.White)
                }

                IconButton(onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                    } else {
                        mediaPlayer?.start()
                    }
                    isPlaying = !isPlaying
                }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = {
                    currentTrackIndex = (currentTrackIndex + 1) % playlist.size
                }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Adelantar", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Apagar música
                IconButton(onClick = { musicEnabled = false }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.VolumeOff, contentDescription = "Apagar", tint = Color.Gray)
                }
            }
        }
    } else {
        // Botón flotante para volver a encender la música (También lo bajamos 1 cm)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = { musicEnabled = true },
                modifier = Modifier.background(Color(0xFF181818), CircleShape)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "Encender", tint = Color.White)
            }
        }
    }
}