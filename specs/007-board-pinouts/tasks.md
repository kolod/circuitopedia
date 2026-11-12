---

description: "Task list for 007-board-pinouts"
---

# Tasks: Development Board Pinouts

**Input**: Design documents from `/specs/007-board-pinouts/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Principle IV.
- **JVM tests, written first:** board data integrity, the planner rules, the pairwise planner
  sweep, the state codec and the spot facts.
- **Compose instrumented tests:** navigation, the planner, saved state and semantics.
- **Manual source audit (SC-004):** this is how transcription is verified.

**Organization**: tasks are grouped by user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on incomplete tasks).
- **[Story]**:
  - US1: pinout, pin details and limits;
  - US2: the planner;
  - US3: localization, accessibility and settings.

## Path Conventions

- `CORE` = `modules/board-pinouts/core/src/main/kotlin/io/github/kolod/circuitopedia/modules/boardpinouts/core`
- `BRD` = `CORE/boards`
- `CORE_TEST` = `modules/board-pinouts/core/src/test/kotlin/io/github/kolod/circuitopedia/modules/boardpinouts/core`
- `UI` = `modules/board-pinouts/src/main/kotlin/io/github/kolod/circuitopedia/modules/boardpinouts`
- `UI_TEST` = `modules/board-pinouts/src/androidTest/kotlin/io/github/kolod/circuitopedia/modules/boardpinouts`
- `RES` = `modules/board-pinouts/src/main/res`
- `AUDIT` = `specs/007-board-pinouts/checklists/source-audit.md`
- Locales: `values` (en), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

**Transcription rule (all board data tasks)**:
- **Get the documents.** Fetch the board's documents from research R1, using the fetch tool or
  asking the user. Never bypass a bot check. Confirm each document number; update R1 if it
  differs.
- **Copy as printed.** Copy every header pin, label, chip pin, alternate function, remap option,
  limit, current and voltage range exactly as printed.
- **Record sources.** Record each PDF's revision and date in `Source.revision`, and the table or
  section in each placement's `SourceRef`.
- **Audit line.** Add one line per board and document to `AUDIT`: board, document, revision, the
  tables used, and "transcribed".
- **Never guess.** If a source lacks a fact, leave it out. If two sources disagree, show the
  stricter value and record both in `AUDIT` (research R1).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: The module builds and appears in the Reference tab.

- [X] T001 [P] Create `modules/board-pinouts/core/build.gradle.kts` like `modules/jellybean-parts/core/build.gradle.kts`: kotlin-jvm, JVM 17, `api(project(":shared:si-format"))`, `testImplementation(libs.junit)`.
- [X] T002 [P] Create `modules/board-pinouts/build.gradle.kts` like `modules/jellybean-parts/build.gradle.kts`, with:
  - namespace `io.github.kolod.circuitopedia.modules.boardpinouts`;
  - `resourcePrefix = "board_pinouts_"`;
  - dependencies on `:modules:board-pinouts:core` and `libs.androidx.activity.compose`.
- [X] T003 [P] Create `RES/drawable/board_pinouts_icon.xml`: a 24dp single-color vector of a small board with a row of header pads (white, no tint).
- [X] T004 [P] Create `RES/values*/strings.xml` with the title and description in six locales:
  - en: Board pinouts / Dev board headers, pin functions and limits;
  - de: Board-Pinbelegungen / Pinleisten, Pinfunktionen und Grenzen von Entwicklungsboards;
  - fr: Brochages de cartes / Connecteurs, fonctions et limites des broches des cartes de développement;
  - es: Patillajes de placas / Conectores, funciones y límites de pines de placas de desarrollo;
  - ja: ボードのピン配置 / 開発ボードのヘッダー、ピン機能と制限;
  - uk: Розпіновки плат / Роз'єми, функції та обмеження пінів плат розробника.
- [X] T005 Create `UI/BoardPinoutsModule.kt` (`AppModule(ModuleId("board-pinouts"), Category.Reference, …) { PinoutsScreen() }`) and a placeholder `UI/PinoutsScreen.kt`. Register the module in `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.

**Checkpoint**: `./gradlew test lint :app:assembleDebug` passes, and the Reference tab lists
"Board pinouts".

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The board model, its integrity checks, and all seven boards transcribed: pins,
limits and placements.

