package mx.bruko.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PenalesViewModel : ViewModel() {

    var juegoActivo by mutableStateOf(false)
        private set
    var rachaGoles by mutableStateOf(0)
        private set
    var apuestaInicial by mutableStateOf(100)

    var zonaBalon by mutableStateOf(0)
        private set
    var zonaPortero by mutableStateOf(0)
        private set

    var mensajeOverlay by mutableStateOf("")
        private set
    var colorOverlay by mutableStateOf(0xFFFFFFFF)
        private set
    var animandoTiro by mutableStateOf(false)
        private set

    // ==========================================
    // ESCALA DE PREMIOS INFINITA (Fórmula Matemática)
    // ==========================================
    val multiplicadorActual: Double
        get() {
            if (rachaGoles == 0) return 0.0
            var mult = 1.0
            // Incremento progresivo: 1.20, 1.45, 1.80, 2.30, 3.00, etc. ¡No hay límite!
            for (i in 1..rachaGoles) {
                mult += (2 + (i * 2))
            }
            // Redondeamos a 2 decimales
            return Math.round(mult * 100.0) / 100.0
        }

    val premioAcumulado: Int
        get() = (apuestaInicial * multiplicadorActual).toInt()

    // ==========================================
    // LÓGICA PRINCIPAL Y VENTAJA DE LA CASA
    // ==========================================
    fun iniciarJuego(saldoActual: Int, onCobrar: (Int) -> Unit) {
        if (saldoActual < apuestaInicial || juegoActivo) return

        onCobrar(apuestaInicial)

        juegoActivo = true
        rachaGoles = 0
        zonaBalon = 0
        zonaPortero = 0
        mensajeOverlay = ""
    }

    fun tirarPenal(zonaSeleccionada: Int) {
        if (!juegoActivo || animandoTiro) return

        animandoTiro = true
        zonaBalon = zonaSeleccionada

        // ALGORITMO DEL CASINO (La casa siempre gana a la larga)
        // El portero empieza con 32% de probabilidad de atajar.
        // Cada gol que metes, se vuelve más difícil (Suma 5%, hasta un tope de 75%).
        val probabilidadAtajar = minOf(32 + (rachaGoles * 5), 75)
        val dado = Random.nextInt(1, 101)
        val atajado = dado <= probabilidadAtajar

        if (atajado) {
            // El casino decidió que pierdes. El portero va a tu misma zona.
            zonaPortero = zonaSeleccionada
        } else {
            // ¡GOL! El casino te deja ganar. El portero se tira a cualquier OTRA zona.
            val zonasRestantes = (1..5).filter { it != zonaSeleccionada }
            zonaPortero = zonasRestantes.random()
        }

        val fueGol = !atajado

        viewModelScope.launch {
            if (fueGol) {
                mensajeOverlay = "¡GOOOOOL!"
                colorOverlay = 0xFF00FF87
                rachaGoles++
            } else {
                mensajeOverlay = "¡ATAJADO!"
                colorOverlay = 0xFFFF003C
            }

            delay(1200)

            animandoTiro = false
            mensajeOverlay = ""
            zonaBalon = 0
            zonaPortero = 0

            if (!fueGol) {
                juegoActivo = false
                rachaGoles = 0
                zonaPortero = 0 // Regresa al centro
            }
        }
    }

    fun retirarseConGanancias(onPagarPremio: (Int) -> Unit) {
        if (!juegoActivo || rachaGoles == 0 || animandoTiro) return

        onPagarPremio(premioAcumulado)
        juegoActivo = false
        rachaGoles = 0
        zonaBalon = 0
        zonaPortero = 0
        mensajeOverlay = "¡COBRADO!"
        colorOverlay = 0xFFFFD700

        viewModelScope.launch {
            delay(1500)
            mensajeOverlay = ""
        }
    }
}