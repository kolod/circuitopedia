# Contract: Core API (normative)

Pure Kotlin, no `android.*` imports.

## `:shared:color-code` — package `io.github.kolod.circuitopedia.shared.colorcode`

```kotlin
enum class TextOnColor { Light, Dark }
enum class BandColor(val argb: Int, val textOnColor: TextOnColor) { Black, Brown, Red, Orange,
    Yellow, Green, Blue, Violet, Gray, White, Gold, Silver }   // order is saved state: append-only

@JvmInline value class Percent(val value: BigDecimal)          // > 0

const val CELL_GRADIENT: Double
fun contrastRatio(argb1: Int, argb2: Int): Double
fun lighten(argb: Int, amount: Double): Int
fun darken(argb: Int, amount: Double): Int

/** Plain digits, no trailing zeros, exponent or grouping, locale decimal separator. */
fun formatDecimal(value: BigDecimal, locale: Locale): String

/**
 * value × 1000^k with the prefix that puts the mantissa in [1, 1000), clamped to the given
 * prefixes. [baseIndex] is the index of the prefix for exponent 0 in [prefixes].
 * Resistor: formatSi(ohms, "Ω", listOf("", "k", "M", "G"), 0, locale)   → "0.1Ω", "4.7kΩ"
 * Inductor: formatSi(µH, "H", listOf("n", "µ", "m"), 1, locale)         → "100nH", "99mH"
 */
fun formatSi(value: BigDecimal, unit: String, prefixes: List<String>, baseIndex: Int, locale: Locale): String
```

Test fixture (`testFixtures`):

```kotlin
/** Problems with a band drawing against the SVG contract of spec 002 R10; empty when it conforms. */
object SvgContract { fun check(svg: File, bandCount: Int): List<String> }
```

## `:modules:resistor-color-code:core` (refactor)

The public API of spec 002 `contracts/core-api.md` is unchanged, except that `BandColor`,
`TextOnColor`, `Percent` and the contrast functions now come from `:shared:color-code` (exposed as
`api`). The IEC meanings become extensions in `ResistorCode.kt`: `BandColor.digit`,
`.multiplierExponent`, `.tolerance`, `.tcr`.

## `:modules:inductor-color-code:core` — package `io.github.kolod.circuitopedia.modules.inductorcolorcode.core`

```kotlin
@JvmInline value class Microhenries(val value: BigDecimal)     // > 0

// InductorCode.kt — Bourns MIL/EIA (research R1)
val BandColor.digit: Int?
val BandColor.multiplierExponent: Int?
val BandColor.tolerance: Percent?
val BandColor.isDecimalPoint: Boolean

enum class BandRole { Digit, DecimalPoint, Multiplier, Tolerance }
enum class BandLayout(val bandCount: Int) { Three(3), Four(4) }

data class Reading(val inductance: Microhenries, val tolerance: Percent)

data class Selection(val layout: BandLayout, val colors: List<BandColor>) {  // init: require valid
    val pointIndex: Int?
    fun roleOf(index: Int): BandRole
    fun with(index: Int, color: BandColor): Selection   // data-model "Transitions"
    fun canSelect(index: Int, color: BandColor): Boolean
    fun read(): Reading
    companion object { fun default(layout: BandLayout): Selection }
}

fun Reading.format(locale: Locale): String                 // "47µH ±10%", "4,7µH ±5%", "220nH ±20%"
fun BandRole.cellLabel(color: BandColor, locale: Locale): String   // "4", ".", "×1mH", "±5%", or ""
```
