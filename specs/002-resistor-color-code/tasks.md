---

description: "Task list for 002-resistor-color-code"
---

# Tasks: Resistor Color Code Calculator

**Input**: Design documents from `/specs/002-resistor-color-code/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Principle IV makes JVM tests mandatory for the calculation logic and requires them to be
written first. The expected values come from IEC 60062:2016 ([research R1](research.md)) and from
the spec's acceptance scenarios. Logic-bearing UI (taps, tabs, saved state, screen-reader
semantics) gets Compose instrumented tests, which need an emulator or device.

**Organization**: Tasks are grouped by user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: US1 is 4-band reading, US2 is the 3/5/6-band tabs, US3 is localization, accessibility and settings

## Path Conventions

- `CORE` = `modules/resistor-color-code/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode/core`
- `CORE_TEST` = `modules/resistor-color-code/core/src/test/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode/core`
- `UI` = `modules/resistor-color-code/src/main/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode`
- `UI_TEST` = `modules/resistor-color-code/src/androidTest/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode`
- `RES` = `modules/resistor-color-code/src/main/res`
- Locales: `values` (en), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Both subprojects build, and the module shows up in the catalog (with a placeholder screen).

- [ ] T001 Add plugin alias `kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }` to `gradle/libs.versions.toml`, and add `alias(libs.plugins.kotlin.jvm) apply false` to the root `build.gradle.kts`.
- [ ] T002 In `settings.gradle.kts`, extend the `modules/*` auto-include. For each included `modules/<name>`, also `include(":modules:<name>:core")` when `modules/<name>/core/build.gradle.kts` exists, and update the comment (research R3). App auto-dependencies must stay on direct children of `:modules` only.
- [ ] T003 [P] Create `modules/resistor-color-code/core/build.gradle.kts`: plugin `kotlin-jvm`, `kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }`, `java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }`, `testImplementation(libs.junit)`.
- [ ] T004 [P] Create `modules/resistor-color-code/build.gradle.kts`, following `modules/README.md`:
  - plugins `android-library` and `kotlin-compose`;
  - `namespace = "io.github.kolod.circuitopedia.modules.resistorcolorcode"`, `compileSdk = 37`, `minSdk = 26`;
  - `compileOptions` source and target 17, `buildFeatures.compose = true`, `resourcePrefix = "resistor_color_code_"`;
  - `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`;
  - lint `error += setOf("MissingTranslation", "MissingDefaultResource", "ExtraTranslation")`;
  - dependencies:
    - `implementation`: `project(":module-api")`, `project(":modules:resistor-color-code:core")`, the Compose BOM, foundation, material3 and ui;
    - `debugImplementation`: `ui-test-manifest`;
    - `androidTestImplementation`: the BOM, `ui-test-junit4`, `androidx-test-ext-junit` and `androidx-test-runner`.
- [ ] T005 [P] Create `RES/drawable/resistor_color_code_icon.xml`, a 24dp single-color vector of a resistor (zigzag or body with bands, white fill, no tint attribute).
- [ ] T006 [P] Create `RES/values/strings.xml` with `resistor_color_code_title` "Resistor color code" and `resistor_color_code_description` "Resistance from 3–6 color bands", plus translations:
  - `values-de`: Widerstandsfarbcode / Widerstand aus 3–6 Farbringen;
  - `values-fr`: Code couleur des résistances / Résistance à partir de 3 à 6 anneaux;
  - `values-es`: Código de colores de resistencias / Resistencia a partir de 3–6 bandas;
  - `values-ja`: 抵抗器のカラーコード / 3～6本の色帯から抵抗値を求める;
  - `values-uk`: Кольорове маркування резисторів / Опір за 3–6 кольоровими смугами.
- [ ] T007 Create `UI/ResistorColorCodeModule.kt` with `val resistorColorCodeModule = AppModule(id = ModuleId("resistor-color-code"), category = Category.Calculations, titleRes = R.string.resistor_color_code_title, descriptionRes = R.string.resistor_color_code_description, iconRes = R.drawable.resistor_color_code_icon, content = { ColorCodeScreen() })`, with a temporary empty `@Composable fun ColorCodeScreen() {}` in `UI/ColorCodeScreen.kt`.
- [ ] T008 Register the module with one line, `resistorColorCodeModule,`, in `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt` (import `io.github.kolod.circuitopedia.modules.resistorcolorcode.resistorColorCodeModule`).

**Checkpoint**: `./gradlew test lint :app:assembleDebug` passes. The catalog lists "Resistor color code".

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The pure model shared by all stories: units, the IEC table, layouts and selection rules.

**⚠️ CRITICAL**: All stories depend on this phase.

- [ ] T009 [P] Create `CORE/Units.kt`: `@JvmInline value class Ohms(val value: BigDecimal)`, `Percent(val value: BigDecimal)`, `PpmPerKelvin(val value: Int)`, each with an `init { require(value > 0) }`.
- [ ] T010 [P] Write `CORE_TEST/ColorCodeTableTest.kt` first. Assert every `BandColor` field against a literal table copied from research R1:
  - `digit`: Black 0 … White 9, Gold and Silver null;
  - `multiplierExponent`: Black 0 … White 9, Gold −1, Silver −2;
  - `tolerance`: Brown 1, Red 2, Orange 0.05, Yellow 0.02, Green 0.5, Blue 0.25, Violet 0.1, Gray 0.01, Gold 5, Silver 10, Black and White null (compare with `compareTo`);
  - `tcr`: Black 250, Brown 100, Red 50, Orange 15, Yellow 25, Green 20, Blue 10, Violet 5, Gray 1, White, Gold and Silver null;
  - the enum order Black…Silver.

  It must fail to compile.
- [ ] T011 Create `CORE/BandColor.kt`: the `TextOnColor { Light, Dark }` enum and `enum class BandColor(digit, multiplierExponent, tolerance, tcr, argb, textOnColor)` with the values from T010 and the palette from research R6.
  - A KDoc block cites "IEC 60062:2016, Marking codes for resistors and capacitors, §5, Tables 1–3", notes the pre-2016 Gray ±0.05% (EIA RS-279) caveat, and says the RGB values are illustrative.
  - Add a comment that ordinals are saved in UI state, so the order is append-only.
  - T010 now passes.
- [ ] T012 [P] Write `CORE_TEST/SelectionTest.kt` first:
  - `BandLayout.Three/Four/Five/Six.roles` match data-model.md;
  - `accepts(0, Black)` is false for every layout, while `accepts(1, Black)` and `accepts(2, Black)` (a digit in Five) are true;
  - `accepts(i, Gold)` is false for digit bands;
  - the tolerance band rejects Black and White, and the TCR band rejects White, Gold and Silver;
  - `Selection.default` for each layout equals FR-014;
  - `with(index, validColor)` changes only that band;
  - `with(0, Black)` returns an equal selection;
  - the `Selection` constructor throws `IllegalArgumentException` for the wrong size or an invalid color.

  It must fail.
- [ ] T013 Create `CORE/BandLayout.kt`: `enum class BandRole { Digit, Multiplier, Tolerance, TemperatureCoefficient }`, `enum class BandLayout(val roles: List<BandRole>)` with `bandCount`, and `fun BandLayout.accepts(index: Int, color: BandColor): Boolean`, implementing the data-model.md rules (no Black on the first digit).
- [ ] T014 Create `CORE/Selection.kt`: `class Selection(layout, colors)` with an `init` validation through `accepts`, `with(index, color)`, `equals`/`hashCode` (or make it a data class), and `companion object { fun default(layout) }` per FR-014. Add `data class Reading(resistance: Ohms, tolerance: Percent, tcr: PpmPerKelvin?)` and leave `read()` as `TODO()`. T012 now passes.

**Checkpoint**: `./gradlew :modules:resistor-color-code:core:test` is green for the table and the selection rules.

---

## Phase 3: User Story 1 - Read a 4-band resistor (Priority: P1) 🎯 MVP

**Goal**: The calculator opens on 4 bands with the reference layout. Tapping cells updates the
value and the drawing.

**Independent Test**: Yellow, Violet, Red, Gold shows "4.7kΩ ±5%". Brown, Black, Orange, Brown
shows "10kΩ ±1%" (spec US1).

### Tests for User Story 1 ⚠️ (write first, confirm failing)

- [ ] T015 [P] [US1] Write `CORE_TEST/ReadingTest.kt` (layout Four), comparing with `BigDecimal.compareTo`:
  - Yellow, Violet, Red, Gold gives 4700 Ω ±5%;
  - Brown, Black, Orange, Brown gives 10000 Ω ±1%;
  - Red, Red, Gold, Gold gives 2.2 Ω;
  - Red, Red, Silver, Silver gives exactly `BigDecimal("0.22")` ±10%;
  - Brown, Black, Silver, Gold gives 0.1 Ω;
  - White, White, White, Brown gives 99 000 000 000 Ω;
  - `tcr` is null.
- [ ] T016 [P] [US1] Write `CORE_TEST/FormatTest.kt` (locale `Locale.ENGLISH`). `Reading.format` gives:
  - "4.7kΩ ±5%", "10kΩ ±1%", "2.2Ω ±5%", "0.22Ω ±10%", "0.1Ω ±5%";
  - "1kΩ ±5%" for Brown Black Red (no ".0");
  - "100Ω ±5%" and "99GΩ ±1%";
  - "470kΩ ±0.05%" (Yellow Violet Yellow Orange), so the tolerance has no trailing zeros.

  `BandRole.cellLabel` gives:
  - Digit+Yellow "4";
  - Multiplier+Black "×1Ω", Red "×100Ω", Orange "×1kΩ", Green "×100kΩ", Blue "×1MΩ", White "×1GΩ", Gold "×0.1Ω", Silver "×0.01Ω";
  - Tolerance+Gold "±5%", Blue "±0.25%".

  No output may contain "E", a grouping separator, or ".0" before a unit.
- [ ] T017 [P] [US1] Write `CORE_TEST/PaletteContrastTest.kt`:
  - `contrastRatio(0xFFFFFFFF, 0xFF000000)` is 21.0 ± 0.01 and `contrastRatio(x, x)` is 1.0;
  - for every `BandColor`, let text be white if `Light` and black if `Dark`; the contrast is ≥ 4.5 against `argb` and against `argb` lightened by 15% and darkened by 15% (mix each channel toward 255 or 0 by 0.15). The failure message names the color and the ratio.
- [ ] T018 [US1] Write `UI_TEST/ColorCodeScreenTest.kt` with `createAndroidComposeRule<ComponentActivity>()` (the v2 API) and `setContent { MaterialTheme { ColorCodeScreen() } }`. US1 cases:
  - the tab `rcc-tab-4` is selected, and `rcc-value` has text "4.7kΩ ±5%";
  - the cells `rcc-cell-0-Yellow`, `rcc-cell-1-Violet`, `rcc-cell-2-Red` and `rcc-cell-3-Gold` are selected;
  - clicking `rcc-cell-0-Brown`, `rcc-cell-1-Black`, `rcc-cell-2-Orange` and `rcc-cell-3-Brown` gives "10kΩ ±1%", with only those cells selected in their columns;
  - clicking the empty `rcc-cell-0-Black` leaves the value unchanged.

  Write the test against the contract tags. It must fail.

### Implementation for User Story 1

- [ ] T019 [US1] Implement `Selection.read()` in `CORE/Selection.kt`:
  - digits = the digit bands joined as an `Int`;
  - `resistance = Ohms(BigDecimal(digits).scaleByPowerOfTen(multiplierExponent))`;
  - tolerance from the tolerance band, or `Percent(BigDecimal(20))` for `Three`;
  - `tcr` from the TCR band for `Six`, otherwise null.

  T015 passes.
- [ ] T020 [US1] Create `CORE/Contrast.kt` with `fun contrastRatio(argb1: Int, argb2: Int): Double` (WCAG 2.x relative luminance with the sRGB linearization `c ≤ 0.04045 ? c/12.92 : ((c+0.055)/1.055)^2.4`, ratio `(L1+0.05)/(L2+0.05)` with L1 ≥ L2). Tune the `argb`/`textOnColor` values in `CORE/BandColor.kt` until T017 passes, and record the final values back in research R6.
- [ ] T021 [US1] Create `CORE/Format.kt`:
  - a private `formatDecimal(BigDecimal, Locale)`: `stripTrailingZeros().toPlainString()` with `.` replaced by `DecimalFormatSymbols.getInstance(locale).decimalSeparator`;
  - a private `formatOhms(Ohms, Locale)`: pick k in 0..3 so that 1 ≤ value/1000^k < 1000, with k = 0 for values < 1, and the prefixes "", "k", "M", "G";
  - `fun Reading.format(locale: Locale): String`, built as "<ohms> ±<tol>%[ ±<tcr>ppm/K]";
  - `fun BandRole.cellLabel(color: BandColor, locale: Locale): String`, per contracts/core-api.md, and `""` for invalid colors.

  T016 passes.
- [ ] T022 [P] [US1] Add the table and drawing strings to `RES/values/strings.xml` (en): 12 color names `resistor_color_code_color_<name>`, `_header_band` "Band %d", `_header_multiplier` "Mult.", `_header_tolerance` "Toler.", `_header_tcr` "TCR", `_label_1st` "1st", `_label_2nd` "2nd", `_label_3rd` "3rd", `_label_mul` "MUL", `_label_tol` "TOL", `_label_tcr` "TCR".
- [ ] T023 [P] [US1] Translate the T022 strings into `RES/values-de`, `values-fr`, `values-es`, `values-ja` and `values-uk`. Use the standard color names (for example de Schwarz, Braun, Rot, Orange, Gelb, Grün, Blau, Violett, Grau, Weiß, Gold, Silber; uk Чорний, Коричневий, Червоний, Помаранчевий, Жовтий, Зелений, Синій, Фіолетовий, Сірий, Білий, Золотий, Срібний). The short drawing labels are translated where a common abbreviation exists, and otherwise kept as "MUL", "TOL" and "TCR".
- [ ] T024 [US1] Create `UI/ColorCodeState.kt`:
  - `class ColorCodeState(initialLayout: BandLayout, initialSelections: Map<BandLayout, Selection>)`, with `var layout by mutableStateOf(...)`, a `selections` `mutableStateMapOf`, `fun select(index, color)` (applies `Selection.with` to the current layout), and `val current: Selection`;
  - a `Saver` that writes a `List<Int>` (the layout ordinal, then for each `BandLayout` in order the color ordinals of its selection) and restores it;
  - `@Composable fun rememberColorCodeState()` using `rememberSaveable(saver = …)`, with defaults `Four` and `Selection.default(it)`.
- [ ] T025 [US1] Create `UI/ColorTable.kt`: `@Composable fun ColorTable(selection: Selection, onSelect: (Int, BandColor) -> Unit)`.
  - The header row has an empty corner, then one header per band: Band N for digits, plus Mult., Toler. and TCR.
  - Then one row per `BandColor` with a name cell, then for each band either:
    - an empty `Box` (when `!layout.accepts(i, color)`), or
    - a cell with a `Brush.verticalGradient(lighten(argb, .15), argb, darken(argb, .15))` background, text `role.cellLabel(color, locale)` colored per `textOnColor`, `Modifier.heightIn(min = 48.dp).weight(1f).selectable(selected, role = Role.RadioButton) { onSelect(i, color) }.testTag("rcc-cell-$i-${color.name}")`, and, when selected, a 3dp `#00E5FF` border plus a 1dp inner `Color.Black.copy(alpha = .6f)` border.
  - The locale comes from `LocalConfiguration.current.locales[0]`, and theme surface colors are used for the header, the name column and empty cells.
- [ ] T026 [US1] Create `UI/ResistorDrawing.kt`: `@Composable fun ResistorDrawing(selection: Selection, modifier: Modifier)`, a `Canvas` with `aspectRatio(2.2f)`.
  - Draw two gray leads and a beige (#E8C39E, darker edge #D9A877) dog-bone body.
  - Band rects in the selected colors at relative x positions per layout. Digit and multiplier bands are evenly spaced in the left 60% of the body, and the TOL and TCR bands sit in the right part (research R7).
  - Above the bands, labels (`_label_*` strings) are drawn with `rememberTextMeasurer()`, each with a leader line down to its band.
  - Use `Modifier.clearAndSetSemantics {}`.
- [ ] T027 [US1] Replace the placeholder in `UI/ColorCodeScreen.kt` with `@Composable fun ColorCodeScreen(state: ColorCodeState = rememberColorCodeState())`. For now it renders only the current layout, `Four`, as a vertically scrollable `Column`:
  - a `Row` with the value `Text(state.current.read().format(locale), style = headlineSmall, modifier = Modifier.weight(1f).testTag("rcc-value"))` and `ResistorDrawing(state.current, Modifier.fillMaxWidth(0.5f))`;
  - then `ColorTable(state.current, state::select)`.

  T018 passes on a device.

**Checkpoint**: The MVP works: the 4-band calculator is fully usable from the catalog.

---

## Phase 4: User Story 2 - Switch between 3, 4, 5 and 6 bands (Priority: P1)

**Goal**: Four tabs, each with its own columns, drawing and value format, and its own remembered
selection.

**Independent Test**: The defaults per tab read 4.7kΩ ±20% / ±5% / ±1% / ±1% ±100ppm/K. A
selection is kept when you leave a tab and come back.

### Tests for User Story 2 ⚠️

- [ ] T028 [P] [US2] Extend `CORE_TEST/ReadingTest.kt`:
  - Three: Yellow, Violet, Red gives 4700 Ω ±20%; Brown, Black, Silver gives 0.1 Ω.
  - Five: Brown, Black, Black, Red, Brown gives 10000 Ω ±1%; Yellow, Violet, Black, Brown, Brown gives 4700 Ω ±1%; Brown, Red, Brown, Black, Brown gives 121 Ω; Yellow, Violet, Green, Brown gives 4750 Ω; Brown, Black, Black, Silver gives 1 Ω; White ×4 gives 999 GΩ.
  - Six: the default gives 4700 Ω ±1% with TCR 100.

  Extend `CORE_TEST/FormatTest.kt` with "4.7kΩ ±20%", "121Ω ±1%", "4.75kΩ ±1%", "999GΩ ±1%" and "4.7kΩ ±1% ±100ppm/K".
- [ ] T029 [P] [US2] Write `CORE_TEST/ExhaustiveReadingTest.kt`. For each `BandLayout`, enumerate every combination with `accepts` true for every band. For each one, compare `read()` and `format(Locale.ENGLISH)` against an independent oracle defined in the test:
  - private literal maps color→digit, exponent, tolerance string and TCR, typed again rather than taken from `BandColor`;
  - resistance as `BigInteger(digits) × 10^exp`, held as a `BigDecimal`;
  - the expected string built by a simple separate algorithm (the integer and fraction split done with `BigDecimal.divide` by 1000^k).

  Also assert the total counts: Three 1 080, Four 10 800, Five 108 000, Six 972 000. The test must finish in under 5 s.
- [ ] T030 [US2] Extend `UI_TEST/ColorCodeScreenTest.kt`:
  - clicking `rcc-tab-3` gives "4.7kΩ ±20%" and no column 3 cells exist;
  - `rcc-tab-5` gives "4.7kΩ ±1%";
  - `rcc-tab-6` gives "4.7kΩ ±1% ±100ppm/K";
  - on tab 5, select `rcc-cell-0-Brown`, go to tab 4 and back to 5, and the cell is still selected while tab 4 still shows its own value;
  - a `swipeLeft` on `rcc-pager` from tab 4 selects tab 5;
  - `StateRestorationTester`: select tab 6 and a cell, emulate a restore, and both are kept.

### Implementation for User Story 2

- [ ] T031 [P] [US2] Add `<plurals name="resistor_color_code_bands">` to `RES/values/strings.xml` (one "%d band", other "%d bands") and translate it:
  - de: one "%d Ring", other "%d Ringe";
  - fr: one "%d anneau", other "%d anneaux";
  - es: one "%d banda", other "%d bandas";
  - ja: other "%d本";
  - uk: one "%d смуга", few "%d смуги", many "%d смуг", other "%d смуги".
- [ ] T032 [US2] In `UI/ColorCodeScreen.kt`, add a `PrimaryTabRow` with 4 tabs (`pluralStringResource(resistor_color_code_bands, n, n)`, `testTag("rcc-tab-$n")`) over a `HorizontalPager(testTag("rcc-pager"))` with one page per `BandLayout`.
  - Sync both ways: `LaunchedEffect(pagerState.currentPage) { state.layout = BandLayout.entries[it] }`, and the initial page comes from `state.layout`.
  - Each page renders that layout's selection (`state.selections[layout]`) with `onSelect` bound to that layout.
  - Change `ColorCodeState.select` to take the layout explicitly.
- [ ] T033 [US2] Extend `UI/ResistorDrawing.kt` with band positions and labels for Three (1st, 2nd, MUL), Five (1st, 2nd, 3rd, MUL, TOL) and Six (… TOL, TCR). Check that labels don't overlap at a 0.5-width drawing on a 360dp screen, and shorten the leader layout if needed. T030 passes on a device.

**Checkpoint**: All four layouts work and keep their own state.

---

## Phase 5: User Story 3 - Any supported language and setting (Priority: P2)

**Goal**: Locale-aware numbers, plural tab names, a working screen reader, and good behavior in
dark theme, landscape and with large fonts.

**Independent Test**: Ukrainian shows "4,7kΩ ±5%" and "5 смуг". TalkBack reads "Band 1, Yellow, 4, selected".

### Tests for User Story 3 ⚠️

- [ ] T034 [P] [US3] Extend `CORE_TEST/FormatTest.kt` with locales:
  - `Locale.forLanguageTag("uk")` gives "4,7kΩ ±5%", "0,22Ω ±10%", "×0,1Ω" and "±0,25%";
  - "de" gives "4,7kΩ ±5%";
  - "ja" and "en" give "4.7kΩ ±5%";
  - "fr" gives "4,7kΩ ±5%".
- [ ] T035 [US3] Extend `UI_TEST/ColorCodeScreenTest.kt`:
  - `rcc-cell-0-Yellow` has a content description that contains "Band 1", "Yellow" and "4", and is `assertIsSelected()`;
  - an empty cell `rcc-cell-0-Black` has no click action;
  - `rcc-value` has a polite `LiveRegion`.

### Implementation for User Story 3

- [ ] T036 [P] [US3] Add the screen-reader strings to `RES/values/strings.xml` and all 5 translations: `resistor_color_code_cell_description` "%1$s, %2$s, %3$s", `_band_multiplier` "Multiplier", `_band_tolerance` "Tolerance", `_band_tcr` "Temperature coefficient". The digit bands reuse `_header_band`.
- [ ] T037 [US3] In `UI/ColorTable.kt`, give each selectable cell `semantics { contentDescription = cell_description(bandName, colorName, label) }`. Empty cells get `clearAndSetSemantics {}`. In `UI/ColorCodeScreen.kt`, give the value `semantics { liveRegion = LiveRegionMode.Polite }`. T034 and T035 pass.
- [ ] T038 [US3] Large font and narrow screens, in `UI/ColorTable.kt` and `UI/ColorCodeScreen.kt`:
  - cell labels use `maxLines = 1` with `autoSize` (`TextAutoSize.StepBased(minFontSize = 10.sp)`), or `softWrap = false` with `overflow = Clip` if autoSize isn't available;
  - the name column uses weight 1.3 against 1 for band columns;
  - in landscape (width > height), put the summary row above the table at full width, with the drawing at 40% width.

  Check visually at 360dp and 200% font size.

**Checkpoint**: All stories are complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T039 [P] Search `modules/resistor-color-code/src/main` for hardcoded user-facing strings: `Text("` and `contentDescription = "`. There must be none, apart from unit symbols built in `core`.
- [ ] T040 [P] Check that `modules/resistor-color-code/core` has no `android` imports: `rg "^import android" modules/resistor-color-code/core` returns nothing.
- [ ] T041 Update `specs/002-resistor-color-code/research.md` R6 with the final palette, if T020 changed it.
- [ ] T042 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease`. It must be green, and `LocaleCoverageTest` and `ModuleRegistryTest` must pass with the new module.
- [ ] T043 Run `./gradlew :modules:resistor-color-code:connectedDebugAndroidTest` on an emulator or device. All tests must pass.
- [ ] T044 Do the manual checks in `specs/002-resistor-color-code/quickstart.md` §4: compare with the reference image, the values for each tab, Ukrainian, dark theme (outline visible on White and Black), landscape, 200% font and TalkBack.
- [ ] T045 Push branch `002-resistor-color-code` and open a PR to `main`. The description cites IEC 60062:2016 and lists the differences from the reference image (spec Assumptions). Merge after CI is green and T043/T044 are done.

---

## Dependencies & Execution Order

- **Setup (T001–T008)**: T001 → T002 → (T003, T004, T005, T006 in parallel) → T007 → T008.
- **Foundational (T009–T014)**: needs T003. The tests T010 and T012 come before T011 and T013–T014.
- **US1 (T015–T027)**: needs Foundational. T015, T016 and T017 are written first. Then T019 (needs T015), T020 (needs T017), T021 (needs T016 and T019), T022 and T023 in parallel, then T024 → T025 → T026 → T027.
- **US2 (T028–T033)**: needs US1's T019, T021 and T027. T028 and T029 run in parallel with T031.
- **US3 (T034–T038)**: needs US1. Its core part (T034) can run alongside US2.
- **Polish (T039–T045)**: needs all stories.

### Parallel Opportunities

- Setup: T003, T004, T005 and T006.
- Foundational: T009, T010 and T012.
- US1: T015, T016 and T017 (tests), and T022 and T023 (strings).
- US2: T028, T029 and T031.
- US3: T034 and T036.

## Parallel Example: User Story 1

```text
Task: "T015 ReadingTest (4-band) in CORE_TEST/ReadingTest.kt"
Task: "T016 FormatTest (en) in CORE_TEST/FormatTest.kt"
Task: "T017 PaletteContrastTest in CORE_TEST/PaletteContrastTest.kt"
Task: "T022 English table strings in RES/values/strings.xml"
```

---

## Implementation Strategy

1. Setup, then Foundational: the module is listed and the model is tested.
2. US1: a usable 4-band calculator. **Stop and validate** against the reference image.
3. US2: all layouts, plus the exhaustive check.
4. US3: locale formatting, the screen reader, and large font and landscape support.
5. Polish, then the PR.

Commit after each task or logical group, in English (Principle VI).

## Notes

- Every new string goes into all six locales in the same phase. `LocaleCoverageTest` fails otherwise.
- Values in tests come from research R1 (IEC 60062:2016), never from `BandColor` itself. The exhaustive test re-types the table on purpose.
