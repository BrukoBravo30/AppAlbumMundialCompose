package mx.bruko.ui.theme

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

val ShieldShape = object : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f) // Punta superior central
            lineTo(size.width, size.height * 0.2f) // Esquina superior derecha
            lineTo(size.width, size.height * 0.85f) // Lateral derecho
            cubicTo(
                size.width, size.height,
                size.width * 0.5f, size.height * 1.15f,
                0f, size.height * 0.85f
            ) // Curva inferior
            lineTo(0f, size.height * 0.2f) // Lateral izquierdo
            close()
        }
        return Outline.Generic(path)
    }
}