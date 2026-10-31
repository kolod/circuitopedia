---

description: "Task list for 005-voltage-divider"
---

# Tasks: Voltage Divider Calculator

**Input**: Design documents from `/specs/005-voltage-divider/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Principle IV makes JVM tests mandatory for calculation logic, and they are written
first. The expected values come from the cited divider formula, the spec's acceptance scenarios, a
round-trip property test and an exhaustive reference search. Logic-bearing UI gets Compose
instrumented tests.

**Organization**: Tasks are grouped by user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**:
  - US1: solving the divider;
  - US2: building it from standard resistors;
  - US3: localization, accessibility and settings.

## Path Conventions

- `RES_LIB` = `shared/resistors/src/main/kotlin/io/github/kolod/circuitopedia/shared/resistors`
- `RES_LIB_TEST` = `shared/resistors/src/test/kotlin/io/github/kolod/circuitopedia/shared/resistors`
- `COMB_CORE` = `modules/resistor-combinations/core/src/{main,test}/kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations/core`
- `CORE` = `modules/voltage-divider/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/voltagedivider/core`
- `CORE_TEST` = `modules/voltage-divider/core/src/test/kotlin/io/github/kolod/circuitopedia/modules/voltagedivider/core`
- `UI` = `modules/voltage-divider/src/main/kotlin/io/github/kolod/circuitopedia/modules/voltagedivider`
- `UI_TEST` = `modules/voltage-divider/src/androidTest/kotlin/io/github/kolod/circuitopedia/modules/voltagedivider`
- `RES` = `modules/voltage-divider/src/main/res`
- Locales: `values` (en), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: The reused spec 004 code lives in `:shared:resistors`, and the new module builds and
shows up in the catalog.

- [X] T001 Create `shared/resistors/build.gradle.kts`:
  - kotlin-jvm, JVM 17;
  - `api(project(":shared:si-format"))`;
  - `testImplementation(libs.junit)`.
- [X] T002 Move these files with `git mv` from `COMB_CORE` to `RES_LIB`, changing their package to
  `io.github.kolod.circuitopedia.shared.resistors`:
  - `Rational.kt`;
  - `ESeries.kt`;
  - `TargetParser.kt`, renamed `ResistanceParser.kt`, with `parseTarget` renamed `parseResistance`;
  - `Format.kt`.

  Split `Connection.kt`:
  - `Kind` and `Combination` move to `RES_LIB/Combination.kt`;
  - `PER_KIND` and `rankingFor` stay in `COMB_CORE/Ranking.kt`.

  Move `RationalTest`, `ESeriesTest`, `ParserTest` and `FormatTest` to `RES_LIB_TEST`.
- [X] T003 Point `modules/resistor-combinations/core/build.gradle.kts` at `api(project(":shared:resistors"))`.
  - Fix the imports in the combinations core, its tests, `CombinationsScreen.kt` and
    `Spoken.kt`, and the `CombinationsScreenTest`, including the `parseTarget` →
    `parseResistance` rename.
  - Update `specs/004-resistor-combinations/contracts/core-api.md` to note the move.
  - Run `./gradlew test` and `./gradlew :modules:resistor-combinations:connectedDebugAndroidTest`:
    all pass, unchanged.
- [X] T004 [P] Create `modules/voltage-divider/core/build.gradle.kts` (kotlin-jvm, JVM 17, `api(project(":shared:resistors"))`, `testImplementation(libs.junit)`).
- [X] T005 [P] Create `modules/voltage-divider/build.gradle.kts` like `modules/resistor-combinations/build.gradle.kts`:
  - namespace `io.github.kolod.circuitopedia.modules.voltagedivider`;
  - `resourcePrefix = "voltage_divider_"`;
  - dependency on `:modules:voltage-divider:core`.
- [X] T006 [P] Create `RES/drawable/voltage_divider_icon.xml`: a 24dp single-color vector of two resistor boxes in a vertical chain with a tap between them (white, no tint).
- [X] T007 [P] Create `RES/values*/strings.xml` with `title` and `description`:
  - en: Voltage divider / Solve a resistive divider and pick standard resistors;
  - de: Spannungsteiler / Teiler berechnen und Normwiderstände wählen;
  - fr: Diviseur de tension / Calculer un diviseur et choisir des résistances normalisées;
  - es: Divisor de tensión / Calcular un divisor y elegir resistencias normalizadas;
  - ja: 分圧回路 / 分圧回路を計算し標準抵抗を選ぶ;
  - uk: Дільник напруги / Розрахунок дільника й підбір стандартних резисторів.
- [X] T008 Create `UI/VoltageDividerModule.kt` (`val voltageDividerModule = AppModule(ModuleId("voltage-divider"), Category.Calculations, …) { DividerScreen() }`) and a placeholder `UI/DividerScreen.kt`. Register the module in `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.

