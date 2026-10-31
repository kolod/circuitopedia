package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.siformat.formatDecimal
import io.github.kolod.circuitopedia.shared.siformat.formatSi
import java.math.BigDecimal
import java.util.Locale

/** "47µH ±10%", "220nH ±20%", or "4,7µH ±5%" in a locale with a decimal comma. */
fun Reading.format(locale: Locale): String =
    formatMicrohenries(inductance.value, locale) + " ±" + formatDecimal(tolerance.value, locale) + "%"

/** Text of a table cell: "4", ".", "×0.1µH", "×1mH", "±5%", or "" if [color] has no meaning here. */
fun BandRole.cellLabel(color: BandColor, locale: Locale): String = when (this) {
    BandRole.Digit -> color.digit?.toString()
    BandRole.DecimalPoint -> ".".takeIf { color.isDecimalPoint }
    // Multipliers are factors on µH, so the small ones stay in µH ("×0.01µH"), as in the Bourns table.
    BandRole.Multiplier -> color.multiplierExponent?.let {
        "×" + formatSi(BigDecimal.ONE.scaleByPowerOfTen(it), "H", listOf("µ", "m"), 0, locale)
    }
    BandRole.Tolerance -> color.tolerance?.let { "±" + formatDecimal(it.value, locale) + "%" }
} ?: ""

/** With the prefix that puts the mantissa in [1, 1000): nH, µH or mH. */
private fun formatMicrohenries(value: BigDecimal, locale: Locale): String =
    formatSi(value, "H", listOf("n", "µ", "m"), 1, locale)
