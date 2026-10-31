# Data Model: Voltage Divider Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

## Quantity (enum, divider core)

`U1, U2, R1, R2, R`. Each has a unit, V or Ω. The ordinals are saved UI state, so new entries may
only be appended.

## Solve (divider core)

`solve(entered: Map<Quantity, Rational>): SolveResult`

| Result | When | Contains |
|--------|------|----------|
| `Solved(values: Map<Quantity, Rational>)` | valid | all five values; the two computed ones are exact |
| `NotEnough(missing: Int)` | fewer than 3 entered | — |
| `TooMany(extra: Int)` | more than 3 entered | — |
| `NoVoltage` | exactly {R1, R2, R} | — |
| `Invalid(reason)` | rules of research R1 fail | reason ∈ {`RatioOutOfRange`, `MixedSigns`, `ResistanceNotBelowTotal`} |

`canCalculate(entered)` is true iff `solve` would not return `NotEnough`, `TooMany` or `NoVoltage`.
The Calculate button uses it (FR-004). Range checks happen in the parsers, so an out-of-range field
counts as not parsed.

## Voltage text

`parseVoltage(text): Rational?`:
- accepts an optional sign, "." or ",", an optional prefix m or k, and an optional "V";
- the magnitude must be within 1mV … 100kV and non-zero;
- returns null otherwise.

`formatVolts(value, locale)`: the sign, then 4 significant digits with the prefix m, none or k.

## Divider build (divider core)

| Field | Type | Notes |
|-------|------|-------|
| `r1` | `Combination` | Single, Series2 or Parallel2 from the series, or Single(entered value) when R1 was entered |
| `r2` | `Combination` | same |
| `chosenCount` | Int | resistors chosen by the search (entered arms count 0) |
| `u2` | Rational | `U1 · r2 / (r1 + r2)` |
| `u2Error` | Rational | `(u2 − U2t) / U2t`, signed |
| `total` | Rational | `r1 + r2`, within [0.9·Rt, 1.1·Rt] |
| `totalDeviation` | Rational | `(total − Rt) / Rt`, signed |

Ranking (FR-010):
1. |u2Error|, ascending;
2. chosenCount;
3. |totalDeviation|;
4. r1's values, then r2's values, lexicographically.

`builds(solved, series): List<DividerBuild>`:
- empty if neither R1 nor R2 was computed;
- otherwise, the best 5 of each chosenCount tier, merged and sorted.

## UI state (module)

| Field | Saved as | Default |
|-------|----------|---------|
| field texts | 5 × String | `""` |
| origins | 5 × ordinal (Empty, Entered, Computed) | Empty |
| series | ordinal | E24 |

The computed texts are saved as shown. Builds are derived and never saved.
