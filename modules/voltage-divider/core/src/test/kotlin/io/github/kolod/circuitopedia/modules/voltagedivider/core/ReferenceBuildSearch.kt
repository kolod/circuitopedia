package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational

/**
 * The slow, obviously correct build search (spec 005 FR-009–FR-011): every arm option of a computed
 * arm (one series value, or two in series or parallel), the entered value for an entered arm, every
 * pair whose total is within ±10% of R, ranked exactly; the best [BUILDS_PER_TIER] per chosen count.
 * It shares only the final order ([dividerRanking]) with [builds].
 */
fun referenceBuilds(solved: SolveResult.Solved, series: ESeries): List<DividerBuild> {
    val chooseR1 = Quantity.R1 in solved.computed
    val chooseR2 = Quantity.R2 in solved.computed
    if (!chooseR1 && !chooseR2) return emptyList()
    val total = solved.values.getValue(Quantity.R)
    val low = total * Rational.of(9, 10)
    val high = total * Rational.of(11, 10)

    fun options(chosen: Boolean, entered: Rational): List<Pair<Combination, Int>> {
        if (!chosen) return listOf(Combination(Kind.Single, listOf(entered)) to 0)
        val v = series.values()
        val singles = v.map { Combination(Kind.Single, listOf(it)) to 1 }
        val pairs = v.indices.flatMap { a ->
            (a until v.size).flatMap { b ->
                listOf(Combination(Kind.Series2, listOf(v[a], v[b])) to 2, Combination(Kind.Parallel2, listOf(v[a], v[b])) to 2)
            }
        }
        return (singles + pairs).filter { it.first.resistance <= high }
    }

    val arm1 = options(chooseR1, solved.values.getValue(Quantity.R1))
    val arm2 = options(chooseR2, solved.values.getValue(Quantity.R2)).sortedBy { it.first.resistance }
    val ranking = dividerRanking()
    val best = mutableMapOf<Int, MutableList<DividerBuild>>()
    for ((b1, n1) in arm1) {
        // Arm 2 values whose total with b1 lies in [low, high], found by exact binary search.
        val from = arm2.binarySearch { (it.first.resistance + b1.resistance).compareTo(low).let { c -> if (c < 0) -1 else 1 } }.let { -it - 1 }
        for (i in from until arm2.size) {
            val (b2, n2) = arm2[i]
            if (b1.resistance + b2.resistance > high) break
            val build = dividerBuild(solved, b1, b2, n1 + n2)
            val tier = best.getOrPut(n1 + n2) { mutableListOf() }
            tier += build
            tier.sortWith(ranking)
            if (tier.size > BUILDS_PER_TIER) tier.removeAt(tier.lastIndex)
        }
    }
    return best.values.flatten().sortedWith(ranking)
}
