package io.github.kolod.circuitopedia.shared.colorcode

/** Whether text on a band color is drawn light (white) or dark (black). */
enum class TextOnColor { Light, Dark }

/**
 * The 12 colors of the electronic color code, as displayed. What a color means depends on the
 * code that uses it (resistors: IEC 60062; inductors: MIL/EIA), so meanings live in each module.
 *
 * [argb] is a practical display approximation: the standards name colors but define no RGB values.
 * [textOnColor] gives at least 4.5:1 contrast across the cell gradient (PaletteContrastTest).
 *
 * The ordinals are stored in saved UI state, so entries may only be appended.
 */
enum class BandColor(val argb: Int, val textOnColor: TextOnColor) {
    Black(0xFF212121.toInt(), TextOnColor.Light),
    Brown(0xFF8B4A3C.toInt(), TextOnColor.Light),
    Red(0xFFC01818.toInt(), TextOnColor.Light),
    Orange(0xFFF2861E.toInt(), TextOnColor.Dark),
    Yellow(0xFFF5E83A.toInt(), TextOnColor.Dark),
    Green(0xFF2EA83A.toInt(), TextOnColor.Dark),
    Blue(0xFF1A55B8.toInt(), TextOnColor.Light),
    Violet(0xFF8236AA.toInt(), TextOnColor.Light),
    Gray(0xFF585858.toInt(), TextOnColor.Light),
    White(0xFFF4F4F4.toInt(), TextOnColor.Dark),
    Gold(0xFFD4A62A.toInt(), TextOnColor.Dark),
    Silver(0xFFBDBDBD.toInt(), TextOnColor.Dark),
}
