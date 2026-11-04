---

description: "Task list for 006-jellybean-reference"
---

# Tasks: Jellybean Parts Reference

**Input**: Design documents from `/specs/006-jellybean-reference/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Principle IV. The catalog's integrity checks and spot values are JVM tests, written
first. Logic-bearing UI gets Compose instrumented tests: navigation, state and semantics. Datasheet
transcription is verified by a manual audit (SC-003), because code cannot catch a mistyped value.

**Organization**: Tasks are grouped by user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: US1 is browsing by category, US2 is a part page (parameters, pinout, source), US3 is
  localization, accessibility and settings.

## Path Conventions

- `CORE` = `modules/jellybean-parts/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/jellybeanparts/core`
- `CAT` = `CORE/catalog`
- `CORE_TEST` = `modules/jellybean-parts/core/src/test/kotlin/io/github/kolod/circuitopedia/modules/jellybeanparts/core`
- `UI` = `modules/jellybean-parts/src/main/kotlin/io/github/kolod/circuitopedia/modules/jellybeanparts`
- `UI_TEST` = `modules/jellybean-parts/src/androidTest/kotlin/io/github/kolod/circuitopedia/modules/jellybeanparts`
- `RES` = `modules/jellybean-parts/src/main/res`
- `AUDIT` = `specs/006-jellybean-reference/checklists/datasheet-audit.md`
- Locales: `values` (en), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

**Transcription rule (all data tasks)**:
- Open the entry's datasheet from the maker's website (research R2) and confirm its document
  number.
- Copy each value exactly as printed, with its kind (min, typ or max), unit and condition, and the
  pins of the stated package.
- Record the PDF's revision and date in `Source.revision`.
- Add one line per entry to `AUDIT`: entry, document, revision, the tables used, and "transcribed".
- Never guess a value. If the datasheet lacks it, leave it out.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: The module builds and appears in the Reference tab.

- [X] T001 [P] Create `modules/jellybean-parts/core/build.gradle.kts`: kotlin-jvm, JVM 17, `api(project(":shared:si-format"))`, `testImplementation(libs.junit)`.
- [X] T002 [P] Create `modules/jellybean-parts/build.gradle.kts` like `modules/voltage-divider/build.gradle.kts`, with namespace `io.github.kolod.circuitopedia.modules.jellybeanparts`, `resourcePrefix = "jellybean_parts_"` and a dependency on `:modules:jellybean-parts:core`.
- [X] T003 [P] Create `RES/drawable/jellybean_parts_icon.xml`, a 24dp single-color vector of a TO-92 transistor outline with three leads (white, no tint).
- [X] T004 [P] Create `RES/values*/strings.xml` with the title and description in six locales:
  - en: Jellybean parts / Common cheap parts: specs and pinouts;
  - de: Standardbauteile / Gängige Bauteile: Daten und Pinbelegung;
  - fr: Composants courants / Composants bon marché : caractéristiques et brochages;
  - es: Componentes comunes / Componentes baratos: datos y patillajes;
  - ja: 定番部品 / よく使う安価な部品の仕様とピン配置;
  - uk: Поширені компоненти / Дешеві деталі: параметри й цоколівки.
- [X] T005 Create `UI/JellybeanPartsModule.kt` (`AppModule(ModuleId("jellybean-parts"), Category.Reference, …) { PartsScreen() }`) and a placeholder `UI/PartsScreen.kt`, then register the module in `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.

**Checkpoint**: `./gradlew test lint :app:assembleDebug` passes. The Reference tab lists "Jellybean
parts".

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The catalog model, its integrity checks, and all 34 entries transcribed from their
datasheets.

- [X] T006 Write `CORE_TEST/CatalogTest.kt` (test-first) with the research R6 integrity checks:
  - 34 entries in FR-002 order, and categories in `Category` order;
  - unique ids;
  - every `Source` has a non-blank maker, document and revision;
  - every parameter has a non-blank unit and at least one bound, and min ≤ typ ≤ max, compared as
    `BigDecimal`;
  - pins numbered 1…pinCount;
  - the 74HC family has no package and every other entry has one;
  - every family has members;
  - every `equivalent` points to an existing id.
