package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class VoltageFormatTest {
    private val en = Locale.ENGLISH
    private fun volts(value: String) = Rational.of(BigDecimal(value))

    @Test
    fun fourSignificantDigitsWithPrefix() {
        assertEquals("3.837V", formatVolts(Rational.of(564, 147), en)) // 12 · 4.7 / 14.7
        assertEquals("4.7mV", formatVolts(volts("0.0047"), en))
        assertEquals("12V", formatVolts(volts("12"), en))
        assertEquals("1kV", formatVolts(volts("1000"), en))
        assertEquals("3.333V", formatVolts(Rational.of(10, 3), en))
    }

    @Test
    fun negativeValuesKeepTheirSign() {
        assertEquals("−12V", formatVolts(volts("-12"), en))
        assertEquals("−4.7mV", formatVolts(volts("-0.0047"), en))
    }

    @Test
    fun decimalSeparatorFollowsTheLocale() {
        assertEquals("3,837V", formatVolts(Rational.of(564, 147), Locale.forLanguageTag("uk")))
    }
}
