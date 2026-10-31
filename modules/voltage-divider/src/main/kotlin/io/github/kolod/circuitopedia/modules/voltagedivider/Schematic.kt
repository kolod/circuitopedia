package io.github.kolod.circuitopedia.modules.voltagedivider

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/**
 * The divider U1 ● ─ [R1] ─ ● U2 ─ [R2] ─ ⏚, drawn top to bottom with a brace marking R = R1 + R2.
 * Decorative: the fields below carry the values (spec FR-002, FR-015).
 */
@Composable
internal fun Schematic(modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val color = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    // A fixed size: the drawing has fixed geometry, and the fields below carry the text that scales.
    val labelStyle = MaterialTheme.typography.titleMedium.copy(color = color, fontSize = with(LocalDensity.current) { 16.dp.toSp() })
    Canvas(modifier.fillMaxWidth().height(220.dp).clearAndSetSemantics {}) {
        val stroke = 2.dp.toPx()
        val x = size.width * 0.42f
        val boxW = 18.dp.toPx()
        val boxH = 46.dp.toPx()
        val top = 14.dp.toPx()
        val r1Top = top + 18.dp.toPx()
        val r1Bottom = r1Top + boxH
        val mid = r1Bottom + 26.dp.toPx()
        val r2Top = mid + 26.dp.toPx()
        val r2Bottom = r2Top + boxH
        val ground = r2Bottom + 18.dp.toPx()

        // Wire, broken by the two resistor boxes.
        drawLine(color, Offset(x, top), Offset(x, r1Top), stroke)
        drawLine(color, Offset(x, r1Bottom), Offset(x, r2Top), stroke)
        drawLine(color, Offset(x, r2Bottom), Offset(x, ground), stroke)
        resistor(color, x, r1Top, boxW, boxH, stroke)
        resistor(color, x, r2Top, boxW, boxH, stroke)

        // Nodes U1 and U2; U2 is tapped to the left as the output.
        val node = 4.dp.toPx()
        drawCircle(accent, node, Offset(x, top))
        drawCircle(accent, node, Offset(x, mid))
        drawLine(color, Offset(x, mid), Offset(x - 36.dp.toPx(), mid), stroke)
        drawCircle(accent, node, Offset(x - 36.dp.toPx(), mid))

        // Ground symbol.
        val g = 14.dp.toPx()
        for (i in 0..2) {
            val half = g * (1 - i * 0.33f)
            val y = ground + i * 5.dp.toPx()
            drawLine(color, Offset(x - half, y), Offset(x + half, y), stroke)
        }

        // Brace for R = R1 + R2 on the right.
        val braceX = x + boxW + 30.dp.toPx()
        val brace = Path().apply {
            moveTo(braceX - 8.dp.toPx(), r1Top)
            lineTo(braceX, r1Top)
            lineTo(braceX, r2Bottom)
            lineTo(braceX - 8.dp.toPx(), r2Bottom)
            moveTo(braceX, (r1Top + r2Bottom) / 2)
            lineTo(braceX + 8.dp.toPx(), (r1Top + r2Bottom) / 2)
        }
        drawPath(brace, color, style = Stroke(stroke))

        label(measurer, "U1", Offset(x - 12.dp.toPx(), top), labelStyle, alignEnd = true)
        label(measurer, "U2", Offset(x - 48.dp.toPx(), mid), labelStyle, alignEnd = true)
        label(measurer, "R1", Offset(x + boxW / 2 + 8.dp.toPx(), (r1Top + r1Bottom) / 2), labelStyle)
        label(measurer, "R2", Offset(x + boxW / 2 + 8.dp.toPx(), (r2Top + r2Bottom) / 2), labelStyle)
        label(measurer, "R", Offset(braceX + 14.dp.toPx(), (r1Top + r2Bottom) / 2), labelStyle)
    }
}

private fun DrawScope.resistor(color: Color, x: Float, top: Float, width: Float, height: Float, stroke: Float) {
    drawRect(color, Offset(x - width / 2, top), Size(width, height), style = Stroke(stroke))
}

/** Draws [text] vertically centered on [anchor], to its right, or ending at it when [alignEnd]. */
private fun DrawScope.label(
    measurer: TextMeasurer,
    text: String,
    anchor: Offset,
    style: androidx.compose.ui.text.TextStyle,
    alignEnd: Boolean = false,
) {
    val layout = measurer.measure(text, style)
    val left = if (alignEnd) anchor.x - layout.size.width else anchor.x
    drawText(layout, topLeft = Offset(left, anchor.y - layout.size.height / 2f))
}