- [X] T007 Create `CORE/Model.kt` per contracts/core-api.md and data-model.md: `Category`, `PartId`, `ParameterKind`, `Value`, `Parameter`, `Package`, `PinFunction` (with symbols), `Pin`, `FamilyMember`, `Source` and `PartEntry`. Also create `CAT/Catalog.kt` (`catalog`, `catalogByCategory`, `entry`), assembling the per-category lists of T008–T013. Create `AUDIT` with its header.
- [X] T008 [P] Transcribe the op-amps and comparators into `CAT/OpAmps.kt` and `CAT/Comparators.kt`: LM358, LM324, TL072, OP07, LM393, LM339. Use the DIP-8 and DIP-14 pinouts with channels A–D.
- [X] T009 [P] Transcribe the timer and the logic family into `CAT/Timers.kt` and `CAT/Logic.kt`:
  - NE555 with its DIP-8 pinout;
  - the 74HC family: supply range, output drive and propagation delay at 4.5V from the cited SN74HC00 datasheet, no package, and members for the common gates.
- [X] T010 [P] Transcribe the bipolar transistors and MOSFETs into `CAT/BipolarTransistors.kt` and `CAT/Mosfets.kt`:
  - 2N3904, 2N3906, 2N2222 (PN2222A), PN2907 (PN2907A) and BC548 in TO-92;
  - BD139 and BD140 in TO-126;
  - 2N7000 in TO-92 and 2N7002 in SOT-23.

  Set `makerVariesPinout` where research R3 found a disagreement.
- [X] T011 [P] Transcribe the diodes into `CAT/Diodes.kt`:
  - 1N4148, 1N914 (equivalent of 1N4148) and SD103 in DO-35;
  - the 1N4001–1N4007 family (members with V_RRM), 1N5817 and 1N5819 in DO-41;
  - BAT54 in SOT-23, single-diode variant, noting the BAT54A/C/S variants in its description key.
- [X] T012 [P] Transcribe the regulators and the reference into `CAT/VoltageRegulators.kt` and `CAT/VoltageReferences.kt`:
  - the 78xx family (members 7805, 7809, 7812, 7815, 7824 with V_OUT and tolerance);
  - the 79xx family (7905, 7912, 7915);
  - LM317 and LM337 in TO-220;
  - TL431 in TO-92.
- [X] T013 [P] Transcribe the sensors and power controllers into `CAT/TemperatureSensors.kt` and `CAT/PowerControllers.kt`:
  - LM35 and TMP36 in TO-92;
  - MC34063 and ICL7660 in DIP-8;
  - the UC384x family in DIP-8 (members UC3842–UC3845 with UVLO thresholds and maximum duty cycle).
- [X] T014 Write `CORE_TEST/SpotValuesTest.kt`, with about two values per category checked against their transcribed datasheet tables, each test naming its table. Examples: 1N4148 V_RRM, LM317 V_REF typ, TL431 V_REF typ, LM35 scale factor, 2N3904 V_CEO, LM358 supply range. Run `./gradlew :modules:jellybean-parts:core:test` until T006 and T014 pass.

**Checkpoint**: The catalog is complete, internally consistent, and logged in `AUDIT`.

---

## Phase 3: User Story 1 - Browse by category (Priority: P1) 🎯 MVP

**Goal**: The categorized list with purposes.

**Independent Test**: The list shows 11 category headers in order and every part with its purpose
in the device language.

- [X] T015 [P] [US1] Add to `RES/values*/strings.xml` in six locales:
  - `category_<name>` (11);
  - `<id>_purpose` (34), one line each, written from the datasheet's own summary, never from the
    forum;
  - `credit` ("Selection based on the EEVblog forum thread \"Jellybean list\" (2014)").
- [X] T016 [US1] Create `UI/Texts.kt`: exhaustive `when` maps from `Category`, `ParameterKind`, `PinFunction` and `Package` to string resources, and a `PartId` → purpose/description resource map.
- [X] T017 [US1] Implement `UI/PartList.kt`:
  - a `LazyColumn` with a `stickyHeader` per category;
  - each row shows the name and purpose and is one tap target;
  - the credit footer;
  - test tags `jb-list`, `jb-header-<Category>` and `jb-row-<id>`.

  Then `UI/PartsScreen.kt`: the saveable `PartId?` state, the saveable list state, and a
  `BackHandler` while a page is open.
- [X] T018 [US1] Write `UI_TEST/PartsScreenTest.kt` part 1: the header order, every row present (scrolling to it), the credit, and that every `PartId` resolves to a purpose string. Run it on an emulator.

**Checkpoint**: The list works end to end.

---

## Phase 4: User Story 2 - Part page (Priority: P1)

**Goal**: Parameters, pinout, warnings, family members and source.

