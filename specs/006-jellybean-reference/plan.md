# Implementation Plan: Jellybean Parts Reference

**Branch**: `006-jellybean-reference` | **Date**: 2026-11-04 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-jellybean-reference/spec.md`

## Summary

This is the first Reference module: 35 jellybean parts in 11 categories, chosen from the EEVblog
"Jellybean list" thread (research R1). Each part page shows:
- its purpose and description;
- a table of key datasheet parameters (min, typ, max, condition);
- the pinout of its common package;
- the cited datasheet.

The catalog is pure-Kotlin data with stable keys (R4). Text is localized in string resources, and
pinouts are drawn per package outline in Compose (R3). Values come from one manufacturer datasheet
per entry. They are transcribed as printed, and audited one entry at a time (R2).

## Technical Context

**Language/Version**: Kotlin 2.4.20, AGP 9.5.0 built-in Kotlin, `kotlin-jvm` for the core, JVM
target 17

**Primary Dependencies**: Compose BOM 2026.09.00 (foundation, material3, ui), plus
`:shared:si-format`, which is used for locale decimals. No new libraries.

**Storage**: Static data compiled into the app. Saved UI state only.

**Testing**: JUnit 4 on the JVM (catalog integrity and spot values), Compose UI tests,
`LocaleCoverageTest`, `ModuleRegistryTest`, Lint, and a manual datasheet audit

**Target Platform**: Android, minSdk 26, compileSdk 37, portrait only

**Project Type**: mobile-app feature module (Reference category)

**Performance Goals**: The list and pages are instant (static data).

**Constraints**: Offline, no permissions, values exactly as the datasheets print them, every value
cited, six locales

**Scale/Scope**: 2 screens (list and page), 35 entries, 8 package drawings, about 300 parameter rows,
about 450 localized strings per locale

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No network. Datasheets are cited by name, not linked or downloaded. |
| **II. Correctness** | ✅ | Each value and pinout has a cited datasheet per entry (R2), copied as printed with kind and condition. Integrity is checked by tests, and every entry is audited manually against its PDF. The thread is credited for the selection only. |
| **III. Simplicity** | ✅ | No new libraries. The data is pure Kotlin (R4). Navigation uses the shell's pattern (R5). Pinouts are Compose drawings without SVG (R3). |
| **IV. Testability** | ✅ | JVM tests for catalog integrity and spot values, written first. Instrumented tests for navigation, state, strings and semantics. |
| **V. Localization** | ✅ | All text in 6 locales. Part numbers and unit symbols stay as printed. Decimals follow the locale. |
| **VI. Version Control** | ✅ | Branch `006-jellybean-reference`. Data goes in category-sized commits. |
| **VII. Distribution** | ✅ | No Play dependencies, no secrets. |

**Post-design re-check**: all gates still pass. One deviation is noted in R2: datasheet revisions
are recorded at transcription, not in this plan.

## Project Structure

### Documentation (this feature)

```text
specs/006-jellybean-reference/
├── plan.md
├── research.md          # R1 selection … R6 testing
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── core-api.md      # catalog types (normative)
│   └── ui-contract.md   # list, page, pinout, strings, tags
├── checklists/
│   ├── requirements.md
│   └── datasheet-audit.md   # created during implementation (SC-003)
└── tasks.md
```

### Source Code (repository root)

```text
modules/jellybean-parts/              # NEW :modules:jellybean-parts (Reference)
├── build.gradle.kts                  # android-library + compose; resourcePrefix "jellybean_parts_"
├── core/                             # :modules:jellybean-parts:core, kotlin-jvm
│   └── src/
│       ├── main/kotlin/io/github/kolod/circuitopedia/modules/jellybeanparts/core/
│       │   ├── Model.kt              # PartId, Category, ParameterKind, Value, Parameter,
│       │   │                         # Package, PinFunction, Pin, Source, PartEntry
│       │   └── catalog/              # one file per category: OpAmps.kt, Comparators.kt, …
│       │       └── Catalog.kt        # ordered list of all entries
│       └── test/kotlin/…/core/       # CatalogTest, SpotValuesTest
└── src/
    ├── main/
    │   ├── kotlin/io/github/kolod/circuitopedia/modules/jellybeanparts/
    │   │   ├── JellybeanPartsModule.kt
    │   │   ├── PartsScreen.kt        # list ↔ page state, BackHandler
    │   │   ├── PartList.kt  PartPage.kt  ParameterTable.kt
    │   │   ├── PinoutDrawing.kt      # Canvas per Package
    │   │   └── Texts.kt              # enum/id → string resource maps
    │   └── res/{drawable/jellybean_parts_icon.xml, values*/strings.xml}
    └── androidTest/kotlin/…/PartsScreenTest.kt

app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt   # + jellybeanPartsModule,
```

**Structure Decision**: A standard module (spec 001 contract), the first in the Reference
category. It depends only on `:shared:si-format`.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| About 450 localized strings per locale | Purposes, descriptions, parameter and pin names in 6 languages (Principle V) | English-only content breaks the constitution. |
| Manual audit of 35 datasheets | Transcription errors cannot be caught by code (SC-003) | Automated scraping of PDFs is unreliable and would need a network. |
