package io.github.kolod.circuitopedia.shared.resistors

import io.github.kolod.circuitopedia.shared.siformat.formatDecimal
import io.github.kolod.circuitopedia.shared.siformat.formatSi
import java.util.Locale

private val OHM_PREFIXES = listOf("", "k", "M", "G")
private val HUNDRED = Rational.of(100, 1)

/** "4.7kΩ ∥ 10kΩ + 220Ω": "+" for series, "∥" for parallel (spec FR-009). */
fun Combination.connectionText(locale: Locale): String {
    val names = values.map { it.formatOhms(locale) }
    return when (kind) {
        Kind.Single -> names.single()
        Kind.Series2, Kind.Series3 -> names.joinToString(" + ")
        Kind.Parallel2, Kind.Parallel3 -> names.joinToString(" ∥ ")
        Kind.ParallelSeries -> "${names[0]} ∥ ${names[1]} + ${names[2]}"
    }
}

/** With 4 significant digits and the prefix that puts the mantissa in [1, 1000): "3.197kΩ", "0.3333Ω". */
fun Rational.formatOhms(locale: Locale): String = formatSi(round(4), "Ω", OHM_PREFIXES, 0, locale)

/** The relative [deviation] in percent with a sign and 3 significant digits: "+0.0213%", "−1.23%", "0%". */
fun formatDeviation(deviation: Rational, locale: Locale): String {
    val percent = (deviation * HUNDRED).round(3)
    if (percent.signum() == 0) return "0%"
    val sign = if (percent.signum() > 0) "+" else "−"
    return sign + formatDecimal(percent.abs(), locale) + "%"
}
