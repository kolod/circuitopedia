package io.github.kolod.circuitopedia.modules.resistorcombinations.core

import java.math.BigDecimal

private val MIN_TARGET = Rational.of(BigDecimal("0.1"))
private val MAX_TARGET = Rational.of(BigDecimal("1000000000"))

/** Multiplier of an SI prefix letter, or of R (ohms), as used in both notations below. */
private val LETTER_EXPONENT = mapOf('R' to 0, 'r' to 0, 'm' to -3, 'k' to 3, 'K' to 3, 'M' to 6, 'G' to 9)

private val UNIT_SUFFIX = Regex("(?i)\\s*(Ω|ohm)$")

/** Resistor code notation, IEC 60062:2016 §4: the letter stands where the decimal point is ("4k7", "R47", "47R"). */
private val CODE = Regex("^(\\d*)([RrmkKMG])(\\d*)$")

/** A decimal number with "." or "," and an optional SI prefix ("4.7k", "4,7 k", "4700"). */
private val DECIMAL = Regex("^(\\d+(?:[.,]\\d+)?|[.,]\\d+)\\s*([mkKMG])?$")

/**
 * The target resistance typed by the user (spec FR-002, research R5), or null if the text is not a
 * supported notation or the value lies outside 0.1Ω … 1GΩ.
 */
fun parseTarget(text: String): Rational? {
    val trimmed = text.trim().replace(UNIT_SUFFIX, "").trim()
    val (digits, exponent) = CODE.matchEntire(trimmed)?.let { match ->
        val (whole, letter, fraction) = match.destructured
        if (whole.isEmpty() && fraction.isEmpty()) return null
        "${whole.ifEmpty { "0" }}.${fraction.ifEmpty { "0" }}" to LETTER_EXPONENT.getValue(letter.single())
    } ?: DECIMAL.matchEntire(trimmed)?.let { match ->
        val (number, prefix) = match.destructured
        number.replace(',', '.') to (prefix.singleOrNull()?.let(LETTER_EXPONENT::getValue) ?: 0)
    } ?: return null
    val value = Rational.of(BigDecimal(digits).scaleByPowerOfTen(exponent))
    return value.takeIf { it >= MIN_TARGET && it <= MAX_TARGET }
}
