package mx.bruko.games

enum class EstadoLetra {
    VACIA,      // Cuadro gris oscuro / negro
    LLENA,      // Usuario escribió, pero no ha validado (Borde brillante)
    CORRECTA,   // Verde/Cyan Neón (Lugar y letra correctos)
    PRESENTE,   // Dorado/Amarillo (Letra correcta, mal lugar)
    AUSENTE     // Gris apagado (No existe en la palabra)
}

// Representa una celda individual en el tablero
data class CeldaWordle(
    val char: Char = ' ',
    val estado: EstadoLetra = EstadoLetra.VACIA
)