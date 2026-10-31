package io.github.kolod.circuitopedia.shared.resistors

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** IEC 60063:2015 values (research R1). */
class ESeriesTest {

    @Test
    fun counts() {
        assertEquals(listOf(3, 6, 12, 24, 48, 96, 192), ESeries.entries.map { it.base.size })
    }

    @Test
    fun coarseSeriesLiterally() {
        assertEquals(listOf(100, 220, 470), ESeries.E3.base)
        assertEquals(listOf(100, 150, 220, 330, 470, 680), ESeries.E6.base)
        assertEquals(listOf(100, 120, 150, 180, 220, 270, 330, 390, 470, 560, 680, 820), ESeries.E12.base)
        assertEquals(
            listOf(100, 110, 120, 130, 150, 160, 180, 200, 220, 240, 270, 300, 330, 360, 390, 430, 470, 510, 560, 620, 680, 750, 820, 910),
            ESeries.E24.base,
        )
    }

    @Test
    fun fineSeriesSpotValues() {
        assertEquals(listOf(100, 105, 110, 115), ESeries.E48.base.take(4))
        assertEquals(953, ESeries.E48.base.last())
        assertEquals(listOf(100, 102, 105, 107), ESeries.E96.base.take(4))
        assertEquals(976, ESeries.E96.base.last())
        assertEquals(listOf(100, 101, 102, 104), ESeries.E192.base.take(4))
        assertEquals(988, ESeries.E192.base.last())
    }

    @Test
    fun e24KeepsItsHistoricalValues() {
        for (value in listOf(270, 300, 330, 360, 390, 430, 470, 820)) assertTrue("$value", value in ESeries.E24.base)
    }

    @Test
    fun e192Has920NotTheCalculated919() {
        // n counts from 0 (value = 10^(n/192)), so n = 185 is index 185.
        assertEquals(920, ESeries.E192.base[185])
        assertFalse(919 in ESeries.E192.base)
    }

    @Test
    fun subsetChains() {
        val coarse = listOf(ESeries.E3, ESeries.E6, ESeries.E12, ESeries.E24)
        coarse.zipWithNext { a, b -> assertTrue("$a ⊂ $b", b.base.containsAll(a.base)) }
        val fine = listOf(ESeries.E48, ESeries.E96, ESeries.E192)
        fine.zipWithNext { a, b -> assertTrue("$a ⊂ $b", b.base.containsAll(a.base)) }
        assertFalse(150 in ESeries.E48.base)
        assertTrue(147 in ESeries.E48.base && 154 in ESeries.E48.base)
    }

    @Test
    fun everySeriesIsStrictlyAscending() {
        for (series in ESeries.entries) assertEquals(series.name, series.base.sorted().distinct(), series.base)
    }

    @Test
    fun availableValuesSpanOneOhmToTenMegohms() {
        for (series in ESeries.entries) {
            val values = series.values()
            assertEquals(series.name, 7 * series.base.size + 1, values.size)
            assertEquals(Rational.of(BigDecimal.ONE), values.first())
            assertEquals(Rational.of(BigDecimal("10000000")), values.last())
            assertEquals(series.name, values.sorted(), values)
        }
        assertTrue(Rational.of(BigDecimal("4700")) in ESeries.E12.values())
        assertTrue(Rational.of(BigDecimal("9.88")) in ESeries.E192.values())
    }
}
