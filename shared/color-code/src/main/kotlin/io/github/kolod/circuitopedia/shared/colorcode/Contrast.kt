package io.github.kolod.circuitopedia.shared.colorcode

import kotlin.math.pow

/** How far a table cell's vertical gradient moves its band color toward white (top) and black (bottom). */
const val CELL_GRADIENT = 0.15

/** WCAG 2.x contrast ratio of two opaque sRGB colors, from 1 to 21. */
fun contrastRatio(argb1: Int, argb2: Int): Double {
    val l1 = relativeLuminance(argb1)
    val l2 = relativeLuminance(argb2)
    return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
}

/** [argb] moved toward white by [amount] (0..1). */
fun lighten(argb: Int, amount: Double): Int = mapChannels(argb) { it + (255 - it) * amount }

/** [argb] moved toward black by [amount] (0..1). */
fun darken(argb: Int, amount: Double): Int = mapChannels(argb) { it * (1 - amount) }

private fun relativeLuminance(argb: Int): Double {
    fun linear(channel: Int): Double {
        val c = channel / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * linear(argb shr 16 and 0xFF) + 0.7152 * linear(argb shr 8 and 0xFF) + 0.0722 * linear(argb and 0xFF)
}

private fun mapChannels(argb: Int, transform: (Int) -> Double): Int {
    fun channel(shift: Int) = transform(argb shr shift and 0xFF).toInt().coerceIn(0, 255) shl shift
    return (argb.toLong() and 0xFF000000L).toInt() or channel(16) or channel(8) or channel(0)
}
