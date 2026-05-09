package mx.bruko.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.bruko.games.CeldaWordle
import mx.bruko.games.EstadoLetra


class WordleViewModel : ViewModel() {

    // ==========================================
    // 1. CONFIGURACIÓN DEL JUEGO (MOCK)
    // ==========================================
    // Cuando conectemos Firestore, esto se llenará dinámicamente con tu base de datos
    var palabraSecreta = "MESSI"
        private set

    val maxIntentos = 6
    val longitudPalabra = palabraSecreta.length

    // ==========================================
    // 2. ESTADO DEL TABLERO
    // ==========================================
    // Matriz 2D: Lista de intentos, donde cada intento es una lista de Celdas
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

    // Multiplicador del Casino (Depende en qué intento gane)
    var multiplicadorGanado by mutableStateOf(0)
        private set
    var apuestaActual by mutableStateOf(0)
    init {
        iniciarNuevoJuego("MESSI") // Palabra de prueba
    }

    fun iniciarNuevoJuego(nuevaPalabra: String) {
        palabraSecreta = nuevaPalabra.uppercase()
        tablero.clear()
        // Rellenamos el tablero con celdas vacías
        for (i in 0 until maxIntentos) {
            tablero.add(List(palabraSecreta.length) { CeldaWordle() })
        }
        intentoActual = 0
        columnaActual = 0
        juegoTerminado = false
        jugadorGano = false
        multiplicadorGanado = 0
    }

    // ==========================================
    // 3. INTERACCIÓN DEL TECLADO
    // ==========================================
    fun ingresarLetra(letra: Char) {
        if (juegoTerminado || columnaActual >= longitudPalabra) return

        // Actualizamos la fila actual con la nueva letra
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
    // 4. ALGORITMO DE VALIDACIÓN (MAGIA MATEMÁTICA)
    // ==========================================
    fun enviarIntento() {
        // Solo puede enviar si llenó toda la fila
        if (columnaActual < longitudPalabra || juegoTerminado) return

        val filaEvaluada = tablero[intentoActual].toMutableList()
        val charsSecretos = palabraSecreta.toCharArray().toMutableList()

        var letrasCorrectas = 0

        // PASO 1: Buscar las VERDES (Correctas exactas)
        for (i in 0 until longitudPalabra) {
            if (filaEvaluada[i].char == charsSecretos[i]) {
                filaEvaluada[i] = filaEvaluada[i].copy(estado = EstadoLetra.CORRECTA)
                charsSecretos[i] = '*' // "Consumimos" la letra para que no se cuente doble
                letrasCorrectas++
            }
        }

        // PASO 2: Buscar las AMARILLAS (Presentes) y GRISES (Ausentes)
        for (i in 0 until longitudPalabra) {
            if (filaEvaluada[i].estado != EstadoLetra.CORRECTA) {
                val indiceEncontrado = charsSecretos.indexOf(filaEvaluada[i].char)

                if (indiceEncontrado != -1) {
                    // La letra existe en otra posición
                    filaEvaluada[i] = filaEvaluada[i].copy(estado = EstadoLetra.PRESENTE)
                    charsSecretos[indiceEncontrado] = '*' // La consumimos
                } else {
                    // La letra no existe o ya se gastaron sus repeticiones
                    filaEvaluada[i] = filaEvaluada[i].copy(estado = EstadoLetra.AUSENTE)
                }
            }
        }

        // Actualizamos la vista
        tablero[intentoActual] = filaEvaluada

        // Evaluar victoria o derrota
        if (letrasCorrectas == longitudPalabra) {
            juegoTerminado = true
            jugadorGano = true
            calcularRecompensaCasino()
        } else if (intentoActual == maxIntentos - 1) {
            juegoTerminado = true
            jugadorGano = false
        } else {
            // Avanzamos al siguiente intento
            intentoActual++
            columnaActual = 0
        }
    }

    private fun calcularRecompensaCasino() {
        // Lógica de multiplicadores según el intento
        multiplicadorGanado = when (intentoActual) {
            0 -> 100 // Ganó a la primera (x100)
            1 -> 50  // (x50)
            2 -> 20  // (x20)
            3 -> 10  // (x10)
            4 -> 5   // (x5)
            else -> 2 // Sexto intento (x2)
        }
    }
}