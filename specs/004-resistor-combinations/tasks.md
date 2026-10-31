---

description: "Task list for 004-resistor-combinations"
---

# Tasks: Resistor Combination Finder

**Input**: Design documents from `/specs/004-resistor-combinations/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Principle IV makes JVM tests mandatory for calculation logic and requires them to be
written first. The expected values come from IEC 60063:2015 ([research R1](research.md)), the
spec's acceptance scenarios, and an exhaustive reference search. Logic-bearing UI (input parsing
feedback, filters, saved state, semantics) gets Compose instrumented tests.

**Organization**: Tasks are grouped by user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: US1 is finding combinations, US2 is choosing connection kinds, US3 is localization,
  accessibility and settings

## Path Conventions

- `SI` = `shared/si-format/src/main/kotlin/io/github/kolod/circuitopedia/shared/siformat`
- `SI_TEST` = `shared/si-format/src/test/kotlin/io/github/kolod/circuitopedia/shared/siformat`
- `CORE` = `modules/resistor-combinations/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations/core`
- `CORE_TEST` = `modules/resistor-combinations/core/src/test/kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations/core`
- `UI` = `modules/resistor-combinations/src/main/kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations`
- `UI_TEST` = `modules/resistor-combinations/src/androidTest/kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations`
- `RES` = `modules/resistor-combinations/src/main/res`
- Locales: `values` (en), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: The SI formatter is in its own library, and the new module builds and shows up in the
catalog.

- [X] T001 Create `shared/si-format/build.gradle.kts` (kotlin-jvm, JVM 17, `testImplementation(libs.junit)`). Move `Format.kt` and `FormatSiTest.kt` from `shared/color-code` to `SI` and `SI_TEST` with `git mv`, changing the package to `io.github.kolod.circuitopedia.shared.siformat`.
- [X] T002 Add `api(project(":shared:si-format"))` to `shared/color-code/build.gradle.kts`. Update the `formatSi`/`formatDecimal` imports in:
  - `modules/resistor-color-code/core/.../Format.kt`;
  - `modules/inductor-color-code/core/.../Format.kt`;
  - `shared/color-code` (if any file uses them).

  Update `specs/003-inductor-color-code/contracts/core-api.md` to say the two functions moved. `./gradlew test` passes.
- [X] T003 [P] Create `modules/resistor-combinations/core/build.gradle.kts` (kotlin-jvm, JVM 17, `api(project(":shared:si-format"))`, `testImplementation(libs.junit)`).
- [X] T004 [P] Create `modules/resistor-combinations/build.gradle.kts` like `modules/inductor-color-code/build.gradle.kts`:
  - namespace `io.github.kolod.circuitopedia.modules.resistorcombinations`;
  - `resourcePrefix = "resistor_combinations_"`;
  - dependencies `:module-api`, `:modules:resistor-combinations:core`, the Compose BOM, foundation, material3 and ui, and the same test dependencies (no shared color code UI).
- [X] T005 [P] Create `RES/drawable/resistor_combinations_icon.xml`, a 24dp single-color vector of two resistors in parallel with a third in series (white, no tint).
- [X] T006 [P] Create `RES/values*/strings.xml` with the `title` and `description` keys:
  - en: Resistor combinations / Match a value with 1–3 standard resistors;
  - de: Widerstandskombinationen / Wert aus 1–3 Normwiderständen bilden;
  - fr: Combinaisons de résistances / Obtenir une valeur avec 1 à 3 résistances normalisées;
  - es: Combinaciones de resistencias / Obtener un valor con 1–3 resistencias normalizadas;
  - ja: 抵抗の組み合わせ / 1～3本の標準抵抗で値を作る;
  - uk: Комбінації резисторів / Номінал з 1–3 стандартних резисторів.
- [X] T007 Create `UI/ResistorCombinationsModule.kt` (`val resistorCombinationsModule = AppModule(ModuleId("resistor-combinations"), Category.Calculations, …) { CombinationsScreen() }`) and a placeholder `UI/CombinationsScreen.kt`. Register the module in `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.

**Checkpoint**: `./gradlew test lint :app:assembleDebug` passes. The catalog lists three calculators.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Exact numbers and the cited E-series data that every story uses.

- [X] T008 [P] Write `CORE_TEST/RationalTest.kt` (test-first):
  - reduction;
  - `+`;
  - `reciprocal`;
  - parallel of 10k and 10k equals 5k exactly;
  - `compareTo`, including the near-tie 1/3 against 0.3333333333;
  - `round(4)` HALF_UP (1234.5 → 1235 and 0.012345 → 0.01235).
