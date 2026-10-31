package io.github.kolod.circuitopedia.modules.resistorcolorcode

import android.content.res.Resources
import android.util.Xml
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

// Vertical geometry: the resistor on top, leader lines below it down to the table header.
private val ArtHeight = 84.dp
private val LeaderZone = 32.dp
private val LeaderStraight = 8.dp
private val LeaderWidth = 3.dp

/** Number of bands drawn in `res/raw/resistor_color_code_resistor.svg` (`band-1` … `band-6`). */
internal const val SVG_BAND_COUNT = 6

/**
 * The SVG bands (1-based) each layout shows, one per column. The last tolerance or TCR band sits on
 * the right end cap (`band-6`); the unused bands are hidden.
 */
internal fun BandLayout.svgBands(): List<Int> = when (this) {
    BandLayout.Three -> listOf(1, 2, 3)
    BandLayout.Four -> listOf(1, 2, 3, 6)
    BandLayout.Five -> listOf(1, 2, 3, 4, 6)
    BandLayout.Six -> listOf(1, 2, 3, 4, 5, 6)
}

/** Extra CSS that colors the shown bands and hides the others. */
internal fun bandCss(layout: BandLayout, colors: List<BandColor>): String = buildString {
    val shown = layout.svgBands()
    for (band in 1..SVG_BAND_COUNT) {
        val column = shown.indexOf(band)
        if (column < 0) append("#band-$band{display:none}")
        else append("#band-$band{fill:#%06X}".format(Locale.ROOT, colors[column].argb and 0xFFFFFF))
    }
}

/** The parsed drawing, its viewBox and the bounds of `band-1` … `band-6`, in SVG units. */
private class ResistorArt(val svg: SVG, val viewBox: Rect, val bands: List<Rect>)

private fun loadArt(resources: Resources): ResistorArt {
    val svg = SVG.getFromResource(resources, R.raw.resistor_color_code_resistor).apply {
        // Otherwise the width/height attributes (kept for Inkscape) fix the size and the viewPort only moves it.
        setDocumentWidth("100%")
        setDocumentHeight("100%")
    }
    // AndroidSVG does not expose element geometry, so read the band outlines from the file itself.
    val pathData = mutableMapOf<String, String>()
    resources.openRawResource(R.raw.resistor_color_code_resistor).use { stream ->
        val parser = Xml.newPullParser().apply { setInput(stream, null) }
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            val id = parser.getAttributeValue(null, "id") ?: continue
            pathData[id] = parser.getAttributeValue(null, "d") ?: continue
        }
    }
    val bands = (1..SVG_BAND_COUNT).map { band ->
        val d = checkNotNull(pathData["band-$band"]) { "band-$band is missing from the resistor drawing" }
        PathParser().parsePathString(d).toPath().getBounds()
    }
    val box = svg.documentViewBox
    return ResistorArt(svg, Rect(box.left, box.top, box.right, box.bottom), bands)
}

/**
 * Decorative resistor in the selected colors, rendered from an SVG above the table header. Each band
 * is joined to the top edge of its header column by a leader line made of vertical and slanted
 * segments only. Must be laid out with the same width and side padding as the table; the value text
 * carries the information.
 */
@Composable
fun ResistorDrawing(selection: Selection, modifier: Modifier = Modifier) {
    val resources = LocalResources.current
    val art = remember(resources) { loadArt(resources) }
    val layout = selection.layout
    val css = remember(selection) { bandCss(layout, selection.colors) }
    val leaderColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier.height(ArtHeight + LeaderZone).clearAndSetSemantics {}) {
        val box = art.viewBox
        // Centered, keeping the SVG's proportions; shrinks only if the screen is too narrow.
        val scale = minOf(size.width / box.width, ArtHeight.toPx() / box.height)
        val left = (size.width - box.width * scale) / 2
        fun x(svgX: Float) = left + (svgX - box.left) * scale
        fun y(svgY: Float) = (svgY - box.top) * scale

        drawIntoCanvas { canvas ->
            val options = RenderOptions.create().css(css).viewPort(left, 0f, box.width * scale, box.height * scale)
            art.svg.renderToCanvas(canvas.nativeCanvas, options)
        }

        val columns = bandColumnCenters(size.width, CellGap.toPx(), layout.bandCount)
        val straight = LeaderStraight.toPx()
        layout.svgBands().forEachIndexed { i, band ->
            val bounds = art.bands[band - 1]
            val bandX = x(bounds.center.x)
            val bandBottom = y(bounds.bottom)
            // Up from the header column, slant across, then up into the band: no horizontal segments.
            val path = Path().apply {
                moveTo(columns[i], size.height)
                lineTo(columns[i], size.height - straight)
                lineTo(bandX, bandBottom + straight)
                lineTo(bandX, bandBottom)
            }
            drawPath(path, leaderColor, style = Stroke(LeaderWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
