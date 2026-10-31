package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Blue
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gray
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Green
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.White
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import io.github.kolod.circuitopedia.shared.colorcode.Percent
import java.math.BigDecimal

/*
 * The resistor color code.
 *
 * Source: IEC 60062:2016, *Marking codes for resistors and capacitors*, §5 and Tables 1–3
 * (digits, multipliers, tolerances, temperature coefficients), cross-checked against Wikipedia,
 * "Electronic color code", section "Resistors" (retrieved 2026-10-31). Before the 2016 edition
 * some manufacturers (EIA RS-279, Yageo datasheets) used Gray for ±0.05%, and Yellow/Gray in
 * place of Gold/Silver on high-voltage parts; this table follows the current IEC edition.
 */

/** The significant figure of a digit band (Black 0 … White 9, in enum order), or null if the color has none. */
val BandColor.digit: Int?
    get() = if (this == Gold || this == Silver) null else ordinal

/** The power of ten of a multiplier band: Black 0 … White 9, Gold −1, Silver −2. Every color has one. */
val BandColor.multiplierExponent: Int?
    get() = when (this) {
        Gold -> -1
        Silver -> -2
        else -> ordinal
    }

/** The tolerance of a tolerance band, or null if the color has none. */
val BandColor.tolerance: Percent?
    get() = when (this) {
        Brown -> percent("1")
        Red -> percent("2")
        Orange -> percent("0.05")
        Yellow -> percent("0.02")
        Green -> percent("0.5")
        Blue -> percent("0.25")
        Violet -> percent("0.1")
        Gray -> percent("0.01")
        Gold -> percent("5")
        Silver -> percent("10")
        Black, White -> null
    }

/** The temperature coefficient of a TCR band, or null if the color has none. */
val BandColor.tcr: PpmPerKelvin?
    get() = when (this) {
        Black -> PpmPerKelvin(250)
        Brown -> PpmPerKelvin(100)
        Red -> PpmPerKelvin(50)
        Orange -> PpmPerKelvin(15)
        Yellow -> PpmPerKelvin(25)
        Green -> PpmPerKelvin(20)
        Blue -> PpmPerKelvin(10)
        Violet -> PpmPerKelvin(5)
        Gray -> PpmPerKelvin(1)
        White, Gold, Silver -> null
    }

private fun percent(value: String) = Percent(BigDecimal(value))
