package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import kotlin.math.abs

/** Builds listed per count of chosen resistors (spec 005 FR-011). */
const val BUILDS_PER_TIER = 5

/** A divider made of standard resistors: the two arms and what they give (spec 005 FR-012). */
data class DividerBuild(
    val r1: Combination,
    val r2: Combination,
    /** Resistors chosen by the search; an entered arm counts 0. */
    val chosenCount: Int,
    val u2: Rational,
    /** Signed relative error of U2 against the solved U2. */
    val u2Error: Rational,
    val total: Rational,
    /** Signed relative deviation of R1 + R2 from the solved R. */
    val totalDeviation: Rational,
)

internal fun dividerBuild(solved: SolveResult.Solved, r1: Combination, r2: Combination, chosenCount: Int): DividerBuild {
    val u1 = solved.values.getValue(Quantity.U1)
    val u2Target = solved.values.getValue(Quantity.U2)
    val totalTarget = solved.values.getValue(Quantity.R)
    val total = r1.resistance + r2.resistance
    val u2 = u1 * r2.resistance / total
    return DividerBuild(r1, r2, chosenCount, u2, (u2 - u2Target) / u2Target, total, (total - totalTarget) / totalTarget)
}

/**
 * The order of FR-010: smaller absolute U2 error, then fewer chosen resistors, then smaller
 * absolute deviation of the total, then the arms' kinds and values (only for a deterministic order).
 */
fun dividerRanking(): Comparator<DividerBuild> =
    compareBy<DividerBuild> { it.u2Error.abs() }
        .thenBy { it.chosenCount }
        .thenBy { it.totalDeviation.abs() }
        .thenComparator { x, y -> compareArms(x.r1, y.r1).takeIf { it != 0 } ?: compareArms(x.r2, y.r2) }

private fun compareArms(x: Combination, y: Combination): Int {
    x.values.zip(y.values).forEach { (a, b) -> a.compareTo(b).let { if (it != 0) return it } }
    return compareValuesBy(x, y, { it.values.size }, { it.kind.ordinal })
}

/**
 * Builds of the divider from standard resistors (FR-009–FR-011): each computed arm is one value of
 * [series] or two in series or parallel, an entered arm stays as entered, and the total stays within
 * ±10% of R. Empty unless R1 or R2 was computed. [checkCancelled] may throw to abandon the search.
 */
fun builds(solved: SolveResult.Solved, series: ESeries, checkCancelled: () -> Unit = {}): List<DividerBuild> =
    BuildSearcher(solved, series.values(), checkCancelled).run()

/** How many pairs [builds] evaluates; a machine-independent measure of its work for tests. */
internal fun buildEvaluations(solved: SolveResult.Solved, series: ESeries): Long =
    BuildSearcher(solved, series.values()) {}.also { it.run() }.evaluations

/** Relative margin above the tier's 5th best Double key, far above Double rounding (research R3). */
private const val RELATIVE_MARGIN = 1e-9
private const val ABSOLUTE_MARGIN = 1e-12

/** Arm encodings in candidate arrays: a fixed (entered) arm, one value, or two in series or parallel. */
private const val FIXED = 0
private const val SINGLE = 1
private const val SERIES = 2
private const val PARALLEL = 3

