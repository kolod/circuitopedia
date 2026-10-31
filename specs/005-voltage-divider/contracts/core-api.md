# Contract: Core API (normative)

Pure Kotlin, no `android.*` imports.

## `:shared:resistors` — package `io.github.kolod.circuitopedia.shared.resistors`

Moved from `:modules:resistor-combinations:core` with unchanged signatures (spec 004
`contracts/core-api.md`):
- `Rational`;
- `ESeries`;
- `Kind`;
- `Combination` (with `resistance` and `deviation`);
- `Combination.connectionText`;
- `Rational.formatOhms`;
- `formatDeviation`.

The only renamed function is `parseTarget`, which becomes `parseResistance`.

```kotlin
fun parseResistance(text: String): Rational?    // FR-002 of spec 004; 0.1Ω … 1GΩ
```

The combinations module keeps `search`, `rankingFor` and `PER_KIND`.

## `:modules:voltage-divider:core` — package `io.github.kolod.circuitopedia.modules.voltagedivider.core`

```kotlin
enum class Quantity { U1, U2, R1, R2, R }

sealed interface SolveResult {
    data class Solved(val values: Map<Quantity, Rational>, val computed: Set<Quantity>) : SolveResult
    data class NotEnough(val missing: Int) : SolveResult
    data class TooMany(val extra: Int) : SolveResult
    data object NoVoltage : SolveResult
    data class Invalid(val reason: Reason) : SolveResult
    enum class Reason { RatioOutOfRange, MixedSigns, ResistanceNotBelowTotal }
}

fun solve(entered: Map<Quantity, Rational>): SolveResult
fun canCalculate(entered: Map<Quantity, Rational>): Boolean

fun parseVoltage(text: String): Rational?                   // 1mV … 100kV in magnitude, sign allowed
fun formatVolts(value: Rational, locale: Locale): String    // "3.837V", "−12V", "4.7mV"

data class DividerBuild(
    val r1: Combination, val r2: Combination, val chosenCount: Int,
    val u2: Rational, val u2Error: Rational, val total: Rational, val totalDeviation: Rational,
)

/** FR-009–FR-011: empty unless R1 or R2 was computed. [checkCancelled] as in spec 004. */
fun builds(solved: SolveResult.Solved, series: ESeries, checkCancelled: () -> Unit = {}): List<DividerBuild>
internal fun buildEvaluations(solved: SolveResult.Solved, series: ESeries): Long   // budget test
```

Test-only: `referenceBuilds(solved, series)`, the exhaustive search over all arm pairs in the window.
