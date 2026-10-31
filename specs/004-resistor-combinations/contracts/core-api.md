# Contract: Core API (normative)

Pure Kotlin, no `android.*` imports.

## `:shared:si-format` — package `io.github.kolod.circuitopedia.shared.siformat`

Moved unchanged from `:shared:color-code` (spec 003 contract):

```kotlin
fun formatSi(value: BigDecimal, unit: String, prefixes: List<String>, baseIndex: Int, locale: Locale): String
fun formatDecimal(value: BigDecimal, locale: Locale): String
```

## `:modules:resistor-combinations:core` — package `io.github.kolod.circuitopedia.modules.resistorcombinations.core`

```kotlin
class Rational : Comparable<Rational> {          // exact, reduced, any sign (deviations)
    operator fun plus(other: Rational): Rational
    operator fun minus(other: Rational): Rational; operator fun times(…); operator fun div(…)
    fun reciprocal(): Rational; fun abs(): Rational
    fun toDouble(): Double
    fun round(significantDigits: Int): BigDecimal   // HALF_UP
    companion object { fun of(value: BigDecimal): Rational }
}

enum class ESeries { E3, E6, E12, E24, E48, E96, E192;
    val base: List<Int>                          // hundredths, IEC 60063 (research R1)
    fun values(): List<Rational>                 // 1Ω … 10MΩ, ascending
}

enum class Kind(val resistorCount: Int) { Single(1), Series2(2), Series3(3), Parallel2(2), Parallel3(3), ParallelSeries(3) }

data class Combination(val kind: Kind, val values: List<Rational>) {
    val resistance: Rational
    fun deviation(target: Rational): Rational    // signed, exact
}

fun rankingFor(target: Rational): Comparator<Combination>   // FR-007
const val PER_KIND = 5

/** The 5 best of Single and of each allowed kind, merged and sorted (FR-006–FR-008). */
fun search(target: Rational, series: ESeries, allowed: Set<Kind>): List<Combination>

/** FR-002 / research R5: null if invalid or outside [0.1Ω, 1GΩ]. */
fun parseTarget(text: String): Rational?

// Format.kt (FR-009)
fun Combination.connectionText(locale: Locale): String   // "4.7kΩ ∥ 10kΩ + 220Ω"
fun Rational.formatOhms(locale: Locale): String            // 4 significant digits: "3.197kΩ"
fun formatDeviation(deviation: Rational, locale: Locale): String   // "+0.0812%", "−1.23%", "0%"
```

Test-only (in `src/test`): `referenceSearch(...)`, the exhaustive exact search used by
`SearchReferenceTest`.