- [ ] T006 Write `CORE_TEST/BoardDataTest.kt` (test first) with the research R8 integrity checks:
  - seven boards in FR-002 order, with unique ids;
  - unique `(header, position)` per board, and every position 1…count present for each layout header;
  - every header pin has a non-blank label;
  - `pin` and `power` are mutually exclusive and one is set;
  - every power pin except `Ground` has a range;
  - every chip pin on the header has exactly one of `FiveVoltTolerant` or `ThreeVoltOnly`;
  - every `Limit` has the extra fields required by data-model.md;
  - every candidate of every slot is a chip pin on that board's headers;
  - `movesAsGroup` placements have one candidate per slot;
  - unique slot signals within a placement, and unique placement ids per board;
  - every placement has at least one required slot or two optional ones;
  - every `FunctionKind` listed for a board has at least one placement;
  - every board has at least two sources with non-blank maker, document and revision.
- [ ] T007 Create `CORE/Model.kt` per contracts/core-api.md and data-model.md. Create `BRD/Boards.kt` (`boards`, `board(id)` and the `Board` extension lookups of core-api.md) from the per-board values of T008–T014. Create `AUDIT` with its header.
- [ ] T008 [P] Transcribe the Arduino Uno R3 into `BRD/UnoR3.kt`:
  - **Headers:** power, analog, both digital headers, including SDA/SCL/AREF/GND, and ICSP. SDA/SCL map to PC4/PC5, and the ICSP pins to PB3/PB4/PB5 and RESET.
  - **Limits:** from the ATmega328P datasheet's electrical characteristics: per-pin current and the port group sums. Board uses: D0/D1 USB-serial, D13 LED.
  - **Placements:** USART0, TWI, SPI, Timer0/1/2 PWM channels and the ADC channels.
  - **Notes:** Timer0 and `millis()`, and SS as an output in SPI master mode.
- [ ] T009 [P] Transcribe the Arduino Nano into `BRD/Nano.kt`: as the Uno, plus A6/A7, which are `InputOnly` and ADC-only, and its ICSP pads.
- [ ] T010 [P] Transcribe the Pico W into `BRD/PicoW.kt`, from the Pico W and RP2040 datasheets:
  - **Pins:** the 40 header pins.
  - **Power:** VBUS, VSYS, 3V3_EN, 3V3(OUT), ADC_VREF, AGND and RUN, with their ranges.
  - **Placements:** per-signal routing for UART0/1, I2C0/1, SPI0/1, PWM slices 0–7 (channels A/B as slots, with every candidate GPIO), the ADC on GP26–28, and USB if it is on the header (otherwise omit it).
  - **Variant note:** the Pico (GP23/24/25/29).
  - **Limits:** 3.3V only, and drive and current from the RP2040 electrical characteristics.
- [ ] T011 [P] Transcribe the Pico 2 W into `BRD/Pico2W.kt`, from the Pico 2 W and RP2350 datasheets:
  - the same structure as T010, with the RP2350 function table (including UART on its extra function select);
  - `FiveVoltTolerant` with its condition on the digital GPIO, and `ThreeVoltOnly` on the ADC pins, as the datasheet states;
  - the GPIO errata as `Erratum` limits, with their numbers;
  - the variant note for the Pico 2.
- [ ] T012 [P] Transcribe the Raspberry Pi 4 Model B into `BRD/RaspberryPi4B.kt`, from the Pi 4 datasheet, the header documentation, BCM2711 ARM Peripherals and the overlay README:
  - **Pins:** the 40 header pins, numbered 1–40.
  - **Limits:** GPIO2/3 `BoardPull` with its value. GPIO0/1 `BoardUse` ID EEPROM and `BootRole`. Default pulls at boot, if the source treats them as boot roles. Per-pin and total GPIO current as documented.
  - **Placements:** one per instance and pin set, for UART0–5, I2C0–6, SPI0–6, PWM0/1 channels, and PCM/I2S.
  - **Notes:** the overlay note on I2C3–6, SPI3–6 and UART2–5, and the Bluetooth/UART0 note.
