package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import java.math.BigDecimal

/** Whether text on a band color is drawn light (white) or dark (black). */
enum class TextOnColor { Light, Dark }

/**
 * The resistor color code.
 *
 * Source: IEC 60062:2016, *Marking codes for resistors and capacitors*, §5 and Tables 1–3
 * (digits, multipliers, tolerances, temperature coefficients), cross-checked against Wikipedia,
 * "Electronic color code", section "Resistors" (retrieved 2026-10-31). Before the 2016 edition
 * some manufacturers (EIA RS-279, Yageo datasheets) used Gray for ±0.05%, and Yellow/Gray in
 * place of Gold/Silver on high-voltage parts; this table follows the current IEC edition.
 *
 * [argb] is a practical display approximation: IEC 60062 names colors but defines no RGB values.
 * [textOnColor] gives at least 4.5:1 contrast across the cell gradient (PaletteContrastTest).
 *
 * The ordinals are stored in saved UI state, so entries may only be appended.
 */
enum class BandColor(
    val digit: Int?,
    val multiplierExponent: Int?,
    val tolerance: Percent?,
    val tcr: PpmPerKelvin?,
    val argb: Int,
    val textOnColor: TextOnColor,
) {
    Black(0, 0, null, ppm(250), 0xFF212121.toInt(), TextOnColor.Light),
    Brown(1, 1, percent("1"), ppm(100), 0xFF8B4A3C.toInt(), TextOnColor.Light),
    Red(2, 2, percent("2"), ppm(50), 0xFFC01818.toInt(), TextOnColor.Light),
    Orange(3, 3, percent("0.05"), ppm(15), 0xFFF2861E.toInt(), TextOnColor.Dark),
    Yellow(4, 4, percent("0.02"), ppm(25), 0xFFF5E83A.toInt(), TextOnColor.Dark),
    Green(5, 5, percent("0.5"), ppm(20), 0xFF2EA83A.toInt(), TextOnColor.Dark),
    Blue(6, 6, percent("0.25"), ppm(10), 0xFF1A55B8.toInt(), TextOnColor.Light),
    Violet(7, 7, percent("0.1"), ppm(5), 0xFF8236AA.toInt(), TextOnColor.Light),
    Gray(8, 8, percent("0.01"), ppm(1), 0xFF585858.toInt(), TextOnColor.Light),
    White(9, 9, null, null, 0xFFF4F4F4.toInt(), TextOnColor.Dark),
    Gold(null, -1, percent("5"), null, 0xFFD4A62A.toInt(), TextOnColor.Dark),
    Silver(null, -2, percent("10"), null, 0xFFBDBDBD.toInt(), TextOnColor.Dark),
}

private fun percent(value: String) = Percent(BigDecimal(value))

private fun ppm(value: Int) = PpmPerKelvin(value)