private class BuildSearcher(
    private val solved: SolveResult.Solved,
    private val values: List<Rational>,
    private val checkCancelled: () -> Unit,
) {
    private val n = values.size
    private val v = DoubleArray(n) { values[it].toDouble() }
    private val totalTarget = solved.values.getValue(Quantity.R)
    private val t = totalTarget.toDouble()
    private val k = (solved.values.getValue(Quantity.R2) / totalTarget).toDouble()
    private val low = 0.9 * t
    private val high = 1.1 * t
    private val exactLow = totalTarget * Rational.of(9, 10)
    private val exactHigh = totalTarget * Rational.of(11, 10)

    var evaluations = 0L
        private set

    fun run(): List<DividerBuild> {
        val chooseR1 = Quantity.R1 in solved.computed
        val chooseR2 = Quantity.R2 in solved.computed
        val tiers = mutableMapOf<Int, Collector>()
        fun tier(count: Int) = tiers.getOrPut(count) { Collector(Double.POSITIVE_INFINITY) }
        when {
            chooseR1 && chooseR2 -> bothArms(tiers)
            chooseR1 -> oneArm(solved.values.getValue(Quantity.R2).toDouble(), chooseUpper = true, ::tier)
            chooseR2 -> oneArm(solved.values.getValue(Quantity.R1).toDouble(), chooseUpper = false, ::tier)
            else -> return emptyList()
        }
        val ranking = dividerRanking()
        return tiers.values.flatMap { it.ranked(ranking) }.sortedWith(ranking)
    }

    /** One arm is entered ([fixed]); the other takes every value in its band (the band is narrow). */
    private fun oneArm(fixed: Double, chooseUpper: Boolean, tier: (Int) -> Collector) {
        for (count in 1..2) {
            val arm = armList(count, low - fixed, high - fixed)
            for (i in 0 until arm.size) {
                if (i and 1023 == 0) checkCancelled()
                val x = arm.value[i]
                val (b1, b2) = if (chooseUpper) x to fixed else fixed to x
                if (!inWindow(b1 + b2) { exactTotal(arm, i, chooseUpper) }) continue
                tier(count).offer(key(b1, b2)) { if (chooseUpper) arm.code(i) + FIXED_CODE else FIXED_CODE + arm.code(i) }
            }
        }
    }

    /**
     * Both arms chosen: for each B1 the best B2 is the one nearest q·B1 (q = k/(1−k)) inside the
     * window. A pass with error bound e only needs the B1 in [0.9R(1 − k(1+e)), 1.1R(1 − k(1−e))]
     * and finds every build with error ≤ e (research R4). Each tier widens e until a pass finds
     * [BUILDS_PER_TIER] builds within it, so sparse cases never fall back to every arm up to 1.1R.
     */
    private fun bothArms(tiers: MutableMap<Int, Collector>) {
        val shapesByTier = mapOf(2 to listOf(1 to 1), 3 to listOf(1 to 2, 2 to 1), 4 to listOf(2 to 2))
        for ((count, shapes) in shapesByTier) {
            for (errorBound in ERROR_BOUNDS) {
                val collector = Collector(if (errorBound.isInfinite()) errorBound else errorBound * (1 + RELATIVE_MARGIN) + ABSOLUTE_MARGIN)
                for ((n1, n2) in shapes) pairPass(n1, n2, errorBound, collector)
                tiers[count] = collector
                if (collector.found() >= BUILDS_PER_TIER) break
            }
        }
    }

    private fun pairPass(n1: Int, n2: Int, errorBound: Double, collector: Collector) {
        val lo1 = if (errorBound.isInfinite()) 0.0 else maxOf(0.0, low * (1 - k * (1 + errorBound)))
        val hi1 = if (errorBound.isInfinite()) high else high * (1 - k * (1 - errorBound))
        val arm1 = armList(n1, lo1, hi1)
        val arm2 = armList(n2, low - hi1, high - lo1)
        val q = k / (1 - k)
        for (i in 0 until arm1.size) {
            if (i and 1023 == 0) checkCancelled()
            val b1 = arm1.value[i]
            val from = low - b1
            val to = high - b1
            val start = arm2.lowerBound(min(max(q * b1, from), to))
            // The error grows away from q·B1 in both directions; stop at the window or the bound.
            var j = start - 1
            while (j >= 0 && arm2.value[j] >= from * (1 - RELATIVE_MARGIN) && offerPair(collector, arm1, i, arm2, j)) j--
            j = start
            while (j < arm2.size && arm2.value[j] <= to * (1 + RELATIVE_MARGIN) && offerPair(collector, arm1, i, arm2, j)) j++
        }
    }

    /** Offers B1 = arm1[i], B2 = arm2[j]; false once its key exceeds the bound (so a walk can stop). */
    private fun offerPair(collector: Collector, arm1: ArmList, i: Int, arm2: ArmList, j: Int): Boolean {
        val b1 = arm1.value[i]
        val b2 = arm2.value[j]
        val key = key(b1, b2)
        if (key > collector.bound) return false
        if (inWindow(b1 + b2) { arm1.exact(i) + arm2.exact(j) }) collector.offer(key) { arm1.code(i) + arm2.code(j) }
        return true
    }

    private fun key(b1: Double, b2: Double): Double {
        evaluations++
        return abs(b2 / (b1 + b2) - k) / k
    }

    /** Whether a total lies in [0.9R, 1.1R]; checked exactly only within a hair of either edge. */
    private inline fun inWindow(total: Double, exact: () -> Rational): Boolean {
        if (total < low * (1 - RELATIVE_MARGIN) || total > high * (1 + RELATIVE_MARGIN)) return false
        if (total > low * (1 + RELATIVE_MARGIN) && total < high * (1 - RELATIVE_MARGIN)) return true
        return exact().let { it >= exactLow && it <= exactHigh }
    }

    private fun exactTotal(arm: ArmList, i: Int, chooseUpper: Boolean): Rational =
        arm.exact(i) + solved.values.getValue(if (chooseUpper) Quantity.R2 else Quantity.R1)

    private fun min(a: Double, b: Double) = if (a < b) a else b
    private fun max(a: Double, b: Double) = if (a > b) a else b

    /** Every arm build of [count] resistors (1: one value; 2: two in series or parallel) with value in [lo, hi], sorted. */
    private fun armList(count: Int, lo: Double, hi: Double): ArmList {
        val list = ArmList()
        if (hi <= 0) return list
        if (count == 1) {
            for (a in lowerBound(lo) until n) {
                if (v[a] > hi) break
                list.add(v[a], SINGLE, a, a)
            }
            return list.sorted()
        }
        // Series a ≤ b with a + b in [lo, hi].
        for (a in 0 until n) {
            if (2 * v[a] > hi) break
            var b = maxOf(a, lowerBound(lo - v[a]))
            while (b < n && v[a] + v[b] <= hi) {
                list.add(v[a] + v[b], SERIES, a, b)
                b++
            }
        }
        // Parallel a ≤ b with a∥b in [lo, hi]; a∥b lies in [a/2, a).
        for (a in lowerBound(lo) until n) {
            if (v[a] / 2 > hi) break
            // a∥b ≥ lo  ⇔  1/b ≤ 1/lo − 1/a. A band starting at or below zero bounds nothing.
            var b = if (lo <= 0) a else (1 / lo - 1 / v[a]).let { g -> if (g <= 0) n else maxOf(a, lowerBound(1 / g)) }
            while (b < n) {
                val p = 1 / (1 / v[a] + 1 / v[b])
                if (p > hi) break
                list.add(p, PARALLEL, a, b)
                b++
            }
        }
        return list.sorted()
    }

    /** The first index whose value is ≥ [x], or n. */
    private fun lowerBound(x: Double): Int {
        var lo = 0
        var hi = n
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (v[mid] < x) lo = mid + 1 else hi = mid
        }
        return lo
    }

    /** Arm builds as parallel arrays, sortable by value. */
    private inner class ArmList {
        var value = DoubleArray(64)
        private var shape = IntArray(64)
        private var first = IntArray(64)
        private var second = IntArray(64)
        var size = 0
            private set

        fun add(x: Double, s: Int, a: Int, b: Int) {
            if (size == value.size) {
                value = value.copyOf(size * 2)
                shape = shape.copyOf(size * 2)
                first = first.copyOf(size * 2)
                second = second.copyOf(size * 2)
            }
            value[size] = x
            shape[size] = s
            first[size] = a
            second[size] = b
            size++
        }

        /** Sorts in place by value, without boxing (lists hold up to hundreds of thousands of builds). */
        fun sorted(): ArmList {
            quickSort(0, size - 1)
            return this
        }

        private fun quickSort(from: Int, to: Int) {
            var lo = from
            var hi = to
            while (lo < hi) {
                val pivot = value[(lo + hi) ushr 1]
                var i = lo
                var j = hi
                while (i <= j) {
                    while (value[i] < pivot) i++
                    while (value[j] > pivot) j--
                    if (i <= j) swap(i++, j--)
                }
                // Recurse into the smaller part, loop on the larger one.
                if (j - lo < hi - i) {
                    quickSort(lo, j)
                    lo = i
                } else {
                    quickSort(i, hi)
                    hi = j
                }
            }
        }

        private fun swap(i: Int, j: Int) {
            value[i] = value[j].also { value[j] = value[i] }
            shape[i] = shape[j].also { shape[j] = shape[i] }
            first[i] = first[j].also { first[j] = first[i] }
            second[i] = second[j].also { second[j] = second[i] }
        }

        fun lowerBound(x: Double): Int {
            var lo = 0
            var hi = size
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (value[mid] < x) lo = mid + 1 else hi = mid
            }
            return lo
        }

        /** [shape, first, second] for a candidate record. */
        fun code(i: Int) = intArrayOf(shape[i], first[i], second[i])

        fun exact(i: Int): Rational = combination(shape[i], first[i], second[i]).resistance
    }

    private fun combination(shape: Int, a: Int, b: Int): Combination = when (shape) {
        SINGLE -> Combination(Kind.Single, listOf(values[a]))
        SERIES -> Combination(Kind.Series2, listOf(values[a], values[b]))
        PARALLEL -> Combination(Kind.Parallel2, listOf(values[a], values[b]))
        else -> error("Not a chosen arm: $shape")
    }

    private fun arm(code: IntArray, offset: Int, entered: Quantity): Pair<Combination, Int> =
        if (code[offset] == FIXED) {
            Combination(Kind.Single, listOf(solved.values.getValue(entered))) to 0
        } else {
            combination(code[offset], code[offset + 1], code[offset + 2]) to (if (code[offset] == SINGLE) 1 else 2)
        }

    /**
     * Keeps every candidate whose key is within the margin of the current 5th best, starting from
     * [initialBound]; the best keys sit in a small sorted array, as in spec 004's search.
     */
    private inner class Collector(initialBound: Double) {
        private val keys = ArrayList<Double>()
        private val codes = ArrayList<IntArray>()
        private val best = DoubleArray(BUILDS_PER_TIER) { Double.POSITIVE_INFINITY }
        private var compactAt = 64

        var bound = initialBound
            private set

        inline fun offer(key: Double, code: () -> IntArray) {
            if (key > bound) return
            keys += key
            codes += code()
            if (key < best[BUILDS_PER_TIER - 1]) {
                var i = BUILDS_PER_TIER - 1
                while (i > 0 && best[i - 1] > key) {
                    best[i] = best[i - 1]
                    i--
                }
                best[i] = key
                bound = minOf(bound, tightBound())
            }
            if (keys.size >= compactAt) compact()
        }

        /** How many candidates lie within the current bound. */
        fun found(): Int = keys.count { it <= bound }

        fun tightBound(): Double = best[BUILDS_PER_TIER - 1].let { if (it.isInfinite()) it else it * (1 + RELATIVE_MARGIN) + ABSOLUTE_MARGIN }

        private fun compact() {
            val keep = keys.indices.filter { keys[it] <= bound }
            val keptKeys = keep.map { keys[it] }
            val keptCodes = keep.map { codes[it] }
            keys.clear(); keys += keptKeys
            codes.clear(); codes += keptCodes
            compactAt = maxOf(64, keys.size * 2)
        }

        /**
         * The best [BUILDS_PER_TIER] in exact order. Exact ranking is costly, and a ratio like ½ ties
         * thousands of builds at 0% error, so candidates are first narrowed in Double by error and then
         * total deviation (FR-010's first and third keys; the second is equal within a tier). Every
         * candidate within the margins of the 5th one on both keys is kept for the exact step.
         */
        fun ranked(ranking: Comparator<DividerBuild>): List<DividerBuild> {
            val within = keys.indices.filter { keys[it] <= bound }
            val deviation = within.associateWith { abs(total(codes[it]) - t) / t }
            val order = within.sortedWith(compareBy<Int> { keys[it] }.thenBy { deviation.getValue(it) })
            val fifth = order.getOrNull(BUILDS_PER_TIER - 1)
            val candidates = if (fifth == null) {
                order
            } else {
                val keyEdge = keys[fifth] * (1 + RELATIVE_MARGIN) + ABSOLUTE_MARGIN
                val tieEdge = keys[fifth] * (1 - RELATIVE_MARGIN) - ABSOLUTE_MARGIN
                val deviationEdge = deviation.getValue(fifth) * (1 + RELATIVE_MARGIN) + ABSOLUTE_MARGIN
                order.filter { keys[it] < tieEdge || (keys[it] <= keyEdge && deviation.getValue(it) <= deviationEdge) }
            }
            return candidates
                .map { i ->
                    val (r1, n1) = arm(codes[i], 0, Quantity.R1)
                    val (r2, n2) = arm(codes[i], 3, Quantity.R2)
                    dividerBuild(solved, r1, r2, n1 + n2)
                }
                .sortedWith(ranking)
                .take(BUILDS_PER_TIER)
        }
    }

    /** The Double total R1 + R2 of a candidate record. */
    private fun total(code: IntArray): Double = armValue(code, 0, Quantity.R1) + armValue(code, 3, Quantity.R2)

    private fun armValue(code: IntArray, offset: Int, entered: Quantity): Double {
        val a = code[offset + 1]
        val b = code[offset + 2]
        return when (code[offset]) {
            FIXED -> solved.values.getValue(entered).toDouble()
            SINGLE -> v[a]
            SERIES -> v[a] + v[b]
            else -> 1 / (1 / v[a] + 1 / v[b])
        }
    }

    private companion object {
        /** Candidate record of an entered arm. */
        val FIXED_CODE = intArrayOf(FIXED, 0, 0)

        /** Relative U2 error bounds tried in turn when both arms are chosen; the last always completes. */
        val ERROR_BOUNDS = doubleArrayOf(0.0, 1e-3, 1e-2, 1e-1, 1.0, Double.POSITIVE_INFINITY)
    }
}
