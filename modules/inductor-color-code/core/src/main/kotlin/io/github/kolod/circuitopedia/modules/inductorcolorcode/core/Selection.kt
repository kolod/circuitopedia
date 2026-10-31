package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import io.github.kolod.circuitopedia.shared.colorcode.Percent
import java.math.BigDecimal

/** The inductance and tolerance that a selection encodes. */
data class Reading(val inductance: Microhenries, val tolerance: Percent)

/**
 * One color per band of [layout], forming a valid marking (spec 003 research R3):
 * at most one decimal point (Gold in Band 1 or 2); every band's color has a meaning for its role;
 * and the first significant digit is not Black.
 */
data class Selection(val layout: BandLayout, val colors: List<BandColor>) {
    init {
        require(colors.size == layout.bandCount) { "$layout needs ${layout.bandCount} colors: $colors" }
        val point = pointIn(colors, preferred = null)
        colors.indices.forEach { require(isValid(colors, it, point)) { "${colors[it]} is not valid for band $it of $colors" } }
    }

    /** The band holding the decimal point (0 or 1), or null. */
    val pointIndex: Int? get() = pointIn(colors, preferred = null)

    fun roleOf(index: Int): BandRole = roleOf(index, pointIndex)

    /**
     * This selection with band [index] set to [color]. Every other band that is no longer valid gets
     * its default color, so a new point moves rather than doubles. Unchanged if [color] itself is not
     * valid there.
     */
    fun with(index: Int, color: BandColor): Selection {
        val next = colors.toMutableList().also { it[index] = color }
        val point = pointIn(next, preferred = index)
        if (!isValid(next, index, point)) return this
        val defaults = defaultColors(layout)
        for (other in next.indices) {
            if (other != index && !isValid(next, other, point)) next[other] = defaults[other]
        }
        return copy(colors = next)
    }

    /** Whether tapping [color] in band [index] would select it. */
    fun canSelect(index: Int, color: BandColor): Boolean = with(index, color).colors[index] == color

    /** Inductance in µH, computed exactly; without a tolerance band the tolerance is ±20%. */
    fun read(): Reading {
        val d = colors.map { it.digit }
        val microhenries = when (pointIndex) {
            null -> BigDecimal(d[0]!! * 10 + d[1]!!).scaleByPowerOfTen(colors[2].multiplierExponent!!)
            1 -> BigDecimal(d[0]!! * 10 + d[2]!!).movePointLeft(1)
            else -> BigDecimal(d[1]!! * 10 + d[2]!!).movePointLeft(2)
        }
        val tolerance = if (layout == BandLayout.Four) colors[3].tolerance!! else NO_BAND_TOLERANCE
        return Reading(Microhenries(microhenries), tolerance)
    }

    private fun roleOf(index: Int, point: Int?): BandRole = when (index) {
        0, 1 -> if (index == point) BandRole.DecimalPoint else BandRole.Digit
        2 -> if (point != null) BandRole.Digit else BandRole.Multiplier
        else -> BandRole.Tolerance
    }

    private fun isValid(colors: List<BandColor>, index: Int, point: Int?): Boolean {
        val color = colors[index]
        return when (roleOf(index, point)) {
            BandRole.DecimalPoint -> color.isDecimalPoint
            BandRole.Digit -> color.digit != null && !(index == firstSignificant(point) && color == Black)
            BandRole.Multiplier -> color.multiplierExponent != null
            BandRole.Tolerance -> color.tolerance != null
        }
    }

    companion object {
        /** A marking without a tolerance band has ±20% (Vishay color chart, research R1). */
        private val NO_BAND_TOLERANCE = Percent(BigDecimal(20))

        /** The defaults of spec FR-014; both read 47 µH. */
        fun default(layout: BandLayout): Selection = Selection(layout, defaultColors(layout))

        private fun defaultColors(layout: BandLayout) = listOf(Yellow, Violet, Black, Silver).take(layout.bandCount)

        /** The decimal point among Bands 1–2: the [preferred] band if it is Gold there, else the first Gold. */
        private fun pointIn(colors: List<BandColor>, preferred: Int?): Int? =
            preferred?.takeIf { it < 2 && colors[it].isDecimalPoint } ?: (0..1).firstOrNull { colors[it].isDecimalPoint }

        /** The band holding the first significant digit: Band 2 if the point is in Band 1, else Band 1. */
        private fun firstSignificant(point: Int?) = if (point == 0) 1 else 0
    }
}