- [ ] T013 [P] Transcribe the Blue Pill into `BRD/BluePill.kt`, from the reference schematic (R1), DS5319 and RM0008 AFIO:
  - **Pins:** both rows and the SWD header.
  - **Limits:** the FT/non-FT column, and the PC13–PC15 current limits. Board uses: PC13 LED, PC14/15 LSE crystal, PA11/12 USB with the pull-up. Boot roles: BOOT1 (PB2) and the JTAG pins.
  - **Placements:** one per remap option (`movesAsGroup`) for USART1–3, I2C1/2, SPI1/2, CAN, TIM1–4 and USB, plus the ADC channels. Omit any option whose pins are not on the 48-pin package.
  - **Resources:** shared USB/CAN SRAM, and the errata conflicts, from the errata sheet with their numbers.
  - **Notes:** JTAG release for PA15/PB3/PB4, and that clones may differ.
- [ ] T014 [P] Transcribe the Black Pill into `BRD/BlackPill.kt`, from the WeAct schematic, DS10314 and RM0383:
  - **Pins:** both rows and the SWD header.
  - **Limits:** FT/TC from the pin table. Board uses: PC13 LED, PA0 KEY, PC14/15 LSE, PA11/12 USB, and the optional SPI flash footprint, if the schematic shows it. Boot roles: BOOT0 and PB2.
  - **Placements:** per-signal routing for USART1/2/6, I2C1–3, SPI1–5/I2S, TIM channels, ADC and USB OTG FS, from the alternate function table.
  - **Variant note:** the STM32F401 differences, from DS10086.
- [ ] T015 Write `CORE_TEST/SpotDataTest.kt`. Each test names its source table. Facts to check:
  - Pi 4: I2C1 on GPIO2/3, with the board pull-up value;
  - Pi 4: I2C3 placements on GPIO2/3 and GPIO4/5;
  - Blue Pill: I2C1 remap on PB8/PB9, `movesAsGroup`;
  - Blue Pill: PA0 not FT, PB10 FT;
  - Blue Pill: USB and CAN share a resource;
  - Nano: A6/A7 input-only;
  - Uno: SDA header pin and A4 map to the same chip pin;
  - Pico W: PWM0 A candidates include GP0 and GP16;
  - RP2350: 5V tolerance on GP0 but not on GP26;
  - Black Pill: PC13 has the LED board use.

  Run `./gradlew :modules:board-pinouts:core:test` until T006 and T015 pass.

**Checkpoint**: all board data is complete, internally consistent and logged in `AUDIT`.

---

## Phase 3: User Story 1 - Pinout, pin details and limits (Priority: P1) 🎯 MVP

**Goal**: the board list, the board page with drawing and list, the limit marks, and the pin
sheet.

**Independent Test**: open each board, toggle the 5V mark, and open several pins. Compare with
the sources.

- [ ] T016 [P] [US1] Add to `RES/values*/strings.xml`, in six locales:
  - `board_<id>_description` (7) and `variant_<key>`;
  - `view_<key>`;
  - `limit_<kind>` and `limit_<kind>_explain`;
  - `use_<BoardUse>`, `boot_<effect>`, `condition_<key>`, `group_<key>`, `note_<key>`;
  - `drawing`, `list`, `marks`, `sources_label`, `function_<kind>`.

  The explanations come from the source wording, not from community sites.
- [ ] T017 [US1] Create `UI/Texts.kt`: exhaustive `when` maps from `LimitKind`, `BoardUse`, `BootEffect`, `ConditionKey`, `GroupKey`, `NoteKey`, `ViewSide`, `FunctionKind` and `PowerRole` to string resources, plus a `BoardId` → description map. Each `LimitKind` also gets a mark symbol and a pattern.
- [ ] T018 [US1] Implement `UI/BoardList.kt`, a `LazyColumn` with tags `bp-list` and `bp-row-<id>` that shows the name and chip. Implement `UI/PinoutsScreen.kt` with the saveable state of research R7 (board, pin, view, marks) and a `BackHandler` that closes the sheet first, then the page.
- [ ] T019 [US1] Implement `UI/BoardDrawing.kt`:
  - a Canvas from `BoardLayout`: outline, USB, pads with numbers, labels outside the rows;
  - mark symbols and patterns for the enabled `LimitKind`s;
  - hit-testing that maps taps on a pad or label to a `HeaderPin`, with at least a 48dp target;
  - `clearAndSetSemantics` with `drawing_description`;
  - tag `bp-drawing`.
