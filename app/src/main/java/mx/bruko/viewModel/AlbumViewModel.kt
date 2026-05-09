package mx.bruko.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import mx.bruko.data.Player

enum class TipoSobre(val precio: Int) {
    NORMAL(5000),
    PREMIUM(15000),
    ULTIMATE(100000)
}

class AlbumViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // --- ECONOMÍA E INVENTARIO ---
    var monedas by mutableStateOf(25000) // Monedas iniciales de regalo

    // Cartas en el Storage (pueden ser repetidas)
    var inventario = mutableStateListOf<Player>()
        private set

    var albumByCountry by mutableStateOf<Map<String, List<Player>>>(emptyMap())
        private set

    var isLoading by mutableStateOf(true)
        private set

    private var allPlayersList = listOf<Player>()

    init {
        fetchAlbumData()
    }

    private fun fetchAlbumData() {
        db.collection("jugadores").get().addOnSuccessListener { result ->
            val allPlayers = result.toObjects(Player::class.java).map {
                it.copy(pegado = false) // EL ÁLBUM INICIA VACÍO
            }
            allPlayersList = allPlayers
            albumByCountry = allPlayers.groupBy { it.pais }.toSortedMap()
            isLoading = false
        }
    }

    // --- LÓGICA DE TIENDA ---
    fun abrirSobre(tipo: TipoSobre): List<Player>? {
        if (monedas < tipo.precio) return null // No hay fondos

        monedas -= tipo.precio // Cobrar
        val sobre = mutableListOf<Player>()

        for (i in 1..5) {
            val randomValue = Math.random()
            val targetRarity = when (tipo) {
                TipoSobre.PREMIUM -> when {
                    randomValue < 0.03 -> "unico"
                    randomValue < 0.15 -> "Diamante"
                    randomValue < 0.45 -> "Oro"
                    randomValue < 0.85 -> "Plata"
                    else -> "Bronce"
                }
                TipoSobre.ULTIMATE -> when {
                    randomValue < 0.15 -> "unico"
                    randomValue < 0.45 -> "Diamante"
                    randomValue < 0.85 -> "Oro"
                    else -> "Plata"
                }
                else -> when {
                    randomValue < 0.005 -> "unico"
                    randomValue < 0.050 -> "Diamante"
                    randomValue < 0.200 -> "Oro"
                    randomValue < 0.500 -> "Plata"
                    else -> "Bronce"
                }
            }

            var pool = allPlayersList.filter { it.rareza == targetRarity }
            if (pool.isEmpty()) pool = allPlayersList

            val jugadorGanado = pool.random()

            // Se va al inventario, NO al álbum directo
            val cartaObtenida = jugadorGanado.copy(pegado = true) // 'pegado=true' solo para que se vea la foto en el inventario/sobre
            sobre.add(cartaObtenida)
            inventario.add(cartaObtenida)
        }
        return sobre
    }

    // --- LÓGICA DE INVENTARIO ---
    fun obtenerPrecioVenta(rareza: String): Int {
        return when (rareza) {
            "unico" -> 25000
            "Diamante" -> 5000
            "Oro" -> 1500
            "Plata" -> 500
            else -> 100 // Bronce
        }
    }

    fun venderCarta(jugador: Player) {
        inventario.remove(jugador)
        monedas += obtenerPrecioVenta(jugador.rareza)
    }

    fun pegarEnAlbum(jugador: Player) {
        // 1. Quitar del inventario
        inventario.remove(jugador)

        // 2. Actualizar el álbum para mostrarla
        val paisList = albumByCountry[jugador.pais]?.toMutableList()
        if (paisList != null) {
            val index = paisList.indexOfFirst { it.nombre == jugador.nombre }
            if (index != -1) {
                paisList[index] = paisList[index].copy(pegado = true)

                // Forzar la recomposición del mapa
                val newAlbum = albumByCountry.toMutableMap()
                newAlbum[jugador.pais] = paisList
                albumByCountry = newAlbum.toSortedMap()
            }
        }
    }
}