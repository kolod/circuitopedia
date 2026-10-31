package io.github.kolod.circuitopedia.shared.resistors

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RationalTest {
    private fun r(value: String) = Rational.of(BigDecimal(value))

    @Test
    fun valuesAreReduced() {
        val half = Rational.of(2, 4)
        assertEquals(Rational.of(1, 2), half)
        assertEquals(r("0.5"), half)
    }

    @Test
    fun arithmetic() {
        assertEquals(r("3.2"), r("1") + r("2.2"))
        assertEquals(r("-1.2"), r("1") - r("2.2"))
        assertEquals(r("0.1"), r("10").reciprocal())
        assertEquals(r("6"), r("2") * r("3"))
        assertEquals(Rational.of(2, 3), r("2") / r("3"))
    }

    @Test
    fun parallelOfTwoEqualResistorsIsHalf() {
        val tenK = r("10000")
        assertEquals(r("5000"), (tenK.reciprocal() + tenK.reciprocal()).reciprocal())
    }

    @Test
    fun comparisonIsExact() {
        val third = Rational.of(1, 3)
        assertTrue(third > r("0.3333333333"))
        assertTrue(third < r("0.3333333334"))
        assertEquals(0, r("1.50").compareTo(r("1.5")))
        assertEquals(r("2"), r("-2").abs())
    }

    @Test
    fun roundsHalfUpToSignificantDigits() {
        assertEquals(BigDecimal("1235"), r("1234.5").round(4))
        assertEquals(BigDecimal("0.01235"), r("0.012345").round(4))
        assertEquals(BigDecimal("0.3333"), Rational.of(1, 3).round(4))
        assertEquals(BigDecimal("-1.23"), r("-1.234").round(3))
        assertEquals(0, BigDecimal.ZERO.compareTo(Rational.ZERO.round(3)))
    }

    @Test
    fun toDoubleIsClose() {
        assertEquals(1.0 / 3, Rational.of(1, 3).toDouble(), 1e-15)
        assertEquals(4700.0, r("4700").toDouble(), 0.0)
    }
}
