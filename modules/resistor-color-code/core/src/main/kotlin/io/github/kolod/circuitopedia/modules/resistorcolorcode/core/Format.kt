package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

/** "4.7kΩ ±5%", or "4,7kΩ ±1% ±100ppm/K" in a locale with a decimal comma. */
fun Reading.format(locale: Locale): String = buildString {
    append(formatOhms(resistance, locale))
    append(" ±").append(formatDecimal(tolerance.value, locale)).append('%')
    tcr?.let { append(" ±").append(it.value).append("ppm/K") }
}

/** Text of a table cell: "4", "×100Ω", "×0.1Ω", "±5%", "±100ppm/K", or "" if [color] has no meaning here. */
fun BandRole.cellLabel(color: BandColor, locale: Locale): String = when (this) {
    BandRole.Digit -> color.digit?.toString()
    BandRole.Multiplier -> color.multiplierExponent?.let { "×" + formatOhms(Ohms(BigDecimal.ONE.scaleByPowerOfTen(it)), locale) }
    BandRole.Tolerance -> color.tolerance?.let { "±" + formatDecimal(it.value, locale) + "%" }
    BandRole.TemperatureCoefficient -> color.tcr?.let { "±${it.value}ppm/K" }
} ?: ""

private val PREFIXES = listOf("", "k", "M", "G")
private val THOUSAND = BigDecimal(1000)

/** With the SI prefix that puts the mantissa in [1, 1000); values below 1 Ω stay in Ω. */
private fun formatOhms(ohms: Ohms, locale: Locale): String {
    var k = 0
    while (k < PREFIXES.lastIndex && ohms.value >= THOUSAND.pow(k + 1)) k++
    return formatDecimal(ohms.value.movePointLeft(3 * k), locale) + PREFIXES[k] + "Ω"
}

/** Plain digits without trailing zeros, exponent or grouping, using the locale's decimal separator. */
private fun formatDecimal(value: BigDecimal, locale: Locale): String =
    value.stripTrailingZeros().toPlainString().replace('.', DecimalFormatSymbols.getInstance(locale).decimalSeparator)
