package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Black
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Brown
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Gold
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Red
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Violet
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Yellow
import java.math.BigDecimal

/** The resistance, tolerance and (6 bands only) temperature coefficient that a selection encodes. */
data class Reading(val resistance: Ohms, val tolerance: Percent, val tcr: PpmPerKelvin?)

/** One color per band of [layout]; every color is valid for its band. */
data class Selection(val layout: BandLayout, val colors: List<BandColor>) {
    init {
        require(colors.size == layout.bandCount) { "$layout needs ${layout.bandCount} colors: $colors" }
        colors.forEachIndexed { i, color -> require(layout.accepts(i, color)) { "$color is not valid for band $i of $layout" } }
    }

    /** This selection with band [index] set to [color], or unchanged if [color] is not valid there. */
    fun with(index: Int, color: BandColor): Selection =
        if (layout.accepts(index, color)) copy(colors = colors.toMutableList().also { it[index] = color }) else this

    /** Resistance = digit bands as a decimal integer × 10^multiplier exponent, computed exactly. */
    fun read(): Reading {
        var digits = 0
        var exponent = 0
        var tolerance = NO_BAND_TOLERANCE
        var tcr: PpmPerKelvin? = null
        layout.roles.zip(colors).forEach { (role, color) ->
            when (role) {
                BandRole.Digit -> digits = digits * 10 + color.digit!!
                BandRole.Multiplier -> exponent = color.multiplierExponent!!
                BandRole.Tolerance -> tolerance = color.tolerance!!
                BandRole.TemperatureCoefficient -> tcr = color.tcr
            }
        }
        return Reading(Ohms(BigDecimal(digits).scaleByPowerOfTen(exponent)), tolerance, tcr)
    }

    companion object {
        /** IEC 60062: a resistor without a tolerance band has ±20%. */
        private val NO_BAND_TOLERANCE = Percent(BigDecimal(20))

        /** The defaults of spec FR-014; all of them read 4.7 kΩ. */
        fun default(layout: BandLayout): Selection = Selection(
            layout,
            when (layout) {
                BandLayout.Three -> listOf(Yellow, Violet, Red)
                BandLayout.Four -> listOf(Yellow, Violet, Red, Gold)
                BandLayout.Five -> listOf(Yellow, Violet, Black, Brown, Brown)
                BandLayout.Six -> listOf(Yellow, Violet, Black, Brown, Brown, Brown)
            },
        )
    }
}
