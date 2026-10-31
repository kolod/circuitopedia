# Research: App Shell with Category Navigation

**Feature**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md) | **Date**: 2026-10-31

Each decision below resolves an open point from the plan's Technical Context.

## R1. UI toolkit

- **Decision**: Jetpack Compose with Material 3. Use the versions already pinned in
  `gradle/libs.versions.toml` (Compose BOM 2026.09.00, activity-compose 1.13.0, lifecycle 2.11.0).
- **Rationale**: The version catalog and root build already declare the Compose compiler plugin
  and these libraries. Material 3 ships `PrimaryTabRow`/`Tab`, `TopAppBar` and `ListItem`, which
  cover every element of the spec. The tabs come with correct accessibility semantics out of the box.
- **Alternatives considered**: Android Views/XML with `TabLayout` + `ViewPager2` +
  `RecyclerView`. This needs Material Components for Android (a new non-Jetpack dependency) and
  far more code for the same result, so it was rejected.

## R2. Kotlin compilation under AGP 9

- **Decision**: Use AGP 9.4.1 built-in Kotlin. Do not apply `org.jetbrains.kotlin.android`.
  Apply only `com.android.application` / `com.android.library` and
  `org.jetbrains.kotlin.plugin.compose`.
- **Rationale**: AGP 9.0 enables built-in Kotlin by default, and applying `kotlin-android` fails
  with "plugin no longer required" (source: developer.android.com/build/migrate-to-built-in-kotlin).
  The existing root `build.gradle.kts` already follows this setup.
- **Alternatives considered**: Opting out of built-in Kotlin. That is deprecated and adds
  configuration with no benefit, so it was rejected.

## R3. Category switching (tap + swipe)

- **Decision**: Put a Material 3 `PrimaryTabRow` above a foundation `HorizontalPager` with one
  page per `Category`, both driven by a single `PagerState` (`rememberPagerState`, which is
  saveable). A tap on a tab calls `animateScrollToPage`. The tab indicator follows
  `pagerState.currentPage`.
- **Rationale**: Covers FR-004 (tap and swipe) and FR-005 (the selected tab survives
  recreation, because the pager state is saved). Everything comes from the existing Compose
  artifacts.
- **Alternatives considered**: Tabs only, with no pager. This fails the swipe requirement.

## R4. List scroll position per category

- **Decision**: Create one `LazyListState` per category with `rememberLazyListState()` at the
  catalog-screen level (above the pager) and pass it into each page.
- **Rationale**: `HorizontalPager` disposes off-screen pages. Hoisting the states keeps each
  list's position while the user swipes between tabs, opens a module and comes back
  (FR-010), and through recreation (`rememberLazyListState` is backed by `rememberSaveable`).
- **Alternatives considered**: Remembering the state inside the page. It is lost whenever the
  page leaves composition.

## R5. Screen navigation (catalog ⇄ module screen)

- **Decision**: No navigation library. The root composable keeps one saveable value,
  `openModuleId: String?`, using `rememberSaveable`. `null` shows the catalog. A known id shows
  that module's screen. `BackHandler` (from activity-compose) and the top-bar "up" button set it
  back to `null`. An unknown id after restore (for example, the module was removed in an update)
  falls back to the catalog.
- **Rationale**: There are exactly two destinations and the depth is one. A nullable string is
  the entire back stack. Opening a module is idempotent (setting the same id twice gives one
  screen), which covers the double-tap edge case. Because modules are addressed by their stable
  id, FR-015 holds. No new dependency is added (Principle III).
- **Alternatives considered**:
  - *Navigation 3 (`androidx.navigation3`)*: official and lightweight, but it needs
    `@Serializable` keys, which adds the kotlinx-serialization plugin and runtime. That is a
    second new dependency for a back stack of depth one. Revisit when a module needs internal
    multi-screen navigation (that would be the second concrete use).
  - *Navigation Compose (`androidx.navigation:navigation-compose`)*: string routes and a larger
    API surface, rejected for the same reason.

## R6. Module integration mechanism

- **Decision**:
  1. A small Android library `:module-api` defines the contract: `Category`, `ModuleId` and the
     `AppModule` descriptor (see [contracts/module-contract.md](contracts/module-contract.md)).
  2. Each future calculator or reference page is its own Gradle subproject under `modules/<name>`
     that depends only on `:module-api`.
  3. `settings.gradle.kts` auto-includes every `modules/*` directory that contains a
     `build.gradle.kts`, and `app/build.gradle.kts` adds every included `:modules:*` project as an
     `implementation` dependency. These are a few lines of build script with no plugin.
  4. `app` holds the one explicit, ordered registry: `ModuleRegistry.modules: List<AppModule>`.
- **Rationale**: Adding a module means creating `modules/<name>/` plus one line in the registry.
  No shell or build file changes (SC-004). The registry is plain Kotlin and resolved at compile
  time, so it is R8-safe, needs no reflection and no runtime discovery cost. Modules cannot see
  each other or the shell (FR-013), because each depends only on `:module-api`.
