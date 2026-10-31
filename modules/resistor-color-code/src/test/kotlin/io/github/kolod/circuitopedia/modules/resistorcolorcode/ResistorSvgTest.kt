package io.github.kolod.circuitopedia.modules.resistorcolorcode

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.shared.colorcode.SvgContract
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks the contract between `resistor_color_code_resistor.svg` and ResistorDrawing.kt. */
class ResistorSvgTest {
    @Test
    fun drawingFollowsTheSvgContract() {
        val problems = SvgContract.check(File("src/main/res/raw/resistor_color_code_resistor.svg"), SVG_BAND_COUNT)
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
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
}
