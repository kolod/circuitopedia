package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

/** Result text of spec FR-009. */
class FormatTest {
    private val en = Locale.ENGLISH
    private val uk = Locale.forLanguageTag("uk")
    private fun ohms(value: String) = Rational.of(BigDecimal(value))

    @Test
    fun connectionText() {
        assertEquals("4.7kΩ", Combination(Kind.Single, listOf(ohms("4700"))).connectionText(en))
        assertEquals("1kΩ + 2.2kΩ", Combination(Kind.Series2, listOf(ohms("1000"), ohms("2200"))).connectionText(en))
        assertEquals("1Ω + 1Ω + 1Ω", Combination(Kind.Series3, listOf(ohms("1"), ohms("1"), ohms("1"))).connectionText(en))
        assertEquals("10kΩ ∥ 10kΩ", Combination(Kind.Parallel2, listOf(ohms("10000"), ohms("10000"))).connectionText(en))
        assertEquals("1MΩ ∥ 2.2MΩ ∥ 10MΩ", Combination(Kind.Parallel3, listOf(ohms("1000000"), ohms("2200000"), ohms("10000000"))).connectionText(en))
        assertEquals("4.7kΩ ∥ 10kΩ + 220Ω", Combination(Kind.ParallelSeries, listOf(ohms("4700"), ohms("10000"), ohms("220"))).connectionText(en))
        assertEquals("4,7kΩ ∥ 10kΩ + 2,2Ω", Combination(Kind.ParallelSeries, listOf(ohms("4700"), ohms("10000"), ohms("2.2"))).connectionText(uk))
    }

    @Test
    fun resistanceHasFourSignificantDigits() {
        // 4.7kΩ ∥ 10kΩ = 3197.278…Ω
        assertEquals("3.197kΩ", Rational.of(47_000_000, 14_700).formatOhms(en))
        assertEquals("0.3333Ω", Rational.of(1, 3).formatOhms(en))
        assertEquals("10MΩ", ohms("10000000").formatOhms(en))
        assertEquals("4.7kΩ", ohms("4700").formatOhms(en))
        assertEquals("1kΩ", ohms("999.96").formatOhms(en))
        assertEquals("3,197kΩ", Rational.of(47_000_000, 14_700).formatOhms(uk))
    }

    @Test
    fun deviationHasSignAndThreeSignificantDigits() {
        assertEquals("0%", formatDeviation(Rational.ZERO, en))
        assertEquals("+0.0213%", formatDeviation(ohms("0.000213"), en))
        assertEquals("−1.23%", formatDeviation(ohms("-0.012345"), en))
        assertEquals("+33.3%", formatDeviation(Rational.of(1, 3), en))
        assertEquals("−1,23%", formatDeviation(ohms("-0.012345"), uk))
    }
}