- **Alternatives considered**:
  - *`java.util.ServiceLoader`*: implicit, relies on reflection and `META-INF` files that R8
    can strip, and gives no control over order. Rejected.
  - *Hilt/Dagger multibindings*: a DI framework, which Principle III explicitly rules out.
  - *KSP code generation of the registry*: a generic engine with zero concrete modules (YAGNI).
  - *Keeping the contract inside `:app`*: future module subprojects would then need to depend on
    `:app`, which is impossible for an application module and would break FR-013.

## R7. Where pure-Kotlin logic of future modules lives

- **Decision (convention for later specs)**: A module that has calculation logic or reference
  data splits into `modules/<name>/core` (Kotlin/JVM, `org.jetbrains.kotlin.jvm`, no Android
  dependencies, JVM unit tests) and `modules/<name>` (Android library with the Compose screen and
  the `AppModule` descriptor). The auto-include rule only picks up `modules/<name>`, and the
  module pulls in its own `core`.
- **Rationale**: This puts the constitution's "no `android.*` imports in calculation code" rule
  into the build, because a JVM module cannot import Android classes. Nothing is created now,
  since this feature has no calculation logic.
- **Alternatives considered**: A single shared `:core` for all calculations. That couples every
  module to every other through one artifact and contradicts FR-013.

## R8. Sorting by localized title

- **Decision**: A pure function in `app`, `entriesFor(category, modules, titleOf, collator)`.
  It filters by category and sorts by `titleOf(module)` with a `java.text.Collator` for the
  current app locale, taken from `LocalConfiguration.current.locales[0]`.
- **Rationale**: `java.text.Collator` exists on both the JVM and Android (on Android it is
  backed by ICU), so the ordering logic is unit-testable on the JVM (Principle IV). It
  handles cases a code-point sort gets wrong, such as Ukrainian `Г < Ґ < Д` and `І`/`Ї`, German
  umlauts, and Spanish `ñ`.
- **Alternatives considered**: `android.icu.text.Collator`. It needs API 24+ and is not
  available in JVM unit tests. `sortedBy { it }` is code-point order, which is wrong for
  `uk`/`de`/`es`.
- **Risk**: JVM and Android collation tables can differ in rare cases. The tests use only
  orderings that both agree on, namely the alphabet-order cases above.

## R9. Automated checks for module declarations (FR-014)

- **Decision**:
  - *Duplicate ids*: a JVM unit test in `app` asserts that `ModuleRegistry.modules` ids are
    unique, and a pure helper `duplicateIds()` produces a message naming each duplicate. The
    helper is also tested directly with sample duplicates.
  - *Malformed ids*: the `ModuleId` constructor rejects anything outside `[a-z0-9]+(-[a-z0-9]+)*`.
    This is covered by a unit test in `:module-api`.
  - *Missing English (default) text or missing locale*: Android Lint with
    `MissingDefaultResource`, `MissingTranslation` and `ExtraTranslation` raised to errors in every
    module. CI already runs `./gradlew lint`.
- **Rationale**: The test and lint steps already exist in `.github/workflows/test.yml`, so
  enforcement costs nothing extra. Lint checks every locale, which also enforces the
  constitution's "all six locales" gate.
- **Alternatives considered**: A runtime `check()` at startup. That finds the error on a device
  instead of before shipping.

## R10. Sample modules for testing (FR-016)

- **Decision**: Sample `AppModule`s, their strings and icons live in `app/src/debug/`. Compose UI
  instrumented tests (`app/src/androidTest/`) pass them to the root composable, which takes
  `modules: List<AppModule>` as a parameter. `MainActivity` passes `ModuleRegistry.modules`, which
  is empty in this release.
- **Rationale**: Instrumented Compose tests resolve strings through the app's `Context`, so
  resources defined only in the androidTest APK would not resolve. The debug source set is
  excluded from release builds, so the samples never ship. Because the registry stays empty, the
  debug app also shows the real empty state.
- **Alternatives considered**: Robolectric JVM UI tests. That is a new non-Jetpack dependency.
  A separate test-only Gradle module has the same resource-context problem as androidTest
  resources.

## R11. SDK levels and identity

- **Decision**: `compileSdk = 37`, `targetSdk = 37`, `minSdk = 26`. `applicationId` and
  namespace are `io.github.kolod.circuitopedia`. `versionCode` and `versionName` come from the
  Gradle properties `appVersionCode`/`appVersionName` (defaults `1` / `0.1.0-dev`). Release
  signing is configured only when `app/keystore.properties` exists. These match
  `.github/workflows/release.yml`.
- **Rationale**: Platform 37 is installed locally (`android-37.0`). API 26 covers almost all
  active devices and gives adaptive launcher icons without legacy PNG fallbacks. The release
  workflow already passes `-PappVersionCode`/`-PappVersionName` and writes
  `app/keystore.properties`, so the build must read exactly these names.
- **Alternatives considered**: `minSdk 24`. It needs legacy PNG launcher icons and gains only a
  negligible share of devices.

## R12. Icons

- **Decision**: Vector drawables (`@DrawableRes`) bundled per module. The shell's "up" arrow is
  its own auto-mirrored vector drawable. No icon library.
- **Rationale**: `material-icons-extended` is large and no longer recommended. Copying the few
  Material Symbols SVGs needed (Apache 2.0) as vector drawables keeps the APK small and avoids a
  dependency.
- **Alternatives considered**: `ImageVector` from `material-icons-core`. That ties the contract
  to an icon library.
