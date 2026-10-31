package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

/** What a band means. Band 1 or 2 may be the decimal point, which turns Band 3 into a digit. */
enum class BandRole { Digit, DecimalPoint, Multiplier, Tolerance }

/** The band count of an inductor. A 3-band inductor has no tolerance band (±20%). */
enum class BandLayout(val bandCount: Int) {
    Three(3),
    Four(4),
}
