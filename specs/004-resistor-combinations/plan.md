# Implementation Plan: Resistor Combination Finder

**Branch**: `004-resistor-combinations` | **Date**: 2026-10-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/004-resistor-combinations/spec.md`

## Summary

The calculator finds 1–3 standard resistors whose connection comes closest to a target value. The
supported connections are a single resistor, 2 or 3 in series, 2 or 3 in parallel, and 2 in
parallel plus 1 in series. The user types the target ("4k7", "1,234k"), chooses an IEC 60063
series (E3 … E192) and the allowed kinds. The list shows the 5 best of each kind, merged by
deviation.

The search runs per kind on the sorted value list. It fixes the first one or two resistors,
binary-searches the ideal last one, and walks outward while results can still improve. It ranks
with `Double` and confirms with exact rationals (research R3–R4). A slow exact reference search
proves it right in tests.

## Technical Context

**Language/Version**: Kotlin 2.4.20, AGP 9.5.0 built-in Kotlin, `kotlin-jvm` for JVM subprojects,
JVM target 17

**Primary Dependencies**: Compose BOM 2026.09.00 (foundation, material3, ui) and kotlinx-coroutines
(already present through Compose) in the module. JVM code uses only the Kotlin stdlib and the JDK
(`java.math`). `:shared:si-format` is new, made from code that already exists (R6).

**Storage**: N/A. Saved UI state only.

**Testing**: JUnit 4 on the JVM (core, si-format), Compose UI tests, the app's `ModuleRegistryTest`
and `LocaleCoverageTest`, and Lint

**Target Platform**: Android, minSdk 26, compileSdk 37, portrait only

**Project Type**: mobile-app feature module plus one small shared library

**Performance Goals**: The list appears within 1 s for E192 with every kind allowed (SC-002). The
JVM benchmark median must stay under 150 ms (R4).

**Constraints**: Offline, no permissions, no new libraries, exact ranking, correct shown digits,
typing never blocked

**Scale/Scope**: 1 screen, 7 series, 6 kinds, up to 1,345 values and about 1e6 prefixes per search

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No permissions, no network, no tracking. |
| **II. Correctness** | ✅ | The E-series values are cited (IEC 60063:2015, verified against a reproduction, with irregularities pinned by tests). The circuit laws are cited (Horowitz & Hill), and the code notation is cited (IEC 60062). Input is validated with a localized error. Ranking is exact (rationals). Rounding rules are defined (R3, FR-009). Correctness is proven against an exhaustive reference search. |
| **III. Simplicity** | ✅ (see Complexity Tracking) | No new libraries. The only new shared library holds two existing formatting functions that now have a third user (R6). The search is plain functions per kind, without an engine. |
| **IV. Testability** | ✅ | Test-first JVM tests for series data, parser, rationals, search (reference equality and spec scenarios), formatting and a performance budget. Instrumented tests cover input, errors, filters, state and semantics. |
| **V. Localization** | ✅ | All strings in 6 locales. The input accepts both `.` and `,`, and the output uses the locale separator. |
| **VI. Version Control** | ✅ | Branch `004-resistor-combinations`. The shared-library move is a separate commit. |
| **VII. Distribution** | ✅ | No Play dependencies, no secrets. |

**Post-design re-check**: all gates still pass.

## Project Structure

### Documentation (this feature)

```text
specs/004-resistor-combinations/
├── plan.md
├── research.md          # R1 E-series … R8 testing
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── core-api.md      # pure Kotlin API (normative)
│   └── ui-contract.md   # screen, strings, test tags
├── checklists/requirements.md
└── tasks.md             # /speckit-tasks
```

### Source Code (repository root)

```text
shared/si-format/                    # NEW :shared:si-format, kotlin-jvm (R6)
└── src/{main,test}/kotlin/io/github/kolod/circuitopedia/shared/siformat/
    ├── Format.kt                    # formatSi, formatDecimal (moved from :shared:color-code)
    └── FormatSiTest.kt              # moved

shared/color-code/build.gradle.kts   # + api(project(":shared:si-format"))
modules/{resistor,inductor}-color-code/core/…/Format.kt   # import change only

modules/resistor-combinations/       # NEW :modules:resistor-combinations
├── build.gradle.kts                 # android-library + compose; resourcePrefix "resistor_combinations_"
├── core/                            # :modules:resistor-combinations:core, kotlin-jvm
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations/core/
│       │   ├── ESeries.kt           # IEC 60063 tables, available values (R1)
│       │   ├── Rational.kt          # exact ohms (R3)
│       │   ├── TargetParser.kt      # FR-002 (R5)
│       │   ├── Connection.kt        # Kind, Combination, Result
│       │   ├── Search.kt            # per-kind search + merge (R4, FR-006–FR-008)
│       │   └── Format.kt            # connection text, resistance, deviation (FR-009)
│       └── test/kotlin/…/core/      # ESeriesTest, ParserTest, RationalTest, SearchReferenceTest,
│                                    # SearchExamplesTest, FormatTest, SearchBenchmarkTest
└── src/
    ├── main/
    │   ├── kotlin/io/github/kolod/circuitopedia/modules/resistorcombinations/
    │   │   ├── ResistorCombinationsModule.kt
    │   │   ├── CombinationsScreen.kt        # field, series dropdown, kind chips, result list
    │   │   └── Spoken.kt                    # screen-reader text of a result (FR-012)
    │   └── res/
    │       ├── drawable/resistor_combinations_icon.xml
    │       └── values{,-de,-fr,-es,-ja,-uk}/strings.xml
    └── androidTest/kotlin/…/CombinationsScreenTest.kt

app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt   # + resistorCombinationsModule,
```

**Structure Decision**: This is a standard module (spec 001 contract). It depends on
`:shared:si-format`, a library allowed by the contract amendment of spec 003.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| A per-kind search with pruning instead of brute force | E192 brute force is about 4e8 combinations, far over SC-002. | Brute force stays, but only as the test reference on small series. |
| `Double` search confirmed by exact rationals | Exact arithmetic on about 1e6 prefixes would be slow. `Double` alone could misorder near-ties or exact matches. | `Double`-only ranking breaks FR-007 tie rules. |
| New `:shared:si-format` library | The third user of `formatSi`; the color code library is the wrong dependency for it. | Copying the function, or depending on the band palette. |
