# Data Model: Resistor Combination Finder

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

All types are in `:modules:resistor-combinations:core`, which is pure Kotlin.

## Rational

An exact positive rational number of ohms. Fields `num: BigInteger` and `den: BigInteger`, with
`den > 0`, `num > 0`, kept reduced.

Operations: `+`, `reciprocal()`, `compareTo`, `toDouble()`, and
`toBigDecimal(significantDigits, HALF_UP)`.

## ESeries (enum)

`E3, E6, E12, E24, E48, E96, E192`. The field `base: List<Int>` holds the base values in
hundredths (100 … 988), taken from research R1.

`values(): List<Rational>` returns, sorted ascending, `base[i] / 100 × 10^k` for k = 0 … 6, plus
10MΩ. The result is cached per entry.

## Kind (enum)

The entries are in UI order. Their ordinals are saved UI state, so new entries may only be
appended.

| Entry | Resistors | Canonical order | Formula |
|-------|-----------|-----------------|---------|
| `Single` | 1 | — | a |
| `Series2` | 2 | a ≤ b | a + b |
| `Series3` | 3 | a ≤ b ≤ c | a + b + c |
| `Parallel2` | 2 | a ≤ b | a∥b |
| `Parallel3` | 3 | a ≤ b ≤ c | a∥b∥c |
| `ParallelSeries` | 3 | a ≤ b, c free | (a∥b) + c |

`Single` is always searched (FR-005).

## Combination

Fields:
- `kind: Kind`;
- `values: List<Rational>`, in canonical order (for `ParallelSeries`: a, b, then c).

Derived:
- `resistance: Rational`, from the kind's formula;
- `deviation(target): Rational`, equal to `(resistance − target) / target` and signed.

## Ranking (FR-007)

Results are compared by these keys in order:

1. `|deviation|`, ascending (exact);
2. resistor count, ascending;
3. largest value, ascending;
4. the values in canonical order, compared lexicographically, ascending.

The last key only makes the order deterministic.

## Search

`search(target: Rational, series: ESeries, allowed: Set<Kind>): List<Combination>`

- For `Single` and each allowed kind, take the 5 best combinations of that kind under the
  ranking. Merge them and sort by the ranking. The result has at most 30 entries (FR-008).
- Within a kind, no combination left out may rank before the kind's last listed one.

## Target (parser result)

`parseTarget(text: String): Rational?` returns `null` for invalid input or a value outside
[0.1Ω, 1GΩ] (research R5).

## UI state (module)

| Field | Saved as | Default |
|-------|----------|---------|
| target text | `String` | `""` |
| series | ordinal | `E24` |
| allowed kinds | ordinals of the allowed non-single kinds | all |

Results are derived and never saved.
