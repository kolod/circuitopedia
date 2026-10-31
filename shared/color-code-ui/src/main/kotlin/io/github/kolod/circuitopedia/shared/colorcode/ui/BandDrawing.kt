package io.github.kolod.circuitopedia.shared.colorcode.ui

import android.content.res.Resources
import android.util.Xml
import androidx.annotation.RawRes
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
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.bandCss
import org.xmlpull.v1.XmlPullParser

// Vertical geometry: the component on top, leader lines below it down to the table header.
private val ArtHeight = 84.dp
private val LeaderZone = 32.dp
private val LeaderStraight = 8.dp
private val LeaderWidth = 3.dp

/** The parsed drawing, its viewBox and the bounds of `band-1` … `band-N`, in SVG units. */
private class BandArt(val svg: SVG, val viewBox: Rect, val bands: List<Rect>)

private fun loadArt(resources: Resources, @RawRes id: Int): BandArt {
    val svg = SVG.getFromResource(resources, id).apply {
        // Otherwise the width/height attributes (kept for Inkscape) fix the size and the viewPort only moves it.
        setDocumentWidth("100%")
        setDocumentHeight("100%")
    }
    // AndroidSVG does not expose element geometry, so read the band outlines from the file itself.
    val pathData = mutableMapOf<String, String>()
    resources.openRawResource(id).use { stream ->
        val parser = Xml.newPullParser().apply { setInput(stream, null) }
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            val elementId = parser.getAttributeValue(null, "id") ?: continue
            pathData[elementId] = parser.getAttributeValue(null, "d") ?: continue
        }
    }
    val bands = generateSequence(1) { it + 1 }
        .map { pathData["band-$it"] }
        .takeWhile { it != null }
        .map { PathParser().parsePathString(it!!).toPath().getBounds() }
        .toList()
    val box = svg.documentViewBox
    return BandArt(svg, Rect(box.left, box.top, box.right, box.bottom), bands)
}

/**
 * Decorative component drawing rendered from the SVG [svg] (contract: spec 002 research R10), with
 * the SVG bands [shownBands] (1-based, one per table column) in [colors] and the other bands hidden.
 * Each shown band is joined to the top edge of its header column by a leader line made of vertical
 * and slanted segments only. Must be laid out with the same width and side padding as the table;
 * the value text carries the information.
 */
@Composable
fun BandDrawing(@RawRes svg: Int, shownBands: List<Int>, colors: List<BandColor>, modifier: Modifier = Modifier) {
    val resources = LocalResources.current
    val art = remember(resources, svg) { loadArt(resources, svg) }
    val css = remember(art, shownBands, colors) { bandCss(shownBands, colors, art.bands.size) }
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

        val columns = bandColumnCenters(size.width, CellGap.toPx(), shownBands.size)
        val straight = LeaderStraight.toPx()
        shownBands.forEachIndexed { i, band ->
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
