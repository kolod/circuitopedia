package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SC-002: every valid combination of every layout against an independent oracle. The table is
 * typed again here from IEC 60062:2016 (research R1) on purpose instead of reading BandColor.
 */
class ExhaustiveReadingTest {

    private val names = listOf("Black", "Brown", "Red", "Orange", "Yellow", "Green", "Blue", "Violet", "Gray", "White", "Gold", "Silver")
    private val digit = mapOf("Black" to 0, "Brown" to 1, "Red" to 2, "Orange" to 3, "Yellow" to 4, "Green" to 5, "Blue" to 6, "Violet" to 7, "Gray" to 8, "White" to 9)
    private val exponent = digit + mapOf("Gold" to -1, "Silver" to -2)
    private val tolerance = mapOf(
        "Brown" to "1", "Red" to "2", "Orange" to "0.05", "Yellow" to "0.02", "Green" to "0.5",
        "Blue" to "0.25", "Violet" to "0.1", "Gray" to "0.01", "Gold" to "5", "Silver" to "10",
    )
    private val tcr = mapOf("Black" to 250, "Brown" to 100, "Red" to 50, "Orange" to 15, "Yellow" to 25, "Green" to 20, "Blue" to 10, "Violet" to 5, "Gray" to 1)

    private fun digitChoices(first: Boolean) = digit.keys.filter { !(first && it == "Black") }

    /** Expected text, built without stripTrailingZeros/movePointLeft. */
    private fun expectedText(digits: Int, exp: Int, tol: String, tcrValue: Int?): String {
        val ohms = BigDecimal(BigInteger.valueOf(digits.toLong()).multiply(BigInteger.TEN.pow(exp + 2)), 2)
        val prefixes = listOf("", "k", "M", "G")
        var k = 0
        while (k < 3 && ohms >= BigDecimal(1000).pow(k + 1)) k++
        val scaled = ohms.divide(BigDecimal(1000).pow(k), 12, RoundingMode.UNNECESSARY)
        val whole = scaled.toBigInteger().toString()
        val fraction = scaled.subtract(BigDecimal(scaled.toBigInteger())).toPlainString().removePrefix("0").removePrefix(".").trimEnd('0')
        val number = if (fraction.isEmpty()) whole else "$whole.$fraction"
        return "$number${prefixes[k]}Ω ±$tol%" + (tcrValue?.let { " ±${it}ppm/K" } ?: "")
    }

    private fun check(layout: BandLayout, digitCount: Int, toleranceBand: Boolean, tcrBand: Boolean): Int {
        var digitCombos = listOf(listOf<String>())
        for (i in 0 until digitCount) digitCombos = digitCombos.flatMap { prefix -> digitChoices(i == 0).map { prefix + it } }
        val tolerances = if (toleranceBand) tolerance.keys.map { it } else listOf<String?>(null)
        val tcrs = if (tcrBand) tcr.keys.map { it } else listOf<String?>(null)
        var count = 0
        for (digits in digitCombos) for (multiplier in names) for (tol in tolerances) for (tc in tcrs) {
            val names = digits + multiplier + listOfNotNull(tol, tc)
            val reading = Selection(layout, names.map { BandColor.valueOf(it) }).read()
            val digitValue = digits.fold(0) { acc, d -> acc * 10 + digit.getValue(d) }
            val expectedOhms = BigDecimal(digitValue).scaleByPowerOfTen(exponent.getValue(multiplier))
            val expectedTol = tol?.let { tolerance.getValue(it) } ?: "20"
            val expectedTcr = tc?.let { tcr.getValue(it) }
            assertEquals("$names ohms", 0, expectedOhms.compareTo(reading.resistance.value))
            assertEquals("$names tolerance", 0, BigDecimal(expectedTol).compareTo(reading.tolerance.value))
            assertEquals("$names tcr", expectedTcr, reading.tcr?.value)
            assertEquals("$names text", expectedText(digitValue, exponent.getValue(multiplier), expectedTol, expectedTcr), reading.format(Locale.ENGLISH))
            count++
        }
        return count
    }

    @Test
    fun threeBands() = assertEquals(1_080, check(BandLayout.Three, 2, toleranceBand = false, tcrBand = false))

    @Test
    fun fourBands() = assertEquals(10_800, check(BandLayout.Four, 2, toleranceBand = true, tcrBand = false))

    @Test
    fun fiveBands() = assertEquals(108_000, check(BandLayout.Five, 3, toleranceBand = true, tcrBand = false))

    @Test
    fun sixBands() = assertEquals(972_000, check(BandLayout.Six, 3, toleranceBand = true, tcrBand = true))
}
