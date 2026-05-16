package mx.bruko.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.bruko.games.CeldaWordle
import mx.bruko.games.EstadoLetra
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
class WordleViewModel : ViewModel() {

    // 1. CONFIGURACIÓN DEL JUEGO (MOCK)
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

    var longitudPalabra by mutableStateOf(palabraSecreta.length)
        private set

    // 2. ESTADO DEL TABLERO
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
    var premioEntregado by mutableStateOf(false)
        private set
    var apuestaActual by mutableStateOf(0)
    init {
        obtenerJugadorAleatorioDeFirestore()
    }

    fun obtenerJugadorAleatorioDeFirestore() {
        cargandoJugador = true
        val db = FirebaseFirestore.getInstance()

        // Generamos un hash aleatorio de 20 caracteres para simular un ID de Firebase
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val idFalsoAleatorio = (1..20).map { caracteres.random() }.joinToString("")

        // Buscamos el primer documento que sea "mayor o igual" al ID falso
        db.collection("futbolistas_juegos")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), idFalsoAleatorio)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val documento = snapshot.documents.first()
                    NacerJuegoConDocumento(documento)
                } else {
                    // Si el ID falso quedó muy alto y no hay nada arriba, buscamos hacia abajo
                    db.collection("futbolistas_juegos")
                        .whereLessThanOrEqualTo(FieldPath.documentId(), idFalsoAleatorio)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snapshotAtras ->
                            if (!snapshotAtras.isEmpty) {
                                NacerJuegoConDocumento(snapshotAtras.documents.first())
                            } else {
                                cargandoJugador = false // Colección vacía
                            }
                        }
                }
            }
            .addOnFailureListener {
                cargandoJugador = false // Error de conexión
            }
    }
    private fun NacerJuegoConDocumento(doc: com.google.firebase.firestore.DocumentSnapshot) {
        // Extraemos los campos que guardamos desde el DataFrame de Colab
        val palabraLimpia = doc.getString("palabra_wordle") ?: "MESSI"

        nombreCompletoReal = doc.getString("nombre") ?: "Desconocido"
        pistaPais = doc.getString("pais") ?: "Internacional"
        pistaPosicion = doc.getString("posicion") ?: "Cualquiera"

        // Inicializamos los parámetros del juego con los datos reales
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
    }

    // Modificamos tu función existente para que no pida un String, sino que use Firestore
    fun iniciarNuevoJuego() {
        obtenerJugadorAleatorioDeFirestore()
    }

    fun iniciarNuevoJuego(nuevaPalabra: String) {
        palabraSecreta = nuevaPalabra.uppercase()

        longitudPalabra = palabraSecreta.length

        premioEntregado = false

        tablero.clear()
        for (i in 0 until maxIntentos) {
            tablero.add(List(longitudPalabra) { CeldaWordle() })
        }

        intentoActual = 0
        columnaActual = 0
        juegoTerminado = false
        jugadorGano = false
        multiplicadorGanado = 0
    }
    fun entregarPremio() {
        premioEntregado = true
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
        val filaIncompleta = tablero[intentoActual].any { it.estado == EstadoLetra.VACIA || it.char == ' ' }
        if (filaIncompleta || juegoTerminado) return
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

// ECONOMÍA WORDLE FÚTBOL
// Skill-based casino
    private fun calcularRecompensaCasino() {

        multiplicadorGanado = when {

            // ======================================
            // TIER VIP
            // ======================================
            apuestaActual >= 1000 -> {
                when (intentoActual) {
                    0 -> 5 // Perfecto
                    1 -> 4
                    2 -> 3
                    3 -> 2
                    4 -> 1
                    else -> 0
                }
            }

            // ======================================
            // TIER REGULAR
            // ======================================
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

            // ======================================
            // TIER CASUAL
            // ======================================
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
}