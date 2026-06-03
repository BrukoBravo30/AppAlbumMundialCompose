package mx.bruko.viewModel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import mx.bruko.data.Player

enum class TipoSobre(val precio: Int) {
    NORMAL(5000),
    PREMIUM(15000),
    ULTIMATE(100000)
}

class AlbumViewModel(private val contexto: Application) : AndroidViewModel(contexto) {

    private val db = FirebaseFirestore.getInstance()

    private val sharedPreferences = contexto.getSharedPreferences(
        "AlbumMundialDatos",
        Context.MODE_PRIVATE
    )

    // VARIABLES DE ESTADO
    var monedas by mutableStateOf(sharedPreferences.getInt("saldo_monedas", 100000))
        private set

    var jugadoresObtenidos = mutableStateListOf<String>()
        private set

    var inventario = mutableStateListOf<Player>()
        private set

    var albumByCountry by mutableStateOf<Map<String, List<Player>>>(emptyMap())
        private set

    var isLoading by mutableStateOf(true)
        private set

    private var allPlayersList = listOf<Player>()

    init {
        // Al iniciar, solo disparamos la descarga de datos. La reconstrucción ocurrirá adentro.
        fetchAlbumData()
    }

    // ==========================================
    // RECONSTRUCCIÓN TRAS RED
    // ==========================================
    private fun fetchAlbumData() {
        db.collection("jugadores").get().addOnSuccessListener { result ->
            // 1. Descargamos la lista maestra completa de Firebase
            allPlayersList = result.toObjects(Player::class.java)

            // 2. Leemos los hilos de persistencia local del disco duro
            val inventarioGuardado = sharedPreferences.getString("inventario_csv", "") ?: ""
            val albumPegadoGuardado = sharedPreferences.getString("album_pegados_csv", "") ?: ""

            val listaIdsInventario = if (inventarioGuardado.isNotEmpty()) inventarioGuardado.split(",") else emptyList()
            val listaIdsAlbum = if (albumPegadoGuardado.isNotEmpty()) albumPegadoGuardado.split(",") else emptyList()

            // Sincronizamos la lista de control de strings
            jugadoresObtenidos.clear()
            jugadoresObtenidos.addAll(listaIdsInventario)

            // 3. Como allPlayersList ya tiene datos, buscamos y reconstruimos el Inventario
            val jugadoresReconstruidos = listaIdsInventario.mapNotNull { nombreGuardado ->
                allPlayersList.find { it.nombre == nombreGuardado }?.copy(pegado = true)
            }
            inventario.clear()
            inventario.addAll(jugadoresReconstruidos)

            // 4. Reconstruimos el Álbum marcando como pegados los que están grabados en la persistencia del álbum
            val allPlayersMapeados = allPlayersList.map { jugador ->
                if (listaIdsAlbum.contains(jugador.nombre)) {
                    jugador.copy(pegado = true)
                } else {
                    jugador.copy(pegado = false)
                }
            }

            // Agrupamos por país y ordenamos
            albumByCountry = allPlayersMapeados.groupBy { it.pais }.toSortedMap()
            isLoading = false
        }
    }

    // ==========================================
    // SISTEMA CENTRAL DE GUARDADO (Mantiene el disco sincronizado)
    // ==========================================
    private fun guardarInventarioEnDisco() {
        // Tomamos los nombres de los jugadores actuales del inventario y los unimos por comas
        val textoParaGuardar = inventario.joinToString(",") { it.nombre }
        val editor = sharedPreferences.edit()
        editor.putString("inventario_csv", textoParaGuardar)
        editor.apply()

        // Sincronizamos la lista de control por seguridad
        jugadoresObtenidos.clear()
        jugadoresObtenidos.addAll(inventario.map { it.nombre })
    }

    private fun guardarAlbumEnDisco() {
        // Buscamos en todo el mapa del álbum cuáles jugadores tienen la bandera 'pegado = true'
        val listaPegados = albumByCountry.values.flatten().filter { it.pegado }.map { it.nombre }
        val textoParaGuardar = listaPegados.joinToString(",")

        val editor = sharedPreferences.edit()
        editor.putString("album_pegados_csv", textoParaGuardar)
        editor.apply()
    }

    // ==========================================
    // LÓGICA DE TIENDA Y JUEGOS
    // ==========================================
    fun abrirSobre(tipo: TipoSobre): List<Player>? {
        if (monedas < tipo.precio) return null

        restarMonedas(tipo.precio)
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
            val cartaObtenida = jugadorGanado.copy(pegado = true)

            sobre.add(cartaObtenida)
            inventario.add(cartaObtenida) // Añadimos a la RAM
        }

        // Guardamos todo el nuevo lote de una sola pasada en el disco duro
        guardarInventarioEnDisco()
        return sobre
    }

    fun venderCarta(jugador: Player) {
        inventario.remove(jugador)
        sumarMonedas(obtenerPrecioVenta(jugador.rareza)) // Suma monedas de forma persistente
        guardarInventarioEnDisco() // Actualiza el archivo borrando la carta vendida
    }

    fun pegarEnAlbum(jugador: Player) {
        // 1. Lo eliminamos del inventario RAM y actualizamos el archivo del inventario
        inventario.remove(jugador)
        guardarInventarioEnDisco()

        // 2. Modificamos el estado visual del álbum
        val paisList = albumByCountry[jugador.pais]?.toMutableList()
        if (paisList != null) {
            val index = paisList.indexOfFirst { it.nombre == jugador.nombre }
            if (index != -1) {
                paisList[index] = paisList[index].copy(pegado = true)

                val newAlbum = albumByCountry.toMutableMap()
                newAlbum[jugador.pais] = paisList
                albumByCountry = newAlbum.toSortedMap()

                // 3. Guardamos en disco la lista actualizada de los jugadores pegados
                guardarAlbumEnDisco()
            }
        }
    }

    fun obtenerPrecioVenta(rareza: String): Int {
        return when (rareza) {
            "unico" -> 25000
            "Diamante" -> 5000
            "Oro" -> 1500
            "Plata" -> 500
            else -> 100
        }
    }

    fun sumarMonedas(cantidad: Int) {
        monedas += cantidad
        val editor = sharedPreferences.edit()
        editor.putInt("saldo_monedas", monedas)
        editor.apply()
    }

    fun restarMonedas(cantidad: Int) {
        if (monedas >= cantidad) {
            monedas -= cantidad
            val editor = sharedPreferences.edit()
            editor.putInt("saldo_monedas", monedas)
            editor.apply()
        }
    }
}