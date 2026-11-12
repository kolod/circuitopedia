package io.github.kolod.circuitopedia.modules.boardpinouts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardUse
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Direction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.HeaderPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Side
import kotlin.math.abs

// The board drawing (research R5): one Canvas for every board, laid out from BoardLayout in header
// pitch units. Labels sit outside each row, away from a neighbouring parallel row. The drawing is
// decorative for screen readers; the pin list carries the semantics.

/** Room for labels on each side, in pitch units. */
private const val LABEL_ROOM = 5.5f

private enum class LabelSide { Left, Right, Above, Below }

private data class Geometry(val board: Board) {
    val layout = board.layout
    private val headers = layout.headers.associateBy { it.id }

    fun unitCenter(pin: HeaderPin): Offset {
        val h = headers.getValue(pin.header)
        val step = (pin.position - 1).toFloat()
        return when (h.direction) {
            Direction.Down -> Offset(h.x, h.y + step)
            Direction.Up -> Offset(h.x, h.y - step)
            Direction.Right -> Offset(h.x + step, h.y)
        }
    }

    fun side(header: String): LabelSide {
        val h = headers.getValue(header)
        val vertical = h.direction != Direction.Right
        val neighbour = layout.headers.firstOrNull { o ->
            o.id != h.id && (o.direction != Direction.Right) == vertical &&
                if (vertical) abs(o.x - h.x) <= 1.5f else abs(o.y - h.y) <= 1.5f
        }
        return if (vertical) {
            when {
                neighbour != null -> if (neighbour.x > h.x) LabelSide.Left else LabelSide.Right
                h.x < layout.width / 2 -> LabelSide.Left
                else -> LabelSide.Right
            }
        } else {
            when {
                neighbour != null -> if (neighbour.y > h.y) LabelSide.Above else LabelSide.Below
                h.y < layout.height / 2 -> LabelSide.Above
                else -> LabelSide.Below
            }
        }
    }

    /** Extra rows above and below the board for labels of horizontal headers. */
    val top: Float = layout.headers.filter { it.direction == Direction.Right && side(it.id) == LabelSide.Above }
        .maxOfOrNull { (LABEL_ROOM - it.y).coerceAtLeast(0f) } ?: 0f
    val bottom: Float = layout.headers.filter { it.direction == Direction.Right && side(it.id) == LabelSide.Below }
        .maxOfOrNull { (it.y + LABEL_ROOM - layout.height).coerceAtLeast(0f) } ?: 0f

    val widthUnits = layout.width + 2 * LABEL_ROOM
    val heightUnits = layout.height + top + bottom + 1f

    fun toPx(unit: Offset, pitch: Float) = Offset((unit.x + LABEL_ROOM) * pitch, (unit.y + top + 0.5f) * pitch)

    /** The tap area of a pin: its pad plus its label. */
    fun hitRect(pin: HeaderPin, pitch: Float): Rect {
        val c = toPx(unitCenter(pin), pitch)
        val half = pitch * 0.5f
        val reach = LABEL_ROOM * pitch
        return when (side(pin.header)) {
            LabelSide.Left -> Rect(c.x - reach, c.y - half, c.x + half, c.y + half)
            LabelSide.Right -> Rect(c.x - half, c.y - half, c.x + reach, c.y + half)
            LabelSide.Above -> Rect(c.x - half, c.y - reach, c.x + half, c.y + half)
            LabelSide.Below -> Rect(c.x - half, c.y - half, c.x + half, c.y + reach)
        }
    }
}