- [X] T009 [P] Write `CORE_TEST/ESeriesTest.kt` (test-first):
  - counts 3/6/12/24/48/96/192;
  - all base values of E3–E24 literally;
  - spot values of E48/E96/E192;
  - E24's 8 irregular values;
  - E192 n=185 is 920;
  - E24 ⊄ E48 (150 missing);
  - E3 ⊂ … ⊂ E24 and E48 ⊂ E96 ⊂ E192;
  - `values()` from 1Ω to 10MΩ, ascending, with size 7·N+1.
- [X] T010 Create `CORE/Rational.kt` and `CORE/ESeries.kt`. `ESeries.kt` holds the R1 tables as literal integer lists, with the IEC 60063:2015 citation and the Wikipedia reproduction, plus the cached `values()`. T008 and T009 must pass.

**Checkpoint**: The data and arithmetic are verified.

---

## Phase 3: User Story 1 - Find combinations (Priority: P1) 🎯 MVP

**Goal**: Type a target, get the best combinations of every kind, sorted by deviation.

**Independent Test**: E24 and "1.234k" gives a list that equals the reference search. E12 4.7k
gives 4.7kΩ (0%) first. E12 5k gives an exact 2-resistor result before any exact 3-resistor one.

### Tests for User Story 1 (write first, must fail)

- [X] T011 [P] [US1] Write `CORE_TEST/ParserTest.kt` as a table of research R5 cases:
  - valid: "4700", "4.7k", "4,7k", "4k7", "4K7", "R47", "1M5", "47R", " 4.7 kΩ ", "100ohm", "0.1", "1G";
  - invalid: "", "abc", "4.7.1", "4,7,1", "-1", "0", "2G", "0.05", "4k7k", "k";
  - exact `Rational` results.
- [X] T012 [P] [US1] Write `CORE_TEST/ReferenceSearch.kt`, a slow exhaustive search over all combinations in canonical order with exact rationals and the FR-007 ranking, and `CORE_TEST/SearchReferenceTest.kt`. Both cases run with all kinds allowed:
  - a grid of targets (0.1Ω, 0.33Ω, 1Ω, 4.7Ω, 10Ω, 123Ω, 1k, 1.234k, 5k, 47k, 1M, 9.9M, 25M, 1G) on E3, E6 and E12;
  - 10 seeded random targets on E24.

  `search(...)` must equal the reference exactly: the same combinations in the same order.
- [X] T013 [P] [US1] Write `CORE_TEST/SearchExamplesTest.kt`:
  - E12 4.7k: the first result is Single 4.7kΩ;
  - E12 5k: the first result is 10k ∥ 10k, before any exact 3-resistor result;
  - each kind appears at most 5 times;
  - the list is sorted by FR-007;
  - no duplicate permutations.
- [X] T014 [P] [US1] Write `CORE_TEST/FormatTest.kt`:
  - `connectionText` for each kind ("1kΩ + 2.2kΩ", "10kΩ ∥ 10kΩ", "4.7kΩ ∥ 10kΩ + 220Ω");
  - `formatOhms` with 4 significant digits ("3.197kΩ", "0.3333Ω", "10MΩ");
  - `formatDeviation` ("0%", "+0.0213%", "−1.23%", 3 significant digits, the minus sign U+2212);
  - `uk` decimal commas.
- [X] T015 [P] [US1] Write `CORE_TEST/SearchBenchmarkTest.kt`: E192, all kinds, 20 fixed targets after a warm-up, median under 150 ms (research R4).

### Implementation for User Story 1

- [X] T016 [US1] Create `CORE/TargetParser.kt` (`parseTarget`) so that T011 passes.
- [X] T017 [US1] Create `CORE/Connection.kt` (`Kind`, `Combination`, `resistance`, `deviation`, the ranking comparator) and `CORE/Search.kt`:
  - the per-kind search from research R4: canonical a ≤ b ≤ c, binary search of the ideal last value, an outward walk bounded by the kind's current 5th best, prefix cut-offs;
  - `Double` keys with a 1e-9 relative margin, then an exact re-rank;
  - merge and sort.

  T012, T013 and T015 must pass.
- [X] T018 [US1] Create `CORE/Format.kt` (`connectionText`, `formatOhms`, `formatDeviation`) using `formatSi`/`formatDecimal`, so that T014 passes.
- [ ] T019 [US1] Add the input and list strings to `RES/values*/strings.xml`: `target_label`, `target_hint`, `target_error`, `series_label` and `empty` (contracts/ui-contract.md), in all six locales.
- [ ] T020 [US1] Implement `UI/CombinationsScreen.kt` per contracts/ui-contract.md:
  - an `OutlinedTextField` (text keyboard) with the hint or error as supporting text;
  - the series `ExposedDropdownMenuBox` (E24 default);
  - a `LazyColumn` of result rows (connection text, resistance, signed deviation).

  The search runs through `snapshotFlow` → `collectLatest` → `withContext(Dispatchers.Default)`. Use `rememberSaveable` for the text and the series. Add the test tags.