**Checkpoint**: `./gradlew test lint :app:assembleDebug` passes. The catalog lists four calculators,
and the combinations module behaves exactly as before.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Voltages can be parsed and shown.

- [X] T009 [P] Write `CORE_TEST/VoltageParserTest.kt` (test-first). Cases:
  - accepted: "5", "3.3", "3,3", "-12", "4.7m", "4,7 mV", "1k", "+5V", " 12 V ";
  - rejected: "", "abc", "0", "0.0005", "200k", "1.2.3", "5 VV", "1M";
  - exact `Rational` results.
- [X] T010 [P] Write `CORE_TEST/VoltageFormatTest.kt`: "3.837V", "4.7mV", "12V", "1kV", "−12V" (U+2212), and `uk` "3,837V".
- [X] T011 Create `CORE/Quantity.kt` (`enum class Quantity { U1, U2, R1, R2, R }`) and `CORE/Voltage.kt` (`parseVoltage`, `formatVolts`, with the sign handled outside `formatSi`), so that T009 and T010 pass.

**Checkpoint**: Parsing and formatting are verified.

---

## Phase 3: User Story 1 - Find the missing values (Priority: P1) 🎯 MVP

**Goal**: Enter three values, tap Calculate, and see the other two.

**Independent Test**: The nine cases and the spec scenarios. Invalid sets show messages, and edits
clear the results.

### Tests for User Story 1 (write first, must fail)

- [X] T012 [P] [US1] Write `CORE_TEST/SolveTest.kt`:
  - **Spec scenarios:**
    - 5V / 3.3V / 10k gives R2 = 19.41k and R = 29.41k, exactly 330000/17 and 500000/17;
    - 12V / 10k / 4.7k gives U2 = 3.837V and R = 14.7k;
    - 2.5V / 1k / 3k gives R2 = 2k and U1 = 3.75V;
    - 9V / 3V / 30k gives R1 = 20k and R2 = 10k.
  - **One case per row of the research R1 table.**
  - **Results that are not solved:**
    - `NotEnough(1)` and `TooMany(1)`;
    - `NoVoltage` for {R1, R2, R};
    - `Invalid(RatioOutOfRange)` for U2 ≥ U1 and for U2 = 0;
    - `Invalid(MixedSigns)`;
    - `Invalid(ResistanceNotBelowTotal)` for R1 ≥ R.
  - **Negative voltages:** -12V / -3V works.
  - **Property round trip:**
    - draw 200 seeded random dividers (U1, R1, R2);
    - compute all five quantities with the forward formula;
    - for every valid triple, `solve` must return the other two exactly (rational equality).
  - **`canCalculate`** matches `solve` for all subsets.

### Implementation for User Story 1

- [X] T013 [US1] Create `CORE/Solve.kt` (`SolveResult`, `solve`, `canCalculate`) per research R1 and contracts/core-api.md, with the formula citation comment, so that T012 passes.
- [X] T014 [US1] Add the strings to `RES/values*/strings.xml` in all six locales:
  - `note_unloaded`;
  - `label_u1`, `label_u2`, `label_r1`, `label_r2` and `label_r`;
  - `computed` and `calculate`;
  - `need_more` and `too_many` (plurals);
  - `no_voltage`;
  - `error_parse`, `error_ratio`, `error_signs` and `error_total`.