@Composable
internal fun BoardDrawing(board: Board, marks: Set<LimitKind>, claims: Claims, onPin: (HeaderPin) -> Unit) {
    val res = LocalResources.current
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    val measurer = rememberTextMeasurer()
    val geometry = remember(board) { Geometry(board) }
    val density = LocalDensity.current
    val description = res.getString(R.string.board_pinouts_drawing_description, board.name)
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val pitchDp = maxWidth / geometry.widthUnits
        val pitch = with(density) { pitchDp.toPx() }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(pitchDp * geometry.heightUnits)
                .pointerInput(board, pitch) {
                    detectTapGestures { tap ->
                        board.pins.filter { geometry.hitRect(it, pitch).contains(tap) }
                            .minByOrNull { (geometry.toPx(geometry.unitCenter(it), pitch) - tap).getDistance() }
                            ?.let(onPin)
                    }
                }
                .clearAndSetSemantics {
                    contentDescription = description
                    testTag = "bp-drawing"
                },
        ) {
            val layout = board.layout
            // Board outline and USB connector.
            val origin = geometry.toPx(Offset(0f, -0.5f), pitch)
            val boardSize = Size(layout.width * pitch, (layout.height + 0.5f) * pitch)
            drawRoundRect(colors.surfaceVariant, origin, boardSize, CornerRadius(pitch * 0.6f))
            drawRoundRect(colors.outline, origin, boardSize, CornerRadius(pitch * 0.6f), style = Stroke(pitch * 0.06f))
            val usbSize = Size(pitch * 2.6f, pitch * 1.4f)
            val usbTopLeft = when (layout.usb) {
                Side.Top -> Offset(origin.x + (boardSize.width - usbSize.width) / 2, origin.y - usbSize.height * 0.4f)
                Side.Bottom -> Offset(origin.x + (boardSize.width - usbSize.width) / 2, origin.y + boardSize.height - usbSize.height * 0.6f)
                Side.Left -> Offset(origin.x - usbSize.height * 0.4f, origin.y + (boardSize.height - usbSize.width) / 2)
                Side.Right -> Offset(origin.x + boardSize.width - usbSize.height * 0.6f, origin.y + (boardSize.height - usbSize.width) / 2)
            }
            val usbDrawn = if (layout.usb == Side.Left || layout.usb == Side.Right) Size(usbSize.height, usbSize.width) else usbSize
            drawRoundRect(colors.outline, usbTopLeft, usbDrawn, CornerRadius(pitch * 0.2f))

            val fontSize = with(density) { (pitch * 0.62f).toSp() }
            drawOnboardParts(board, geometry, pitch, LimitKind.BoardUse.color(dark), colors.onSurfaceVariant) { use, pin ->
                measurer.measure("${res.getString(use.tag())} · ${pin.label}", TextStyle(fontSize = fontSize, color = LimitKind.BoardUse.color(dark)))
            }
            board.pins.forEach { pin ->
                val c = geometry.toPx(geometry.unitCenter(pin), pitch)
                val claim = pin.pin?.let { claims[it] }
                val r = pitch * 0.32f
                // Pad: square for power, filled when assigned.
                val power = pin.power
                if (power != null) {
                    val fill = when (power.role) {
                        PowerRole.Ground -> colors.onSurfaceVariant
                        PowerRole.NotConnected -> colors.surface
                        else -> colors.error
                    }
                    drawRect(fill, Offset(c.x - r, c.y - r), Size(2 * r, 2 * r))
                } else if (claim != null) {
                    drawCircle(colors.primary, r, c)
                } else {
                    drawCircle(colors.surface, r, c)
                    drawCircle(colors.onSurfaceVariant, r, c, style = Stroke(pitch * 0.07f))
                }
                // Label: name, assigned signal, and the symbols of the enabled marks.
                val text = buildAnnotatedString {
                    val labelStyle = SpanStyle(
                        color = if (claim != null) colors.primary else colors.onSurface,
                        fontWeight = if (claim != null) FontWeight.Bold else FontWeight.Normal,
                    )
                    withStyle(labelStyle) {
                        append(pin.label)
                        if (claim != null) append(" ${claim.second}")
                    }
                    // Onboard parts are always tagged, whatever marks are enabled.
                    board.usesOf(pin).forEach { use ->
                        append(" ")
                        withStyle(SpanStyle(color = LimitKind.BoardUse.color(dark), fontWeight = FontWeight.Bold)) {
                            append(res.getString(use.tag()))
                        }
                    }
                    board.kindsOf(pin).filter { it in marks && it != LimitKind.BoardUse }.forEach { kind ->
                        append(" ")
                        withStyle(SpanStyle(color = kind.color(dark), fontWeight = FontWeight.Bold)) { append(kind.symbol()) }
                    }
                }
                val measured = measurer.measure(text, TextStyle(fontSize = fontSize))
                val w = measured.size.width.toFloat()
                val h = measured.size.height.toFloat()
                val gap = pitch * 0.55f
                when (geometry.side(pin.header)) {
                    LabelSide.Left -> drawText(measured, topLeft = Offset(c.x - gap - w, c.y - h / 2))
                    LabelSide.Right -> drawText(measured, topLeft = Offset(c.x + gap, c.y - h / 2))
                    LabelSide.Above -> rotate(-90f, c) { drawText(measured, topLeft = Offset(c.x + gap, c.y - h / 2)) }
                    LabelSide.Below -> rotate(90f, c) { drawText(measured, topLeft = Offset(c.x + gap, c.y - h / 2)) }
                }
            }
        }
    }
}

