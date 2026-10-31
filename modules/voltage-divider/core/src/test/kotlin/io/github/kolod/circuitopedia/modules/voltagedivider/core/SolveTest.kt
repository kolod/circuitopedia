package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R2
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U2
import io.github.kolod.circuitopedia.modules.voltagedivider.core.SolveResult.Reason
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The nine closed-form cases of research R1 against the forward formula U2 = U1·R2/(R1+R2), R = R1+R2. */
class SolveTest {
    private fun q(value: String) = Rational.of(BigDecimal(value))
    private fun solved(vararg entered: Pair<Quantity, String>): Map<Quantity, Rational> {
        val result = solve(entered.associate { (k, v) -> k to q(v) })
        assertTrue("$result", result is SolveResult.Solved)
        return (result as SolveResult.Solved).values
    }

    // Spec scenarios

    @Test
    fun fiveToThreePointThreeWithTenK() {
        val v = solved(U1 to "5", U2 to "3.3", R1 to "10000")
        assertEquals(Rational.of(330_000, 17), v[R2])
        assertEquals(Rational.of(500_000, 17), v[R])
    }

    @Test
    fun twelveVoltsOverTenKAndFourSevenK() {
        val v = solved(U1 to "12", R1 to "10000", R2 to "4700")
        assertEquals(Rational.of(564, 147), v[U2])
        assertEquals(q("14700"), v[R])
    }

    @Test
    fun outputVoltageWithUpperResistorAndTotal() {
        val v = solved(U2 to "2.5", R1 to "1000", R to "3000")
        assertEquals(q("2000"), v[R2])
        assertEquals(q("3.75"), v[U1])
    }

    @Test
    fun bothVoltagesWithTotal() {
        val v = solved(U1 to "9", U2 to "3", R to "30000")
        assertEquals(q("20000"), v[R1])
        assertEquals(q("10000"), v[R2])
    }

    @Test
    fun negativeVoltagesOfTheSameSign() {
        val v = solved(U1 to "-12", U2 to "-3", R1 to "3000")
        assertEquals(q("1000"), v[R2])
    }

    // Not solvable

    @Test
    fun wrongNumberOfValues() {
        assertEquals(SolveResult.NotEnough(1), solve(mapOf(U1 to q("5"), U2 to q("3"))))
        assertEquals(SolveResult.NotEnough(3), solve(emptyMap()))
        assertEquals(SolveResult.TooMany(1), solve(mapOf(U1 to q("5"), U2 to q("3"), R1 to q("1"), R2 to q("1"))))
        assertEquals(SolveResult.NoVoltage, solve(mapOf(R1 to q("1"), R2 to q("2"), R to q("3"))))
    }

    @Test
    fun invalidDividers() {
        fun reason(vararg entered: Pair<Quantity, String>) = solve(entered.associate { (k, v) -> k to q(v) })
        assertEquals(SolveResult.Invalid(Reason.RatioOutOfRange), reason(U1 to "5", U2 to "6", R1 to "1000"))
        assertEquals(SolveResult.Invalid(Reason.RatioOutOfRange), reason(U1 to "5", U2 to "5", R1 to "1000"))
        assertEquals(SolveResult.Invalid(Reason.MixedSigns), reason(U1 to "5", U2 to "-3", R1 to "1000"))
        assertEquals(SolveResult.Invalid(Reason.ResistanceNotBelowTotal), reason(U1 to "5", R1 to "3000", R to "3000"))
        assertEquals(SolveResult.Invalid(Reason.ResistanceNotBelowTotal), reason(U2 to "1", R2 to "4000", R to "3000"))
    }

    @Test
    fun canCalculateMatchesSolve() {
        val full = mapOf(U1 to q("9"), U2 to q("3"), R1 to q("20000"), R2 to q("10000"), R to q("30000"))
        for (mask in 0 until 32) {
            val entered = Quantity.entries.filterIndexed { i, _ -> mask shr i and 1 == 1 }.associateWith { full.getValue(it) }
            val result = solve(entered)
            val expected = result !is SolveResult.NotEnough && result !is SolveResult.TooMany && result !is SolveResult.NoVoltage
            assertEquals("$entered", expected, canCalculate(entered))
        }
    }

    // Property: every valid triple of a known divider gives back the other two exactly (spec SC-002).

    @Test
    fun everyTripleRoundTrips() {
        val random = Random(2026_10_31)
        repeat(200) {
            val u1 = Rational.of(BigDecimal(random.nextInt(1, 100_000)).scaleByPowerOfTen(-3) * (if (random.nextBoolean()) 1 else -1))
            val r1 = Rational.of(random.nextLong(10, 10_000_000), 10)
            val r2 = Rational.of(random.nextLong(10, 10_000_000), 10)
            val r = r1 + r2
            val full = mapOf(U1 to u1, U2 to u1 * r2 / r, R1 to r1, R2 to r2, R to r)
            for (mask in 0 until 32) {
                val keys = Quantity.entries.filterIndexed { i, _ -> mask shr i and 1 == 1 }
                if (keys.size != 3 || keys.toSet() == setOf(R1, R2, R)) continue
                val result = solve(keys.associateWith { full.getValue(it) })
                assertTrue("$keys of $full: $result", result is SolveResult.Solved)
                assertEquals("$keys", full, (result as SolveResult.Solved).values)
                assertEquals("$keys", Quantity.entries.toSet() - keys.toSet(), result.computed)
            }
        }
    }

    private operator fun BigDecimal.times(sign: Int) = if (sign < 0) negate() else this
}
