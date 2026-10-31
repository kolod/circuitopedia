package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Test

/** Research R4 budget: E192 with every kind, median under 150 ms on a desktop JVM (spec SC-002). */
class SearchBenchmarkTest {
    @Test
    fun e192MedianStaysWithinBudget() {
        val targets = listOf(
            "0.15", "0.9", "3.14", "12.3", "47", "99.9", "123", "333", "1000", "1234",
            "4321", "9999", "12345", "47000", "100000", "314159", "999999", "2500000", "9999999", "30000000",
        ).map { Rational.of(BigDecimal(it)) }
        val all = Kind.entries.toSet()
        repeat(3) { search(targets[it], ESeries.E192, all) }
        val millis = targets.map { target ->
            val start = System.nanoTime()
            search(target, ESeries.E192, all)
            (System.nanoTime() - start) / 1_000_000.0
        }.sorted()
        val median = millis[millis.size / 2]
        println("E192 search: median %.1f ms, max %.1f ms".format(median, millis.last()))
        assertTrue("median $median ms", median < 150)
        // Targets near the ends of the range once took seconds; no single target may lag far behind.
        assertTrue("max ${millis.last()} ms", millis.last() < 400)
    }
}
