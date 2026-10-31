---

description: "Task list for 003-inductor-color-code"
---

# Tasks: Inductor Color Code Calculator

**Input**: Design documents from `/specs/003-inductor-color-code/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Principle IV makes JVM tests mandatory for calculation logic and requires them to be
written first. The expected values come from the Bourns MIL/EIA sheets ([research R1](research.md))
and the spec's acceptance scenarios. Logic-bearing UI gets Compose instrumented tests, which need an
emulator or device. The resistor's existing tests guard the refactor in Phase 2.

**Organization**: Tasks are grouped by user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: US1 is 4-band reading, US2 is the 3-band tab and the decimal point, US3 is
  localization, accessibility and settings

## Path Conventions

- `SHARED` = `shared/color-code/src/main/kotlin/io/github/kolod/circuitopedia/shared/colorcode`
- `SHARED_TEST` = `shared/color-code/src/test/kotlin/io/github/kolod/circuitopedia/shared/colorcode`
- `SHARED_FIX` = `shared/color-code/src/testFixtures/kotlin/io/github/kolod/circuitopedia/shared/colorcode`
- `SUI` = `shared/color-code-ui/src/main/kotlin/io/github/kolod/circuitopedia/shared/colorcode/ui`
- `SUI_RES` = `shared/color-code-ui/src/main/res`
- `RCORE` = `modules/resistor-color-code/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode/core`
- `RUI` = `modules/resistor-color-code/src/main/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode`
- `CORE` = `modules/inductor-color-code/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/inductorcolorcode/core`
- `CORE_TEST` = `modules/inductor-color-code/core/src/test/kotlin/io/github/kolod/circuitopedia/modules/inductorcolorcode/core`
- `UI` = `modules/inductor-color-code/src/main/kotlin/io/github/kolod/circuitopedia/modules/inductorcolorcode`
- `UI_TEST` = `modules/inductor-color-code/src/androidTest/kotlin/io/github/kolod/circuitopedia/modules/inductorcolorcode`
- `RES` = `modules/inductor-color-code/src/main/res`
- Locales: `values` (en), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: The shared libraries and the new module build, and the module shows up in the catalog.

- [X] T001 In `settings.gradle.kts`, auto-include every `shared/<name>` that has a `build.gradle.kts` as `:shared:<name>`, sorted, with a comment saying shared libraries are not app modules and are never auto-added to `:app` (research R4).
- [X] T002 [P] Create `shared/color-code/build.gradle.kts`: plugins `kotlin-jvm` and `java-test-fixtures`, JVM target 17 (as `modules/resistor-color-code/core/build.gradle.kts`), `testImplementation(libs.junit)`, `testFixturesImplementation(libs.junit)`.
- [X] T003 [P] Create `shared/color-code-ui/build.gradle.kts`: android-library + kotlin-compose, `namespace = "io.github.kolod.circuitopedia.shared.colorcode.ui"`, compileSdk 37, minSdk 26, compileOptions 17, compose on, `resourcePrefix = "color_code_"`, the same lint errors as the modules; `api(project(":shared:color-code"))`, `implementation` Compose BOM, foundation, material3, ui and `libs.androidsvg`.
- [X] T004 [P] Create `modules/inductor-color-code/core/build.gradle.kts` (kotlin-jvm, JVM 17), with `api(project(":shared:color-code"))` and `testImplementation(libs.junit)`.
- [X] T005 [P] Create `modules/inductor-color-code/build.gradle.kts` like the resistor module's: namespace `io.github.kolod.circuitopedia.modules.inductorcolorcode`, `resourcePrefix = "inductor_color_code_"`. Dependencies are `:module-api`, `:modules:inductor-color-code:core`, `:shared:color-code-ui`, Compose, the test and androidTest dependencies of the resistor module, and `testImplementation(testFixtures(project(":shared:color-code")))`.
- [X] T006 [P] Create `RES/drawable/inductor_color_code_icon.xml`, a 24dp single-color vector of a coil (loops between two leads, white fill, no tint attribute).
- [X] T007 [P] Create `RES/values*/strings.xml` with `inductor_color_code_title` and `inductor_color_code_description`:
  - en: Inductor color code / Inductance from color bands;
  - de: Induktivitäts-Farbcode / Induktivität aus Farbringen;
  - fr: Code couleur des inductances / Inductance à partir des anneaux de couleur;
  - es: Código de colores de inductores / Inductancia a partir de bandas de color;
  - ja: インダクタのカラーコード / 色帯からインダクタンスを求める;
  - uk: Кольорове маркування котушок індуктивності / Індуктивність за кольоровими смугами.
- [X] T008 Create `UI/InductorColorCodeModule.kt` with `val inductorColorCodeModule = AppModule(ModuleId("inductor-color-code"), Category.Calculations, R.string.inductor_color_code_title, R.string.inductor_color_code_description, R.drawable.inductor_color_code_icon) { InductorScreen() }`, and a temporary empty `@Composable fun InductorScreen() {}` in `UI/InductorScreen.kt`.
- [X] T009 Register `inductorColorCodeModule,` in `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.

