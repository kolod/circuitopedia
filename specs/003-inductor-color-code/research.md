# Research: Inductor Color Code Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

## R1. Source of the inductor color code (Principle II)

- **Decision**: Use the **Bourns** document *Color Code Marking (MIL STD)* and *Color Code Marking
  (EIA STD)* (Bourns technical library, inductive components, "ColorCodeMarkings.pdf", 2006) as
  the normative source. It reproduces the MIL coding for cylindrical RF choke coils (the MIL-C-15305
  / MIL-PRF-15305 family) and the EIA coding for molded inductors. The tolerance codes are
  cross-checked against a Vishay color chart and the Token Electronics guide.

  | Color  | Digit (Band 1–2, and Band 3 with a decimal point) | Multiplier (µH) | Tolerance |
  |--------|------|------|-------|
  | Black  | 0 | ×1    | — |
  | Brown  | 1 | ×10   | — |
  | Red    | 2 | ×100  | — |
  | Orange | 3 | ×1000 | — |
  | Yellow … White | 4 … 9 | — | — |
  | Gold   | decimal point (Band 1 or 2, MIL) | ×0.1 (EIA) | ±5% |
  | Silver | — | ×0.01 (EIA) | ±10% |
  | none   | — | — | ±20% (3 bands) |

  The MIL text, quoted: "When either the first or second band of the three bands is gold, this
  band shall represent the decimal point for inductance values less than 10, and the other two
  bands shall represent significant figures. For inductance values of 10 or more, the first two
  bands shall represent significant figures, and the third band shall represent the multiplier."
  Bourns' examples: Blue, Gold, Gray, Silver = 6.8µH ±10% (MIL); Blue, Gray, Gold, Silver = 6.8µH
  ±10% (EIA); Red, Violet, Brown, Gold = 270µH ±5% (both). Values are in microhenries.
- **Discrepancies found**, resolved in favor of the cited source:
  - Some web references (RF Cafe, learnabout-electronics) add tolerances Brown ±1%, Red ±2%,
    Orange ±3%, Yellow ±4% and multipliers Yellow ×10k, Green ×100k, Blue ×1M. Neither Bourns table
    lists them, and the pages cite no standard, so they are **not offered**. Spec FR-006 and the
    Constitution Constraints table are updated to the Bourns table.
  - Learnabout-electronics allows the gold point in Band 2 *or 3*. The MIL text says the first or
    second band, so only Bands 1 and 2 are offered (spec FR-010a).
  - The MIL marking has an extra wide silver identifier band in front. It carries no value and is
    out of scope (spec Assumptions); the remaining four bands are read as the 4-band code.
- **Consequences**: The largest value is White, White, Orange = 99 × 1000µH = **99mH** (spec edge
  case updated from 990mH). The smallest is 0.10µH = 100nH (Brown, Black, Silver or Gold, Brown,
  Black). The 4-band Tolerance column offers only Gold and Silver.
- **Alternatives considered**: MIL-PRF-15305 itself (DLA ASSIST) needs a login and was not
  available; the Bourns reproduction quotes its marking clause verbatim.

## R2. Value formatting

- **Decision**: Inductance is stored exactly in µH as `BigDecimal` (the spec 002 R2 approach) and
  shown with the SI prefix that puts the mantissa in [1, 1000): **nH, µH or mH**. Trailing zeros
  are dropped; the decimal separator follows the locale. The unit symbol is "H" with the micro
  sign U+00B5 ("µ"), as on Bourns' sheets, in every locale (spec Unit symbol). Multiplier cells read
  "×0.01µH", "×0.1µH", "×1µH", "×10µH", "×100µH", "×1mH".
- **Shared code**: The SI-prefix formatter is generalized from the resistor's `formatOhms` (R4):
  `formatSi(value, unit, prefixes, locale)`, where the resistor passes "", k, M, G (values below 1Ω
  stay in Ω) and the inductor passes n, µ, m relative to µH.

## R3. Decimal point and band validity

- **Decision**: A `Selection` is valid when it satisfies all of:
  - at most one Gold among Bands 1–2, which is the decimal point;
  - the band holding the first significant digit (Band 1, or Band 2 when the point is in Band 1)
    is not Black;
  - Band 3 is a digit (Black … White) when there is a point, and a multiplier otherwise;
  - Band 4 (if present) is a tolerance.

  `with(index, color)` sets the tapped band, then replaces every other band that became invalid
  with that band's default color (spec FR-010b), in band order. A tap that would leave the tapped
  band itself invalid is ignored. A table cell is selectable iff `with(index, color)` keeps
  `color` at `index`, so the table always shows exactly the choices that work from the current
  selection.
