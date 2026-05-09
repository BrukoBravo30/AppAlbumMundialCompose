package mx.bruko.data

data class Player(
    val nombre: String = "",
    val pais: String = "",
    val posicion: String = "",
    val foto_url: String = "",
    val rareza: String = "",
    val valor_numerico: Long = 0L,
    val pegado: Boolean = false
)