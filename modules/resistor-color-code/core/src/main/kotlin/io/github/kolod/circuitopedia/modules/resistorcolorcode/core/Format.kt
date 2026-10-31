package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.siformat.formatDecimal
import io.github.kolod.circuitopedia.shared.siformat.formatSi
import java.math.BigDecimal
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

/** With the SI prefix that puts the mantissa in [1, 1000); values below 1 Ω stay in Ω. */
private fun formatOhms(ohms: Ohms, locale: Locale): String =
    formatSi(ohms.value, "Ω", listOf("", "k", "M", "G"), 0, locale)
