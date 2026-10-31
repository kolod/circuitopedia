package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U2
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Research R4 budget for E192 with both arms chosen (spec SC-003). As in spec 004, the asserted
 * measure is the number of evaluated pairs, which does not depend on the machine; timing is printed only.
 */
class BuildSearchBudgetTest {
    private val cases = listOf(
        "3.3" to "30000", "1.2" to "4700", "4.9" to "1000000", "0.05" to "150", "2.5" to "10000",
        "4.99" to "220", "0.333" to "68000", "1.8" to "9000000", "3.7" to "33", "2.2" to "100000",
    ).map { (u2, r) ->
        solve(mapOf(U1 to q("5"), U2 to q(u2), R to q(r))) as SolveResult.Solved
    }

    private fun q(value: String) = Rational.of(BigDecimal(value))

    @Test
    fun e192WorkStaysWithinBudget() {
        val counts = cases.map { buildEvaluations(it, ESeries.E192) }
        println("E192 builds: max ${counts.max()} evaluations, median ${counts.sorted()[counts.size / 2]}")
        for ((solved, count) in cases.zip(counts)) assertTrue("${solved.values}: $count", count < MAX_EVALUATIONS)
    }

    @Test
    fun e192TimingForInformation() {
        repeat(2) { builds(cases[it], ESeries.E192) }
        val millis = cases.map {
            val start = System.nanoTime()
            builds(it, ESeries.E192)
            (System.nanoTime() - start) / 1_000_000.0
        }.sorted()
        println("E192 builds: median %.1f ms, max %.1f ms".format(millis[millis.size / 2], millis.last()))
    }

    private companion object {
        /** About three times the largest count measured on 2026-10-31 (181 k, see the printed output). */
        const val MAX_EVALUATIONS = 600_000L
    }
}
