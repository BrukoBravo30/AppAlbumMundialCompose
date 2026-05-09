package mx.bruko.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import mx.bruko.data.Player

class AlbumViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Guardamos los jugadores agrupados por país
    var albumByCountry by mutableStateOf<Map<String, List<Player>>>(emptyMap())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var allPlayersList by mutableStateOf<List<Player>>(emptyList())
        private set

    init {
        fetchAlbumData()
    }

    private fun fetchAlbumData() {
        db.collection("jugadores").get().addOnSuccessListener { result ->
            val allPlayers = result.toObjects(Player::class.java)
            allPlayersList = allPlayers // Guardamos la lista plana para los sobres
            albumByCountry = allPlayers.groupBy { it.pais }.toSortedMap()
            isLoading = false
        }
    }

    fun abrirSobre(): List<Player> {
        val sobre = mutableListOf<Player>()
        if (allPlayersList.isEmpty()) return sobre

        for (i in 1..5) {
            val randomValue = Math.random() // Genera un número entre 0.0 y 1.0

            // TABLA DE PROBABILIDADES
            val targetRarity = when {
                randomValue < 0.01 -> "unico"      // 1% de probabilidad
                randomValue < 0.06 -> "Diamante"   // 5% de probabilidad (del 1% al 6%)
                randomValue < 0.20 -> "Oro"        // 14% de probabilidad
                randomValue < 0.55 -> "Plata"      // 35% de probabilidad
                else -> "Bronce"                   // 45% de probabilidad
            }

            // Filtramos jugadores por la rareza ganada
            var pool = allPlayersList.filter { it.rareza == targetRarity }

            // Si por alguna razón no hay jugadores de esa rareza, tomamos de toda la base
            if (pool.isEmpty()) pool = allPlayersList

            // .random() permite que salgan repetidos de forma natural
            val jugadorGanado = pool.random()

            // Aquí forzamos visualmente a que parezca 'pegado' solo para la animación del sobre
            sobre.add(jugadorGanado.copy(pegado = true))
        }
        return sobre
    }
}

