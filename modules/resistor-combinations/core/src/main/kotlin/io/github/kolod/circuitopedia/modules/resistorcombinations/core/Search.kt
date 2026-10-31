package io.github.kolod.circuitopedia.modules.resistorcombinations.core

/**
 * The best [PER_KIND] combinations of a single resistor and of each [allowed] kind, merged and
 * sorted by [rankingFor] (spec FR-006–FR-008).
 *
 * Each kind fixes its first one or two resistors in canonical order, binary-searches the ideal
 * last one and walks outward while results can still make the kind's best [PER_KIND] (research R4).
 * The search compares Double keys with a safety margin and ranks the survivors exactly.
 */
fun search(target: Rational, series: ESeries, allowed: Set<Kind>): List<Combination> {
    val searcher = Searcher(target, series.values())
    return (allowed + Kind.Single).flatMap(searcher::best).sortedWith(rankingFor(target))
}

/**
 * Relative margin above the kind's current [PER_KIND]-th best Double key. It is far above the
 * Double rounding error (about 1e-15), so nothing that ranks in the exact top [PER_KIND] is dropped.
 */
private const val RELATIVE_MARGIN = 1e-9
private const val ABSOLUTE_MARGIN = 1e-12

private class Searcher(private val target: Rational, private val values: List<Rational>) {
    private val n = values.size
    private val v = DoubleArray(n) { values[it].toDouble() }
    private val g = DoubleArray(n) { 1 / v[it] }
    private val t = target.toDouble()

    /**
     * Two passes: a quick one that only tries the two values next to each ideal, which yields real
     * combinations and so a safe bound, then the full walk starting from that bound. Without the
     * first pass, prefixes far from the target (tiny a in parallel kinds) would walk every c.
     */
    fun best(kind: Kind): List<Combination> {
        val seed = Collector(Double.POSITIVE_INFINITY)
        enumerate(kind, seed, nearestOnly = true)
        val collector = Collector(seed.tightBound())
        enumerate(kind, collector, nearestOnly = false)
        return collector.ranked(kind)
    }

    private fun enumerate(kind: Kind, collector: Collector, nearestOnly: Boolean) {
        fun walk(from: Int, ideal: Double, resistance: (Int) -> Double, indices: (Int) -> IntArray) =
            walk(collector, from, ideal, nearestOnly, resistance, indices)
        when (kind) {
            Kind.Single -> walk(0, t, { v[it] }) { intArrayOf(it) }
            Kind.Series2 -> for (a in 0 until n) {
                if (2 * v[a] - t > collector.bound * t) break
                walk(a, t - v[a], { v[a] + v[it] }) { intArrayOf(a, it) }
            }
            Kind.Parallel2 -> for (a in 0 until n) {
                if (v[a] / 2 - t > collector.bound * t) break
                walk(a, idealParallel(1 / t - g[a]), { 1 / (g[a] + g[it]) }) { intArrayOf(a, it) }
            }
            Kind.Series3 -> for (a in 0 until n) {
                if (3 * v[a] - t > collector.bound * t) break
                for (b in a until n) {
                    if (v[a] + 2 * v[b] - t > collector.bound * t) break
                    walk(b, t - v[a] - v[b], { v[a] + v[b] + v[it] }) { intArrayOf(a, b, it) }
                }
            }
            Kind.Parallel3 -> for (a in 0 until n) {
                if (v[a] / 3 - t > collector.bound * t) break
                for (b in a until n) {
                    if (1 / (g[a] + 2 * g[b]) - t > collector.bound * t) break
                    walk(b, idealParallel(1 / t - g[a] - g[b]), { 1 / (g[a] + g[b] + g[it]) }) { intArrayOf(a, b, it) }
                }
            }
            Kind.ParallelSeries -> for (a in 0 until n) {
                if (v[a] / 2 + v[0] - t > collector.bound * t) break
                for (b in a until n) {
                    val pair = 1 / (g[a] + g[b])
                    if (pair + v[0] - t > collector.bound * t) break
                    walk(0, t - pair, { pair + v[it] }) { intArrayOf(a, b, it) }
                }
            }
        }
    }

