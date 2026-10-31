package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Digit
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Multiplier
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.TemperatureCoefficient
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Tolerance

enum class BandRole { Digit, Multiplier, Tolerance, TemperatureCoefficient }

/** The band sequence of a resistor, read left to right. A 3-band resistor has no tolerance band (±20%). */
enum class BandLayout(val roles: List<BandRole>) {
    Three(listOf(Digit, Digit, Multiplier)),
    Four(listOf(Digit, Digit, Multiplier, Tolerance)),
    Five(listOf(Digit, Digit, Digit, Multiplier, Tolerance)),
    Six(listOf(Digit, Digit, Digit, Multiplier, Tolerance, TemperatureCoefficient));

    val bandCount: Int get() = roles.size
}

/** Whether [color] may be chosen for band [index] of this layout. Black is never the first digit. */
fun BandLayout.accepts(index: Int, color: BandColor): Boolean {
    val role = roles.getOrNull(index) ?: return false
    if (index == 0 && color == BandColor.Black) return false
    return role.meaningOf(color) != null
}

internal fun BandRole.meaningOf(color: BandColor): Any? = when (this) {
    Digit -> color.digit
    Multiplier -> color.multiplierExponent
    Tolerance -> color.tolerance
    TemperatureCoefficient -> color.tcr
}
