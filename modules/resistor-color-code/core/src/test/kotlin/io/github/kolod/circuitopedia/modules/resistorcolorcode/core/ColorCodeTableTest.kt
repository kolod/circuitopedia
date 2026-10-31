package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Black
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Blue
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Brown
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Gold
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Gray
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
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every cell of IEC 60062:2016 §5, Tables 1–3 (see specs/002-resistor-color-code/research.md R1). */
class ColorCodeTableTest {

    @Test
    fun rowOrderMatchesTheTable() {
        assertEquals(
            listOf(Black, Brown, Red, Orange, Yellow, Green, Blue, Violet, Gray, White, Gold, Silver),
            BandColor.entries,
        )
    }

    @Test
    fun digits() {
        val expected = mapOf(
            Black to 0, Brown to 1, Red to 2, Orange to 3, Yellow to 4,
            Green to 5, Blue to 6, Violet to 7, Gray to 8, White to 9, Gold to null, Silver to null,
        )
        for ((color, digit) in expected) assertEquals("$color", digit, color.digit)
    }

    @Test
    fun multiplierExponents() {
        val expected = mapOf(
            Black to 0, Brown to 1, Red to 2, Orange to 3, Yellow to 4, Green to 5,
            Blue to 6, Violet to 7, Gray to 8, White to 9, Gold to -1, Silver to -2,
        )
        for ((color, exponent) in expected) assertEquals("$color", exponent, color.multiplierExponent)
    }

    @Test
    fun tolerances() {
        val expected = mapOf(
            Brown to "1", Red to "2", Orange to "0.05", Yellow to "0.02", Green to "0.5", Blue to "0.25",
            Violet to "0.1", Gray to "0.01", Gold to "5", Silver to "10",
        )
        for (color in BandColor.entries) {
            val tolerance = expected[color]
            if (tolerance == null) {
                assertNull("$color", color.tolerance)
            } else {
                assertTrue("$color: ${color.tolerance}", BigDecimal(tolerance).compareTo(color.tolerance!!.value) == 0)
            }
        }
    }

    @Test
    fun temperatureCoefficients() {
        val expected = mapOf(
            Black to 250, Brown to 100, Red to 50, Orange to 15, Yellow to 25,
            Green to 20, Blue to 10, Violet to 5, Gray to 1,
        )
        for (color in BandColor.entries) assertEquals("$color", expected[color], color.tcr?.value)
    }
}