**Independent Test**: Open five parts from different categories and compare them with their
datasheets. Back returns to the same position.

- [X] T019 [P] [US2] Add to `RES/values*/strings.xml` in six locales:
  - `<id>_description` (34);
  - `param_<kind>`;
  - `pin_<function>`;
  - `view_<package>`;
  - `key_parameters`, `pinout`, `members`, `equivalent_of`, `warning_pinout_varies`, `source_label`;
  - the column headers.
- [X] T020 [US2] Implement `UI/ParameterTable.kt`:
  - one row per parameter with min, typ, max and unit columns that wrap;
  - the condition in small text under the name;
  - decimals formatted with `formatDecimal` for the locale;
  - for families, a members table.
- [X] T021 [US2] Implement `UI/PinoutDrawing.kt`: one Canvas outline per `Package`, with pins numbered and labeled with short symbols, plus the view note. The drawing gets `clearAndSetSemantics {}`. Under it goes a pin table, "1  Collector", with tag `jb-pin-<n>`.
- [X] T022 [US2] Implement `UI/PartPage.kt`, which shows:
  - the title, category and purpose;
  - the description;
  - the parameter table;
  - the pinout and the warning when `makerVariesPinout` is set;
  - the equivalent note;
  - the source line;
  - tags `jb-page`, `jb-param-<kind>`, `jb-source` and `jb-warning`.
- [X] T023 [US2] Extend `UI_TEST/PartsScreenTest.kt`:
  - open BC548 and check the parameters, the 3 pins, the warning and the source;
  - check 78xx members, 74HC with no pinout, and 1N914 with its equivalent note;
  - back returns to the same list position;
  - state restoration of an open page.

**Checkpoint**: Both P1 stories work.

---

## Phase 5: User Story 3 - Any language and setting (Priority: P2)

**Goal**: Spoken parameter rows and pins, locale decimals, 200% font, dark theme.

- [X] T024 [P] [US3] Add spoken words for parameter kinds (minimum, typical, maximum) and units (volt, milliampere, …) to `RES/values*/strings.xml` in six locales.
- [X] T025 [US3] Give each parameter row a spoken description ("V_CEO, maximum 30 volt, …") and each pin row "Pin 1, collector" via semantics in `UI/ParameterTable.kt` and `UI/PinoutDrawing.kt`.
- [X] T026 [US3] Extend `UI_TEST/PartsScreenTest.kt` with the spoken texts of a parameter row and a pin row.
- [ ] T027 [US3] On the emulator, check:
  - Ukrainian: translated texts and decimal commas;
  - dark theme;
  - 200% font: tables wrap and nothing is cut off;
  - TalkBack texts through a uiautomator dump.

  Reset the settings afterwards.

**Checkpoint**: All stories work in every supported setting.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T028 Run the manual datasheet audit (SC-003). Re-check every value and pin of every entry against its PDF, fix any mismatch, and mark each `AUDIT` line "audited" with the date.
- [ ] T029 [P] Check that `modules/jellybean-parts/core` has no `android` imports.
- [ ] T030 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease` and `./gradlew connectedDebugAndroidTest`. All must be green.
- [ ] T031 Do the manual checks in `specs/006-jellybean-reference/quickstart.md` §4.
- [ ] T032 Push branch `006-jellybean-reference` and open a PR to `main`. The description credits the EEVblog thread, lists the datasheets and links the audit. Before merging, wait until CI is no longer pending on the latest commit.

---

## Dependencies & Execution Order

- **Phase 1**: T001–T004 can run in parallel. T005 comes after them.
- **Phase 2**: T006 → T007, then T008–T013 in parallel (separate files), then T014.
- **US1**: needs Phase 2. T015 → T016 → T017 → T018.
- **US2**: needs US1's screen. T019 → T020 → T021 → T022 → T023.
- **US3**: needs US2.
- **Polish**: T028 can start once Phase 2 is done, and must finish before T032.

### Parallel Opportunities

- T001–T004.
- T008–T013, the six transcription tasks.
- T015 alongside T016 drafting, and T019 alongside T020/T021, since they touch different files.
- T024.

## Implementation Strategy

1. **MVP**: Phases 1–3. The categorized list with purposes, backed by the full transcribed catalog.
2. **Then** Phase 4: part pages. With it, both P1 stories are done.
3. **Then** Phase 5, the audit, Polish and the PR.

Commit after each task or logical group, in English. Commit each transcription task separately, so
the audit can follow the diffs.
