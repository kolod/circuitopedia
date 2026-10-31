package io.github.kolod.circuitopedia.modules.voltagedivider.core

/**
 * The five quantities of a divider U1 — R1 — U2 — R2 — GND: input and output voltage, upper and
 * lower resistor, and the total resistance. Ordinals are saved UI state, so entries may only be appended.
 */
enum class Quantity(val isVoltage: Boolean) {
    U1(true),
    U2(true),
    R1(false),
    R2(false),
    R(false),
}