/**
 * Symbols of the onboard LEDs and buttons wired to header pins, placed in a column in the middle of
 * the board, each tagged with its pin and joined to that pin's pad by a dashed line (FR-017).
 */
private fun DrawScope.drawOnboardParts(
    board: Board,
    geometry: Geometry,
    pitch: Float,
    color: Color,
    wire: Color,
    label: (BoardUse, HeaderPin) -> TextLayoutResult,
) {
    val parts = board.pins.flatMap { pin -> board.usesOf(pin).filter { it == BoardUse.Led || it == BoardUse.Button }.map { it to pin } }
        .distinctBy { (use, pin) -> use to pin.pin }
    if (parts.isEmpty()) return
    val layout = board.layout
    val step = 3.2f
    val firstY = layout.height / 2 - (parts.size - 1) * step / 2
    val stroke = pitch * 0.09f
    parts.forEachIndexed { i, (use, pin) ->
        val center = geometry.toPx(Offset(layout.width / 2, firstY + i * step), pitch)
        val pad = geometry.toPx(geometry.unitCenter(pin), pitch)
        // Wire at right angles: out of the symbol's near side, along a rail just inside the header
        // row (each part on its own rail), then into the pad, so it never crosses other pads.
        val toward = if (pad.x < center.x) -1f else 1f
        val start = Offset(center.x + toward * pitch * 0.9f, center.y)
        val railX = pad.x - toward * pitch * (0.75f + 0.3f * i)
        val path = Path().apply {
            moveTo(start.x, start.y)
            lineTo(railX, start.y)
            lineTo(railX, pad.y)
            lineTo(pad.x - toward * pitch * 0.34f, pad.y)
        }
        drawPath(
            path, wire,
            style = Stroke(pitch * 0.05f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(pitch * 0.25f, pitch * 0.2f))),
        )
        when (use) {
            BoardUse.Led -> drawLed(center, pitch, color, stroke)
            else -> drawButton(center, pitch, color, stroke)
        }
        val text = label(use, pin)
        drawText(text, topLeft = Offset(center.x - text.size.width / 2f, center.y + pitch * 0.75f))
    }
}

/** A diode triangle with its bar and two light arrows. */
private fun DrawScope.drawLed(c: Offset, pitch: Float, color: Color, stroke: Float) {
    val h = pitch * 0.5f
    val triangle = Path().apply {
        moveTo(c.x - h, c.y - h)
        lineTo(c.x - h, c.y + h)
        lineTo(c.x + h * 0.6f, c.y)
        close()
    }
    drawLine(color, Offset(c.x - pitch * 0.9f, c.y), Offset(c.x + pitch * 0.9f, c.y), strokeWidth = stroke)
    drawPath(triangle, color)
    drawLine(color, Offset(c.x + h * 0.6f, c.y - h), Offset(c.x + h * 0.6f, c.y + h), strokeWidth = stroke)
    listOf(0f, pitch * 0.3f).forEach { dx ->
        val from = Offset(c.x - h * 0.1f + dx, c.y - h * 1.1f)
        val to = Offset(from.x + pitch * 0.35f, from.y - pitch * 0.35f)
        drawLine(color, from, to, strokeWidth = stroke * 0.8f)
        drawLine(color, to, Offset(to.x - pitch * 0.15f, to.y), strokeWidth = stroke * 0.8f)
        drawLine(color, to, Offset(to.x, to.y + pitch * 0.15f), strokeWidth = stroke * 0.8f)
    }
}

/** A push button: two contacts, the bridging bar above them and its plunger. */
private fun DrawScope.drawButton(c: Offset, pitch: Float, color: Color, stroke: Float) {
    val w = pitch * 0.55f
    drawLine(color, Offset(c.x - pitch * 0.9f, c.y), Offset(c.x - w, c.y), strokeWidth = stroke)
    drawLine(color, Offset(c.x + w, c.y), Offset(c.x + pitch * 0.9f, c.y), strokeWidth = stroke)
    drawCircle(color, pitch * 0.09f, Offset(c.x - w, c.y))
    drawCircle(color, pitch * 0.09f, Offset(c.x + w, c.y))
    val barY = c.y - pitch * 0.35f
    drawLine(color, Offset(c.x - w, barY), Offset(c.x + w, barY), strokeWidth = stroke)
    drawLine(color, Offset(c.x, barY), Offset(c.x, barY - pitch * 0.35f), strokeWidth = stroke)
    drawLine(color, Offset(c.x - pitch * 0.2f, barY - pitch * 0.35f), Offset(c.x + pitch * 0.2f, barY - pitch * 0.35f), strokeWidth = stroke)
}
