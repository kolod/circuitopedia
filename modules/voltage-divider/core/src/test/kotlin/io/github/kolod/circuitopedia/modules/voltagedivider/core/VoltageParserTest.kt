package io.github.kolod.circuitopedia.modules.voltagedivider.core

import io.github.kolod.circuitopedia.shared.resistors.Rational
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoltageParserTest {
    private fun volts(value: String) = Rational.of(BigDecimal(value))

    @Test
    fun acceptedNotations() {
        val cases = mapOf(
            "5" to "5",
            "3.3" to "3.3",
            "3,3" to "3.3",
            "-12" to "-12",
            "−12" to "-12",
            "+5V" to "5",
            " 12 V " to "12",
            "12v" to "12",
            "4.7m" to "0.0047",
            "4,7 mV" to "0.0047",
            "1k" to "1000",
            "1.5kV" to "1500",
            "100k" to "100000",
            "1m" to "0.001",
            ",5" to "0.5",
        )
        for ((text, expected) in cases) assertEquals(text, volts(expected), parseVoltage(text))
    }

    @Test
    fun rejectedNotations() {
        for (text in listOf("", " ", "abc", "0", "-0", "0.0005", "200k", "100.1k", "1.2.3", "5 VV", "1M", "V", "k", "--5", "5 k V x")) {
            assertNull(text, parseVoltage(text))
        }
    }
}
