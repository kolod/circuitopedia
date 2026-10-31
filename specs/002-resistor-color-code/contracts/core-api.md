# Contract: `:modules:resistor-color-code:core` public API

**Feature**: [../spec.md](../spec.md)

This is the pure Kotlin/JVM API that the Android module consumes. The signatures are normative.
Package: `io.github.kolod.circuitopedia.modules.resistorcolorcode.core`.

```kotlin
@JvmInline value class Ohms(val value: BigDecimal)
@JvmInline value class Percent(val value: BigDecimal)
@JvmInline value class PpmPerKelvin(val value: Int)

enum class TextOnColor { Light, Dark }

enum class BandColor(
    val digit: Int?,
    val multiplierExponent: Int?,
    val tolerance: Percent?,
    val tcr: PpmPerKelvin?,
    val argb: Int,
    val textOnColor: TextOnColor,
) { Black, Brown, Red, Orange, Yellow, Green, Blue, Violet, Gray, White, Gold, Silver }

enum class BandRole { Digit, Multiplier, Tolerance, TemperatureCoefficient }

enum class BandLayout(val roles: List<BandRole>) {
    Three, Four, Five, Six;
    val bandCount: Int
}

/** Whether [color] may be chosen for band [index] of [layout] (Black is not a first digit). */
fun BandLayout.accepts(index: Int, color: BandColor): Boolean

class Selection(val layout: BandLayout, val colors: List<BandColor>) {
    fun with(index: Int, color: BandColor): Selection
    fun read(): Reading
    companion object { fun default(layout: BandLayout): Selection }
}

data class Reading(val resistance: Ohms, val tolerance: Percent, val tcr: PpmPerKelvin?)

/** "4.7kΩ ±5%", "4,7kΩ ±1% ±100ppm/K", using the decimal separator of [locale]. */
fun Reading.format(locale: Locale): String

/** Text of a table cell: "4", "×100Ω", "×0,1Ω", "±5%", "±100ppm/K". */
fun BandRole.cellLabel(color: BandColor, locale: Locale): String

/** WCAG 2.x contrast ratio of two opaque sRGB colors. */
fun contrastRatio(argb1: Int, argb2: Int): Double
```

Guarantees:

- `read()` is exact. It does no binary floating point.
- `format` never emits scientific notation, grouping separators or trailing fractional zeros.
- Nothing in this API imports `android.*`.
