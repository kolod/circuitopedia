# Implementation Plan: Development Board Pinouts

**Branch**: `007-board-pinouts` | **Date**: 2026-11-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/007-board-pinouts/spec.md`

## Summary

This is the second Reference module. It covers seven development boards: Uno R3, Nano, Pico W,
Pico 2 W, Pi 4 Model B, Blue Pill and Black Pill. Each board page shows:
- a header drawing made from layout data, with a list view of the same pins;
- per-pin details: names, alternate functions and limits, the limits marked by symbol and color;
- a **planner** that assigns peripherals to pins and blocks invalid combinations.

One placement model covers the four routing styles (research R3):
- STM32F1 group remap;
- fixed ATmega328P routing;
- BCM2711 pin-set alternate modes;
- per-signal routing on RP2040, RP2350 and STM32F4.

The data is pure Kotlin with stable keys, and the planner rules are pure functions written test
first. The data is transcribed from cited board and chip documents and audited per board
(research R1).

## Technical Context

**Language/Version**: Kotlin 2.4.20, AGP 9.5.0 built-in Kotlin, `kotlin-jvm` for the core, JVM
target 17

**Primary Dependencies**: Compose BOM (foundation, material3, ui), activity-compose
(`BackHandler`), and `:shared:si-format` for locale decimals. No new libraries.

**Storage**: Static data compiled into the app. The saved UI state includes assignments, as a
compact string (research R7).

**Testing**: JUnit 4 on the JVM (board data, planner rules, a pairwise planner sweep, spot facts),
Compose UI tests, `LocaleCoverageTest`, `ModuleRegistryTest`, Lint, and a manual source audit

**Target Platform**: Android, minSdk 26, compileSdk 37, portrait only

**Project Type**: mobile-app feature module (Reference category)

**Performance Goals**: Board pages open instantly. A planner verdict for all placements of a
board takes under 16ms. That is about 400 placements × a few assignments, which is trivial.

**Constraints**: Offline, no permissions, every pin, function and limit cited, values as printed,
six locales, and readable without color or drawing

**Scale/Scope**: 7 boards, about 200 header pins, 300–400 placements, 1 screen with 3 levels and 2
sheets, about 250 localized strings per locale

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No network. Documents are cited by name, not linked or downloaded by the app. |
| **II. Correctness** | ✅ | Every pin, function, limit and value comes from a cited board or chip document (R1), copied as printed. Integrity and planner rules are checked by tests, and spot facts name their source tables. A manual audit is done per board (SC-004). There is no numeric input. Invalid combinations cannot be picked, and the reason is shown. Precision: values as printed. |
| **III. Simplicity** | ✅ | No new libraries. One placement model, with rules as plain functions; no solver, no DI. The data is pure Kotlin (R6). Drawings are made from layout data, with one Canvas for all boards (R5). |
| **IV. Testability** | ✅ | Planner rules are written test first on hand-made boards, then swept pairwise over the real data. UI tests cover navigation, the planner, saved state and semantics. |
| **V. Localization** | ✅ | All text is in 6 locales. Pin, signal, instance and chip names stay as printed. Decimals follow the locale. |
| **VI. Version Control** | ✅ | Branch `007-board-pinouts`. Data goes in one commit per board. |
| **VII. Distribution** | ✅ | No Play dependencies, no secrets. |

**Post-design re-check**: all gates still pass. As in spec 006, one deviation is noted in R1:
document revisions are recorded at transcription, not in this plan. The two "abstractions"
(`BoardLayout` and `Placement`) each have seven concrete uses.

## Project Structure

### Documentation (this feature)

```text
specs/007-board-pinouts/
├── plan.md
├── research.md          # R1 sources … R8 testing
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── core-api.md      # catalog + planner (normative)
│   └── ui-contract.md   # list, board page, sheets, strings, tags
├── checklists/
│   ├── requirements.md
│   └── source-audit.md  # created during implementation (SC-004)
└── tasks.md
```

### Source Code (repository root)

```text
modules/board-pinouts/                # NEW :modules:board-pinouts (Reference)
├── build.gradle.kts                  # android-library + compose; resourcePrefix "board_pinouts_"
├── core/                             # :modules:board-pinouts:core, kotlin-jvm
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/modules/boardpinouts/core/
│       │   ├── Model.kt              # Board, BoardLayout, HeaderPin, PowerPin, ChipPin, Limit,
│       │   │                         # LimitKind, GroupLimit, Value, BoardFunction, Placement,
│       │   │                         # SignalSlot, Source, keys (BoardUse, BootEffect, NoteKey …)
│       │   ├── Planner.kt            # Assignment, Availability, Conflict, Warning, rules
│       │   ├── StateCodec.kt         # encode/decode of assignments
│       │   └── boards/               # one file per board + Boards.kt (ordered list)
│       │       ├── UnoR3.kt  Nano.kt  PicoW.kt  Pico2W.kt  RaspberryPi4B.kt
│       │       └── BluePill.kt  BlackPill.kt  Boards.kt
│       └── test/kotlin/…/core/       # BoardDataTest, PlannerTest, SpotDataTest, StateCodecTest
└── src/
    ├── main/
    │   ├── kotlin/io/github/kolod/circuitopedia/modules/boardpinouts/
    │   │   ├── BoardPinoutsModule.kt
    │   │   ├── PinoutsScreen.kt      # list ↔ board page ↔ sheets, BackHandler, saved state
    │   │   ├── BoardList.kt  BoardPage.kt  PinList.kt
    │   │   ├── BoardDrawing.kt       # Canvas from BoardLayout, hit-testing, marks
    │   │   ├── PinSheet.kt  PlannerSheet.kt
    │   │   └── Texts.kt              # enum/key → string resource maps
    │   └── res/{drawable/board_pinouts_icon.xml, values*/strings.xml}
    └── androidTest/kotlin/…/PinoutsScreenTest.kt

app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt   # + boardPinoutsModule
```

**Structure Decision**: A standard module (spec 001 contract) in the Reference category, after
the jellybean parts. It depends only on `:shared:si-format`. Nothing is shared with
`:modules:jellybean-parts`: their `Value` types look alike, but extracting a shared type for two
uses would be premature, and the modules stay independent.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| A planner with conflict rules | The user chose it (spec, scope). It prevents invalid group remaps. | Highlighting only: rejected by the user. |
| 300–400 transcribed placements | Every alternate function on the header must be offered (FR-005, FR-007). | A subset would make the planner wrongly report a function as missing. |
| Manual audit of about 14 documents | Transcription errors cannot be caught by code (SC-004). | Automated PDF scraping is unreliable and needs a network. |
| A custom saved-state codec | Assignments for all boards must survive process death (FR-013). | `Parcelable` would require `android.*` types in the core. |
