# Implementation Plan: App Shell with Category Navigation

**Branch**: `001-app-shell-navigation` | **Date**: 2026-10-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-app-shell-navigation/spec.md`

## Summary

This feature delivers the first runnable Circuitopedia app. The main screen has a top app bar,
two category tabs (Calculations, Reference) and a swipeable pager. Each page is a list of the
modules registered for that category, sorted by localized title, or a localized empty-state
message when the category has no modules. Tapping an entry opens that module's screen, framed by
the shell's top bar with an "up" control.

Modules plug in through a small `:module-api` Android library. It defines `Category`, `ModuleId`
and the `AppModule` descriptor. Each future module lives in `modules/<name>/`. Gradle includes
it automatically, and it is registered with one line in `ModuleRegistry`. The release registry
is empty. Sample modules exist only in the debug source set, for instrumented tests.

Navigation is a single saveable `openModuleId`, so no navigation library is needed. Unique ids
are checked by unit tests. Default and all-locale strings are checked by Lint, which CI already
runs.

## Technical Context

**Language/Version**: Kotlin 2.4.20 (AGP 9.4.1 built-in Kotlin), JDK 25 toolchain, Gradle 9.8.1

**Primary Dependencies**: Jetpack Compose (BOM 2026.09.00: ui, foundation, material3),
activity-compose 1.13.0, lifecycle-runtime-compose 2.11.0, core-ktx 1.19.1. All are already in
the version catalog. Test-only additions are listed under New Dependencies below.

**Storage**: N/A. Only saved UI state (`rememberSaveable`). Nothing is persisted.

**Testing**: JUnit 4 (JVM unit tests in `:app` and `:module-api`). Compose UI tests
(`ui-test-junit4`) with AndroidX Test runner for instrumented tests. Android Lint.

**Target Platform**: Android, minSdk 26, targetSdk/compileSdk 37

**Project Type**: mobile-app (single Android application plus one Android library for the
module contract)

**Performance Goals**: Cold start to the visible tabs in under 1 s on a mid-range device
(SC-002). Tab switching with no loading indicator at 60 fps (SC-003).

**Constraints**: Fully offline. No permissions. No new third-party libraries. Release build
reproducible from a tag, signed only by CI (`app/keystore.properties` written by
`release.yml`).

**Scale/Scope**: 2 screens (catalog, module frame), 2 categories, 0 shipped modules. The design
is meant to handle dozens of modules later.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| **I. Free, Ad-Free, Private** | ✅ | No ads, IAP, analytics or crash SDKs. Manifest declares no permissions (no `INTERNET`). Nothing uses the network. |
| **II. Correctness** | N/A | No formulas, reference values or numeric input in this feature. The contract (R7) prepares the pure-Kotlin `core` split for future calculation modules. |
| **III. Simplicity** | ✅ (see Complexity Tracking) | No new runtime libraries. No navigation library, DI, ServiceLoader or codegen (R5, R6). The `AppModule` contract is a plain class, not an interface hierarchy. The pure logic (ordering, duplicate detection, id validation) has no Android imports. |
| **IV. Testability** | ✅ | JVM tests: `ModuleIdTest`, `ModuleRegistryTest`, `CatalogOrderingTest` (written first). Logic-bearing UI (tab/pager sync, back handling, state restore) is covered by Compose UI tests. |
| **V. Localization** | ✅ | All shell strings are in resources ([ui-contract](contracts/ui-contract.md)) with en/de/fr/es/ja/uk. Lint `MissingTranslation`/`MissingDefaultResource` are errors. Sorting uses locale collation (R8). There is no numeric input, so the decimal-separator rule does not apply. |
| **VI. Version Control** | ✅ | Work on `001-app-shell-navigation`. Merge into `main` only when tasks are done and `test` + `lint` + `assembleDebug` are green in CI. |
| **VII. Distribution** | ✅ | No Play Services/Billing/Core. Signing config reads `app/keystore.properties` only if present (gitignored). Version comes from `-PappVersionCode`/`-PappVersionName`, as `release.yml` passes them. |

**Post-design re-check (after Phase 1)**: unchanged, all gates pass. The design adds one
Gradle subproject (`:module-api`) and a build-script convention for `modules/*`, both recorded
below.

## Project Structure

### Documentation (this feature)

```text
specs/001-app-shell-navigation/
├── plan.md              # This file
├── research.md          # Phase 0: decisions R1–R12
├── data-model.md        # Phase 1: Category, ModuleId, AppModule, registry, nav state
├── quickstart.md        # Phase 1: build/test/run validation guide
├── contracts/
│   ├── module-contract.md   # how modules integrate (normative)
│   └── ui-contract.md       # screens, strings, behavior rules
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
settings.gradle.kts            # + include(":module-api"); auto-include modules/* (R6)
gradle/libs.versions.toml      # + compose foundation, ui-test-junit4, ui-test-manifest,
                               #   androidx-test-ext-junit, androidx-test-runner,
                               #   android-library plugin alias

module-api/
├── build.gradle.kts           # com.android.library + kotlin.plugin.compose
└── src/
    ├── main/kotlin/io/github/kolod/circuitopedia/module/
    │   ├── Category.kt
    │   ├── ModuleId.kt
    │   └── AppModule.kt
    └── test/kotlin/io/github/kolod/circuitopedia/module/
        └── ModuleIdTest.kt

app/
├── build.gradle.kts           # application; depends on :module-api + all :modules:*;
│                              # version from -P props; optional release signing; lint rules
└── src/
    ├── main/
    │   ├── AndroidManifest.xml            # no permissions
    │   ├── kotlin/io/github/kolod/circuitopedia/
    │   │   ├── MainActivity.kt            # edge-to-edge, setContent { CircuitopediaApp(ModuleRegistry.modules) }
    │   │   ├── CircuitopediaApp.kt        # theme + openModuleId + BackHandler
    │   │   ├── ModuleRegistry.kt          # val modules: List<AppModule> = listOf()
    │   │   ├── RegistryChecks.kt          # duplicateIds(): pure
    │   │   ├── catalog/
    │   │   │   ├── CatalogScreen.kt       # top bar, PrimaryTabRow, HorizontalPager, lists, empty state
    │   │   │   └── CatalogOrdering.kt     # entriesFor(category, modules, titleOf, collator): pure
    │   │   ├── module/
    │   │   │   └── ModuleScreen.kt        # TopAppBar(title, up) + module.content()
    │   │   └── theme/
    │   │       └── Theme.kt               # Material 3, system dark, dynamic color 31+
    │   └── res/
    │       ├── values{,-de,-fr,-es,-ja,-uk}/strings.xml
    │       ├── drawable/ic_arrow_back.xml # autoMirrored
    │       ├── drawable/ic_launcher_foreground.xml
    │       └── mipmap-anydpi/ic_launcher.xml, ic_launcher_round.xml
    ├── debug/
    │   ├── kotlin/io/github/kolod/circuitopedia/samples/SampleModules.kt
    │   └── res/values/sample_strings.xml, drawable/sample_*.xml
    ├── test/kotlin/io/github/kolod/circuitopedia/
    │   ├── ModuleRegistryTest.kt
    │   └── catalog/CatalogOrderingTest.kt
    └── androidTest/kotlin/io/github/kolod/circuitopedia/
        ├── CatalogScreenTest.kt           # tabs, swipe, lists, empty state, sorting
        └── NavigationTest.kt              # open/back/up, recreate, unknown id fallback

modules/                       # empty in this feature; one subdirectory per future module
```

**Structure Decision**: The app is a single application project, `:app`, with one shared
contract library, `:module-api`. The `modules/` folder is a convention: each future calculator
or reference page becomes `modules/<name>` (an Android library), optionally with
`modules/<name>/core` (Kotlin/JVM) for calculation logic. Both are wired in by the
auto-include/auto-depend build script, so adding a module touches no shell file.

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| `:module-api` subproject before any real module exists | FR-011–FR-013 require a module contract that modules can depend on without depending on the shell. It already has concrete users: the shell, plus ≥2 debug sample modules used by tests. | Keeping the contract inside `:app` makes future `modules/*` depend on an application module, which Gradle forbids, and the contract would have to be moved in the very next spec. |
| Auto-include of `modules/*` in `settings.gradle.kts` and auto-dependency in `app/build.gradle.kts` (about 10 lines) | SC-004: adding a module must touch only the module and one registry line. | Manual `include` plus `implementation(project(...))` per module means three edits in three files per module, which fails SC-004. A Gradle convention plugin (`build-logic`) is heavier and has a single use. |

## New Dependencies

All are official AndroidX artifacts. Only test dependencies are new:

- `androidx.compose.ui:ui-test-junit4` (androidTest) and `androidx.compose.ui:ui-test-manifest`
  (debugImplementation): needed for Compose UI tests. Constitution IV requires testing the
  logic-bearing UI, and nothing in the stdlib can drive Compose.
- `androidx.test.ext:junit`, `androidx.test:runner` (androidTest): the instrumentation runner and
  `ActivityScenario` for recreation tests.
- `androidx.compose.foundation:foundation` (via the BOM): `HorizontalPager`. It already comes in
  transitively through material3 and is declared explicitly because it is used directly.

No new runtime library beyond the existing catalog.