**Checkpoint**: `./gradlew test lint :app:assembleDebug` passes. The catalog lists both calculators.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The shared libraries exist, the resistor module runs on them unchanged in behavior,
and the inductor's code table is in place.

### Shared core and resistor core refactor

- [X] T010 [P] Write `SHARED_TEST/FormatSiTest.kt` (test-first) for `formatSi` and `formatDecimal` (contract core-api):
  - resistor family: 0.1Ω, 4.7kΩ, 1kΩ, 999GΩ;
  - inductor family from µH: 0.1 → "100nH", 0.22 → "220nH", 4.7 → "4.7µH", 1000 → "1mH", 99000 → "99mH";
  - decimal comma in `uk`/`de`, dot in `en`/`ja`.
- [X] T011 Move `BandColor` (argb and textOnColor only), `TextOnColor`, `Percent` and `Contrast.kt` from `RCORE` to `SHARED`, with package `io.github.kolod.circuitopedia.shared.colorcode`. Keep the entry order and the palette comment (spec 002 R6).
  - Add `SHARED/Format.kt` with `formatDecimal` and `formatSi`, so that T010 passes.
  - Move `PaletteContrastTest` to `SHARED_TEST`.
- [X] T012 Create `RCORE/ResistorCode.kt` with the IEC 60062 meanings as `BandColor` extensions (`digit`, `multiplierExponent`, `tolerance`, `tcr`), keeping the citation comment from the old `BandColor.kt`.
  - Point the resistor core's `build.gradle.kts` at `api(project(":shared:color-code"))`.
  - Switch `Units.kt`, `Format.kt` (use `formatSi`/`formatDecimal`), `BandLayout.kt`, `Selection.kt` and the resistor core tests to the shared types.
  - All 39 resistor core tests must pass unchanged in substance.
- [X] T013 [P] Create `SHARED_FIX/SvgContract.kt`, `object SvgContract { fun check(svg: File, bandCount: Int): List<String> }`, implementing the four rules of spec 002 R10 with `javax.xml` DOM. Rewrite `modules/resistor-color-code/src/test/…/ResistorSvgTest.kt` to use it for the file checks and keep its layout and CSS tests.

### Shared UI and resistor UI refactor

- [X] T014 Move the common strings from the resistor module to `SUI_RES/values*/strings.xml`, renamed `color_code_*`, in all six locales:
  - the `bands` plurals;
  - the 12 color names;
  - `header_band`, `header_multiplier`, `header_tolerance`;
  - `band_multiplier`, `band_tolerance`;
  - `cell_description`.

  Remove them from `modules/resistor-color-code/src/main/res`. Only the title, description and TCR strings stay there.
- [X] T015 Create `SUI/ColorNames.kt` (`BandColor.nameRes()`). Move the table from `RUI/ColorTable.kt` to `SUI/ColorTable.kt`, made data-driven with `TableColumn` (contract shared-ui):
  - the header is built from `columns.map { it.header }`;
  - a cell is selectable iff its color is in `labels`;
  - the description comes from `color_code_cell_description` with `bandName`, the color name and the label;
  - geometry (`CellGap`, `TableSidePadding`, `bandColumnCenters`) and `CellText` move unchanged.
