# Research: Resistor Combination Finder

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

## R1. E-series values (Principle II)

- **Decision**: The base values of one decade follow **IEC 60063:2015** (3rd edition), as reproduced
  in Wikipedia, "E series of preferred numbers" (retrieved 2026-10-31). They are stored as integers
  in hundredths (100 = 1.00), and the code cites both sources.
  - E3: 1.0 2.2 4.7.
  - E6: E3 plus 1.5 3.3 6.8.
  - E12: E6 plus 1.2 1.8 2.7 3.9 5.6 8.2.
  - E24: E12 plus 1.1 1.3 1.6 2.0 2.4 3.0 3.6 4.3 5.1 6.2 7.5 9.1.
  - E48, E96 and E192 have three significant digits. Their full lists are copied literally into
    `ESeries.kt`, 48, 96 and 192 values.
- **Irregularities to keep**, each pinned by a test:
  - Eight E24 values (2.7, 3.0, 3.3, 3.6, 3.9, 4.3, 4.7, 8.2) differ from the rounded geometric
    series. They are kept for historical reasons.
  - E192 n = 185 is **9.20**, not the calculated 9.19.
  - E24 is **not** a subset of E48: 1.5 is missing, and E48 has 1.47 and 1.54. The E3 ⊂ E6 ⊂ E12 ⊂
    E24 chain and the E48 ⊂ E96 ⊂ E192 chain both hold.
- **Available resistors (FR-004)**: every base value × 10^k for k = 0 … 6, plus 10MΩ. That is
  1Ω … 9.88MΩ, then 10MΩ. E192 gives 7 × 192 + 1 = 1,345 values, and E24 gives 169.
- **Alternatives considered**: computing the series from the geometric formula. It gets E24's
  eight irregular values and E192's 9.20 wrong.

## R2. Circuit laws

- **Decision**: Series `R = ΣRᵢ` and parallel `1/R = Σ1/Rᵢ`. These follow from Ohm's law and
  Kirchhoff's laws. Source: P. Horowitz and W. Hill, *The Art of Electronics*, 3rd ed., Cambridge University Press,
  2015, §1.2 (resistors in series and parallel).
  The code comment cites it.

## R3. Exact arithmetic

- **Decision**: Resistances are exact **rationals** in ohms: a `BigInteger` numerator and
  denominator, reduced. Series sums and single values are integers in hundredths of an ohm. A
  parallel result is a ratio. Deviations are compared exactly by cross-multiplication. Display
  rounds HALF_UP to 4 significant digits for resistance and 3 for deviation (FR-009).
- **Search in `Double`, rank exactly**: the search below uses `Double` keys, whose relative error
  is about 1e-15. It keeps every candidate whose `Double` key is within a relative margin of 1e-9
  of the current 5th best of its kind. Only those candidates are ranked with exact rationals. No
  candidate that belongs in the top 5 can be dropped, because the margin is about a million times
  the floating-point error. Exact ties, such as several exact matches, are kept as well.
- **Alternatives considered**:
  - `Long` arithmetic: three-resistor parallel products reach about 1e27 and overflow.
  - `BigDecimal` with a fixed scale: still not exact for 1/3-type results.

## R4. Search algorithm (SC-002: E192, every kind, under 1 s)

Notation: V is the sorted list of available values (n ≤ 1,345) and T is the target. Each kind
keeps the best 5 candidates under FR-007, plus the ties described in R3. To avoid permutation
duplicates (FR-006), every enumeration uses the canonical order a ≤ b ≤ c.

| Kind | Enumerate | Ideal last value | Cost (E192) |
|------|-----------|------------------|-------------|
| Single | — | T | binary search, O(log n) |
| 2 series a≤b | a | b = T − a | O(n log n) |
| 2 parallel a≤b | a > T | b = 1/(1/T − 1/a) | O(n log n) |
| 3 series a≤b≤c | a ≤ b | c = T − a − b | ≈ 0.9 M × log n |
| 3 parallel a≤b≤c | a ≤ b, 1/a + 1/b < 1/T | c = 1/(1/T − 1/a − 1/b) | ≈ 0.9 M × log n |
| (a∥b) + c, a≤b | a ≤ b, a∥b < T; c free | c = T − a∥b | ≈ 0.9 M × log n |

- For each prefix, binary-search the position of the ideal last value in V, restricted to c ≥ b
  where order matters for duplicates. Then walk outward from that position on both sides while
  the deviation can still beat the kind's current 5th best. Deviation grows away from the ideal,
  because the result is monotonic in the last value for both series and parallel, so the walk
  stops after a few steps.
- Prefixes that can no longer help are cut off early. In series kinds, once a + b ≥ T + (current
  bound), larger b only get worse. In parallel kinds, once 1/a + 1/b ≥ 1/T, larger b only get
  worse. The `(a∥b) + c` kind with c free is the costliest; it is the reason for a measured
  budget, not a guess.
