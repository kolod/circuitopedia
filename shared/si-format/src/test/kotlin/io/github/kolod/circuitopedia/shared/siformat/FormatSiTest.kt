package io.github.kolod.circuitopedia.shared.siformat

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatSiTest {
    private val uk = Locale.forLanguageTag("uk")

    private fun ohms(value: String, locale: Locale = Locale.ENGLISH) =
        formatSi(BigDecimal(value), "Ω", listOf("", "k", "M", "G"), 0, locale)

    private fun microhenries(value: String, locale: Locale = Locale.ENGLISH) =
        formatSi(BigDecimal(value), "H", listOf("n", "µ", "m"), 1, locale)

    @Test
    fun resistorFamilyKeepsValuesBelowOneInOhms() {
        assertEquals("0.1Ω", ohms("0.1"))
        assertEquals("0.22Ω", ohms("0.22"))
        assertEquals("4.7kΩ", ohms("4700"))
        assertEquals("1kΩ", ohms("1000"))
        assertEquals("999GΩ", ohms("999000000000"))
    }

    @Test
    fun inductorFamilyUsesNanoMicroAndMilli() {
        assertEquals("100nH", microhenries("0.1"))
        assertEquals("220nH", microhenries("0.22"))
        assertEquals("770nH", microhenries("0.77"))
        assertEquals("4.7µH", microhenries("4.7"))
        assertEquals("47µH", microhenries("47"))
        assertEquals("1mH", microhenries("1000"))
        assertEquals("99mH", microhenries("99000"))
    }

    @Test
    fun trailingZerosAreDropped() {
        assertEquals("100µH", microhenries("100.00"))
        assertEquals("1kΩ", ohms("1000.0"))
    }

    @Test
    fun decimalSeparatorFollowsTheLocale() {
        assertEquals("4,7µH", microhenries("4.7", uk))
        assertEquals("4,7kΩ", ohms("4700", Locale.GERMAN))
        assertEquals("4.7µH", microhenries("4.7", Locale.JAPANESE))
        assertEquals("0,5", formatDecimal(BigDecimal("0.50"), uk))
    }
}
