package io.github.kolod.circuitopedia.shared.colorcode

import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

private val THOUSAND = BigDecimal(1000)

/**
 * [value] with the SI prefix that puts the mantissa in [1, 1000), clamped to [prefixes]; for
 * example "4.7kΩ" or "220nH". [baseIndex] is the index in [prefixes] of the unit [value] is in.
 */
fun formatSi(value: BigDecimal, unit: String, prefixes: List<String>, baseIndex: Int, locale: Locale): String {
    var k = baseIndex
    while (k < prefixes.lastIndex && value >= THOUSAND.pow(k - baseIndex + 1)) k++
    while (k > 0 && value < BigDecimal.ONE.movePointLeft(3 * (baseIndex - k))) k--
    return formatDecimal(value.movePointLeft(3 * (k - baseIndex)), locale) + prefixes[k] + unit
}

/** Plain digits without trailing zeros, exponent or grouping, using the locale's decimal separator. */
fun formatDecimal(value: BigDecimal, locale: Locale): String =
    value.stripTrailingZeros().toPlainString().replace('.', DecimalFormatSymbols.getInstance(locale).decimalSeparator)
