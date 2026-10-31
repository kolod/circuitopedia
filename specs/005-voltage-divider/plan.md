# Implementation Plan: Voltage Divider Calculator

**Branch**: `005-voltage-divider` | **Date**: 2026-10-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/005-voltage-divider/spec.md`

## Summary

A divider calculator. The user enters any three independent quantities of U1, U2, R1, R2 and R,
leaves the other two empty, and taps Calculate. The empty ones are computed in closed form with
exact arithmetic (research R1). If R1 or R2 was computed, the screen lists builds of the divider
from standard resistors:
- each computed arm is 1 resistor, or 2 in series or in parallel;
- an entered arm stays as entered;
- the total stays within ±10% of R;
- builds are ranked by output-voltage error, with the 5 best per resistor count.

The search walks narrow, value-sorted lists of arm builds (research R4). A test proves it against an
exhaustive reference.

The pieces of spec 004 that the divider reuses move into a new shared library, `:shared:resistors`:
the E-series, exact rationals, the resistance parser, connection texts and formatting (research R2).

## Technical Context

**Language/Version**: Kotlin 2.4.20, AGP 9.5.0 built-in Kotlin, `kotlin-jvm` for JVM subprojects,
JVM target 17

**Primary Dependencies**: The Compose BOM 2026.09.00 (foundation, material3, ui) and
kotlinx-coroutines, which the module already gets through Compose. JVM code uses only the Kotlin
stdlib and the JDK. It also uses `:shared:si-format` and the new `:shared:resistors` library, which
is made of moved code.

**Storage**: N/A. Saved UI state only.

**Testing**: JUnit 4 on the JVM, Compose UI tests, the app's `ModuleRegistryTest` and
`LocaleCoverageTest`, and Lint

**Target Platform**: Android, minSdk 26, compileSdk 37, portrait only

**Project Type**: mobile-app feature module plus one shared library, which is extracted from an
existing module

**Performance Goals**: Builds for E192 with both arms chosen appear within 1 s on a phone (SC-003).
The evaluation budget is asserted in a JVM test.

**Constraints**: Offline, no permissions, no new libraries, exact solving and ranking, the
combinations module unchanged in behavior

**Scale/Scope**: 1 screen, 9 solve cases, 7 series, up to 4 tiers of builds

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No permissions, no network, no tracking. |
| **II. Correctness** | ✅ | The formula is cited, and all nine solved forms are derived (R1) and tested against the forward formula with exact round trips. Units are V and Ω with SI prefixes. Validation messages cover every invalid case. Arithmetic is exact. The build search is proven against an exhaustive reference. |
| **III. Simplicity** | ✅ (see Complexity Tracking) | No new libraries. The shared library is code that now has a second user (R2). Hand-over between modules was dropped in the spec, so the app shell is unchanged. |
| **IV. Testability** | ✅ | Test-first JVM tests for solving, parsing, formatting, build search (reference and examples) and the work budget. Instrumented tests for the button state, filling, clearing, builds, state and semantics. |
| **V. Localization** | ✅ | All strings in 6 locales. Input accepts "." and ",". Output uses the locale separator. |
| **VI. Version Control** | ✅ | Branch `005-voltage-divider`. The library extraction is its own commit. |
| **VII. Distribution** | ✅ | No Play dependencies, no secrets. |

**Post-design re-check**: all gates still pass.

## Project Structure

### Documentation (this feature)

```text
specs/005-voltage-divider/
├── plan.md
├── research.md          # R1 nine cases … R6 testing
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── core-api.md      # :shared:resistors and divider core API (normative)
│   └── ui-contract.md   # screen, strings, test tags
├── checklists/requirements.md
└── tasks.md             # /speckit-tasks
```

### Source Code (repository root)

```text
shared/resistors/                     # NEW :shared:resistors, kotlin-jvm (R2), moved from combinations core
└── src/{main,test}/kotlin/io/github/kolod/circuitopedia/shared/resistors/
    ├── Rational.kt  ESeries.kt  Combination.kt (Kind, Combination)
    ├── ResistanceParser.kt (parseResistance)  Format.kt (connectionText, formatOhms, formatDeviation)
    └── tests: RationalTest, ESeriesTest, ParserTest, FormatTest (moved)

modules/resistor-combinations/        # imports only; Search.kt, Ranking.kt (rankingFor, PER_KIND) stay

modules/voltage-divider/              # NEW :modules:voltage-divider
├── build.gradle.kts                  # android-library + compose; resourcePrefix "voltage_divider_"
├── core/                             # :modules:voltage-divider:core, kotlin-jvm, api(:shared:resistors)
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/modules/voltagedivider/core/
│       │   ├── Quantity.kt           # Quantity enum, entered values
│       │   ├── Solve.kt              # nine cases, validation (R1)
│       │   ├── Voltage.kt            # parseVoltage, formatVolts
│       │   └── BuildSearch.kt        # arm lists, windowed pair search, ranking (R4)
│       └── test/kotlin/…/core/       # SolveTest, VoltageParserTest, VoltageFormatTest,
│                                     # ReferenceBuildSearch, BuildSearchReferenceTest,
│                                     # BuildSearchExamplesTest, BuildSearchBudgetTest
└── src/
    ├── main/
    │   ├── kotlin/io/github/kolod/circuitopedia/modules/voltagedivider/
    │   │   ├── VoltageDividerModule.kt
    │   │   ├── DividerScreen.kt      # fields, Calculate, notes, chips, builds
    │   │   ├── Schematic.kt          # Canvas drawing
    │   │   └── Spoken.kt             # screen-reader texts
    │   └── res/{drawable/voltage_divider_icon.xml, values*/strings.xml}
    └── androidTest/kotlin/…/DividerScreenTest.kt

app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt   # + voltageDividerModule,
```

**Structure Decision**: A standard module (spec 001 contract) that depends on shared libraries,
which spec 003 allowed.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| Extracting `:shared:resistors` from the combinations module | A second user, and modules may not depend on each other | Copying E-series tables, rationals and the parser into a second module would mean double maintenance of cited data. |
| A two-pass, banded pair search | E192 (2,2) has about 1.8 M × 1.8 M pairs unbanded | Brute force is kept as the test reference on small series only. |
