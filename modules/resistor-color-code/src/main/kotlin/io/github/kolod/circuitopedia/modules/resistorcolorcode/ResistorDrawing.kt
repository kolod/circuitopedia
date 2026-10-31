package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection

private val LeadColor = Color(0xFFB0B0B0)
private val BodyColor = Color(0xFFE8C39E)
private val BodyEdgeColor = Color(0xFFD9A877)

// Horizontal geometry as fractions of the width.
private const val BODY_START = 0.15f
private const val BODY_END = 0.85f
private const val CAP_LENGTH = 0.17f
private const val BAND_WIDTH = 0.045f

/** Band centers per layout: digits and multiplier evenly on the left, tolerance and TCR set apart on the right. */
private fun BandLayout.bandCenters(): List<Float> = when (this) {
    BandLayout.Three -> listOf(0.24f, 0.33f, 0.42f)
    BandLayout.Four -> listOf(0.24f, 0.33f, 0.42f, 0.71f)
    BandLayout.Five -> listOf(0.23f, 0.31f, 0.39f, 0.47f, 0.71f)
    BandLayout.Six -> listOf(0.23f, 0.31f, 0.39f, 0.47f, 0.67f, 0.76f)
}

/** Decorative resistor in the selected colors with leader-line labels; the value text carries the information. */
@Composable
fun ResistorDrawing(selection: Selection, modifier: Modifier = Modifier) {
    val layout = selection.layout
    val resources = LocalResources.current
    val labels = List(layout.bandCount) { resources.getString(layout.drawingLabelRes(it)) }
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface)
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier.aspectRatio(2.2f).clearAndSetSemantics {}) {
        val centers = layout.bandCenters()
        val measured = labels.map { measurer.measure(it, labelStyle) }
        val labelBottom = measured.maxOf { it.size.height }.toFloat()
        val axis = size.height * 0.72f
        val capHalf = size.height * 0.2f
        val bodyHalf = size.height * 0.15f

        // Leads, then the "dog-bone" body: two wider end caps and a narrower middle.
        drawLine(LeadColor, Offset(0f, axis), Offset(size.width, axis), strokeWidth = size.height * 0.05f, cap = StrokeCap.Round)
        drawBodyPart(BODY_START + CAP_LENGTH * 0.5f, BODY_END - CAP_LENGTH * 0.5f, axis, bodyHalf)
        drawBodyPart(BODY_START, BODY_START + CAP_LENGTH, axis, capHalf)
        drawBodyPart(BODY_END - CAP_LENGTH, BODY_END, axis, capHalf)

        centers.forEachIndexed { i, center ->
            val half = if (center < BODY_START + CAP_LENGTH || center > BODY_END - CAP_LENGTH) capHalf else bodyHalf
            val bandTop = axis - half
            drawRect(
                Color(selection.colors[i].argb),
                topLeft = Offset(size.width * (center - BAND_WIDTH / 2), bandTop),
                size = Size(size.width * BAND_WIDTH, half * 2),
            )

            // Labels spread evenly across the width, each joined to its band by an elbow line.
            val labelCenter = size.width * (0.08f + 0.84f * if (centers.size == 1) 0.5f else i / (centers.size - 1f))
            val text = measured[i]
            drawText(text, topLeft = Offset(labelCenter - text.size.width / 2f, 0f))
            val elbow = (labelBottom + bandTop) / 2f
            val path = Path().apply {
                moveTo(labelCenter, labelBottom + 2f)
                lineTo(labelCenter, elbow)
                lineTo(size.width * center, elbow)
                lineTo(size.width * center, bandTop)
            }
            drawPath(path, lineColor, style = Stroke(width = 1.5f))
        }
    }
}

private fun DrawScope.drawBodyPart(start: Float, end: Float, axis: Float, half: Float) {
    val topLeft = Offset(size.width * start, axis - half)
    val partSize = Size(size.width * (end - start), half * 2)
    val radius = CornerRadius(half * 0.6f)
    drawRoundRect(BodyColor, topLeft, partSize, radius)
    drawRoundRect(BodyEdgeColor, topLeft, partSize, radius, style = Stroke(width = 1.5f))
}
