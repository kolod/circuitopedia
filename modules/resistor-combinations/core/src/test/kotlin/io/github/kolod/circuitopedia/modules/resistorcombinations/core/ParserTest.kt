package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Target notations of spec FR-002 and research R5. */
class ParserTest {
    private fun ohms(value: String) = Rational.of(BigDecimal(value))

    @Test
    fun acceptedNotations() {
        val cases = mapOf(
            "4700" to "4700",
            "4.7k" to "4700",
            "4,7k" to "4700",
            "4.7 k" to "4700",
            "4k7" to "4700",
            "4K7" to "4700",
            "4k" to "4000",
            "R47" to "0.47",
            "47R" to "47",
            "4R7" to "4.7",
            "1M5" to "1500000",
            "470m" to "0.47",
            " 4.7 kΩ " to "4700",
            "2k2Ω" to "2200",
            "100ohm" to "100",
            "100 Ohm" to "100",
            "0.1" to "0.1",
            ",5" to "0.5",
            "1G" to "1000000000",
            "1.234k" to "1234",
        )
        for ((text, expected) in cases) assertEquals(text, ohms(expected), parseTarget(text))
    }

    @Test
    fun rejectedNotations() {
        val cases = listOf("", "  ", "abc", "4.7.1", "4,7,1", "4.7,1", "-1", "0", "0.0", "2G", "1.5G", "0.05", "4k7k", "k", "1.5k7", "4 k 7", "Ω", "4x")
        for (text in cases) assertNull(text, parseTarget(text))
    }

    @Test
    fun rangeBoundariesAreInclusive() {
        assertEquals(ohms("0.1"), parseTarget("100m"))
        assertEquals(ohms("1000000000"), parseTarget("1000M"))
        assertNull(parseTarget("99m"))
        assertNull(parseTarget("1000000001"))
    }
}