- **Why defaults, not "nearest"**: a single, predictable rule that the spec's acceptance
  scenarios pin down (Yellow, Gold, Violet → tap Violet in Band 2 → Band 3 becomes Black).

## R4. Sharing code with the resistor calculator (Principle III)

- **Decision**: With two concrete uses, extract the common parts into two shared **libraries**
  (not app modules, so the "a module never depends on another module" rule of spec 001 holds):
  - `shared/color-code` → `:shared:color-code`, Kotlin/JVM, no Android: `BandColor` (the 12
    colors with display RGB and text color, no meanings), `Percent`, contrast helpers, decimal
    and SI-prefix formatting.
  - `shared/color-code-ui` → `:shared:color-code-ui`, Android library: the tabs + pager
    scaffold, the fixed-header color table, the SVG band drawing with leader lines, color names
    and the common strings ("N bands", "Band %d", "Mult.", "Toler.", "Multiplier", "Tolerance").
    AndroidSVG moves here from the resistor module.

  The meanings of the colors stay in each module's `core`: `ResistorCode` (IEC 60062) and
  `InductorCode` (R1). The UI is data-driven (contract `contracts/shared-ui.md`): a module passes
  column headers, per-cell labels (or `null` for not selectable), the selected colors and the
  value text, so the shared code knows nothing about resistors or inductors.
- **settings.gradle.kts**: also auto-includes every `shared/<name>` as `:shared:<name>`. The app
  does **not** auto-depend on them; modules declare what they use.
- **Module contract (spec 001)**: amended so a module MAY depend on `:shared:*` libraries, never
  on another `:modules:*` project. Shared libraries have no resource prefix rule of their own
  beyond `color_code_`.
- **Kept per module**: tab state and its `Saver` (the inductor's decimal-point rules differ),
  the module descriptor, title, description, icon and SVG.
- **Saved state compatibility**: `BandColor` keeps its entry order, so the resistor's saved
  ordinals stay valid.
- **Alternatives considered**: copying the resistor UI into the inductor module (about 450
  lines duplicated, and every fix made twice); a generic engine with type parameters over layouts
  and roles (more abstraction than two uses need).

## R5. Inductor drawing

- **Decision**: An Inkscape SVG, `res/raw/inductor_color_code_inductor.svg`, following the
  resistor's SVG contract (spec 002 R10): a `viewBox`, `band-1` … `band-4` as paths left to
  right, fills as attributes, no transforms. The body is a molded axial choke: a straight
  cylinder with rounded ends (no "dog-bone" caps), in a pale green typical of molded RF chokes,
  with gray leads. Band 4 (tolerance) sits apart near the right end. The 3-band tab shows bands
  1–3. If the user supplies their own drawing, it replaces this one as long as it keeps the
  contract.
- The decimal point is drawn as a gold band like any other: on real parts it is an ordinary band.

## R6. Strings and test tags

- **Decision**: Common strings live in `:shared:color-code-ui` with the prefix `color_code_` (all
  six locales); the resistor module's copies are removed. The inductor module has only its title,
  description and screen-reader band name for the decimal point ("Decimal point"). Test tags take
  a per-module prefix: `rcc-` stays for the resistor, `icc-` is used for the inductor.
- **Rationale**: one translation of each color name; `LocaleCoverageTest` already scans every
  `src/main/res`, so the shared library is covered automatically.

## R7. Testing strategy

- **JVM, test-first**:
  - `:shared:color-code`: formatting (`formatSi` for both unit families and four locales),
    contrast (moved from the resistor core).
  - `:modules:inductor-color-code:core`:
    - `InductorCodeTableTest` against R1 cell by cell;
    - `SelectionTest` for the validity and replacement rules (R3) and spec scenarios;
    - `ReadingTest` for the spec values (47µH, 2.2µH, 220nH, 4.7µH, 770nH, 99mH, 100nH);
    - `ExhaustiveReadingTest` over every valid selection of both layouts against an independent
      oracle (literal table and string arithmetic).
  - The resistor core tests move with the code and must still pass unchanged in substance.
- **Instrumented**: `InductorScreenTest` (defaults, taps, decimal point header switch, tab memory,
  state restoration, cell semantics); the resistor's 8 tests must still pass after the refactor.
- **SVG contract**: a JVM test per drawing, sharing one checker in the `testFixtures` of
  `:shared:color-code` (pure JVM XML parsing, `java-test-fixtures` plugin).
