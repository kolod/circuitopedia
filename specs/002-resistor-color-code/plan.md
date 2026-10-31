# Implementation Plan: Resistor Color Code Calculator

**Branch**: `002-resistor-color-code` | **Date**: 2026-10-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-resistor-color-code/spec.md`

## Summary

This is the first real module: a resistor color-code calculator with tabs for 3, 4, 5 and 6
bands. It shows a summary line (the value plus a drawn resistor) and a tappable color table,
following [design/reference-4-bands.png](design/reference-4-bands.png). The table follows IEC
60062:2016, which was verified in [research R1](research.md).

The logic (color table, layouts, selection rules, exact `BigDecimal` reading, locale formatting,
palette contrast) lives in a pure Kotlin/JVM subproject `modules/resistor-color-code/core` and
is developed test-first, including an exhaustive check of every valid combination. The Compose
UI and the `AppModule` descriptor live in the Android library `modules/resistor-color-code`. The
module is registered with one line in `ModuleRegistry`.

## Technical Context

**Language/Version**: Kotlin 2.4.20, AGP 9.4.1 built-in Kotlin for Android modules,
`org.jetbrains.kotlin.jvm` for `core`, JVM target 17

**Primary Dependencies**: Compose BOM 2026.09.00 (foundation, material3, ui) through the existing
catalog, and AndroidSVG 1.4 for the resistor drawing (added after the first review, R10). `core`
uses only the Kotlin stdlib and the JDK (`java.math`, `java.util.Locale`,
`java.text.DecimalFormatSymbols`).

**Storage**: N/A. Saved UI state only.

**Testing**: JUnit 4 on the JVM (`core`), Compose UI tests (module `androidTest`), and the app's
existing `ModuleRegistryTest` and `LocaleCoverageTest`, plus Lint

**Target Platform**: Android, minSdk 26, compileSdk 37

**Project Type**: mobile-app feature module (Android library + JVM library)

**Performance Goals**: The value updates within one frame of a tap (SC-003). The exhaustive JVM
test runs in under 5 s.

**Constraints**: Offline, no permissions, one justified new library (R10), exact decimal arithmetic, contrast
≥ 4.5:1

**Scale/Scope**: 1 screen, 4 layouts, 12 colors, about 1.1 M valid combinations in total

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No permissions, no network, no tracking. |
| **II. Correctness** | ✅ | Every value is cited (IEC 60062:2016, verified in R1, with discrepancies documented). Explicit units: `Ohms`, `Percent` and `PpmPerKelvin` value classes. Input is selection-only and invalid cells cannot be selected (validation by construction plus `require`). Precision rules are defined in the spec and R5. Arithmetic is exact (R2). |
| **III. Simplicity** | ✅ (see Complexity Tracking) | One new library, AndroidSVG, justified in the spec and R10 (UI layer only). All logic is in a pure JVM subproject. The palette and contrast check are plain functions. |
| **IV. Testability** | ✅ | Test-first JVM tests: table, readings, exhaustive oracle, formatting per locale, contrast, selection. Logic-bearing UI (taps, tabs, state, semantics) has instrumented tests. |
| **V. Localization** | ✅ | All strings in 6 locales, including `plurals` for "N bands". The decimal separator follows the locale (R5). There is no free numeric input, so the `.`/`,` parsing rule does not apply. |
| **VI. Version Control** | ✅ | Branch `002-resistor-color-code`. Merge after tasks and tests. |
| **VII. Distribution** | ✅ | No Play dependencies, no secrets. |

**Post-design re-check**: all gates still pass.

## Project Structure

### Documentation (this feature)

```text
specs/002-resistor-color-code/
├── plan.md
├── research.md          # R1 table verification … R9 testing
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── core-api.md      # pure Kotlin API (normative)
│   └── ui-contract.md   # layout, strings, behavior, test tags
├── design/reference-4-bands.png
└── tasks.md             # /speckit-tasks
```

### Source Code (repository root)

```text
settings.gradle.kts            # + include modules/<name>/core when present (one-time, R3)
build.gradle.kts               # + alias(libs.plugins.kotlin.jvm) apply false
gradle/libs.versions.toml      # + plugin kotlin-jvm (version.ref = "kotlin")

modules/resistor-color-code/
├── build.gradle.kts           # android-library + compose; compileOptions 17; resourcePrefix;
│                              # implementation(:module-api, :modules:resistor-color-code:core)
├── core/
│   ├── build.gradle.kts       # kotlin-jvm, jvmTarget 17, testImplementation(junit)
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode/core/
│       │   ├── Units.kt               # Ohms, Percent, PpmPerKelvin
│       │   ├── BandColor.kt           # table (IEC 60062:2016 citation) + palette
│       │   ├── BandLayout.kt          # BandRole, BandLayout, accepts()
│       │   ├── Selection.kt           # Selection, Reading, read(), defaults
│       │   ├── Format.kt              # Reading.format(), cellLabel()
│       │   └── Contrast.kt            # contrastRatio()
│       └── test/kotlin/…/core/
│           ├── ColorCodeTableTest.kt
│           ├── SelectionTest.kt
│           ├── ReadingTest.kt
│           ├── ExhaustiveReadingTest.kt
│           ├── FormatTest.kt
│           └── PaletteContrastTest.kt
└── src/
    ├── main/
    │   ├── kotlin/io/github/kolod/circuitopedia/modules/resistorcolorcode/
    │   │   ├── ResistorColorCodeModule.kt    # val resistorColorCodeModule: AppModule
    │   │   ├── ColorCodeScreen.kt            # tabs + pager + page
    │   │   ├── ColorCodeState.kt             # state + Saver
    │   │   ├── ColorTable.kt
    │   │   └── ResistorDrawing.kt            # renders the SVG, band CSS, leader lines
    │   └── res/
    │       ├── drawable/resistor_color_code_icon.xml
    │       ├── raw/resistor_color_code_resistor.svg   # editable drawing (R10)
    │       └── values{,-de,-fr,-es,-ja,-uk}/strings.xml
    ├── test/kotlin/…/ResistorSvgTest.kt      # SVG ↔ code contract
    └── androidTest/kotlin/…/ColorCodeScreenTest.kt

app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt   # + resistorColorCodeModule,
```

**Structure Decision**: This follows the module convention from spec 001 (contract in
`specs/001-app-shell-navigation/contracts/module-contract.md`). It is the first use of the
`core` sub-project, which needs a one-time change in `settings.gradle.kts`.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| Editing `settings.gradle.kts` and the root build (shell files) for a module feature | The `core` subproject must be included, and the `kotlin-jvm` plugin declared. This finishes the convention promised in 001 R7. Later modules won't touch these files. | Pure-Kotlin packages inside the Android module would leave "no `android.*` in calculation code" (Principle III) unenforced. |
| Exhaustive test over about 1.1 M combinations | SC-002 asks for it. It catches any transcription or formatting slip that sampled tests miss. | Sampling only. Principle II is non-negotiable, and the exhaustive run takes seconds. |