- [X] T015 [US1] Create `UI/Schematic.kt`, a Canvas drawing of U1 ● ─ [R1] ─ ● U2 ─ [R2] ─ ⏚ laid out vertically:
  - labels drawn with `rememberTextMeasurer`;
  - a brace for R;
  - theme colors;
  - `clearAndSetSemantics {}`.
- [X] T016 [US1] Implement `UI/DividerScreen.kt` per contracts/ui-contract.md as one `LazyColumn`:
  - the schematic and the unloaded note;
  - five `OutlinedTextField`s with V/Ω suffixes, with computed ones tinted and labeled "computed";
  - the Calculate button, enabled by `canCalculate`;
  - the message line, with priority as in the contract;
  - the rules: Calculate fills the computed fields; editing an entered field clears them; typing into a computed field makes it entered;
  - `rememberSaveable` for the texts and origins;
  - test tags.
- [X] T017 [US1] Write `UI_TEST/DividerScreenTest.kt` part 1:
  - Calculate is disabled with 2 fields filled and enabled with 3;
  - 5 / 3.3 / 10k then Calculate fills R2 and R;
  - editing U1 clears them;
  - typing into a computed field makes it entered;
  - {R1, R2, R} keeps Calculate disabled with the note;
  - U2 > U1 shows an error;
  - state restoration.

  Run it on an emulator.

**Checkpoint**: The divider is solved end to end.

---

## Phase 4: User Story 2 - Build from standard resistors (Priority: P1)

**Goal**: Builds of the divider from standard resistors, ranked by U2 error within ±10% of R.

**Independent Test**: The fast search equals the exhaustive reference. On E24, 9V / 3V / 30k puts
20k / 10k at 0% first.

### Tests for User Story 2 (write first, must fail)

- [ ] T018 [P] [US2] Write `CORE_TEST/ReferenceBuildSearch.kt`, an exhaustive exact search over every arm build pair (Single, Series2 or Parallel2 for each computed arm; the entered value for an entered arm), with:
  - the total in [0.9·Rt, 1.1·Rt];
  - the FR-010 ranking;
  - the best 5 per chosenCount, merged.

  Write `CORE_TEST/BuildSearchReferenceTest.kt`. Its cases are:
  - ratios ≈ 0.1, 0.5 and 0.9 with totals 100Ω, 10k and 5M on E3 and E6 (all tiers);
  - E12 with an entered R1, and with an entered R2;
  - a target near the end of the value range.

  `builds(...)` must equal the reference exactly.
- [ ] T019 [P] [US2] Write `CORE_TEST/BuildSearchExamplesTest.kt`. Cases:
  - **R1 entered:** 5V / 3.3V / R1 = 10k on E24; every build keeps R1 = 10k, and chosenCount ∈ {1, 2}.
  - **Both arms chosen:** 9V / 3V / 30k on E24; the first build is 20k / 10k with u2Error 0 and chosenCount 2.
  - **Window:** every total within ±10%.
  - **Tiers:** at most 5 per tier.
  - **Order:** sorted by FR-010.
  - **Nothing to choose:** R1 and R2 both entered give an empty list.
  - **Cancellation:** the cancellation callback stops the search.
- [ ] T020 [P] [US2] Write `CORE_TEST/BuildSearchBudgetTest.kt`: E192, both arms chosen, 10 fixed targets. `buildEvaluations` stays under a budget, set to about 3× the measured maximum. The timing is printed only.

### Implementation for User Story 2

- [ ] T021 [US2] Create `CORE/BuildSearch.kt` per research R4, so that T018–T020 pass:
  - `DividerBuild`;
  - `builds`;
  - `buildEvaluations`;
  - banded arm lists for Single, Series2 and Parallel2;
  - the one-arm walk;
  - the two-arm two-pass search with the window clamp;
  - `Double` keys with a margin, then an exact re-rank.
