package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Blue
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gray
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Green
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.White
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/** Every cell of the Bourns MIL/EIA inductor color code table (research R1). */
class InductorCodeTableTest {

    @Test
    fun digits() {
        val expected = mapOf(
            Black to 0, Brown to 1, Red to 2, Orange to 3, Yellow to 4,
            Green to 5, Blue to 6, Violet to 7, Gray to 8, White to 9,
            Gold to null, Silver to null,
        )
        for (color in BandColor.entries) assertEquals(color.name, expected.getValue(color), color.digit)
    }

    @Test
    fun multipliersInPowersOfTenOfMicrohenries() {
        val expected = mapOf(Black to 0, Brown to 1, Red to 2, Orange to 3, Gold to -1, Silver to -2)
        for (color in BandColor.entries) assertEquals(color.name, expected[color], color.multiplierExponent)
    }

    @Test
    fun tolerances() {
        val expected = mapOf(Gold to BigDecimal("5"), Silver to BigDecimal("10"))
        for (color in BandColor.entries) assertEquals(color.name, expected[color], color.tolerance?.value)
    }

    @Test
    fun onlyGoldIsADecimalPoint() {
        for (color in BandColor.entries) assertEquals(color.name, color == Gold, color.isDecimalPoint)
    }
}