- **Two lessons from implementation**, both measured:
  1. Prefixes far from the target (a tiny `a` in the parallel kinds) walked every `c` while the bound
     was still loose, which made the search O(n³). A first pass now tries only the two values next
     to each ideal. Those are real combinations, so the pass yields a safe initial bound, and the full
     walk starts from it.
  2. For targets near the ends of the range, almost every prefix improves the top 5. Re-sorting on
     every acceptance cost seconds (E192, 30MΩ, `Parallel3`: 4.3 s). The collector now keeps the
     best 5 keys in a small sorted array and compacts its candidate list only when the list has
     doubled. After both fixes: median 47 ms, maximum 85 ms over the 20 benchmark targets.
  3. On Android, a local wrapper function around the inline `walk` kept its lambdas from being
     inlined, which cost about 1.8 M allocations per search. HotSpot removes them, ART does not. The
     wrapper is gone. Measured in-process on the API 36 emulator with a debug build, the slowest of
     8 E192 targets took 319 ms (SC-002: 1 s).
- **Cancellation**: `search` takes a `checkCancelled` callback, called once per first resistor. The
  UI passes the coroutine's `ensureActive`, so the search for an outdated input stops instead of
  competing with the new one while the user types.
- **Budget**: a JVM benchmark test runs E192 with every kind allowed over 20 targets and fails if
  the median exceeds 150 ms or any target exceeds 400 ms. Mid-range phones are about 3–5 times slower than a desktop JVM, which
  leaves SC-002 at 1 s with margin.
- **Correctness (SC-003)**: a JVM test runs a slow reference search that enumerates every
  combination with exact rationals. It compares the reference with the fast search for all
  targets of a fixed grid on E3, E6 and E12, which are small enough, and for random targets on
  E24. The two results must be identical.
- **Alternatives considered**:
  - Precomputing every pair sum and pair conductance (n² ≈ 1.8 M entries, sorted) and searching
    them. It is faster per target but takes about 30 MB per series and needs a rebuild on every
    series change.
  - Pure brute force n³: about 4e8 combinations for E192, far too slow.

## R5. Input parsing (FR-002)

- **Decision**: A pure-Kotlin parser, case-sensitive for the SI letters m and M. It accepts:
  - optional surrounding spaces and an optional trailing "Ω", "ohm" or "R" (but not with the code
    notation's R already used);
  - a number with "." or "," as decimal separator (at most one), optionally followed by one prefix
    letter: m, k, K, M, G;
  - the IEC 60062 §4 code notation: digits, one letter from R, m, k, K, M, G in place of the
    decimal point, then digits ("4k7", "R47", "1M5", "47R"). "K" is accepted as k because it is
    common in practice.

  Anything else is invalid, and so is a value outside [0.1Ω, 1GΩ] (spec). The parser returns an
  exact rational and is tested in a table: valid forms, invalid forms, boundaries, both separators.
- The keyboard is a text keyboard. The letters are needed, and number keyboards hide them.

## R6. Shared code

- **Decision**: Move `formatSi` and `formatDecimal` out of `:shared:color-code` into a new pure-JVM
  library `shared/si-format` (`:shared:si-format`, package
  `io.github.kolod.circuitopedia.shared.siformat`). `:shared:color-code` exposes it through
  `api(…)`. The calculator needs exactly these two functions, and depending on the color code
  library (band palette, contrast) for them would be the wrong dependency. The two cores that
  import them change two import lines each.
- **Not shared**: the color code UI. This screen is a form plus a list, not tabs and a table.
- **Alternatives considered**:
  - copying `formatSi` (duplicates a tested function);
  - depending on `:shared:color-code` (pulls in an unrelated palette).

## R7. UI and threading

- **Decision**: One Compose screen, top to bottom:
  - an `OutlinedTextField` for the target, with a supporting text that shows the hint or the error;
  - a series dropdown (`ExposedDropdownMenuBox`; 7 entries are too many for segmented buttons at
    200% font);
  - a `FlowRow` of 5 `FilterChip`s for the kinds;
  - a `LazyColumn` of results.

  Each result row shows the connection text (FR-009) on the first line, and the resistance with
  the signed deviation on the second. The deviation is colored only as a secondary cue: green-ish
  when exact, otherwise the default color. Each row is one semantics node with a spoken
  description (FR-012).
- **Threading**: the inputs go through `snapshotFlow`, then `collectLatest`, then
  `withContext(Dispatchers.Default)`. Typing never blocks the UI, and a newer input replaces the
  older result. The core search is a plain function with no coroutine dependency, and it is fast
  enough not to need cancellation (R4 budget).
- **State**: `rememberSaveable` for the target text, the series (ordinal) and the allowed kinds (a
  set of ordinals). Results are recomputed rather than saved.

## R8. Testing strategy

- **JVM, written first**:
  - `ESeriesTest`: counts, spot values, the irregularities and subset chains from R1, and the
    decade expansion.
  - `ParserTest`: the R5 table.
  - `RationalTest`: arithmetic and comparison.
  - `SearchReferenceTest`: fast search equals the reference search (R4).
  - `SearchExamplesTest`: the spec scenarios. E12 4.7k gives 4.7kΩ (0%) first. E12 5k gives an
    exact 2-resistor result above any equal 3-resistor one. Each kind appears at most 5 times.
    Forbidden kinds are absent.
  - `FormatTest`: connection text, 4 and 3 significant digits, signs, "0%", locales.
  - `SearchBenchmarkTest`: the R4 budget.
- **Instrumented**:
  - typing updates the list;
  - an invalid entry shows the error and no list;
  - the series dropdown and the kind chips;
  - state restoration;
  - the spoken description of a row.
