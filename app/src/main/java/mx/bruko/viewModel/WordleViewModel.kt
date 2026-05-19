package mx.bruko.viewModel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.bruko.games.CeldaWordle
import mx.bruko.games.EstadoLetra

class WordleViewModel(private val contexto: Application) : AndroidViewModel(contexto) {

    // ==========================================
    // PERSISTENCIA (Disco Duro)
    // ==========================================
    private val sharedPreferences = contexto.getSharedPreferences("WordleDatos", Context.MODE_PRIVATE)

    var apuestaPagada by mutableStateOf(sharedPreferences.getBoolean("apuesta_pagada", false))
        private set
    var apuestaActual by mutableStateOf(sharedPreferences.getInt("apuesta_actual", 0))

    // 1. CONFIGURACIÓN DEL JUEGO
    var palabraSecreta by mutableStateOf("")
        private set
    var nombreCompletoReal by mutableStateOf("")
        private set
    var pistaPais by mutableStateOf("")
        private set
    var pistaPosicion by mutableStateOf("")
        private set
    var cargandoJugador by mutableStateOf(false)
        private set
    val maxIntentos = 5

    var longitudPalabra by mutableStateOf(0)
        private set

    // 2. ESTADO DEL TABLERO
    var tablero = mutableStateListOf<List<CeldaWordle>>()
        private set

    var intentoActual by mutableStateOf(0)
        private set
    var columnaActual by mutableStateOf(0)
        private set

    // Estado de la partida
    var juegoTerminado by mutableStateOf(false)
        private set
    var jugadorGano by mutableStateOf(false)
        private set
    var mensajeResultado by mutableStateOf("")
        private set

    // Multiplicador del Casino
    var multiplicadorGanado by mutableStateOf(0)
        private set
    var premioEntregado by mutableStateOf(false)
        private set

    // Temporizador
    var tiempoRestante by mutableIntStateOf(40)
        private set
    private var timerJob: Job? = null

    private var tiempoFinMillis by mutableStateOf(sharedPreferences.getLong("tiempo_fin_millis", 0L))

    init {
        if (apuestaPagada) {
            sincronizarReloj()
            if (!juegoTerminado) iniciarTimer()
        }
        // Solo generamos un jugador nuevo si no hay un juego activo pagado previamente
        obtenerJugadorAleatorioDeFirestore()
    }

    // ==========================================
    // LÓGICA DE APUESTAS Y DISCO
    // ==========================================
    fun registrarPago(monto: Int) {
        apuestaActual = monto
        apuestaPagada = true

        // Calculamos la hora exacta en la que se acaba el tiempo (Hora actual + 40 segundos)
        val nuevoFin = System.currentTimeMillis() + (40 * 1000L)
        tiempoFinMillis = nuevoFin

        val editor = sharedPreferences.edit()
        editor.putBoolean("apuesta_pagada", true)
        editor.putInt("apuesta_actual", monto)
        editor.putLong("tiempo_fin_millis", nuevoFin) // Guardamos la hora límite
        editor.apply()
    }
    fun limpiarPagoDelDisco() {
        apuestaPagada = false
        apuestaActual = 0
        tiempoFinMillis = 0L
        val editor = sharedPreferences.edit()
        editor.putBoolean("apuesta_pagada", false)
        editor.putInt("apuesta_actual", 0)
        editor.putLong("tiempo_fin_millis", 0L)
        editor.apply()
    }

