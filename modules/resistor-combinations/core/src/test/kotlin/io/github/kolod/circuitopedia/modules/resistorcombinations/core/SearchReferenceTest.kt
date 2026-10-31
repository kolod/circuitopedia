package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

/** The fast search must list exactly what the exhaustive reference lists (spec SC-003). */
class SearchReferenceTest {
    private val grid = listOf("0.1", "0.33", "1", "4.7", "10", "123", "1000", "1234", "5000", "47000", "1000000", "9900000", "25000000", "1000000000")

    private fun check(series: ESeries, target: Rational, allowed: Set<Kind> = Kind.entries.toSet()) {
        val expected = referenceSearch(target, series, allowed)
        val actual = search(target, series, allowed)
        assertEquals("$series $target $allowed", expected.map { it.describe() }, actual.map { it.describe() })
    }

    private fun Combination.describe() = "$kind ${values.joinToString(" ")}"

    @Test
    fun gridOnE3() = grid.forEach { check(ESeries.E3, Rational.of(BigDecimal(it))) }

    @Test
    fun gridOnE6() = grid.forEach { check(ESeries.E6, Rational.of(BigDecimal(it))) }

    @Test
    fun gridOnE12() = grid.forEach { check(ESeries.E12, Rational.of(BigDecimal(it))) }

    @Test
    fun randomTargetsOnE24() {
        val random = Random(2026_10_31)
        repeat(2) {
            // Log-uniform over 1Ω … 10MΩ with three significant digits.
            val exponent = random.nextInt(0, 7)
            val mantissa = random.nextInt(100, 1000)
            check(ESeries.E24, Rational.of(BigDecimal(mantissa).scaleByPowerOfTen(exponent - 2)))
        }
    }

    @Test
    fun restrictedKindSets() {
        val target = Rational.of(BigDecimal("1234"))
        check(ESeries.E12, target, setOf(Kind.Series2))
        check(ESeries.E12, target, setOf(Kind.Parallel2, Kind.Parallel3))
        check(ESeries.E12, target, setOf(Kind.ParallelSeries))
        check(ESeries.E12, target, emptySet())
    }
}
