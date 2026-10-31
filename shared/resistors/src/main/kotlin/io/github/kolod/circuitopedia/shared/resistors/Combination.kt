package io.github.kolod.circuitopedia.shared.resistors

/**
 * How the resistors of a combination are connected. Values are kept in a canonical order so that
 * permutations of the same circuit are one combination: a ≤ b ≤ c, except that in
 * [ParallelSeries] only the parallel pair a ≤ b is ordered and c is the series resistor.
 *
 * The ordinals are stored in saved UI state, so entries may only be appended.
 */
enum class Kind(val resistorCount: Int) {
    Single(1),
    Series2(2),
    Series3(3),
    Parallel2(2),
    Parallel3(3),
    ParallelSeries(3),
}

/**
 * A kind of connection with its resistor values in canonical order.
 *
 * Series `R = R1 + R2 (+ R3)` and parallel `1/R = 1/R1 + 1/R2 (+ 1/R3)` follow from Ohm's and
 * Kirchhoff's laws (P. Horowitz, W. Hill, *The Art of Electronics*, 3rd ed., 2015, §1.2).
 */
data class Combination(val kind: Kind, val values: List<Rational>) {
    init {
        require(values.size == kind.resistorCount) { "$kind needs ${kind.resistorCount} values: $values" }
    }

    val resistance: Rational by lazy {
        when (kind) {
            Kind.Single, Kind.Series2, Kind.Series3 -> values.reduce(Rational::plus)
            Kind.Parallel2, Kind.Parallel3 -> parallel(values)
            Kind.ParallelSeries -> parallel(values.take(2)) + values[2]
        }
    }

    /** Signed relative deviation `(R − target) / target`. */
    fun deviation(target: Rational): Rational = (resistance - target) / target
}

private fun parallel(values: List<Rational>) = values.map(Rational::reciprocal).reduce(Rational::plus).reciprocal()
