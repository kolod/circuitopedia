package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Pin
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction

/** The short label drawn next to a pin: its symbol, plus the channel for multi-channel parts. */
private fun Pin.symbol(): String = function.symbol + (channel?.let { " $it" } ?: "")

/** The drawing height of each package, in dp. The drawing does not scale with the font size. */
private fun Package.drawingHeight(): Dp = when (this) {
    Package.Dip8, Package.Dip14 -> (pinCount / 2 * 26 + 30).dp
    Package.To92 -> 160.dp
    Package.To126 -> 180.dp
    Package.To220 -> 200.dp
    Package.Sot23 -> 140.dp
    Package.Do35, Package.Do41 -> 90.dp
}

/**
 * The package outline with numbered, labeled pins, then the view note and the pin table. The
 * drawing is decorative for TalkBack; the pin table rows are spoken ("Pin 1, Collector").
 */
@Composable
internal fun Pinout(packageType: Package, pins: List<Pin>) {
    val measurer = rememberTextMeasurer()
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    // Fixed-size labels: the drawing keeps its geometry at any font scale; the table below scales.
    val style = with(LocalDensity.current) { TextStyle(fontSize = 12.dp.toSp(), color = ink) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(packageType.drawingHeight())
                .clearAndSetSemantics { testTag = "jb-drawing" },
        ) {
            val d = Drawer(this, measurer, style, ink, accent)
            when (packageType) {
                Package.Dip8, Package.Dip14 -> d.dip(pins)
                Package.To92 -> d.to92(pins)
                Package.To126 -> d.to126(pins)
                Package.To220 -> d.to220(pins)
                Package.Sot23 -> d.sot23(pins)
                Package.Do35, Package.Do41 -> d.axial(pins)
            }
        }
        Text(
            stringResource(packageType.view()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        pins.forEach { pin ->
            val name = stringResource(pin.function.label()) + (pin.channel?.let { " $it" } ?: "")
            val spoken = stringResource(R.string.jellybean_parts_spoken_pin, pin.number, name)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clearAndSetSemantics {
                        contentDescription = spoken
                        testTag = "jb-pin-${pin.number}"
                    },
            ) {
                Text("${pin.number}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.widthIn(min = 32.dp))
                Text(name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private enum class Align { Start, Center, End }

/** Drawing helpers in dp units, centered horizontally on the canvas. */
private class Drawer(
    val scope: DrawScope,
    val measurer: TextMeasurer,
    val style: TextStyle,
    val ink: Color,
    val accent: Color,
) {
    val u = scope.density
    val cx = scope.size.width / 2
    val stroke = Stroke(width = 1.5f * u)

    fun x(dp: Float) = cx + dp * u
    fun y(dp: Float) = dp * u

    fun text(s: String, at: Offset, align: Align, color: Color = ink) {
        val layout = measurer.measure(s, style.copy(color = color))
        val left = when (align) {
            Align.Start -> at.x
            Align.Center -> at.x - layout.size.width / 2
            Align.End -> at.x - layout.size.width
        }
        scope.drawText(layout, topLeft = Offset(left, at.y - layout.size.height / 2))
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
        scope.drawLine(ink, Offset(x(x1), y(y1)), Offset(x(x2), y(y2)), strokeWidth = 2f * u)

    fun rect(left: Float, top: Float, width: Float, height: Float, radius: Float = 2f) =
        scope.drawRoundRect(
            ink,
            topLeft = Offset(x(left), y(top)),
            size = Size(width * u, height * u),
            cornerRadius = CornerRadius(radius * u),
            style = stroke,
        )

    /**
     * Number and symbol under a lead that ends at [bottom]. [lowered] puts the symbol one line lower,
     * so the symbols of closely spaced leads ("GND" between "IN" and "OUT") do not run together.
     */
    fun leadLabel(pin: Pin, atX: Float, bottom: Float, lowered: Boolean = false) {
        text("${pin.number}", Offset(x(atX), y(bottom + 10)), Align.Center, accent)
        text(pin.symbol(), Offset(x(atX), y(bottom + if (lowered) 44 else 26)), Align.Center)
    }

    /** Top view: pins 1…n/2 down the left side, then n/2+1…n up the right side. */
    fun dip(pins: List<Pin>) {
        val half = pins.size / 2
        val pitch = 26f
        val top = 10f
        val height = half * pitch + 10f
        val width = 84f
        rect(-width / 2, top, width, height)
        scope.drawArc(
            ink, startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(x(-8f), y(top - 8f)), size = Size(16f * u, 16f * u), style = stroke,
        )
        for (i in 0 until half) {
            val py = top + 5f + pitch * (i + 0.5f)
            val left = pins[i]
            val right = pins[pins.size - 1 - i]
            line(-width / 2 - 14f, py, -width / 2, py)
            line(width / 2, py, width / 2 + 14f, py)
            text("${left.number}", Offset(x(-width / 2 + 6f), y(py)), Align.Start, accent)
            text("${right.number}", Offset(x(width / 2 - 6f), y(py)), Align.End, accent)
            text(left.symbol(), Offset(x(-width / 2 - 18f), y(py)), Align.End)
            text(right.symbol(), Offset(x(width / 2 + 18f), y(py)), Align.Start)
        }
    }

    /** Flat face toward the viewer, rounded top, three leads down. */
    fun to92(pins: List<Pin>) {
        val path = Path().apply {
            moveTo(x(-30f), y(60f))
            lineTo(x(-30f), y(30f))
            arcTo(Rect(Offset(x(-30f), y(6f)), Size(60f * u, 48f * u)), 180f, 180f, false)
            lineTo(x(30f), y(60f))
            close()
        }
        scope.drawPath(path, ink, style = stroke)
        leads(pins, spacing = 20f, top = 60f, length = 34f)
    }

    /** Marked face toward the viewer, mounting hole in the body. */
    fun to126(pins: List<Pin>) {
        rect(-34f, 6f, 68f, 76f)
        scope.drawCircle(ink, radius = 8f * u, center = Offset(x(0f), y(26f)), style = stroke)
        leads(pins, spacing = 22f, top = 82f, length = 34f)
    }

    /** Front toward the viewer, the metal tab with its hole above the body. */
    fun to220(pins: List<Pin>) {
        rect(-38f, 6f, 76f, 36f)
        scope.drawCircle(ink, radius = 8f * u, center = Offset(x(0f), y(24f)), style = stroke)
        rect(-38f, 42f, 76f, 62f)
        leads(pins, spacing = 24f, top = 104f, length = 34f)
    }

    fun leads(pins: List<Pin>, spacing: Float, top: Float, length: Float) {
        pins.forEachIndexed { i, pin ->
            val lx = (i - (pins.size - 1) / 2f) * spacing
            line(lx, top, lx, top + length)
            leadLabel(pin, lx, top + length, lowered = i % 2 == 1)
        }
    }

    /** Top view: pins 1 and 2 at the bottom, pin 3 at the top. */
    fun sot23(pins: List<Pin>) {
        val top = 46f
        rect(-36f, top, 72f, 40f)
        val bottom = top + 40f
        line(-20f, bottom, -20f, bottom + 14f)
        line(20f, bottom, 20f, bottom + 14f)
        line(0f, top - 14f, 0f, top)
        leadLabel(pins[0], -20f, bottom + 14f)
        leadLabel(pins[1], 20f, bottom + 14f)
        text(pins[2].symbol(), Offset(x(0f), y(top - 40f)), Align.Center)
        text("${pins[2].number}", Offset(x(0f), y(top - 24f)), Align.Center, accent)
    }

    /** Horizontal axial body with the band on the cathode side. */
    fun axial(pins: List<Pin>) {
        val cy = 30f
        line(-90f, cy, -36f, cy)
        line(36f, cy, 90f, cy)
        rect(-36f, cy - 14f, 72f, 28f, radius = 6f)
        val bandRight = pins.indexOfFirst { it.function == PinFunction.Cathode } == 1
        val bandX = if (bandRight) 22f else -30f
        scope.drawRect(ink, topLeft = Offset(x(bandX), y(cy - 14f)), size = Size(8f * u, 28f * u))
        listOf(-80f to pins[0], 80f to pins[1]).forEach { (lx, pin) ->
            text("${pin.number}", Offset(x(lx), y(cy + 18f)), Align.Center, accent)
            text(pin.symbol(), Offset(x(lx), y(cy + 34f)), Align.Center)
        }
    }
}
