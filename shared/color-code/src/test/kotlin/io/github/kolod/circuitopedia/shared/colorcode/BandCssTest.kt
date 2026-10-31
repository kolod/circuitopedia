package io.github.kolod.circuitopedia.shared.colorcode

import org.junit.Assert.assertEquals
import org.junit.Test

class BandCssTest {
    @Test
    fun colorsShownBandsAndHidesTheRest() {
        val colors = listOf(BandColor.Yellow, BandColor.Violet, BandColor.Red, BandColor.Gold)
        assertEquals(
            "#band-1{fill:#F5E83A}#band-2{fill:#8236AA}#band-3{fill:#C01818}" +
                "#band-4{display:none}#band-5{display:none}#band-6{fill:#D4A62A}",
            bandCss(listOf(1, 2, 3, 6), colors, 6),
        )
    }
}