- [X] T016 Move `RUI/ResistorDrawing.kt`'s SVG loading, CSS and leader lines to `SUI/BandDrawing.kt` as `BandDrawing(svg, shownBands, colors, modifier)`, with the band count read from the SVG. `RUI/ResistorDrawing.kt` keeps only `BandLayout.svgBands()` and a call to `BandDrawing`.
- [X] T017 Create `SUI/ColorCodeScaffold.kt` from `RUI/ColorCodeScreen.kt`, following the contract: `bandCounts`, `selectedTab`, `onTabSelected`, `tagPrefix`, `page`, `onSelect`, and `ColorCodePage(value, columns, drawing)`. Rewrite `RUI/ColorCodeScreen.kt` to build the `ColorCodePage` from the resistor core and call the scaffold with `tagPrefix = "rcc"`. Update `RUI/Labels.kt` to the shared strings.
- [X] T018 Update `modules/resistor-color-code/build.gradle.kts`:
  - depend on `:shared:color-code-ui`;
  - drop the direct AndroidSVG dependency;
  - add `testImplementation(testFixtures(project(":shared:color-code")))`.
- [X] T019 Amend `specs/001-app-shell-navigation/contracts/module-contract.md` §"What a module MUST provide" item 2: a module MAY depend on `:shared:*` libraries (top-level `shared/`), never on another `:modules:*` project or `:app`. Also amend `modules/README.md`, if it lists allowed dependencies.
- [X] T020 Run `./gradlew test lint :app:assembleDebug` and `./gradlew :modules:resistor-color-code:connectedDebugAndroidTest`. All tests pass, including the resistor's 8 UI tests with their `rcc-*` tags. On the emulator the resistor screen looks as before.

### Inductor code table

- [X] T021 [P] Write `CORE_TEST/InductorCodeTableTest.kt` (test-first), checking every cell of the research R1 table:
  - `digit` for Black … White, null for Gold and Silver;
  - `multiplierExponent` 0, 1, 2, 3 for Black … Orange, −1 for Gold, −2 for Silver, null otherwise;
  - `tolerance` 5 for Gold, 10 for Silver, null otherwise;
  - `isDecimalPoint` only for Gold.
- [X] T022 Create `CORE/Units.kt` (`Microhenries`, BigDecimal > 0) and `CORE/InductorCode.kt` with the four extensions and the Bourns citation comment (research R1, including the discrepancies), so that T021 passes.

**Checkpoint**: The resistor works on the shared libraries; the inductor table is verified.

---

## Phase 3: User Story 1 - Read a 4-band inductor (Priority: P1) 🎯 MVP

**Goal**: The 4-band tab reads values without a decimal point (Gold/Silver as multipliers).

**Independent Test**: Yellow, Violet, Black, Silver shows "47µH ±10%". Brown, Black, Brown, Gold
shows "100µH ±5%". Red, Red, Gold, Gold shows "2.2µH ±5%".

### Tests for User Story 1 (write first, must fail)

- [X] T023 [P] [US1] Write `CORE_TEST/ReadingTest.kt` for 4 bands without a point:
  - 47µH ±10%;
  - 100µH ±5%;
  - 2.2µH ±5%;
  - Brown, Black, Silver, Silver = 0.1µH;
  - White, White, Orange, Gold = 99 000µH.
- [X] T024 [P] [US1] Write `CORE_TEST/SelectionTest.kt` part 1:
  - the 4-band default;
  - `with` sets a valid band;
  - invalid taps are ignored: Black in Band 1, Silver in Band 1, Green as Mult., Red as Toler.;
  - `roleOf` is Digit, Digit, Multiplier, Tolerance.
- [X] T025 [P] [US1] Write `CORE_TEST/FormatTest.kt`:
  - `format` in `en`/`uk`/`de`/`ja` ("47µH ±10%", "4,7µH ±5%", "100nH ±10%", "99mH ±5%");
  - `cellLabel` for every role: "4", ".", "×0.01µH" … "×1mH", "±5%", "±10%".