    /** The resistance whose conductance is [conductance]; +∞ when no positive value helps. */
    private fun idealParallel(conductance: Double) = if (conductance > 0) 1 / conductance else Double.POSITIVE_INFINITY

    /**
     * Tries the last resistor from index [from] on, starting next to [ideal] and walking outward in
     * both directions. [resistance] is monotonic in the last value, so the deviation grows away
     * from [ideal] and each direction stops once it exceeds the collector's bound. With
     * [nearestOnly], only the value on each side of [ideal] is tried.
     */
    private inline fun walk(
        collector: Collector,
        from: Int,
        ideal: Double,
        nearestOnly: Boolean,
        resistance: (Int) -> Double,
        indices: (Int) -> IntArray,
    ) {
        val start = lowerBound(from, ideal)
        var i = start - 1
        while (i >= from && collector.offer(key(resistance(i))) { indices(i) } && !nearestOnly) i--
        i = start
        while (i < n && collector.offer(key(resistance(i))) { indices(i) } && !nearestOnly) i++
    }

    private fun key(resistance: Double) = kotlin.math.abs(resistance - t) / t

    /** The first index ≥ [from] whose value is ≥ [ideal], or n. */
    private fun lowerBound(from: Int, ideal: Double): Int {
        var lo = from
        var hi = n
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (v[mid] < ideal) lo = mid + 1 else hi = mid
        }
        return lo
    }

    /**
     * Keeps every candidate whose key is within the margin of the current [PER_KIND]-th best,
     * starting from [initialBound], a key that at least [PER_KIND] real combinations reach.
     *
     * The best [PER_KIND] keys live in a small sorted array, so tightening the bound is O(PER_KIND)
     * per accepted candidate; the candidate list is compacted only when it has doubled. (Targets near
     * the ends of the range improve with almost every prefix, so sorting on every accept was slow.)
     */
    private inner class Collector(initialBound: Double) {
        private var keys = DoubleArray(64)
        private var candidates = arrayOfNulls<IntArray>(64)
        private var size = 0
        private var compactAt = 64
        private val best = DoubleArray(PER_KIND) { Double.POSITIVE_INFINITY }

        /** Relative deviation above which a candidate cannot make the kind's best [PER_KIND]. */
        var bound = initialBound
            private set

        inline fun offer(key: Double, indices: () -> IntArray): Boolean {
            if (key > bound) return false
            add(key, indices())
            return true
        }

        fun add(key: Double, indices: IntArray) {
            if (size == keys.size) {
                keys = keys.copyOf(size * 2)
                candidates = candidates.copyOf(size * 2)
            }
            keys[size] = key
            candidates[size] = indices
            size++
            if (key < best[PER_KIND - 1]) {
                var i = PER_KIND - 1
                while (i > 0 && best[i - 1] > key) {
                    best[i] = best[i - 1]
                    i--
                }
                best[i] = key
                bound = minOf(bound, tightBound())
            }
            if (size >= compactAt) compact()
        }

        /** The bound from everything offered so far, for seeding a second pass. */
        fun tightBound(): Double = best[PER_KIND - 1].let { if (it.isInfinite()) it else it * (1 + RELATIVE_MARGIN) + ABSOLUTE_MARGIN }

        private fun compact() {
            var kept = 0
            for (i in 0 until size) {
                if (keys[i] <= bound) {
                    keys[kept] = keys[i]
                    candidates[kept] = candidates[i]
                    kept++
                }
            }
            for (i in kept until size) candidates[i] = null
            size = kept
            compactAt = maxOf(64, kept * 2)
        }

        fun ranked(kind: Kind): List<Combination> =
            (0 until size).filter { keys[it] <= bound }
                .map { i -> Combination(kind, candidates[i]!!.map { values[it] }) }
                .sortedWith(rankingFor(target))
                .take(PER_KIND)
    }
}
