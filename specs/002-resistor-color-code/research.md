# Research: Resistor Color Code Calculator

**Feature**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md) | **Date**: 2026-10-31

## R1. Verification of the color code table (Principle II)

- **Decision**: Use the table in the spec's Constitution Constraints unchanged. It matches IEC
  60062:2016 as reproduced in Wikipedia, "Electronic color code", section "Resistors" (retrieved
  2026-10-31), value by value:
  - digits Black 0 … White 9;
  - multipliers Silver 10⁻², Gold 10⁻¹, Black 10⁰ … White 10⁹;
  - tolerances Brown ±1 (F), Red ±2 (G), Orange ±0.05 (W), Yellow ±0.02 (P), Green ±0.5 (D),
    Blue ±0.25 (C), Violet ±0.1 (B), Gray ±0.01 (L), Gold ±5 (J), Silver ±10 (K), none ±20 (M);
  - TCR Black ±250 (U), Brown ±100 (S), Red ±50 (R), Orange ±15 (P), Yellow ±25 (Q), Green ±20
    (Z), Blue ±10 (Z), Violet ±5 (M), Gray ±1 (K).
- **Known discrepancies, documented and not implemented**:
  - Before IEC 60062:2016, some manufacturers (EIA RS-279, still Yageo datasheets) used Gray for
    ±0.05%.
  - Yellow and Gray were sometimes used in place of Gold and Silver on high-voltage resistors.

  The app follows the current IEC edition. The source comment in code cites both the standard
  and these caveats.
- **Spec change**: IEC gives TCR as ± values, so the TCR display was changed to "±100ppm/K"
  (spec FR-006, US2 scenario 4, Precision).
- **Alternatives considered**: EIA RS-279 values. That standard is superseded, and mixing
  standards would make results ambiguous.

## R2. Exact arithmetic

- **Decision**: `java.math.BigDecimal`. Resistance = `BigDecimal(digits).scaleByPowerOfTen(exp)`,
  with `exp` ∈ [−2, 9]. Formatting divides by 10^(3k) with `movePointLeft` and
  `stripTrailingZeros()`.
- **Rationale**: Exact by construction. 2 × 10⁻² is exactly `0.22`, never
  `0.22000000000000003`. It is part of the JVM standard library (no new dependency) and works in
  JVM unit tests.
- **Alternatives considered**: A hand-rolled `(significand: Int, exponent: Int)` value. It is
  exact as well, but formatting would be reimplemented by hand. `Double` gives the artifacts
  the spec forbids.

## R3. Where the logic lives

- **Decision**: Two Gradle subprojects, following the convention of spec 001 (research R7):
  - `modules/resistor-color-code/core`, Kotlin/JVM (`org.jetbrains.kotlin.jvm`), with no Android
    dependency. It holds the color table, band layouts, selection rules, the reading
    computation, value formatting (with `java.util.Locale` and `java.text.DecimalFormatSymbols`),
    the band display palette and the WCAG contrast check.
  - `modules/resistor-color-code`, an Android library with Compose UI, strings, the icon and the
    `AppModule` descriptor.
- **Shell change needed**: `settings.gradle.kts` currently includes only `modules/<name>`. It must
  also include `modules/<name>/core` when that directory has a `build.gradle.kts`. This is a
  one-time completion of the convention that spec 001 R7 already announced. Later modules need
  no shell change. The root build also declares the `kotlin-jvm` plugin (`apply false`), with
  the catalog version `kotlin`, which AGP 9 aligns with its bundled KGP.
- **Alternatives considered**: Pure-Kotlin packages inside the Android module. Principle III's
  "no `android.*` imports" rule would then rely on review only, not on the build.

## R4. JVM target alignment

- **Decision**: `core` compiles with `jvmTarget = 17` (`java { toolchain }` is not set; the daemon
  JDK 25 compiles to 17 bytecode). The Android module sets `compileOptions` source and target
  to 17, so it can inline Kotlin code from `core`.
- **Rationale**: Android's D8 accepts Java 17 bytecode at minSdk 26. Kotlin refuses to inline
  bytecode built for a higher JVM target than the consumer's.

## R5. Formatting the value

- **Decision**: A pure function `formatReading(reading, locale)` in `core`:
  - **Prefix**: `k = floor(log1000(ohms))` clamped to 0..3 (Ω, k, M, G). Values < 1Ω use
    k = 0.
  - **Mantissa**: `ohms.movePointLeft(3k).stripTrailingZeros().toPlainString()`, with `.`
    replaced by `DecimalFormatSymbols.getInstance(locale).decimalSeparator`.
  - **Assembly**: `"$mantissa${prefix}Ω ±${tolerance}%"`, where the tolerance is formatted the
    same way (plain string, locale separator), and `" ±${tcr}ppm/K"` is appended when present.
  - Multiplier cell labels use the same function: `"×" + format(10^exp Ω)`, for example "×0.1Ω",
    "×1kΩ".
- **Rationale**: `toPlainString` avoids scientific notation such as `1E+3`. Replacing only the
  separator keeps grouping out, because there are at most 3 integer digits after prefixing.
  `NumberFormat` would also work, but it needs `maximumFractionDigits` tuning and could insert
  locale grouping. It is not needed here.
