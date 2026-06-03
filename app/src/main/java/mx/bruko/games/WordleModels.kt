package mx.bruko.games

enum class EstadoLetra {
    VACIA,
    LLENA,
    CORRECTA,
    PRESENTE,
    AUSENTE
}

// Representa una celda individual en el tablero
data class CeldaWordle(
    val char: Char = ' ',
    val estado: EstadoLetra = EstadoLetra.VACIA
)