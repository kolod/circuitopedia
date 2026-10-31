package io.github.kolod.circuitopedia.modules.resistorcolorcode

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Checks the contract between `resistor_color_code_resistor.svg` and ResistorDrawing.kt. */
class ResistorSvgTest {
    private val document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(File("src/main/res/raw/resistor_color_code_resistor.svg"))

    private val elements: List<Element> = document.getElementsByTagName("*").let { nodes ->
        (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun byId(id: String) = elements.firstOrNull { it.getAttribute("id") == id }

    @Test
    fun rootHasViewBox() {
        assertEquals(4, document.documentElement.getAttribute("viewBox").trim().split(Regex("[\\s,]+")).size)
    }

    @Test
    fun everyBandIsAPathWithAPresentationFill() {
        for (band in 1..SVG_BAND_COUNT) {
            val element = checkNotNull(byId("band-$band")) { "band-$band is missing" }
            assertEquals("band-$band must be a path", "path", element.tagName)
            assertTrue("band-$band has no path data", element.getAttribute("d").isNotBlank())
            // CSS added at render time does not override an inline style, so the fill must be an attribute.
            assertFalse("band-$band sets fill in style", element.getAttribute("style").contains("fill"))
        }
    }

    @Test
    fun noElementIsTransformed() {
        val transformed = elements.filter { it.hasAttribute("transform") }.map { it.getAttribute("id") }
        assertTrue("transformed elements: $transformed", transformed.isEmpty())
    }

    @Test
    fun everyLayoutShowsOneBandPerColumnLeftToRight() {
        for (layout in BandLayout.entries) {
            val bands = layout.svgBands()
            assertEquals(layout.name, layout.bandCount, bands.size)
            assertEquals(layout.name, bands.sorted().distinct(), bands)
            assertTrue(layout.name, bands.all { it in 1..SVG_BAND_COUNT })
        }
    }

    @Test
    fun cssColorsShownBandsAndHidesTheRest() {
        val colors = listOf(BandColor.Yellow, BandColor.Violet, BandColor.Red, BandColor.Gold)
        val css = bandCss(BandLayout.Four, colors)
        assertTrue(css, css.contains("#band-1{fill:#F5E83A}"))
        assertTrue(css, css.contains("#band-6{fill:#D4A62A}"))
        assertTrue(css, css.contains("#band-4{display:none}"))
        assertTrue(css, css.contains("#band-5{display:none}"))
    }
}