- [ ] T022 [US2] Add the strings `builds_header`, `series_label`, `no_builds`, `build_u2` and `build_r` to `RES/values*/strings.xml` in six locales. Then extend `UI/DividerScreen.kt`:
  - after a Solved result with R1 or R2 computed, show the series chips (E24 first, `rememberSaveable`) and the build rows;
  - each row shows R1 and R2 as connection text, then "U2 = … (±…%)" and "R = … (±…%)";
  - the builds run on `Dispatchers.Default` with `ensureActive`;
  - when there are no builds, show the `no_builds` message.
- [ ] T023 [US2] Extend `UI_TEST/DividerScreenTest.kt`:
  - 9 / 3 / 30k, then Calculate, shows builds, and the first one reads 20kΩ / 10kΩ at 0%;
  - switching to E3 updates the list;
  - with R1 and R2 entered, no build section is shown.

**Checkpoint**: Both P1 stories work.

---

## Phase 5: User Story 3 - Any language and setting (Priority: P2)

**Goal**: Spoken fields and builds, locale formatting, 200% font, dark theme.

**Independent Test**: In Ukrainian, "3,3" works and the results show decimal commas. TalkBack reads
"R2, 19.41 kilohm, computed".

- [ ] T024 [P] [US3] Add the strings `state_entered`, `state_computed` and the spoken unit and connection words to `RES/values*/strings.xml` in six locales.
- [ ] T025 [US3] Create `UI/Spoken.kt`, which builds:
  - field descriptions: "name, value with unit words, state";
  - build descriptions: R1 and R2 spoken, then U2 and its error, then R and its deviation.

  Apply both with semantics in `UI/DividerScreen.kt`.
- [ ] T026 [US3] Extend `UI_TEST/DividerScreenTest.kt`:
  - the computed field's content description;
  - a build row's content description.
- [ ] T027 [US3] On the emulator, check:
  - Ukrainian ("3,3", "4,7k", decimal commas);
  - dark theme;
  - 200% font (everything reachable, nothing cut off);
  - the spoken texts, using a uiautomator dump.

  Reset the settings afterwards.

**Checkpoint**: All stories work in every supported setting.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T028 [P] Check that `shared/resistors` and `modules/voltage-divider/core` have no `android` imports.
- [ ] T029 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease`. It must be green.
- [ ] T030 Run `./gradlew connectedDebugAndroidTest` on an emulator. All modules' tests must pass.
- [ ] T031 Do the manual checks in `specs/005-voltage-divider/quickstart.md` §4. For E192 with both arms chosen, measure the time in-process on the emulator with a temporary instrumented timing test, then delete it, as in spec 004.
- [ ] T032 Push branch `005-voltage-divider` and open a PR to `main`. The description:
  - cites the formula source;
  - lists the nine cases;
  - explains the banded search and its proof;
  - mentions the `:shared:resistors` extraction.

  Before merging, wait until CI is no longer pending on the latest commit.

---

## Dependencies & Execution Order

- **Phase 1**: T001 → T002 → T003 (the move, then the imports and verification). T004–T007 can run in parallel after T001. T008 comes after T004–T007.
- **Phase 2**: T009–T010 → T011.
- **US1**: T012 → T013, then T014 → T015 → T016 → T017.
- **US2**: needs T013 (Solved). T018–T020 → T021, then T022 → T023.
- **US3**: needs the US1 and US2 screens.
- **Polish**: last.

### Parallel Opportunities

- T004–T007.
- T009 and T010.
- T018–T020 (separate test files).
- T024 alongside T025 drafting.

## Implementation Strategy

1. **MVP**: Phases 1–3. The divider is solved, and the combinations module has moved onto
   `:shared:resistors` unchanged.
2. **Then** Phase 4 (standard resistors). With it, both P1 stories are done.
3. **Then** Phase 5 and Polish, and the PR.

Commit after each task or logical group, in English. The library extraction (T001–T003) goes in its
own commit.
