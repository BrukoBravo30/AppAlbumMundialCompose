package mx.bruko.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

// 1. EL MODELO DE DATOS
// Esta clase representa una bola. Contiene toda su "ruta del destino" precalculada.
data class BolaPlinko(
    val id: String = UUID.randomUUID().toString(),
    val apuesta: Int,
    val ruta: List<Boolean>, // false = Rebote Izquierda, true = Rebote Derecha
    val bucketFinal: Int,
    val multiplicador: Double,
    val premioGanado: Int
)

class PlinkoViewModel : ViewModel() {

    // ==========================================
    // 1. CONFIGURACIÓN DEL TABLERO Y ECONOMÍA
    // ==========================================
    // 16 Filas de clavos generan 17 espacios (buckets) abajo.
    // La simetría es perfecta. La casa siempre tiene la ventaja al centro.
    val multiplicadores = listOf(120.0, 35.0, 10.0, 3.0, 1.2, 0.5, 0.3, 0.2, 0.15, 0.2, 0.3, 0.5, 1.2, 3.0, 10.0, 35.0, 120.0)

    val preciosDisponibles = listOf(100, 500, 1000, 10000)
    var apuestaSeleccionada by mutableStateOf(100)
        private set

    // Usamos una lista observable porque en Plinko el usuario puede lanzar
    // 10 bolas seguidas como ametralladora, y todas deben caer al mismo tiempo.
    var bolasActivas = mutableStateListOf<BolaPlinko>()
        private set

    //Variable de estado para bloquear el botón temporalmente
    var botonHabilitado by mutableStateOf(true)
        private set
    fun cambiarApuesta(nuevoMonto: Int) {
        apuestaSeleccionada = nuevoMonto
    }

    // ==========================================
    // 2. EL MOTOR DE "FÍSICAS" (DISTRIBUCIÓN BINOMIAL)
    // ==========================================
    fun comprarBola(saldoActual: Int, onCobrar: (Int) -> Unit) {
        // Validación de seguridad: saldo y cooldown activo
        if (saldoActual < apuestaSeleccionada || !botonHabilitado) return

        // 1. Activamos el Cooldown de inmediato
        botonHabilitado = false
        viewModelScope.launch {
            delay(1200) // 400 milisegundos de delay entre cada pelota (ajustable)
            botonHabilitado = true
        }

        // 2. Cobramos la apuesta
        onCobrar(apuestaSeleccionada)

        var posicionFinal = 0
        val rutaGenerada = mutableListOf<Boolean>()

        // 3. CONTROL DE LA CASA: Interceptamos el azar con un dado de 100 caras
        val dadoCasino = Random.nextDouble(0.0, 100.0)

        when {
            dadoCasino <= 1.5 -> {
                // TIER EXTREMO (1.5% de probabilidad forzada para el premio 100x)
                val irTodoDerecha = Random.nextBoolean()
                posicionFinal = if (irTodoDerecha) 16 else 0

                // Generamos la ruta física correspondiente (16 aciertos seguidos hacia un lado)
                for (i in 0 until 16) {
                    rutaGenerada.add(irTodoDerecha)
                }
            }
            dadoCasino <= 5.0 -> {
                // TIER PREMIUM (Otro 3.5% de probabilidad para los premios de 29x en los extremos 1 o 15)
                val irHaciaDerecha = Random.nextBoolean()
                posicionFinal = if (irHaciaDerecha) 15 else 1

                // Para caer en 1 o 15, la bola debe rebotar 15 veces hacia un lado y solo 1 vez al opuesto
                val filaDelReboteOpuesto = (0 until 16).random()
                for (i in 0 until 16) {
                    if (i == filaDelReboteOpuesto) {
                        rutaGenerada.add(!irHaciaDerecha) // El único rebote extraño
                    } else {
                        rutaGenerada.add(irHaciaDerecha)
                    }
                }
            }
            else -> {
                // DISTRIBUCIÓN NATURAL (95% restante del tiempo usa la Campana de Gauss normal)
                for (i in 0 until 16) {
                    val rebotaDerecha = Random.nextBoolean()
                    rutaGenerada.add(rebotaDerecha)
                    if (rebotaDerecha) {
                        posicionFinal++
                    }
                }
            }
        }

        // 4. Procesamiento final de la bola
        val mult = multiplicadores[posicionFinal]
        val premio = (apuestaSeleccionada * mult).toInt()

        val nuevaBola = BolaPlinko(
            apuesta = apuestaSeleccionada,
            ruta = rutaGenerada,
            bucketFinal = posicionFinal,
            multiplicador = mult,
            premioGanado = premio
        )

        bolasActivas.add(nuevaBola)
    }

    fun bolaLlegoAlFondo(bola: BolaPlinko, onPagarPremio: (Int) -> Unit) {
        onPagarPremio(bola.premioGanado)
        bolasActivas.remove(bola)
    }
}
