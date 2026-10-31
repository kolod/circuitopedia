package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.R2
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U1
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity.U2
import io.github.kolod.circuitopedia.shared.resistors.Rational

/** The outcome of solving a divider from the entered quantities (spec 005 FR-004–FR-006). */
sealed interface SolveResult {
    /** All five values, exact; [computed] are the two that were not entered. */
    data class Solved(val values: Map<Quantity, Rational>, val computed: Set<Quantity>) : SolveResult
    data class NotEnough(val missing: Int) : SolveResult
    data class TooMany(val extra: Int) : SolveResult

    /** R1, R2 and R alone fix no voltage. */
    data object NoVoltage : SolveResult
    data class Invalid(val reason: Reason) : SolveResult

    enum class Reason { RatioOutOfRange, MixedSigns, ResistanceNotBelowTotal }
}

private val ONE = Rational.of(1, 1)

/**
 * Solves an unloaded divider from exactly three entered quantities, in closed form (research R1).
 *
 * `U2 = U1 · R2 / (R1 + R2)` and `R = R1 + R2` follow from Ohm's and Kirchhoff's laws
 * (P. Horowitz, W. Hill, *The Art of Electronics*, 3rd ed., 2015, §1.2). Every case uses only
 * + − × ÷, so the results are exact.
 */
fun solve(entered: Map<Quantity, Rational>): SolveResult {
    if (entered.size < 3) return SolveResult.NotEnough(3 - entered.size)
    if (entered.size > 3) return SolveResult.TooMany(entered.size - 3)
    if (entered.keys == setOf(R1, R2, R)) return SolveResult.NoVoltage

    val u1 = entered[U1]
    val u2 = entered[U2]
    if (u1 != null && u2 != null) {
        if (u1.signum != u2.signum) return SolveResult.Invalid(SolveResult.Reason.MixedSigns)
        val ratio = u2 / u1
        if (ratio.signum <= 0 || ratio >= ONE) return SolveResult.Invalid(SolveResult.Reason.RatioOutOfRange)
    }
    val total = entered[R]
    if (total != null && listOfNotNull(entered[R1], entered[R2]).any { it >= total }) {
        return SolveResult.Invalid(SolveResult.Reason.ResistanceNotBelowTotal)
    }

    // First both resistors, then whichever voltage is missing.
    val (r1, r2) = when {
        R1 in entered && R2 in entered -> entered.getValue(R1) to entered.getValue(R2)
        R1 in entered && R in entered -> entered.getValue(R1) to entered.getValue(R) - entered.getValue(R1)
        R2 in entered && R in entered -> entered.getValue(R) - entered.getValue(R2) to entered.getValue(R2)
        else -> {
            // Both voltages and one resistance: the ratio k = U2/U1 fixes R2/R.
            val k = u2!! / u1!!
            when {
                R1 in entered -> entered.getValue(R1).let { it to it * k / (ONE - k) }
                R2 in entered -> entered.getValue(R2).let { it * (ONE - k) / k to it }
                else -> entered.getValue(R).let { (it - it * k) to it * k }
            }
        }
    }
    val r = r1 + r2
    val values = mapOf(
        U1 to (u1 ?: (u2!! * r / r2)),
        U2 to (u2 ?: (u1!! * r2 / r)),
        R1 to r1,
        R2 to r2,
        R to r,
    )
    return SolveResult.Solved(values, Quantity.entries.toSet() - entered.keys)
}

/** Whether Calculate can run: three entered quantities that are not just the three resistances (FR-004). */
fun canCalculate(entered: Map<Quantity, Rational>): Boolean =
    entered.size == 3 && entered.keys != setOf(R1, R2, R)