- **Locale behavior** (JDK/ICU data): `uk`, `de`, `fr`, `es` use `,`; `en` and `ja` use `.`.
  Tests pin these.

## R6. Band display colors and contrast (SC-005, FR-007)

- **Decision**: The palette (sRGB) lives in `core` as `BandColor.argb`. Each cell's text color
  (black or white) is chosen at build time by the WCAG 2.x contrast ratio, and a JVM test
  asserts ≥ 4.5:1 for every color against both gradient extremes (base lightened by 15% and
  darkened by 15%). Final palette (Red, Blue, Violet and Gray were darkened from the first draft to pass the test):

  | Color  | Base     | Text  |
  |--------|----------|-------|
  | Black  | #212121  | white |
  | Brown  | #8B4A3C  | white |
  | Red    | #C01818  | white |
  | Orange | #F2861E  | black |
  | Yellow | #F5E83A  | black |
  | Green  | #2EA83A  | black |
  | Blue   | #1A55B8  | white |
  | Violet | #8236AA  | white |
  | Gray   | #585858  | white |
  | White  | #F4F4F4  | black |
  | Gold   | #D4A62A  | black |
  | Silver | #BDBDBD  | black |

  IEC 60062 defines names, not RGB values (see the Wikipedia footnote on RAL colors), so these
  are practical approximations close to the reference image.
- **Selection outline**: a 3dp cyan (#00E5FF) ring, as in the reference, plus a 1dp inner dark
  (#000000 at 60%) hairline. The cyan alone is weak on White, Silver and Yellow, and the
  hairline keeps the ring visible on every row (FR-008).
- **Rationale**: Putting the contrast logic in `core` makes SC-005 a fast JVM test instead of a
  manual check.

## R7. UI structure

- **Decision**: Compose. `PrimaryTabRow` with 4 tabs over a `HorizontalPager` (the same pattern as
  the catalog, spec 001 R3). Each page is a `Column`:
  - the value `Text` (headlineSmall);
  - the `ResistorDrawing` at full table width;
  - the `ColorTableHeader`, all fixed;
  - the `ColorTableBody`, the only part that scrolls (vertically).

  The table has 4dp side margins and never scrolls horizontally. This was revised after the
  first on-device review: long translated headers would not fit next to a half-width drawing.
- **ColorTable**: built from `Row`s and `Column`s with weights (12 rows + header, up to 7 columns).
  It is not a `LazyColumn`: 13 short rows are cheap, and a lazy list nested in a scroll
  container is an anti-pattern. Each selectable cell gets `Modifier.selectable(selected, role =
  Role.RadioButton)`, minimum height 48dp, a vertical-gradient background and a centered label.
  Each band column gets `selectableGroup()` semantics through a merged `contentDescription`
  template "%1$s, %2$s, %3$s" (band, color, meaning).
- **ResistorDrawing**: a `Canvas`:
  - two gray leads;
  - a beige "dog-bone" body (rounded rect plus end caps);
  - band rects at fixed relative positions per layout. The digit and multiplier bands are spaced
    evenly, and the tolerance and TCR bands are pushed right, as in the reference;
  - no text labels, which would only repeat the table headers. Each band is joined by a 3dp
    leader line to the top edge of its header column. The line is built from vertical and
    slanted segments only, because horizontal ones merge with the header edge. Column centers
    come from `bandColumnCenters`, shared with the table layout.

  It carries `Modifier.clearAndSetSemantics {}` because it is decorative (FR-016).
- **Rationale**: Uses existing Compose and Material 3 only (Principle III).

## R8. State

- **Decision**: `ColorCodeState` in the Android module holds the selected tab and
  `Map<BandLayout, Selection>`. It is saved with a custom `Saver` to a `List<Int>`: the layout
  ordinal followed by the color ordinals per layout. Defaults come from `core`
  (`Selection.default(layout)`). The pure update rule `Selection.with(index, color)` (it ignores
  invalid colors) lives in `core` and is unit-tested.
- **Rationale**: FR-015 (state per tab, survives recreation). The ordinals are stable because the
  enums are append-only and ordered as the table rows.

## R9. Testing strategy

- **JVM (`core`)**, written test-first:
  - `ColorCodeTableTest` checks every cell against R1.
  - `ReadingTest` covers the spec's acceptance and edge values.
  - `ExhaustiveReadingTest` enumerates every valid selection of all 4 layouts (about 1.2 M
    combinations, under 2 s) and compares with an independent oracle in the test: a literal
    table, `BigInteger` arithmetic and simple string building.
  - `FormatTest` covers prefixes, trailing zeros, sub-Ω values, and the `en`/`uk`/`de`/`ja`
    separators.
  - `PaletteContrastTest` checks SC-005. `SelectionTest` covers defaults and invalid-cell taps.
- **Instrumented (`modules/resistor-color-code/src/androidTest`)**: `ColorCodeScreenTest` covers:
  - the 4-band default "4.7kΩ ±5%";
  - taps → "10kΩ ±1%";
  - 3-, 5- and 6-band defaults;
  - per-tab memory, swipe, state restoration, and the semantics of a cell (content description
    + selected).
- **App level**: the existing `ModuleRegistryTest` and `LocaleCoverageTest` automatically cover
  the new module's id and strings.
