package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Black
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Brown
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Gold
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Green
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Orange
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Red
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Silver
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Violet
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.White
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Yellow
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingTest {

    private fun read(layout: BandLayout, vararg colors: BandColor) = Selection(layout, colors.toList()).read()

    private fun assertReading(expectedOhms: String, expectedTolerance: String, reading: Reading, tcr: Int? = null) {
        assertEquals("ohms", 0, BigDecimal(expectedOhms).compareTo(reading.resistance.value))
        assertEquals("tolerance", 0, BigDecimal(expectedTolerance).compareTo(reading.tolerance.value))
        assertEquals("tcr", tcr, reading.tcr?.value)
    }

    // 4 bands (US1)

    @Test
    fun fourBandDefault() = assertReading("4700", "5", read(BandLayout.Four, Yellow, Violet, Red, Gold))

    @Test
    fun fourBandTenKilohm() = assertReading("10000", "1", read(BandLayout.Four, Brown, Black, Orange, Brown))

    @Test
    fun fourBandGoldMultiplier() = assertReading("2.2", "5", read(BandLayout.Four, Red, Red, Gold, Gold))

    @Test
    fun fourBandSilverMultiplierIsExact() {
        val reading = read(BandLayout.Four, Red, Red, Silver, Silver)
        assertEquals(BigDecimal("0.22"), reading.resistance.value.stripTrailingZeros())
        assertReading("0.22", "10", reading)
    }

    @Test
    fun fourBandSmallest() = assertReading("0.1", "5", read(BandLayout.Four, Brown, Black, Silver, Gold))

    @Test
    fun fourBandLargest() = assertReading("99000000000", "1", read(BandLayout.Four, White, White, White, Brown))

    @Test
    fun fourBandHasNoTcr() = assertNull(read(BandLayout.Four, Yellow, Violet, Red, Gold).tcr)

    // 3, 5 and 6 bands (US2)

    @Test
    fun threeBandHasTwentyPercentTolerance() = assertReading("4700", "20", read(BandLayout.Three, Yellow, Violet, Red))

    @Test
    fun threeBandSmallest() = assertReading("0.1", "20", read(BandLayout.Three, Brown, Black, Silver))

    @Test
    fun fiveBandTenKilohm() = assertReading("10000", "1", read(BandLayout.Five, Brown, Black, Black, Red, Brown))

    @Test
    fun fiveBandDefault() = assertReading("4700", "1", read(BandLayout.Five, Yellow, Violet, Black, Brown, Brown))

    @Test
    fun fiveBandThreeSignificantDigits() {
        assertReading("121", "1", read(BandLayout.Five, Brown, Red, Brown, Black, Brown))
        assertReading("4750", "1", read(BandLayout.Five, Yellow, Violet, Green, Brown, Brown))
    }

    @Test
    fun fiveBandSmallestAndLargest() {
        assertReading("1", "1", read(BandLayout.Five, Brown, Black, Black, Silver, Brown))
        assertReading("999000000000", "1", read(BandLayout.Five, White, White, White, White, Brown))
    }

    @Test
    fun sixBandDefault() =
        assertReading("4700", "1", read(BandLayout.Six, Yellow, Violet, Black, Brown, Brown, Brown), tcr = 100)
}
