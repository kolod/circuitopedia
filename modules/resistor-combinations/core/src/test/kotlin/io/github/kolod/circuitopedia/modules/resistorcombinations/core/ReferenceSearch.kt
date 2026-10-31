package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigInteger

/**
 * The slow, obviously correct search: every combination of every kind in canonical order, ranked
 * with exact arithmetic, the best [PER_KIND] of each kind merged (spec FR-006–FR-008).
 *
 * To stay fast enough for tests it works on unreduced integer fractions of hundredths of an ohm and
 * keeps a running top [PER_KIND] instead of sorting everything; it shares no code with [search]
 * except the final merge order.
 */
fun referenceSearch(target: Rational, series: ESeries, allowed: Set<Kind>): List<Combination> {
    val values = series.values()
    val h = values.map { it.num * BigInteger.valueOf(100) / it.den } // exact: values have ≤ 2 decimals
    val n = h.size
    val tNum = target.num * BigInteger.valueOf(100)
    val tDen = target.den

    fun best(kind: Kind, combinations: Sequence<IntArray>): List<Combination> {
        val top = ArrayList<Candidate>()
        for (indices in combinations) {
            val (rNum, rDen) = resistance(kind, indices.map { h[it] })
            // |R − T| as an unreduced fraction; T's denominator is common to all, so compare |R·tDen − tNum·rDen| / rDen.
            val candidate = Candidate(indices, (rNum * tDen - tNum * rDen).abs(), rDen)
            if (top.size < PER_KIND || candidate < top.last()) {
                top += candidate
                top.sort()
                if (top.size > PER_KIND) top.removeAt(top.lastIndex)
            }
        }
        return top.map { c -> Combination(kind, c.indices.map { values[it] }) }
    }

    fun pairs(c: Boolean) = sequence {
        for (a in 0 until n) for (b in a until n) if (c) for (x in 0 until n) yield(intArrayOf(a, b, x)) else yield(intArrayOf(a, b))
    }

    val perKind = (allowed + Kind.Single).map { kind ->
        best(
            kind,
            when (kind) {
                Kind.Single -> (0 until n).asSequence().map { intArrayOf(it) }
                Kind.Series2, Kind.Parallel2 -> pairs(false)
                Kind.Series3, Kind.Parallel3 -> sequence {
                    for (a in 0 until n) for (b in a until n) for (c in b until n) yield(intArrayOf(a, b, c))
                }
                Kind.ParallelSeries -> pairs(true)
            },
        )
    }
    return perKind.flatten().sortedWith(rankingFor(target))
}

/** Resistance in hundredths of an ohm as an unreduced fraction. */
private fun resistance(kind: Kind, h: List<BigInteger>): Pair<BigInteger, BigInteger> = when (kind) {
    Kind.Single, Kind.Series2, Kind.Series3 -> h.reduce(BigInteger::add) to BigInteger.ONE
    Kind.Parallel2 -> h[0] * h[1] to h[0] + h[1]
    Kind.Parallel3 -> h[0] * h[1] * h[2] to h[0] * h[1] + h[0] * h[2] + h[1] * h[2]
    Kind.ParallelSeries -> h[0] * h[1] + h[2] * (h[0] + h[1]) to h[0] + h[1]
}

/** Ranking of FR-007 within one kind: |R − T|, then the largest value, then the values in order. */
private class Candidate(val indices: IntArray, val diffNum: BigInteger, val diffDen: BigInteger) : Comparable<Candidate> {
    override fun compareTo(other: Candidate): Int {
        val byDeviation = (diffNum * other.diffDen).compareTo(other.diffNum * diffDen)
        if (byDeviation != 0) return byDeviation
        val byMax = indices.max().compareTo(other.indices.max())
        if (byMax != 0) return byMax
        return indices.indices.map { indices[it].compareTo(other.indices[it]) }.firstOrNull { it != 0 } ?: 0
    }
}
