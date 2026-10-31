# Research: Voltage Divider Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

## R1. The nine cases (Principle II)

Source: an unloaded divider gives `U2 = U1 · R2 / (R1 + R2)` and `R = R1 + R2`, from Ohm's and
Kirchhoff's laws (P. Horowitz, W. Hill, *The Art of Electronics*, 3rd ed., 2015, §1.2, cited in
spec 004). With `k = U2 / U1` (the ratio, 0 < k < 1), every case solves in closed form using only
`+ − × ÷`, so the results are exact rationals:

| Entered | Computed |
|---------|----------|
| U1, U2, R1 | R2 = R1·U2/(U1−U2), R = R1·U1/(U1−U2) |
| U1, U2, R2 | R1 = R2·(U1−U2)/U2, R = R2·U1/U2 |
| U1, U2, R | R2 = R·U2/U1, R1 = R − R2 |
| U1, R1, R2 | R = R1 + R2, U2 = U1·R2/R |
| U1, R1, R | R2 = R − R1, U2 = U1·R2/R |
| U1, R2, R | R1 = R − R2, U2 = U1·R2/R |
| U2, R1, R2 | R = R1 + R2, U1 = U2·R/R2 |
| U2, R1, R | R2 = R − R1, U1 = U2·R/R2 |
| U2, R2, R | R1 = R − R2, U1 = U2·R/R2 |
| R1, R2, R | not solvable: no voltage (FR-004) |

**Validity (FR-006)**, checked on the entered values and on the solution:
- if both voltages are entered: same sign and 0 < U2/U1 < 1;
- if R is entered: R1 < R and R2 < R;
- every resistance of the solution > 0;
- range checks on the entered values (spec).

These rules make every denominator in the table non-zero.

**Tests**: a table test of the spec scenarios, and a property test. The property test draws random
dividers (U1, R1, R2), computes all five quantities with the forward formula, then feeds every
valid triple back in. It must reproduce the other two values exactly (rational equality), which
covers SC-002.

## R2. Shared code (Principle III)

- **Decision**: Move the pieces the divider reuses from `:modules:resistor-combinations:core` into
  a new pure-JVM library, `shared/resistors` (`:shared:resistors`, package
  `io.github.kolod.circuitopedia.shared.resistors`):
  - `Rational`;
  - `ESeries`;
  - `Kind` and `Combination`, which give connection texts for the arms;
  - the resistance parser, renamed `parseResistance`;
  - `connectionText`, `formatOhms` and `formatDeviation`.

  The combination search, `rankingFor` and `PER_KIND` stay in the combinations module, because
  only it uses them. `:shared:resistors` exposes `:shared:si-format` through `api(...)`.
- **Why now**: the divider is the second user of each piece. A module cannot depend on another
  module (spec 001 FR-013), so copying would be the only other option.
- **Moves keep history**: `git mv` in a separate commit. The combinations module changes only its
  imports, and its 33 JVM tests and 7 UI tests must stay green.
- **Voltage parsing and formatting** stay in the divider core (single user). The parser accepts a
  sign, "." or ",", the prefixes m and k, and an optional "V". The formatter puts the sign in front
  of the magnitude, because `formatSi` assumes positive values.

## R3. Exact arithmetic

As in spec 004 R3: entered text is parsed to exact `Rational`s, the nine forms are computed
exactly, and display rounds HALF_UP (4 significant digits for values, 3 for errors). The build
search ranks with `Double` keys within a 1e-9 relative margin and confirms with exact rationals.

## R4. Searching standard-resistor builds (FR-009–FR-011, SC-003, SC-004)

Notation:
- target ratio `k = R2t / Rt`, where `Rt = R1t + R2t` is the target total (entered or computed);
- window `W = [0.9·Rt, 1.1·Rt]`;
- arm shapes: Single, Series2 or Parallel2 over the series values;
- a build is `(B1, B2)` with B1 + B2 in W;
- its error is `|B2/(B1+B2) − k| / k`.

**Arm lists in a band.** `armValues(series, shapes, lo, hi)` returns every arm build whose value is
in `[lo, hi]`, sorted by value, with a `Double` and the exact `Combination`.
- Singles: a binary-searched slice.
- Series pairs a ≤ b: for each a, the b range comes from a binary search on `lo − a … hi − a`.
- Parallel pairs a ≤ b: for each a, the b range comes from the conductance bounds.

The band is a ±10% window, so even E192 pairs give lists of tens of thousands of builds, not 1.8 M.

**One arm entered (F).** The other arm X takes values in `W − F`, which comes out narrower than
±10%. The error is monotonic in X: increasing for R2, decreasing for R1. So the search walks the
band list outward from the ideal value, separately for the 1-resistor and 2-resistor tiers.

**Both arms chosen.** The tiers are (1,1), (1,2), (2,1) and (2,2). A build ranks by error, so for
each B1 the best B2 is the one nearest to `q·B1` with `q = k/(1−k)`, clamped into `W − B1`.
- B1 can only come from a band `[0.9·Rt·(1 − k(1+e)), 1.1·Rt·(1 − k(1−e))]`, where `e` is the
  error bound. A build with error ≤ e has `B1/T = 1 − k'` with `|k' − k| ≤ e·k`, and the total T is
  inside W.
