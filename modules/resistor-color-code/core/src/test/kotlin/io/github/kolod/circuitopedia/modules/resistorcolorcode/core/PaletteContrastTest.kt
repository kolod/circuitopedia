package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteContrastTest {

    @Test
    fun contrastRatioOfReferenceColors() {
        assertEquals(21.0, contrastRatio(0xFFFFFFFF.toInt(), 0xFF000000.toInt()), 0.01)
        assertEquals(21.0, contrastRatio(0xFF000000.toInt(), 0xFFFFFFFF.toInt()), 0.01)
        assertEquals(1.0, contrastRatio(0xFF808080.toInt(), 0xFF808080.toInt()), 0.0001)
    }

    /** SC-005 / FR-007: cell text is readable on the whole gradient of every band color. */
    @Test
    fun cellTextHasAtLeastAaContrastOnEveryBandColor() {
        for (color in BandColor.entries) {
            val text = if (color.textOnColor == TextOnColor.Light) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            for (background in listOf(color.argb, lighten(color.argb, CELL_GRADIENT), darken(color.argb, CELL_GRADIENT))) {
                val ratio = contrastRatio(text, background)
                assertTrue("$color: %.2f on #%08X".format(ratio, background), ratio >= 4.5)
            }
        }
    }
}
