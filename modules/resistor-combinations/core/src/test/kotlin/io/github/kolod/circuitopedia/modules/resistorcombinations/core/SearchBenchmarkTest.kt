package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Research R4 budget for E192 with every kind (spec SC-002). Wall-clock limits are not asserted:
 * shared CI runners are too slow and noisy for them. The number of evaluated combinations is
 * machine-independent and catches the algorithmic regressions that made targets take seconds
 * (an O(n³) walk would evaluate hundreds of millions). Timing is printed for information only.
 */
class SearchBenchmarkTest {
    private val targets = listOf(
        "0.15", "0.9", "3.14", "12.3", "47", "99.9", "123", "333", "1000", "1234",
        "4321", "9999", "12345", "47000", "100000", "314159", "999999", "2500000", "9999999", "30000000",
    ).map { Rational.of(BigDecimal(it)) }
    private val all = Kind.entries.toSet()

    @Test
    fun e192WorkStaysWithinBudget() {
        val evaluations = targets.map { searchEvaluations(it, ESeries.E192, all) }
        println("E192 search: max ${evaluations.max()} evaluations, median ${evaluations.sorted()[evaluations.size / 2]}")
        for ((target, count) in targets.zip(evaluations)) assertTrue("$target: $count evaluations", count < MAX_EVALUATIONS)
    }

    @Test
    fun e192TimingForInformation() {
        repeat(3) { search(targets[it], ESeries.E192, all) }
        val millis = targets.map { target ->
            val start = System.nanoTime()
            search(target, ESeries.E192, all)
            (System.nanoTime() - start) / 1_000_000.0
        }.sorted()
        println("E192 search: median %.1f ms, max %.1f ms".format(millis[millis.size / 2], millis.last()))
    }

    private companion object {
        /** About three times the largest count measured on 2026-10-31 (see the printed output). */
        const val MAX_EVALUATIONS = 30_000_000L
    }
}
