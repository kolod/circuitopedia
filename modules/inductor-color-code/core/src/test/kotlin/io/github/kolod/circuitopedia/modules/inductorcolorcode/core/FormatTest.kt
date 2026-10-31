package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.DecimalPoint
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.Digit
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.Multiplier
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.Tolerance
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Green
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.White
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    private val uk = Locale.forLanguageTag("uk")

    private fun format(locale: Locale, vararg colors: BandColor) =
        Selection(if (colors.size == 3) BandLayout.Three else BandLayout.Four, colors.toList()).read().format(locale)

    @Test
    fun values() {
        assertEquals("47µH ±10%", format(Locale.ENGLISH, Yellow, Violet, Black, Silver))
        assertEquals("100µH ±5%", format(Locale.ENGLISH, Brown, Black, Brown, Gold))
        assertEquals("100nH ±10%", format(Locale.ENGLISH, Brown, Black, Silver, Silver))
        assertEquals("99mH ±5%", format(Locale.ENGLISH, White, White, Orange, Gold))
        assertEquals("1mH ±20%", format(Locale.ENGLISH, Brown, Black, Red))
        assertEquals("220nH ±20%", format(Locale.ENGLISH, Gold, Red, Red))
    }

    @Test
    fun decimalSeparatorFollowsTheLocale() {
        assertEquals("4,7µH ±5%", format(uk, Yellow, Gold, Violet, Gold))
        assertEquals("4,7µH ±5%", format(Locale.GERMAN, Yellow, Violet, Gold, Gold))
        assertEquals("4.7µH ±5%", format(Locale.JAPANESE, Yellow, Gold, Violet, Gold))
    }

    @Test
    fun cellLabels() {
        assertEquals("4", Digit.cellLabel(Yellow, Locale.ENGLISH))
        assertEquals(".", DecimalPoint.cellLabel(Gold, Locale.ENGLISH))
        assertEquals("×0.01µH", Multiplier.cellLabel(Silver, Locale.ENGLISH))
        assertEquals("×0.1µH", Multiplier.cellLabel(Gold, Locale.ENGLISH))
        assertEquals("×1µH", Multiplier.cellLabel(Black, Locale.ENGLISH))
        assertEquals("×10µH", Multiplier.cellLabel(Brown, Locale.ENGLISH))
        assertEquals("×100µH", Multiplier.cellLabel(Red, Locale.ENGLISH))
        assertEquals("×1mH", Multiplier.cellLabel(Orange, Locale.ENGLISH))
        assertEquals("×0,1µH", Multiplier.cellLabel(Gold, uk))
        assertEquals("±5%", Tolerance.cellLabel(Gold, Locale.ENGLISH))
        assertEquals("±10%", Tolerance.cellLabel(Silver, Locale.ENGLISH))
    }

    @Test
    fun colorsWithoutAMeaningHaveAnEmptyLabel() {
        assertEquals("", Multiplier.cellLabel(Green, Locale.ENGLISH))
        assertEquals("", Tolerance.cellLabel(Red, Locale.ENGLISH))
        assertEquals("", Digit.cellLabel(Gold, Locale.ENGLISH))
        assertEquals("", DecimalPoint.cellLabel(Silver, Locale.ENGLISH))
    }
}