- [ ] T021 [US1] Write `UI_TEST/CombinationsScreenTest.kt` part 1:
  - empty → the empty message;
  - "4.7k" in E12 → the first row is "4.7kΩ" and "0%";
  - "abc" → the error and no list;
  - switching the series updates the list;
  - state restoration keeps the text and the series.

  Run it on an emulator.

**Checkpoint**: The MVP works end to end.

---

## Phase 4: User Story 2 - Choose connection kinds (Priority: P2)

**Goal**: Each kind can be switched on or off. A single resistor is always included.

**Independent Test**: With only "2 in series" on, every row is a single resistor or a 2-resistor
series connection.

- [ ] T022 [P] [US2] Extend `CORE_TEST/SearchExamplesTest.kt` and `SearchReferenceTest.kt`:
  - a forbidden kind never appears;
  - with no kinds allowed, only Single results;
  - the reference equality holds for 3 restricted kind sets.
- [ ] T023 [US2] Make `search` honor `allowed`, always adding `Single`. T022 must pass.
- [ ] T024 [US2] Add the chip strings `kind_series2`, `kind_series3`, `kind_parallel2`, `kind_parallel3` and `kind_parallel_series` to `RES/values*/strings.xml` in all six locales. Add a `FlowRow` of `FilterChip`s to `UI/CombinationsScreen.kt`, all on by default and saved with `rememberSaveable` as ordinals.
- [ ] T025 [US2] Extend `UI_TEST/CombinationsScreenTest.kt`:
  - turning chips off removes those kinds from the list;
  - the chips survive state restoration.

**Checkpoint**: Filtering works and stays correct.

---

## Phase 5: User Story 3 - Any language and setting (Priority: P2)

**Goal**: Spoken result descriptions, locale formatting, 200% font, dark theme.

**Independent Test**: In Ukrainian, "1,234k" works and shows decimal commas. TalkBack reads a row
as one item with unit words.

- [ ] T026 [P] [US3] Add the spoken strings `spoken_series`, `spoken_parallel`, `spoken_deviation`, `spoken_ohm`, `spoken_kilohm`, `spoken_megohm` and `spoken_gigohm` to `RES/values*/strings.xml` in all six locales.
- [ ] T027 [US3] Create `UI/Spoken.kt`, which builds a row's description ("10 kilohm parallel 10 kilohm, 5 kilohm, deviation 0 percent") from those strings, using the locale decimal separator. Apply it with `clearAndSetSemantics` on each row in `UI/CombinationsScreen.kt`.
- [ ] T028 [US3] Extend `UI_TEST/CombinationsScreenTest.kt`: a row's content description equals the expected spoken text.
- [ ] T029 [US3] On the emulator, check:
  - Ukrainian ("1,234k", decimal commas, all labels);
  - dark theme;
  - 200% font, with nothing cut off in the field, chips or rows;
  - the spoken text, through a uiautomator dump.

  Reset the settings afterwards.

**Checkpoint**: All stories work in every supported setting.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T030 [P] Check that `shared/si-format` and `modules/resistor-combinations/core` have no `android` imports.
- [ ] T031 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease`. It must be green.
- [ ] T032 Run `./gradlew connectedDebugAndroidTest` on an emulator: the app, both color code modules and the combinations tests all pass.
- [ ] T033 Do the manual checks in `specs/004-resistor-combinations/quickstart.md` §4, including E192 responsiveness.
- [ ] T034 Push branch `004-resistor-combinations` and open a PR to `main`. The description cites IEC 60063:2015 and the circuit-law source, explains the search and its proof against the reference, and mentions the `si-format` move. Merge after CI is green and T032/T033 are done.

---

## Dependencies & Execution Order

- **Phase 1**: T001 → T002 (move first, then fix imports). T003–T006 can run in parallel after T001. T007 comes after T004–T006.
- **Phase 2** blocks the stories: T008 and T009 (tests) come before T010.
- **US1**: tests T011–T015 come first, then T016 → T017 → T018. The UI tasks T019 → T020 → T021 come after the core.
- **US2** needs US1's search and screen. **US3** needs US1's screen.
- **Polish** comes last.

### Parallel Opportunities

- T003–T006.
- T008 and T009.
- T011–T015 (separate test files).
- T022 alongside T024's strings.
- T026 alongside T027 drafting.

## Implementation Strategy

1. **MVP**: Phases 1–3. Typing a value lists the best combinations of every kind, proven against
   the reference search.
2. **Then** Phase 4 (filters) and Phase 5 (spoken text and settings checks).
3. **Then** Polish and the PR.

Commit after each task or logical group, in English. The `si-format` move (T001–T002) goes in its
own commit.
