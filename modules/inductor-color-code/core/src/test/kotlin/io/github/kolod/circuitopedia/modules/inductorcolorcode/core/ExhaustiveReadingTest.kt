package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Every valid selection of both layouts against an independent oracle: a literal copy of the
 * Bourns table (research R1) and plain string building, without the production code's helpers.
 */
class ExhaustiveReadingTest {
    private val names = listOf("Black", "Brown", "Red", "Orange", "Yellow", "Green", "Blue", "Violet", "Gray", "White", "Gold", "Silver")
    private val digit = (0..9).associateBy { names[it] }
    private val exponent = mapOf("Black" to 0, "Brown" to 1, "Red" to 2, "Orange" to 3, "Gold" to -1, "Silver" to -2)
    private val tolerance = mapOf("Gold" to "5", "Silver" to "10")

    /** The value in µH as a decimal string, or null if the colors are not a valid marking. */
    private fun oracle(colors: List<String>): Pair<String, String>? {
        val (a, b, c) = colors
        val tol = if (colors.size == 4) tolerance[colors[3]] ?: return null else "20"
        val value = when {
            a == "Gold" && b == "Gold" -> return null
            a == "Gold" -> {
                val first = digit[b] ?: return null
                if (first == 0) return null
                "0.$first${digit[c] ?: return null}"
            }
            b == "Gold" -> {
                val first = digit[a] ?: return null
                if (first == 0) return null
                "$first.${digit[c] ?: return null}"
            }
            else -> {
                val first = digit[a] ?: return null
                if (first == 0) return null
                "$first${digit[b] ?: return null}E${exponent[c] ?: return null}"
            }
        }
        return value to tol
    }

    /** "220nH", "4.7µH", "99mH": mantissa in [1, 1000) with n, µ or m. */
    private fun oracleText(microhenries: BigDecimal, tol: String): String {
        val nano = microhenries.movePointRight(3)
        val (mantissa, prefix) = when {
            nano < BigDecimal(1000) -> nano to "n"
            nano < BigDecimal(1_000_000) -> nano.movePointLeft(3) to "µ"
            else -> nano.movePointLeft(6) to "m"
        }
        return "${mantissa.stripTrailingZeros().toPlainString()}${prefix}H ±$tol%"
    }

    private fun check(layout: BandLayout, expectedCount: Int) {
        var valid = 0
        fun visit(prefix: List<BandColor>) {
            if (prefix.size < layout.bandCount) {
                BandColor.entries.forEach { visit(prefix + it) }
                return
            }
            val expected = oracle(prefix.map { names[it.ordinal] })
            val selection = runCatching { Selection(layout, prefix) }.getOrNull()
            assertEquals("validity of $prefix", expected != null, selection != null)
            if (expected == null || selection == null) return
            valid++
            val reading = selection.read()
            val value = BigDecimal(expected.first)
            assertEquals("$prefix", 0, value.compareTo(reading.inductance.value))
            assertEquals("$prefix", 0, BigDecimal(expected.second).compareTo(reading.tolerance.value))
            assertEquals("$prefix", oracleText(value, expected.second), reading.format(Locale.ENGLISH))
        }
        visit(emptyList())
        assertEquals(expectedCount, valid)
    }

    @Test
    fun threeBands() = check(BandLayout.Three, 720)

    @Test
    fun fourBands() = check(BandLayout.Four, 1440)
}