- [ ] T020 [US1] Implement `UI/PinList.kt`, with tags `bp-pins` and `bp-pin-<header>-<position>`. Each row shows the number, label, chip pin and limit symbols. Each row is one semantics node.
- [ ] T021 [US1] Implement `UI/PinSheet.kt`, a modal bottom sheet with tag `bp-pin-sheet`. It shows:
  - the label, the chip pin and the other header positions;
  - limit rows with tag `bp-limit-<kind>`, with values formatted by `formatDecimal` for the locale;
  - the group limits;
  - the functions on this pin, from `functionsOf`.
- [ ] T022 [US1] Implement `UI/BoardPage.kt`. It shows:
  - the title and chip;
  - the Drawing | List switch, which defaults to List when fontScale > 1.3;
  - the mark chips, with tags `bp-mark-<kind>`, defaulting to 5V, 3.3V and power;
  - the view note;
  - the drawing or the list;
  - the variants and notes;
  - the sources line, with tag `bp-sources`.
- [ ] T023 [US1] Write `UI_TEST/PinoutsScreenTest.kt` part 1. Run it on an emulator. It checks:
  - the board order;
  - opening each board shows every header pin in the list;
  - on the Blue Pill, the 5V mark state, and that PA12's sheet shows the USB board use and the FT condition;
  - back from the sheet, then from the page;
  - the restored state of an open board and pin.

**Checkpoint**: US1 works end to end, without the planner.

---

## Phase 4: User Story 2 - Planner (Priority: P1)

**Goal**: assign peripherals to pins, with group moves, blocked conflicts, warnings and saved
assignments.

**Independent Test**: on each board, assign a fitting I2C + UART + SPI set. Then add a
conflicting placement and see it blocked, with its reason.

- [ ] T024 [US2] Write `CORE_TEST/PlannerTest.kt` (test first) on small hand-made boards. It covers every rule of research R3:
  - same instance;
  - pin taken in a used slot;
  - shared resource;
  - warnings only when nothing blocks;
  - self-exclusion when editing;
  - `defaultAssignment` picks the first free candidate;
  - `unuse` and `use`, including group placements, where required slots may be unused;
  - `pinAvailability` per candidate;
  - `claims`.
- [ ] T025 [US2] Implement `CORE/Planner.kt` per contracts/core-api.md until T024 passes.
- [ ] T026 [US2] Add a pairwise sweep to `CORE_TEST/PlannerTest.kt` (SC-002). For every real board, assign each placement's default, then check every other placement: it must be `Blocked` exactly when the two share an instance, a used pin or a resource. It must be `Warned` exactly when nothing blocks and it uses a board-use or boot-role pin.
- [ ] T027 [P] [US2] Write `CORE_TEST/StateCodecTest.kt` (test first): round trip, unknown board or placement ids dropped, malformed text gives an empty map. Then implement `CORE/StateCodec.kt`.
- [ ] T028 [P] [US2] Add the planner strings to `RES/values*/strings.xml` in six locales:
  - `planner`, `add_function`, `clear_all`, `clear_all_confirm`, `remove`, `edit`, `used`;
  - `moves_as_group`, `remap_stays`;
  - `conflict_*`, `resource_<id>`, `warning_*`;
  - `spoken_assigned`, `spoken_blocked`.
- [ ] T029 [US2] Implement `UI/PlannerSheet.kt` per the ui-contract:
  - the assignments summary;
  - Add function → placements, with tags `bp-placement-<id>`, each with its verdict, the reasons or warnings, and its notes; blocked placements are disabled;
  - Edit, with candidate pins and verdicts per slot and "used" switches;
  - Remove;
  - Clear all, with confirmation.
