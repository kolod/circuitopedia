package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.Percent
import java.math.BigDecimal

/*
 * The inductor color code for molded axial inductors, in microhenries.
 *
 * Source: Bourns, "Color Code Marking (MIL STD)" and "Color Code Marking (EIA STD)" (technical
 * library, inductive components, ColorCodeMarkings.pdf), which reproduce the MIL marking of
 * cylindrical RF choke coils (MIL-C-15305 / MIL-PRF-15305 family) and the EIA marking of molded
 * inductors. Tolerances cross-checked against a Vishay color chart.
 *
 * MIL: "When either the first or second band of the three bands is gold, this band shall represent
 * the decimal point for inductance values less than 10, and the other two bands shall represent
 * significant figures." EIA: Gold ×0.1 and Silver ×0.01 as multipliers instead.
 *
 * Uncited web tables also list tolerances Brown ±1% … Yellow ±4% and multipliers Yellow ×10k …
 * Blue ×1M; neither Bourns table has them, so they are not offered (spec 003 research R1).
 */

/** The significant figure of a digit band (Black 0 … White 9, in enum order), or null if the color has none. */
val BandColor.digit: Int?
    get() = if (this == Gold || this == Silver) null else ordinal

/** The power of ten (in µH) of a multiplier band, or null if the color has none. */
val BandColor.multiplierExponent: Int?
    get() = when (this) {
        Black -> 0
        Brown -> 1
        Red -> 2
        Orange -> 3
        Gold -> -1
        Silver -> -2
        else -> null
    }

/** The tolerance of a tolerance band, or null if the color has none. */
val BandColor.tolerance: Percent?
    get() = when (this) {
        Gold -> Percent(BigDecimal("5"))
        Silver -> Percent(BigDecimal("10"))
        else -> null
    }

/** Whether this color marks the decimal point when it is in Band 1 or Band 2 (MIL). */
val BandColor.isDecimalPoint: Boolean
    get() = this == Gold
