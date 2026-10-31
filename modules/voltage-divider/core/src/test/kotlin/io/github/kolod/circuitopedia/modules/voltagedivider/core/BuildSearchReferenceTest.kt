package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R2
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U2
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/** The fast build search must list exactly what the exhaustive reference lists (spec SC-004). */
class BuildSearchReferenceTest {
    private fun solved(vararg entered: Pair<Quantity, String>) =
        solve(entered.associate { (k, v) -> k to Rational.of(BigDecimal(v)) }) as SolveResult.Solved

    private fun check(series: ESeries, solved: SolveResult.Solved) {
        val expected = referenceBuilds(solved, series).map { it.describe() }
        val actual = builds(solved, series).map { it.describe() }
        assertEquals("$series ${solved.values}", expected, actual)
    }

    private fun DividerBuild.describe() = "${r1.kind}${r1.values} / ${r2.kind}${r2.values} n=$chosenCount"

    /** Ratios near 0, ½ and 1 at small, middle and large totals, both arms chosen. */
    private val bothArms = listOf("0.1", "0.5", "0.9", "0.33", "0.667").flatMap { ratio ->
        listOf("100", "10000", "5000000").map { total -> solved(U1 to "10", U2 to (BigDecimal(ratio) * BigDecimal(10)).toPlainString(), R to total) }
    }

    @Test
    fun bothArmsOnE3() = bothArms.forEach { check(ESeries.E3, it) }

    @Test
    fun bothArmsOnE6() = bothArms.take(6).forEach { check(ESeries.E6, it) }

    @Test
    fun enteredUpperArmOnE12() {
        check(ESeries.E12, solved(U1 to "5", U2 to "3.3", R1 to "10000"))
        check(ESeries.E12, solved(U1 to "12", U2 to "1.2", R1 to "47"))
    }

    @Test
    fun enteredLowerArmOnE12() {
        check(ESeries.E12, solved(U1 to "5", U2 to "3.3", R2 to "6800"))
        check(ESeries.E12, solved(U1 to "24", U2 to "23", R2 to "1000000"))
    }

    @Test
    fun totalNearTheEndsOfTheValueRange() {
        check(ESeries.E3, solved(U1 to "10", U2 to "5", R to "3"))
        check(ESeries.E3, solved(U1 to "10", U2 to "5", R to "18000000"))
    }
}
