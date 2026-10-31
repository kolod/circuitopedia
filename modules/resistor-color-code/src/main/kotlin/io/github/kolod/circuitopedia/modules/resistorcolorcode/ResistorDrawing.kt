package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection

private val LeadColor = Color(0xFFB0B0B0)
private val BodyColor = Color(0xFFE8C39E)
private val BodyEdgeColor = Color(0xFFD9A877)

// Vertical geometry: the resistor on top, leader lines below it down to the table header.
private val DrawingHeight = 104.dp
private val Axis = 30.dp
private val CapHalfHeight = 18.dp
private val BodyHalfHeight = 13.dp
private val LeaderStraight = 8.dp
private val LeaderWidth = 3.dp
private val LeadLength = 32.dp

/** Body length to end-cap diameter; fixed so the resistor keeps its shape on every screen width. */
private const val BODY_ASPECT = 7.5f

// Horizontal geometry as fractions of the body length.
private const val CAP_LENGTH = 0.24f
private const val BAND_WIDTH = 0.06f

/**
 * Band centers along the body. The first band sits on the left cap, the last tolerance or TCR band
 * on the right cap, and the rest on the straight middle. No band crosses a cap-to-middle transition
 * (caps span 0–0.24 and 0.76–1, with rounded corners), so every band matches the body outline.
 */
private fun BandLayout.bandCenters(): List<Float> = when (this) {
    BandLayout.Three -> listOf(0.12f, 0.4f, 0.52f)
    BandLayout.Four -> listOf(0.12f, 0.4f, 0.52f, 0.88f)
    BandLayout.Five -> listOf(0.12f, 0.36f, 0.48f, 0.6f, 0.88f)
    BandLayout.Six -> listOf(0.12f, 0.34f, 0.44f, 0.54f, 0.65f, 0.88f)
}

/**
 * Decorative resistor in the selected colors, drawn above the table header. Each band is joined to
 * the top edge of its header column by a leader line made of vertical and slanted segments only.
 * Must be laid out with the same width and side padding as the table; the value text carries the information.
 */
@Composable
fun ResistorDrawing(selection: Selection, modifier: Modifier = Modifier) {
    val layout = selection.layout
    val leaderColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier.height(DrawingHeight).clearAndSetSemantics {}) {
        val columns = bandColumnCenters(size.width, CellGap.toPx(), layout.bandCount)
        val leadLength = LeadLength.toPx()
        val axis = Axis.toPx()
        val capHalf = CapHalfHeight.toPx()
        val bodyHalf = BodyHalfHeight.toPx()
        // Centered horizontally, with a fixed aspect ratio unless the screen is too narrow for it.
        val bodyLength = minOf(capHalf * 2 * BODY_ASPECT, size.width - 2 * leadLength)
        val bodyStart = (size.width - bodyLength) / 2

        // Leads, then the "dog-bone" body: two wider end caps and a narrower middle.
        drawLine(
            LeadColor,
            Offset(bodyStart - leadLength, axis),
            Offset(bodyStart + bodyLength + leadLength, axis),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawBodyPart(bodyStart + bodyLength * CAP_LENGTH * 0.5f, bodyStart + bodyLength * (1 - CAP_LENGTH * 0.5f), axis, bodyHalf)
        drawBodyPart(bodyStart, bodyStart + bodyLength * CAP_LENGTH, axis, capHalf)
        drawBodyPart(bodyStart + bodyLength * (1 - CAP_LENGTH), bodyStart + bodyLength, axis, capHalf)

        val straight = LeaderStraight.toPx()
        layout.bandCenters().forEachIndexed { i, position ->
            val onCap = position < CAP_LENGTH || position > 1 - CAP_LENGTH
            val half = if (onCap) capHalf else bodyHalf
            val bandX = bodyStart + bodyLength * position
            drawRect(
                Color(selection.colors[i].argb),
                topLeft = Offset(bandX - bodyLength * BAND_WIDTH / 2, axis - half),
                size = Size(bodyLength * BAND_WIDTH, half * 2),
            )

            // Up from the header column, slant across, then up into the band: no horizontal segments.
            val path = Path().apply {
                moveTo(columns[i], size.height)
                lineTo(columns[i], size.height - straight)
                lineTo(bandX, axis + half + straight)
                lineTo(bandX, axis + half)
            }
            drawPath(path, leaderColor, style = Stroke(LeaderWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

private fun DrawScope.drawBodyPart(start: Float, end: Float, axis: Float, half: Float) {
    val topLeft = Offset(start, axis - half)
    val partSize = Size(end - start, half * 2)
    val radius = CornerRadius(half * 0.6f)
    drawRoundRect(BodyColor, topLeft, partSize, radius)
    drawRoundRect(BodyEdgeColor, topLeft, partSize, radius, style = Stroke(width = 1.5f))
}
