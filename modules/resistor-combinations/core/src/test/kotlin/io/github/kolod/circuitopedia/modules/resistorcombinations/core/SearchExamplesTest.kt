package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The spec's acceptance scenarios on the fast search. */
class SearchExamplesTest {
    private fun ohms(value: String) = Rational.of(BigDecimal(value))
    private val all = Kind.entries.toSet()

    @Test
    fun exactSingleResistorComesFirst() {
        val first = search(ohms("4700"), ESeries.E12, all).first()
        assertEquals(Kind.Single, first.kind)
        assertEquals(listOf(ohms("4700")), first.values)
        assertEquals(Rational.ZERO, first.deviation(ohms("4700")))
    }

    @Test
    fun exactTwoResistorsBeatExactThree() {
        val target = ohms("5000")
        val results = search(target, ESeries.E12, all)
        val first = results.first()
        assertEquals(Kind.Parallel2, first.kind)
        assertEquals(listOf(ohms("10000"), ohms("10000")), first.values)
        val firstThree = results.indexOfFirst { it.kind.resistorCount == 3 && it.deviation(target) == Rational.ZERO }
        if (firstThree >= 0) assertTrue(results.indexOf(first) < firstThree)
    }

    @Test
    fun eachKindAtMostFiveTimesAndSorted() {
        val target = ohms("1234")
        val results = search(target, ESeries.E24, all)
        assertEquals(Kind.entries.size * PER_KIND, results.size)
        for (kind in Kind.entries) assertTrue("$kind", results.count { it.kind == kind } <= PER_KIND)
        assertEquals(results.sortedWith(rankingFor(target)), results)
    }

    @Test
    fun valuesAreInCanonicalOrder() {
        for (result in search(ohms("1234"), ESeries.E24, all)) {
            val ordered = if (result.kind == Kind.ParallelSeries) result.values.take(2) else result.values
            assertEquals("$result", ordered.sorted(), ordered)
        }
    }

    @Test
    fun forbiddenKindsNeverAppear() {
        val results = search(ohms("1234"), ESeries.E24, setOf(Kind.Series2))
        assertTrue(results.all { it.kind == Kind.Single || it.kind == Kind.Series2 })
        assertTrue(search(ohms("1234"), ESeries.E24, emptySet()).all { it.kind == Kind.Single })
    }

    @Test
    fun cancellationStopsTheSearch() {
        var checks = 0
        val stop = IllegalStateException("cancelled")
        val thrown = runCatching {
            search(ohms("1234"), ESeries.E192, all) { if (++checks == 3) throw stop }
        }.exceptionOrNull()
        assertTrue(thrown === stop)
        assertEquals(3, checks)
    }

    @Test
    fun resistanceFollowsTheCircuitLaws() {
        val k10 = ohms("10000")
        val k4 = ohms("4700")
        val r220 = ohms("220")
        assertEquals(ohms("14920"), Combination(Kind.Series3, listOf(r220, k4, k10)).resistance)
        assertEquals(ohms("5000"), Combination(Kind.Parallel2, listOf(k10, k10)).resistance)
        assertEquals(Rational.of(10000, 3), Combination(Kind.Parallel3, listOf(k10, k10, k10)).resistance)
        assertEquals(Rational.of(47_000_000, 14_700) + r220, Combination(Kind.ParallelSeries, listOf(k4, k10, r220)).resistance)
    }
}