- Two passes, as in spec 004:
  1. The first pass uses `e = 0`, the ±10% band around R1t, to find candidate builds. Their 5th best
     error per tier gives the bound `e`.
  2. The second pass uses the band for that `e`, plus the 1e-9 margin, and walks outward from each
     ideal `q·B1` while the error stays within the tier's bound.

  If a tier has fewer than 5 candidates after the first pass, the second pass uses
  `e = ∞`, which means every B1 < 1.1·Rt.
- **Cost estimate**: E192 (2,2) is about 4·10⁴ B1 values × log(4·10⁴) per binary search, plus short
  walks. That is a few million steps.

**As implemented**, measured on 2026-10-31:
- **Widening bound.** The fixed two passes became a widening bound per tier: e = 0, 0.1%, 1%, 10%,
  100%, then ∞. Each pass is complete for its e, because its B1 band contains every build with
  error ≤ e, so the search stops at the first pass that finds 5 builds within e. Before this, a
  tier with fewer than 5 candidates in the first pass fell back to every arm build up to 1.1·R.
  The median went from 83 to 27 ms, and the most evaluations for a target from 1.24 M to 181 k.
- **Negative band edge.** An entered arm can push the other arm's band edge below zero. The
  parallel-pair bound then has to accept every b. A bug here dropped all parallel pairs, and the
  reference test caught it.
- **Arm-list sort.** Arm lists are sorted in place, without boxing.
- **Exact ties.** A ratio like ½ ties thousands of builds at 0% error, for example every pair of
  equal arms, and ranking all of them exactly took about 1 s on the emulator. Candidates are now
  narrowed in `Double` by error and then by total deviation, keeping everything within the 1e-9
  margins of the 5th one, before the exact ranking.
- **Measured in-process** on the API 36 emulator (debug build, 10 E192 targets, both arms
  chosen): at most 334 ms, with the ½ case at 166 ms instead of 992 ms (SC-003: 1 s).

**Ranking (FR-010)**: |error| (exact), then the resistor count, then |T − Rt| (exact), then the
values in order. The best 5 per tier are merged and sorted.

**Proof (SC-004)**: a test-only reference enumerates every pair of arm builds whose total is in W,
with exact rationals and no bands. It runs on E3 and E6 for all tiers, and on E12 for the tiers
with at least one single resistor. It must equal the fast search: the same builds in the same
order. Targets include ratios near 0, ½ and 1, totals near the ends of the value range, and an
entered arm.

**Budget**: a deterministic evaluation counter, as in spec 004. CI runners are too noisy for
wall-clock limits. Timing is printed only, and the on-device time is measured in-process on the
emulator.

## R5. UI

- **Schematic**: a Compose `Canvas` drawing of U1 ── R1 ── U2 ── R2 ── GND, laid out vertically
  (portrait):
  - a node dot at U1, R1 as a resistor box, a node dot at U2, R2, then a ground symbol;
  - labels U1, R1, U2, R2 drawn with `TextMeasurer`;
  - R shown as a brace beside R1 and R2;
  - decorative for screen readers (FR-015).
- **Fields**: five `OutlinedTextField`s with unit suffixes (V, Ω).
  - A computed field shows its value with a tinted container and the supporting text
    "computed".
  - Typing into a computed field makes it entered and clears the other computed values (FR-008).
  - Text keyboard (letters k, m, R are needed).
- **Calculate button**: enabled per FR-004. Below it, the note or error.
- **Standard resistors**: shown after a calculation in which R1 or R2 was computed.
  - E-series chips (E24 first), as in spec 004.
  - Rows show "R1: …", "R2: …", "U2 = 3.311V (+0.33%)" and "R = 29.84kΩ (−0.53%)". Each row is
    one semantics node with spoken text.
- **Layout**: one `LazyColumn` (schematic, fields, button, chips, builds), so it works at 200% font,
  as learned in spec 004.
- **Threading**: the build search runs on `Dispatchers.Default` with `ensureActive` cancellation, as
  in spec 004.
- **State**: `rememberSaveable` for the five texts, their origins (entered or computed) and the
  series ordinal. Builds are recomputed.

## R6. Testing strategy

- **JVM (test-first)**:
  - `SolveTest`: the 9 cases, the spec scenarios, invalid sets and messages, the property round
    trip.
  - `VoltageParserTest` and `VoltageFormatTest`: signs, prefixes, locales.
  - `BuildSearchReferenceTest` (R4).
  - `BuildSearchExamplesTest`: the spec scenarios. With 9V / 3V / 30k on E24, the first build is
    20k / 10k at 0%. With R1 entered, R1 stays 10k in every build. The window holds. There are at
    most 5 builds per tier.
  - `BuildSearchBudgetTest`.
  - The moved spec 004 tests, unchanged.
- **Instrumented**:
  - Calculate is enabled or disabled;
  - computing fills the empty fields;
  - an edit clears the results;
  - typing into a computed field;
  - the build list appears and the series switch updates it;
  - state restoration;
  - the spoken text of a field and of a build.