### Implementation for User Story 1

- [X] T026 [US1] Create `CORE/BandLayout.kt` (`BandRole` incl. `DecimalPoint`, `BandLayout` Three/Four), `CORE/Selection.kt` (`Selection`, `Reading`, `pointIndex`, `roleOf`, `with`, `canSelect`, `read`, `default`) and `CORE/Format.kt` (`Reading.format`, `BandRole.cellLabel` using `formatSi`), following data-model.md and contracts/core-api.md, so that T023–T025 pass.
- [X] T027 [P] [US1] Create `RES/raw/inductor_color_code_inductor.svg` following research R5:
  - a molded choke in pale green with rounded ends and gray leads;
  - `band-1` … `band-4` as paths with `fill` attributes, band 4 near the right end;
  - a `viewBox`, no transforms.

  Add `modules/inductor-color-code/src/test/…/InductorSvgTest.kt`, which asserts `SvgContract.check(file, 4)` is empty.
- [X] T028 [US1] Create `UI/InductorState.kt`: the selected layout plus one `Selection` per layout, `select(layout, index, color)` via `with`, a `Saver` as in data-model.md, and `rememberInductorState()` defaulting to `Four`.
- [X] T029 [US1] Create `UI/InductorDrawing.kt` (`BandDrawing` with the inductor SVG; `shownBands` [1,2,3] or [1,2,3,4]). Replace the placeholder in `UI/InductorScreen.kt` with `ColorCodeScaffold`:
  - `bandCounts` [3, 4], `tagPrefix = "icc"`;
  - each page's columns: header and band name from `roleOf`, `labels` = the colors with `canSelect`, text from `roleOf(i).cellLabel`, with Gold in Bands 1–2 always ".";
  - the value from `read().format(locale)`.
- [X] T030 [US1] Write `UI_TEST/InductorScreenTest.kt` part 1 (page-scoped lookups as in the resistor test):
  - the 4-band default "47µH ±10%";
  - Brown, Black, Brown, Gold → "100µH ±5%";
  - an invalid cell does nothing.

  Run on an emulator.

**Checkpoint**: A 4-band inductor can be read end to end.

---

## Phase 4: User Story 2 - 3 bands and the decimal point (Priority: P1)

**Goal**: The 3-band tab, and Gold as a decimal point with the replacement rules.

**Independent Test**: Spec US2 acceptance scenarios 1–7 and quickstart §4 steps 3–5.

### Tests for User Story 2 (write first, must fail)

- [X] T031 [P] [US2] Extend `CORE_TEST/SelectionTest.kt` with the decimal-point rules (research R3):
  - Yellow, Gold, Violet → roles Digit, DecimalPoint, Digit;
  - tap Violet in Band 2 → Yellow, Violet, Black;
  - tap Gold in Band 1 → Gold, Violet, Violet;
  - tap Gold in Band 2 while Band 1 is Gold → Band 1 becomes Yellow;
  - Black is not selectable in Band 2 when the point is in Band 1;
  - Gold/Silver is not selectable in Band 3 in point mode;
  - the 3-band default.
- [X] T032 [P] [US2] Extend `CORE_TEST/ReadingTest.kt`:
  - 4.7µH: Yellow, Gold, Violet and Yellow, Violet, Gold;
  - 220nH: Gold, Red, Red and Red, Red, Silver;
  - 770nH;
  - Gold, Brown, Black = 100nH;
  - the 3-band default = 47µH ±20%;
  - Bourns' examples: 6.8µH ±10% (Blue, Gold, Gray, Silver and Blue, Gray, Gold, Silver) and 270µH ±5%.
- [X] T033 [P] [US2] Write `CORE_TEST/ExhaustiveReadingTest.kt`. For both layouts, enumerate all color tuples, keep those for which `Selection` construction is valid, and compare `read()` and `format(Locale.ENGLISH)` with an independent oracle in the test (a literal table plus string arithmetic). Assert the counts 720 and 1,440.

### Implementation for User Story 2

