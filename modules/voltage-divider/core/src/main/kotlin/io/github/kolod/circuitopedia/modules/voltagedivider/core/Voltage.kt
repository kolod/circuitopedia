package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.shared.resistors.Rational
import io.github.kolod.circuitopedia.shared.siformat.formatSi
import java.math.BigDecimal
import java.util.Locale

private val MIN_VOLTS = Rational.of(BigDecimal("0.001"))
private val MAX_VOLTS = Rational.of(BigDecimal("100000"))

/** An optional sign, a number with "." or ",", an optional m or k prefix and an optional "V". */
private val VOLTAGE = Regex("^([+\\-−]?)\\s*(\\d+(?:[.,]\\d+)?|[.,]\\d+)\\s*([mkK])?\\s*[Vv]?$")

/** A voltage typed by the user, or null if invalid or outside 1mV … 100kV in magnitude (spec 005). */
fun parseVoltage(text: String): Rational? {
    val match = VOLTAGE.matchEntire(text.trim()) ?: return null
    val (sign, number, prefix) = match.destructured
    val exponent = when (prefix) {
        "m" -> -3
        "k", "K" -> 3
        else -> 0
    }
    var value = BigDecimal(number.replace(',', '.')).scaleByPowerOfTen(exponent)
    if (sign == "-" || sign == "−") value = value.negate()
    val result = Rational.of(value)
    return result.takeIf { it.abs() >= MIN_VOLTS && it.abs() <= MAX_VOLTS }
}

/** With 4 significant digits and the prefix m, none or k: "3.837V", "−4.7mV" (sign kept outside formatSi). */
fun formatVolts(value: Rational, locale: Locale): String {
    val sign = if (value.signum < 0) "−" else ""
    return sign + formatSi(value.abs().round(4), "V", listOf("m", "", "k"), 1, locale)
}
