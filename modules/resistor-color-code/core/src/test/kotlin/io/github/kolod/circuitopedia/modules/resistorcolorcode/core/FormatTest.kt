package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Blue
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Green
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.White
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Digit
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Multiplier
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.TemperatureCoefficient
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Tolerance
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    private val en = Locale.ENGLISH

    private fun format(layout: BandLayout, vararg colors: BandColor, locale: Locale = en) =
        Selection(layout, colors.toList()).read().format(locale)

    // US1 (English, 4 bands)

    @Test
    fun fourBandValues() {
        assertEquals("4.7kΩ ±5%", format(BandLayout.Four, Yellow, Violet, Red, Gold))
        assertEquals("10kΩ ±1%", format(BandLayout.Four, Brown, Black, Orange, Brown))
        assertEquals("2.2Ω ±5%", format(BandLayout.Four, Red, Red, Gold, Gold))
        assertEquals("0.22Ω ±10%", format(BandLayout.Four, Red, Red, Silver, Silver))
        assertEquals("0.1Ω ±5%", format(BandLayout.Four, Brown, Black, Silver, Gold))
        assertEquals("1kΩ ±5%", format(BandLayout.Four, Brown, Black, Red, Gold))
        assertEquals("100Ω ±5%", format(BandLayout.Four, Brown, Black, Brown, Gold))
        assertEquals("99GΩ ±1%", format(BandLayout.Four, White, White, White, Brown))
        assertEquals("470kΩ ±0.05%", format(BandLayout.Four, Yellow, Violet, Yellow, Orange))
    }

    @Test
    fun cellLabels() {
        assertEquals("4", Digit.cellLabel(Yellow, en))
        assertEquals("0", Digit.cellLabel(Black, en))
        val multipliers = mapOf(
            Black to "×1Ω", Brown to "×10Ω", Red to "×100Ω", Orange to "×1kΩ", Yellow to "×10kΩ",
            Green to "×100kΩ", Blue to "×1MΩ", Violet to "×10MΩ", BandColor.Gray to "×100MΩ",
            White to "×1GΩ", Gold to "×0.1Ω", Silver to "×0.01Ω",
        )
        for ((color, label) in multipliers) assertEquals(label, Multiplier.cellLabel(color, en))
        assertEquals("±5%", Tolerance.cellLabel(Gold, en))
        assertEquals("±0.25%", Tolerance.cellLabel(Blue, en))
        assertEquals("±100ppm/K", TemperatureCoefficient.cellLabel(Brown, en))
        assertEquals("", Digit.cellLabel(Gold, en))
        assertEquals("", Tolerance.cellLabel(Black, en))
    }

    // US2 (other layouts)

    @Test
    fun otherLayouts() {
        assertEquals("4.7kΩ ±20%", format(BandLayout.Three, Yellow, Violet, Red))
        assertEquals("121Ω ±1%", format(BandLayout.Five, Brown, Red, Brown, Black, Brown))
        assertEquals("4.75kΩ ±1%", format(BandLayout.Five, Yellow, Violet, Green, Brown, Brown))
        assertEquals("999GΩ ±1%", format(BandLayout.Five, White, White, White, White, Brown))
        assertEquals("1kΩ ±1%", format(BandLayout.Five, Brown, Black, Black, Brown, Brown))
        assertEquals("4.7kΩ ±1% ±100ppm/K", format(BandLayout.Six, Yellow, Violet, Black, Brown, Brown, Brown))
    }

    // US3 (locales)

    @Test
    fun decimalSeparatorFollowsTheLocale() {
        val uk = Locale.forLanguageTag("uk")
        assertEquals("4,7kΩ ±5%", format(BandLayout.Four, Yellow, Violet, Red, Gold, locale = uk))
        assertEquals("0,22Ω ±10%", format(BandLayout.Four, Red, Red, Silver, Silver, locale = uk))
        assertEquals("×0,1Ω", Multiplier.cellLabel(Gold, uk))
        assertEquals("±0,25%", Tolerance.cellLabel(Blue, uk))
        for (tag in listOf("de", "fr", "es")) {
            assertEquals(tag, "4,7kΩ ±5%", format(BandLayout.Four, Yellow, Violet, Red, Gold, locale = Locale.forLanguageTag(tag)))
        }
        for (tag in listOf("en", "ja")) {
            assertEquals(tag, "4.7kΩ ±5%", format(BandLayout.Four, Yellow, Violet, Red, Gold, locale = Locale.forLanguageTag(tag)))
        }
    }
}