- [ ] T030 [US2] Wire the assignments into the UI:
  - in `UI/PinoutsScreen.kt`, a saveable `Map<BoardId, List<Assignment>>` through a `Saver` that uses `encode`/`decode`;
  - assigned pins filled with their signal in `UI/BoardDrawing.kt`, and shown in `UI/PinList.kt`;
  - in `UI/PinSheet.kt`, the verdicts of the pin's functions, opening the planner at a placement, and Remove;
  - the planner button with its count in `UI/BoardPage.kt`.
- [ ] T031 [US2] Extend `UI_TEST/PinoutsScreenTest.kt`:
  - Pi 4: add I2C1 and see pins 3 and 5 assigned; the I2C3 placement on GPIO2/3 is blocked with its reason, while the one on GPIO4/5 is available with the overlay note;
  - Blue Pill: I2C1 remap is shown as moving as a group; CAN is blocked once USB is assigned;
  - Pico 2 W: choose per-signal pins;
  - unuse a slot, then remove an assignment, then clear all;
  - assignments survive state restoration.

**Checkpoint**: both P1 stories work.

---

## Phase 5: User Story 3 - Any language and setting (Priority: P2)

**Goal**: screen-reader texts, locale decimals, 200% font, dark theme, and marks without color.

- [ ] T032 [P] [US3] Add `spoken_pin`, and the spoken words for units and limits ("volt", "milliampere", "kilohm"), to `RES/values*/strings.xml` in six locales.
- [ ] T033 [US3] Give each list row its spoken text (contracts/ui-contract.md): number, label, assignment and limits. Give blocked placements "disabled + reasons". Do this in `UI/PinList.kt` and `UI/PlannerSheet.kt`. Check that every planner action works from the list alone.
- [ ] T034 [US3] Extend `UI_TEST/PinoutsScreenTest.kt` with:
  - the spoken text of a Pi 4 pin row (GPIO2 with its assignment and pull-up);
  - a blocked placement's spoken reason;
  - the list being the default view at fontScale 2.0.
- [ ] T035 [US3] On the emulator, check:
  - Ukrainian: texts and decimal commas;
  - dark and light themes: marks readable by symbol and pattern;
  - 200% font: nothing is cut off;
  - TalkBack texts, through a uiautomator dump.

  Reset the settings afterwards.

**Checkpoint**: every story works in every supported setting.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T036 Run the manual source audit (SC-004): re-check every board's pins, functions, placements and limits against its documents, fix any mismatch, and mark each `AUDIT` line "audited" with the date.
- [ ] T037 [P] Check that `modules/board-pinouts/core` has no `android` imports.
- [ ] T038 [P] Add the module to the Unreleased section of `CHANGELOG.md` (English).
- [ ] T039 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease` and `./gradlew connectedDebugAndroidTest`, then reinstall with `:app:installDebug`. All must be green.
- [ ] T040 Do the manual checks in `specs/007-board-pinouts/quickstart.md` §4.
- [ ] T041 Push branch `007-board-pinouts` and open a PR to `main`, only after the user says so. The description lists the boards and documents and links the audit. Before merging, wait until CI is no longer pending on the latest commit, and merge only on the user's "merge".

---

## Dependencies & Execution Order

- **Phase 1**: T001–T004 in parallel, then T005.
- **Phase 2**: T006 → T007, then T008–T014 in parallel (separate files), then T015.
- **US1**: needs Phase 2. T016 and T017 → T018 → T019 and T020 → T021 → T022 → T023.
- **US2**:
  - T024 → T025 → T026 can start right after Phase 2, because the core does not depend on the UI;
  - T027 and T028 can run in parallel;
  - T029–T031 need US1's screen.
- **US3**: needs US1 and US2.
- **Polish**: T036 can start once Phase 2 is done, and must finish before T041.

### Parallel Opportunities

- T001–T004.
- T008–T014: seven board transcriptions in separate files. These are the largest tasks.
- The planner core (T024–T027) alongside the US1 UI (T016–T023).
- The string tasks T016, T028 and T032, alongside code in other files.

## Implementation Strategy

1. **MVP**: Phases 1–3. A pinout reference with limits and functions for all seven boards. It is
   useful on its own.
2. **Then** Phase 4, the planner. With it, both P1 stories are done.
3. **Then** Phase 5, the audit, Polish and the PR.

Commit after each task or logical group, in English. Commit each board transcription separately,
so the audit can follow the diffs.
