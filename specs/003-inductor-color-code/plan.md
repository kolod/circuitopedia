# Implementation Plan: Inductor Color Code Calculator

**Branch**: `003-inductor-color-code` | **Date**: 2026-10-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-inductor-color-code/spec.md`

## Summary

A second color code calculator, for molded axial inductors: tabs for 3 and 4 bands, the value in
nH/µH/mH, an SVG drawing with leader lines and a fixed-header color table, as in spec 002. Gold
in Band 1 or 2 is a decimal point (MIL marking), which turns Band 3 from a multiplier into a
digit. The color code follows Bourns' reproduction of the MIL and EIA markings (research R1).

Because this is the second calculator of its kind, the parts it shares with the resistor
calculator move into two shared libraries, `:shared:color-code` (pure Kotlin) and
`:shared:color-code-ui` (Compose), and the resistor module is refactored onto them (R4).

## Technical Context

**Language/Version**: Kotlin 2.4.20, AGP 9.5.0 built-in Kotlin for Android modules,
`org.jetbrains.kotlin.jvm` for JVM subprojects, JVM target 17

**Primary Dependencies**: Compose BOM 2026.09.00 (foundation, material3, ui) and AndroidSVG 1.4,
both already in the catalog. JVM code uses only the Kotlin stdlib and the JDK.

**Storage**: N/A. Saved UI state only.

**Testing**: JUnit 4 on the JVM (shared library, both cores, SVG contracts), Compose UI tests
(both modules' `androidTest`), the app's `ModuleRegistryTest` and `LocaleCoverageTest`, and Lint

**Target Platform**: Android, minSdk 26, compileSdk 37, portrait only

**Project Type**: mobile-app feature module plus two shared libraries

**Performance Goals**: The value updates within one frame of a tap (SC-003). The exhaustive JVM
test runs in under 1 s (a few thousand combinations).

**Constraints**: Offline, no permissions, no new libraries, exact decimal arithmetic, contrast
≥ 4.5:1, the resistor calculator's behavior and test tags unchanged

**Scale/Scope**: 1 new screen, 2 layouts, 12 colors; refactor of 1 existing module

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No permissions, no network, no tracking. |
| **II. Correctness** | ✅ | The code table is cited (Bourns MIL/EIA, R1) and cross-checked; discrepancies with uncited tables are recorded and resolved toward the source. Explicit units: `Microhenries`, `Percent`. Input is selection-only, invalid cells can't be selected, and the selection is valid by construction (R3). Exact arithmetic and defined formatting (R2). |
| **III. Simplicity** | ✅ (see Complexity Tracking) | No new libraries. The shared libraries exist because there are now two concrete uses, as the YAGNI rule requires; their API is data-driven, without generic engines (R4). Calculation code stays in pure JVM subprojects. |
| **IV. Testability** | ✅ | Test-first JVM tests: table, selection rules, readings, exhaustive oracle, formatting, contrast, SVG contract. Instrumented tests for the inductor screen; the resistor's existing tests guard the refactor. |
| **V. Localization** | ✅ | All new strings in 6 locales; common strings move to the shared UI library once instead of per module. Locale decimal separator (R2). No free numeric input. |
| **VI. Version Control** | ✅ | Branch `003-inductor-color-code`; the refactor and the new module are separate, small commits. Merge after tasks and tests. |
| **VII. Distribution** | ✅ | No Play dependencies, no secrets. |

**Post-design re-check**: all gates still pass.

## Project Structure

### Documentation (this feature)

```text
specs/003-inductor-color-code/
├── plan.md
├── research.md          # R1 source … R7 testing
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── core-api.md      # inductor core + shared core API (normative)
│   ├── shared-ui.md     # shared Compose components (normative)
│   └── ui-contract.md   # inductor screen, strings, test tags
├── checklists/requirements.md
└── tasks.md             # /speckit-tasks
```

### Source Code (repository root)

```text
settings.gradle.kts                 # + auto-include shared/<name> as :shared:<name> (R4)

