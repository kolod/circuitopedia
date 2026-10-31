package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.Rational

/** Number of results listed per kind of connection (spec 004 FR-008). */
const val PER_KIND = 5

/**
 * The order of spec FR-007: smaller absolute deviation first, then fewer resistors, then the smaller
 * largest value, then the values in canonical order (only to make the order deterministic).
 */
fun rankingFor(target: Rational): Comparator<Combination> =
    compareBy<Combination> { (it.resistance - target).abs() }
        .thenBy { it.kind.resistorCount }
        .thenBy { it.values.max() }
        .thenComparator { x, y ->
            x.values.zip(y.values).map { (a, b) -> a.compareTo(b) }.firstOrNull { it != 0 } ?: 0
        }