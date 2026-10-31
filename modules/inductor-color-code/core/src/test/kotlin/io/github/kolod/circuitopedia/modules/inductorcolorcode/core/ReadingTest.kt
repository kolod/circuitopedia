package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Blue
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gray
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.White
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/** Readings from the spec's acceptance scenarios and edge cases, and Bourns' examples. */
class ReadingTest {
    private fun read(vararg colors: BandColor): Reading {
        val layout = if (colors.size == 3) BandLayout.Three else BandLayout.Four
        return Selection(layout, colors.toList()).read()
    }

    private fun assertReads(microhenries: String, tolerance: String, reading: Reading) {
        assertEquals(0, BigDecimal(microhenries).compareTo(reading.inductance.value))
        assertEquals(0, BigDecimal(tolerance).compareTo(reading.tolerance.value))
    }

    // US1: 4 bands without a decimal point

    @Test
    fun fourBandDefaultIs47Microhenries() = assertReads("47", "10", read(Yellow, Violet, Black, Silver))

    @Test
    fun brownBlackBrownGoldIs100Microhenries() = assertReads("100", "5", read(Brown, Black, Brown, Gold))

    @Test
    fun goldMultiplierGivesTenths() = assertReads("2.2", "5", read(Red, Red, Gold, Gold))

    @Test
    fun silverMultiplierGivesHundredths() = assertReads("0.1", "10", read(Brown, Black, Silver, Silver))

    @Test
    fun largestValueIs99Millihenries() = assertReads("99000", "5", read(White, White, Orange, Gold))

    // US2: 3 bands and the decimal point

    @Test
    fun threeBandDefaultHasTwentyPercent() = assertReads("47", "20", read(Yellow, Violet, Black))

    @Test
    fun pointInBandTwo() = assertReads("4.7", "20", read(Yellow, Gold, Violet))

    @Test
    fun goldMultiplierGivesTheSameValueAsThePoint() = assertReads("4.7", "20", read(Yellow, Violet, Gold))

    @Test
    fun pointInBandOne() = assertReads("0.22", "20", read(Gold, Red, Red))

    @Test
    fun silverMultiplierMatchesThePointInBandOne() = assertReads("0.22", "20", read(Red, Red, Silver))

    @Test
    fun pointInBandOneWithVioletVioletIs770Nanohenries() = assertReads("0.77", "10", read(Gold, Violet, Violet, Silver))

    @Test
    fun smallestValueWithThePoint() = assertReads("0.1", "20", read(Gold, Brown, Black))

    @Test
    fun bournsMilExampleIs6point8Microhenries() = assertReads("6.8", "10", read(Blue, Gold, Gray, Silver))

    @Test
    fun bournsEiaExampleIs6point8Microhenries() = assertReads("6.8", "10", read(Blue, Gray, Gold, Silver))

    @Test
    fun bournsExampleIs270Microhenries() = assertReads("270", "5", read(Red, Violet, Brown, Gold))
}