    // ==========================================
    // CARGA DE DATOS FIREBASE
    // ==========================================
    fun obtenerJugadorAleatorioDeFirestore() {
        cargandoJugador = true
        val db = FirebaseFirestore.getInstance()

        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val idFalsoAleatorio = (1..20).map { caracteres.random() }.joinToString("")

        db.collection("futbolistas_juegos")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), idFalsoAleatorio)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    NacerJuegoConDocumento(snapshot.documents.first())
                } else {
                    db.collection("futbolistas_juegos")
                        .whereLessThanOrEqualTo(FieldPath.documentId(), idFalsoAleatorio)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snapshotAtras ->
                            if (!snapshotAtras.isEmpty) {
                                NacerJuegoConDocumento(snapshotAtras.documents.first())
                            } else {
                                cargandoJugador = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                cargandoJugador = false
            }
    }

    private fun NacerJuegoConDocumento(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val palabraLimpia = doc.getString("palabra_wordle") ?: "MESSI"
        nombreCompletoReal = doc.getString("nombre") ?: "Desconocido"
        pistaPais = doc.getString("pais") ?: "Internacional"
        pistaPosicion = doc.getString("posicion") ?: "Cualquiera"

        palabraSecreta = palabraLimpia.uppercase()
        longitudPalabra = palabraSecreta.length

        reiniciarTableroLimpio()
        cargandoJugador = false
    }

    private fun reiniciarTableroLimpio() {
        tablero.clear()
        for (i in 0 until maxIntentos) {
            tablero.add(List(longitudPalabra) { CeldaWordle() })
        }
        intentoActual = 0
        columnaActual = 0
        juegoTerminado = false
        jugadorGano = false
        multiplicadorGanado = 0
        premioEntregado = false
        mensajeResultado = "" // Limpiamos el mensaje
        tiempoRestante = 40
        timerJob?.cancel()
    }

    fun iniciarNuevoJuego() {
        limpiarPagoDelDisco() // Si inician nuevo juego a la fuerza, limpiamos el cobro anterior
        obtenerJugadorAleatorioDeFirestore()
    }

    fun entregarPremio() {
        premioEntregado = true
        limpiarPagoDelDisco() // Al entregar el premio, termina el contrato de esta apuesta
    }

    // ==========================================
    // INTERACCIÓN DEL TECLADO
    // ==========================================
    fun ingresarLetra(letra: Char) {
        if (juegoTerminado || columnaActual >= longitudPalabra) return

        val filaActual = tablero[intentoActual].toMutableList()
        filaActual[columnaActual] = CeldaWordle(char = letra, estado = EstadoLetra.LLENA)
        tablero[intentoActual] = filaActual
        columnaActual++
    }

    fun borrarLetra() {
        if (juegoTerminado || columnaActual <= 0) return

        columnaActual--
        val filaActual = tablero[intentoActual].toMutableList()
        filaActual[columnaActual] = CeldaWordle(char = ' ', estado = EstadoLetra.VACIA)
        tablero[intentoActual] = filaActual
    }

    // ==========================================
    // ALGORITMO WORDLE
    // ==========================================
    fun enviarIntento() {
        val filaIncompleta = tablero[intentoActual].any { it.estado == EstadoLetra.VACIA || it.char == ' ' }
        if (filaIncompleta || juegoTerminado) return
        if (columnaActual < longitudPalabra || juegoTerminado) return

        val filaEvaluada = tablero[intentoActual].toMutableList()
        val charsSecretos = palabraSecreta.toCharArray().toMutableList()
        var letrasCorrectas = 0

        for (i in 0 until longitudPalabra) {
            if (filaEvaluada[i].char == charsSecretos[i]) {
                filaEvaluada[i] = filaEvaluada[i].copy(estado = EstadoLetra.CORRECTA)
                charsSecretos[i] = '*'
                letrasCorrectas++
            }
        }

        for (i in 0 until longitudPalabra) {
            if (filaEvaluada[i].estado != EstadoLetra.CORRECTA) {
                val indiceEncontrado = charsSecretos.indexOf(filaEvaluada[i].char)
                if (indiceEncontrado != -1) {
                    filaEvaluada[i] = filaEvaluada[i].copy(estado = EstadoLetra.PRESENTE)
                    charsSecretos[indiceEncontrado] = '*'
                } else {
                    filaEvaluada[i] = filaEvaluada[i].copy(estado = EstadoLetra.AUSENTE)
                }
            }
        }

        tablero[intentoActual] = filaEvaluada

        if (letrasCorrectas == longitudPalabra) {
            victoria()
        } else if (intentoActual == maxIntentos - 1) {
            derrota()
        } else {
            intentoActual++
            columnaActual = 0
        }
    }

    private fun victoria() {
        juegoTerminado = true
        jugadorGano = true
        timerJob?.cancel() // Detenemos el reloj si gana
        mensajeResultado = "¡GOLAZO! Adivinaste."
        calcularRecompensaCasino()
    }

    private fun derrota() {
        juegoTerminado = true
        jugadorGano = false
        timerJob?.cancel()
        mensajeResultado = "❌ Fallaste. Era: $nombreCompletoReal"
        limpiarPagoDelDisco() // Perdió, hay que volver a cobrarle en el siguiente
    }

    private fun calcularRecompensaCasino() {
        multiplicadorGanado = when {
            apuestaActual >= 1000 -> {
                when (intentoActual) {
                    0 -> 5
                    1 -> 4
                    2 -> 3
                    3 -> 2
                    4 -> 1
                    else -> 0
                }
            }

            apuestaActual >= 500 -> {
                when (intentoActual) {
                    0 -> 10
                    1 -> 6
                    2 -> 3
                    3 -> 2
                    4 -> 1
                    else -> 0
                }
            }

            else -> {
                when (intentoActual) {
                    0 -> 8
                    1 -> 4
                    2 -> 2
                    3 -> 1
                    else -> 0
                }
            }
        }
    }
    //temporizador
    fun iniciarTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (apuestaPagada && !juegoTerminado) {
                val ahora = System.currentTimeMillis()
                val restante = ((tiempoFinMillis - ahora) / 1000L).toInt()

                if (restante <= 0) {
                    tiempoRestante = 0
                    tiempoAgotado()
                    break
                } else {
                    tiempoRestante = restante
                    delay(500L) // Actualizamos la UI medio segundo para que se vea fluido
                }
            }
        }
    }
    // temporizador
    private fun tiempoAgotado() {
        juegoTerminado = true
        jugadorGano = false
        mensajeResultado = "⏱️ ¡Pito final! Era: $nombreCompletoReal"
        limpiarPagoDelDisco()
    }
    fun sincronizarReloj() {
        if (apuestaPagada && !juegoTerminado) {
            val ahora = System.currentTimeMillis()
            val restante = ((tiempoFinMillis - ahora) / 1000L).toInt()

            if (restante <= 0) {
                tiempoRestante = 0
                tiempoAgotado()
            } else {
                tiempoRestante = restante
            }
        }
    }
    fun validarEstadoAlEntrar() {
        if (apuestaPagada && !juegoTerminado) {
            val ahora = System.currentTimeMillis()
            val restante = ((tiempoFinMillis - ahora) / 1000L).toInt()

            if (restante <= 0) {
                // Se le acabó el tiempo mientras estaba en otro lado
                tiempoRestante = 0
                tiempoAgotado()
            } else {
                // Aún le queda tiempo, reanudamos
                tiempoRestante = restante
                iniciarTimer()
            }
        }
    }

}