- [X] T034 [US2] Complete `CORE/Selection.kt`: the `init` validity check, the point-aware `roleOf`, the replacement loop in `with`, and the three `read` cases, so that T031–T033 pass.
- [X] T035 [US2] In `UI/InductorScreen.kt`, the third header and band name follow `roleOf(2)`: "Band 3" in point mode, "Mult." otherwise. Every band replaced by the rules updates in the same frame (state from `Selection`).
- [X] T036 [US2] Extend `UI_TEST/InductorScreenTest.kt`:
  - the 3-band default "47µH ±20%";
  - Yellow, Gold, Violet, Silver → "4.7µH ±10%" with header "Band 3";
  - tap Gold in Band 1 → "770nH ±10%";
  - tap Yellow in Band 1 → "47µH ±10%" with header "Mult.";
  - per-tab memory, swipe, and state restoration (`StateRestorationTester`).

**Checkpoint**: All spec acceptance scenarios for US1 and US2 pass.

---

## Phase 5: User Story 3 - Any language and setting (Priority: P2)

**Goal**: Six languages, screen reader, dark theme and 200% font.

**Independent Test**: Ukrainian shows "4,7µH ±5%" and "3 смуги". TalkBack reads "Band 2, Gold,
Decimal point, selected".

- [X] T037 [P] [US3] Add `inductor_color_code_decimal_point` to `RES/values*/strings.xml`: Decimal point / Dezimalpunkt / Virgule décimale / Punto decimal / 小数点 / Десяткова кома. Use it as the cell meaning in screen-reader descriptions of Gold in Bands 1–2.
- [X] T038 [US3] Extend `UI_TEST/InductorScreenTest.kt`: the cell semantics are a content description of "Band 2, Gold, Decimal point" plus the selected state.
- [X] T039 [US3] On the emulator, check:
  - Ukrainian: value, tabs, colors;
  - dark theme: outline visible on White and Black;
  - 200% font: no label cut off in the 4-band table;
  - the screen-reader labels, through a uiautomator dump.

  Reset the font scale afterwards.

**Checkpoint**: All user stories work in every supported setting.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T040 [P] Check that `shared/color-code` and `modules/inductor-color-code/core` have no `android` imports: `rg "^import android" shared/color-code modules/inductor-color-code/core` returns nothing.
- [X] T041 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease`. It must be green, with `LocaleCoverageTest` and `ModuleRegistryTest` passing.
- [X] T042 Run `./gradlew connectedDebugAndroidTest` on an emulator: app 16, resistor 8 and inductor tests all pass.
- [X] T043 Do the manual checks in `specs/003-inductor-color-code/quickstart.md` §4, including step 7, which checks that the resistor is unchanged.
- [ ] T044 Push branch `003-inductor-color-code` and open a PR to `main`. The description cites the Bourns MIL/EIA sheets, summarizes the R1 discrepancies and describes the shared-library refactor. Merge after CI is green and T042/T043 are done.

---

## Dependencies & Execution Order

- **Phase 1** first. T001 comes before T002–T005, since the settings must include the projects.
- **Phase 2** blocks all stories:
  - T010 → T011 → T012 (shared core, then resistor core);
  - T014 → T015 → T016 → T017 → T018 (shared UI, then resistor UI);
  - T020 verifies the refactor;
  - T021 → T022 is independent of the refactor and can run alongside T014–T019.
- **US1** (Phase 3) needs Phase 2. **US2** needs US1's `Selection` (T026). **US3** needs US1 and US2's screen.
- **Polish** last.

### Parallel Opportunities

- T002–T007 (different files).
- T010, T013 and T021 (different projects).
- T023–T025 (separate test files); T027 alongside T026.
- T031–T033 (separate test files).

## Implementation Strategy

1. **MVP**: Phases 1–3. A 4-band inductor can be read, and the resistor runs on the shared code.
2. **Then** Phase 4: decimal point and 3 bands. With it, US2 and the spec scenarios are done.
3. **Then** Phase 5 and the Polish phase. After that, the PR.

Commit after each task or logical group, in English. Keep the refactor (Phase 2) in its own commits
so it can be reviewed apart from the new module.