shared/
├── color-code/                     # :shared:color-code, kotlin-jvm + java-test-fixtures
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/shared/colorcode/
│       │   ├── BandColor.kt        # 12 colors: argb + textOnColor (moved, meanings removed)
│       │   ├── Percent.kt          # moved from the resistor core
│       │   ├── Contrast.kt         # moved
│       │   └── Format.kt           # formatDecimal(), formatSi()
│       ├── test/kotlin/…           # PaletteContrastTest (moved), FormatSiTest
│       └── testFixtures/kotlin/…   # SvgContract.check(file, bandCount)
└── color-code-ui/                  # :shared:color-code-ui, android-library + compose
    ├── build.gradle.kts            # api(:shared:color-code), implementation(androidsvg)
    └── src/main/
        ├── kotlin/io/github/kolod/circuitopedia/shared/colorcode/ui/
        │   ├── ColorCodeScaffold.kt   # tabs + pager + fixed page layout
        │   ├── ColorTable.kt          # moved, data-driven (TableColumn)
        │   ├── BandDrawing.kt         # SVG + CSS + leader lines (from ResistorDrawing)
        │   └── ColorNames.kt          # BandColor.nameRes()
        └── res/values{,-de,-fr,-es,-ja,-uk}/strings.xml   # color_code_* common strings

modules/resistor-color-code/        # refactored onto shared/, behavior and test tags unchanged
├── core/…/core/ResistorCode.kt     # IEC meanings as BandColor extensions (were enum fields)
└── src/main/…                      # screen/drawing now thin wrappers; strings trimmed

modules/inductor-color-code/        # NEW, :modules:inductor-color-code
├── build.gradle.kts                # android-library + compose; resourcePrefix "inductor_color_code_"
├── core/                           # :modules:inductor-color-code:core, kotlin-jvm
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/modules/inductorcolorcode/core/
│       │   ├── Units.kt            # Microhenries
│       │   ├── InductorCode.kt     # Bourns table (R1): digit, multiplier, tolerance, point
│       │   ├── BandLayout.kt       # Three, Four; BandRole incl. DecimalPoint
│       │   ├── Selection.kt        # validity + replacement rules (R3), read()
│       │   └── Format.kt           # Reading.format(), cellLabel()
│       └── test/kotlin/…/core/     # InductorCodeTableTest, SelectionTest, ReadingTest,
│                                   # ExhaustiveReadingTest, FormatTest
└── src/
    ├── main/
    │   ├── kotlin/io/github/kolod/circuitopedia/modules/inductorcolorcode/
    │   │   ├── InductorColorCodeModule.kt   # val inductorColorCodeModule: AppModule
    │   │   ├── InductorScreen.kt            # ColorCodeScaffold + columns from core
    │   │   ├── InductorState.kt             # tab + per-tab Selection, Saver
    │   │   └── InductorDrawing.kt           # BandDrawing with the inductor SVG
    │   └── res/
    │       ├── drawable/inductor_color_code_icon.xml
    │       ├── raw/inductor_color_code_inductor.svg
    │       └── values{,-de,-fr,-es,-ja,-uk}/strings.xml
    ├── test/kotlin/…/InductorSvgTest.kt
    └── androidTest/kotlin/…/InductorScreenTest.kt

app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt   # + inductorColorCodeModule,
specs/001-app-shell-navigation/contracts/module-contract.md          # may depend on :shared:*
```

**Structure Decision**: The inductor follows the spec 001 module convention, like the resistor.
Shared code lives in a new top-level `shared/` directory, outside `modules/`, so the app does not
pick it up as a module and the "no module depends on another module" rule still holds.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| Two shared libraries and a refactor of the resistor module inside an inductor spec | The second color code calculator is the second concrete use the YAGNI rule asks for. Doing it now keeps one table, one drawing and one set of translations. | Duplicating about 450 lines of UI and the color names in a second module, with every later fix needed twice. |
| Editing `settings.gradle.kts` and the spec 001 module contract | `shared/<name>` must be included, and modules must be allowed to depend on it. One-time change; later shared libraries need no edit. | Putting shared code in `:module-api` would bloat the shell contract with calculator-specific UI and AndroidSVG. |
