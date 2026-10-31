package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R2
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U2
import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The spec's acceptance scenarios for standard-resistor builds (US2). */
class BuildSearchExamplesTest {
    private fun q(value: String) = Rational.of(BigDecimal(value))
    private fun solved(vararg entered: Pair<Quantity, String>) =
        solve(entered.associate { (k, v) -> k to q(v) }) as SolveResult.Solved

    @Test
    fun enteredUpperArmIsKept() {
        val list = builds(solved(U1 to "5", U2 to "3.3", R1 to "10000"), ESeries.E24)
        assertTrue(list.isNotEmpty())
        for (build in list) {
            assertEquals(Combination(Kind.Single, listOf(q("10000"))), build.r1)
            assertTrue("$build", build.chosenCount in 1..2)
        }
    }

    @Test
    fun exactTwoResistorBuildComesFirst() {
        val first = builds(solved(U1 to "9", U2 to "3", R to "30000"), ESeries.E24).first()
        assertEquals(Combination(Kind.Single, listOf(q("20000"))), first.r1)
        assertEquals(Combination(Kind.Single, listOf(q("10000"))), first.r2)
        assertEquals(Rational.ZERO, first.u2Error)
        assertEquals(2, first.chosenCount)
    }

    @Test
    fun windowTiersAndOrderHold() {
        val s = solved(U1 to "5", U2 to "3.3", R to "30000")
        val list = builds(s, ESeries.E24)
        val total = q("30000")
        for (build in list) {
            assertTrue("$build", build.total >= total * Rational.of(9, 10) && build.total <= total * Rational.of(11, 10))
        }
        for (count in 2..4) assertTrue("tier $count", list.count { it.chosenCount == count } in 1..BUILDS_PER_TIER)
        assertEquals(list.sortedWith(dividerRanking()), list)
    }

    @Test
    fun nothingToChooseWhenBothArmsAreEntered() {
        assertTrue(builds(solved(U1 to "12", R1 to "10000", R2 to "4700"), ESeries.E24).isEmpty())
    }

    @Test
    fun cancellationStopsTheSearch() {
        var checks = 0
        val stop = IllegalStateException("cancelled")
        val thrown = runCatching {
            builds(solved(U1 to "5", U2 to "3.3", R to "30000"), ESeries.E192) { if (++checks == 3) throw stop }
        }.exceptionOrNull()
        assertTrue(thrown === stop)
    }
}
