---

description: "Task list for 001-app-shell-navigation"
---

# Tasks: App Shell with Category Navigation

**Input**: Design documents from `/specs/001-app-shell-navigation/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: This feature has no calculation logic. Under Principle IV it still has pure logic
that MUST be developed test-first with JVM tests: id validation, category ordering with locale
collation, and duplicate-id detection. Logic-bearing UI (tab/pager sync, back handling, state
restore) is covered by Compose UI instrumented tests. Those need an emulator or device.

**Organization**: Tasks are grouped by user story so each story can be built and tested on its own.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: The user story the task belongs to (US1–US4)

## Path Conventions

- Kotlin sources: `<module>/src/<sourceSet>/kotlin/io/github/kolod/circuitopedia/...`
  (abbreviated below as `…/circuitopedia/`)
- App package: `io.github.kolod.circuitopedia`. Contract package:
  `io.github.kolod.circuitopedia.module`
- Locales: `values` (en, base), `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: The Gradle projects build. The app installs and opens an empty screen.

- [ ] T001 Extend `gradle/libs.versions.toml`. Add plugin alias `android-library` (`com.android.library`, `version.ref = "agp"`). Add libraries `androidx-compose-foundation` (`androidx.compose.foundation:foundation`, BOM-managed), `androidx-compose-ui-test-junit4` and `androidx-compose-ui-test-manifest` (BOM-managed), `androidx-test-ext-junit` and `androidx-test-runner`, each with a version entry set to the latest stable release that is at least two weeks old.
- [ ] T002 Add `alias(libs.plugins.android.library) apply false` to the root `build.gradle.kts`. Do not add `kotlin-android`, because AGP 9 has built-in Kotlin (research R2).
- [ ] T003 Update `settings.gradle.kts`. Add `include(":module-api")`, then auto-include every direct subdirectory of `modules/` that contains a `build.gradle.kts` as `:modules:<dirName>` (research R6). Keep `include(":app")`.
- [ ] T004 [P] Create `module-api/build.gradle.kts`. Apply `com.android.library` and `kotlin.plugin.compose`. Set `namespace = "io.github.kolod.circuitopedia.module"`, `compileSdk = 37`, `minSdk = 26`, `buildFeatures { compose = true }`. Dependencies: `api(platform(compose BOM))`, `api(androidx-compose-ui)` (it brings `androidx.annotation` and the Compose runtime), `testImplementation(junit)`.
- [ ] T005 Create `app/build.gradle.kts`:
  - Plugins: `com.android.application` and `kotlin.plugin.compose`.
  - `namespace`/`applicationId = "io.github.kolod.circuitopedia"`, `compileSdk = 37`, `minSdk = 26`, `targetSdk = 37`.
  - `versionCode = providers.gradleProperty("appVersionCode")` (default `1`) and `versionName = providers.gradleProperty("appVersionName")` (default `"0.1.0-dev"`).
  - `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`.
  - Release signing config created only when `app/keystore.properties` exists, reading `storeFile`, `storePassword`, `keyAlias` and `keyPassword`, the exact keys written by `.github/workflows/release.yml`.
  - Release: `isMinifyEnabled = true`, `isShrinkResources = true` with `proguard-android-optimize.txt` and `proguard-rules.pro`. `buildFeatures { compose = true }`.
  - Dependencies: `implementation(project(":module-api"))`. Also every `:modules:*` project from `rootProject.childProjects["modules"]?.childProjects` (or from the same `modules/*` scan as settings) as `implementation`. Plus core-ktx, activity-compose, lifecycle-runtime-compose, Compose BOM, ui, ui-graphics, foundation, material3, ui-tooling-preview, `debugImplementation(ui-tooling, ui-test-manifest)`, `testImplementation(junit)`, `androidTestImplementation(platform(BOM), ui-test-junit4, androidx-test-ext-junit, androidx-test-runner)`.
- [ ] T006 [P] Create an empty `app/proguard-rules.pro` with a header comment. No rules are needed, because nothing uses reflection.
- [ ] T007 [P] Create `modules/README.md`. It explains that each subdirectory is one calculator or reference module, is auto-included by `settings.gradle.kts`, and must follow `specs/001-app-shell-navigation/contracts/module-contract.md`. Include a minimal sample `build.gradle.kts` for a module (android-library + compose, `implementation(project(":module-api"))`, `resourcePrefix = "<name>_"`). The file also keeps `modules/` in git.
- [ ] T008 [P] Create launcher icon resources: `app/src/main/res/drawable/ic_launcher_foreground.xml` (a simple vector, for example a resistor or zigzag glyph inside the 66dp safe zone), `app/src/main/res/values/ic_launcher_background.xml` (color), `app/src/main/res/mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml` (adaptive icons with a `<monochrome>` layer).
- [ ] T009 Create `app/src/main/res/values/strings.xml` with `app_name` = `Circuitopedia`. Add the same `app_name` to `app/src/main/res/values-de/strings.xml`, `values-fr`, `values-es`, `values-ja` and `values-uk` (it is a proper name, not translated).
- [ ] T010 Create `app/src/main/AndroidManifest.xml`. Declare no `<uses-permission>`. The `<application>` uses `android:label="@string/app_name"`, the `@mipmap/ic_launcher` and `ic_launcher_round` icons, `android:theme="@android:style/Theme.Material.Light.NoActionBar"` and `android:supportsRtl="true"`. Declare `MainActivity` as `exported` with the MAIN/LAUNCHER intent filter.

**Checkpoint**: `./gradlew :app:assembleDebug` succeeds once Phase 2 adds `MainActivity`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: The module contract, theme, entry point, empty registry and debug sample modules
that every story depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T011 [P] Write `module-api/src/test/kotlin/io/github/kolod/circuitopedia/module/ModuleIdTest.kt` first. It accepts `a`, `ohms-law`, `555-timer`, and a 64-char id. It rejects `""`, `Ohms`, `ohms_law`, `-x`, `x-`, `a--b`, `ом`, and a 65-char id, each with `IllegalArgumentException`. Run it and confirm it FAILS.
- [ ] T012 [P] Create `module-api/src/main/kotlin/io/github/kolod/circuitopedia/module/Category.kt`: `enum class Category { Calculations, Reference }`, with KDoc saying that the entry order is the tab order and that the first entry is the default tab.
- [ ] T013 Create `module-api/src/main/kotlin/io/github/kolod/circuitopedia/module/ModuleId.kt`: `@JvmInline value class ModuleId(val value: String)` with an `init` `require` for the regex `[a-z0-9]+(-[a-z0-9]+)*` and length 1..64. `./gradlew :module-api:test` now passes.
- [ ] T014 Create `module-api/src/main/kotlin/io/github/kolod/circuitopedia/module/AppModule.kt`: `class AppModule(id: ModuleId, category: Category, @StringRes titleRes: Int, @StringRes descriptionRes: Int, @DrawableRes iconRes: Int, content: @Composable () -> Unit)`, exactly as in `contracts/module-contract.md`, with KDoc stating the module's obligations from that contract.
- [ ] T015 [P] Create `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`: `object ModuleRegistry { val modules: List<AppModule> = listOf() }`, with a comment that this is the single place where modules are registered, one line each.
- [ ] T016 [P] Create `app/src/main/kotlin/io/github/kolod/circuitopedia/theme/Theme.kt`: `CircuitopediaTheme(content)` using Material 3, with `dynamicDarkColorScheme`/`dynamicLightColorScheme` on API 31+ and `darkColorScheme()`/`lightColorScheme()` otherwise, chosen by `isSystemInDarkTheme()`.
- [ ] T017 Create `app/src/main/kotlin/io/github/kolod/circuitopedia/CircuitopediaApp.kt`: `@Composable fun CircuitopediaApp(modules: List<AppModule>)`, which wraps content in `CircuitopediaTheme` and for now shows an empty `Surface`. Phases 3 and 5 will extend it.
- [ ] T018 Create `app/src/main/kotlin/io/github/kolod/circuitopedia/MainActivity.kt`: a `ComponentActivity` that calls `enableEdgeToEdge()` and then `setContent { CircuitopediaApp(ModuleRegistry.modules) }`.
- [ ] T019 [P] Create the debug-only samples (research R10):
  - `app/src/debug/res/values/sample_strings.xml` with `tools:ignore="MissingTranslation"`, holding titles and descriptions for these samples:
    - Calculations: `sample-zener` "Zener diode", `sample-ohm` "Ohm's law", `sample-alpha` "Attenuator", `sample-long` with a 70+ character title and description.
    - Reference: `sample-pinouts` "Pinouts".
  - `app/src/debug/res/drawable/sample_icon.xml`, a 24dp vector.
- [ ] T020 Create `app/src/debug/kotlin/io/github/kolod/circuitopedia/samples/SampleModules.kt`. Expose `val sampleModules: List<AppModule>` built from T019 resources, where each `content` shows a `Text` tagged `Modifier.testTag("content-<id>")`. Also expose `fun manySampleCalculations(n: Int)`, which reuses the long-title strings with ids `sample-n-<i>` for scroll tests.

**Checkpoint**: `./gradlew :module-api:test :app:assembleDebug` passes. The app launches to an
empty themed screen.

---

## Phase 3: User Story 1 - Launch and switch categories (Priority: P1) 🎯 MVP

**Goal**: The top app bar shows the app name. Two tabs, Calculations (default) and Reference,
switch by tap or swipe, and the selected tab survives recreation.

**Independent Test**: Install, launch, tap and swipe between tabs, then rotate. The selected tab
stays marked and kept (spec US1).

### Tests for User Story 1 ⚠️

- [ ] T021 [US1] Write `app/src/androidTest/kotlin/io/github/kolod/circuitopedia/CatalogScreenTest.kt` using `createAndroidComposeRule<ComponentActivity>()` with `setContent { CircuitopediaApp(emptyList()) }`. Assert:
  - The app name appears in the top bar.
  - The tabs "Calculations" and "Reference" exist in that order, and "Calculations" `assertIsSelected()`.
  - Clicking "Reference" makes it selected.
  - `performTouchInput { swipeLeft() }` on the pager (tag `catalog-pager`) selects "Reference", and `swipeRight()` returns to "Calculations".
  - After `activityRule.activity.recreate()` (use a `StateRestorationTester` for the compose state) the selected tab is kept.

  Look up strings through `activity.getString(R.string.…)`. Confirm the test fails.

### Implementation for User Story 1

- [ ] T022 [P] [US1] Add `tab_calculations` ("Calculations") and `tab_reference` ("Reference") to `app/src/main/res/values/strings.xml`.
- [ ] T023 [P] [US1] Translate `tab_calculations` and `tab_reference` in `app/src/main/res/values-de/strings.xml` (Berechnungen / Nachschlagen), `values-fr` (Calculs / Référence), `values-es` (Cálculos / Referencia), `values-ja` (計算 / リファレンス) and `values-uk` (Розрахунки / Довідник).
- [ ] T024 [US1] Create `app/src/main/kotlin/io/github/kolod/circuitopedia/catalog/CatalogScreen.kt`:
  - `@Composable fun CatalogScreen(modules: List<AppModule>, onOpen: (ModuleId) -> Unit)`.
  - A `Scaffold` with a `TopAppBar` showing `app_name`.
  - `PrimaryTabRow(selectedTabIndex = pagerState.currentPage)` over `Category.entries`, with labels from a private `Category.labelRes()` mapping and `maxLines = 2` for large fonts.
  - Below it, `HorizontalPager(state = pagerState, modifier = Modifier.testTag("catalog-pager"))`, where `pagerState = rememberPagerState { Category.entries.size }`.
  - A tab click calls `scope.launch { pagerState.animateScrollToPage(i) }`.
  - Each page calls a private `CategoryPage(category, …)`, which is an empty `Box` for now.
- [ ] T025 [US1] Update `app/src/main/kotlin/io/github/kolod/circuitopedia/CircuitopediaApp.kt` to show `CatalogScreen(modules, onOpen = {})` inside the theme. T021 now passes on an emulator.

**Checkpoint**: The MVP runs. Tabs switch by tap and swipe, the default tab is correct, and the
state survives rotation.

---

## Phase 4: User Story 2 - Browse the list of resources (Priority: P1)

**Goal**: Each tab lists its own modules, sorted by localized title with locale collation. An
empty category shows a localized message.

**Independent Test**: The release build shows both empty messages. Tests using `sampleModules`
show each entry in the right tab, sorted, with icon, title and description (spec US2).

### Tests for User Story 2 ⚠️

- [ ] T026 [P] [US2] Write `app/src/test/kotlin/io/github/kolod/circuitopedia/catalog/CatalogOrderingTest.kt` (JVM) first, against the signature `fun <T> entriesFor(category: Category, items: List<T>, categoryOf: (T) -> Category, titleOf: (T) -> String, collator: Comparator<in String>): List<T>`, using simple test data classes. Cases:
  - Filters to the category, and other-category items are absent.
  - An empty input or a category with no items gives an empty result.
  - English `["Zener","ohm","Attenuator"]` sorts to `Attenuator, ohm, Zener`, because the sort ignores case.
  - With `Collator.getInstance(Locale.forLanguageTag("uk"))`, `["Дільник","Ґрати","Гасіння"]` sorts to `Гасіння, Ґрати, Дільник`, and `["Їжак","Іскра","Йод"]` sorts to `Іскра, Їжак, Йод`.
  - German `["Zähler","Ampere","Ätzen"]` sorts to `Ampere, Ätzen, Zähler`.
  - Spanish `["ñu","nodo","oro"]` sorts to `nodo, ñu, oro`.
  - Equal titles keep registry order (stable sort).

  Run it and confirm it FAILS.
- [ ] T027 [US2] Extend `app/src/androidTest/kotlin/io/github/kolod/circuitopedia/CatalogScreenTest.kt`:
  - With `emptyList()`, each tab shows its empty message (`empty_calculations` / `empty_reference`).
  - With `sampleModules`, Calculations shows the 4 calculation samples in order "Attenuator", "Ohm's law", the long title, "Zener diode" (check the order with `onAllNodesWithTag("module-entry")`), and does not show "Pinouts". Reference shows only "Pinouts".
  - With `manySampleCalculations(40)`, `performScrollToIndex(39)` on the list (tag `module-list-Calculations`) makes the last entry displayed.
  - The long-title entry is displayed and its description node has `maxLines` 1.

### Implementation for User Story 2

- [ ] T028 [P] [US2] Add `empty_calculations` ("No calculators yet.") and `empty_reference` ("No reference pages yet.") to `app/src/main/res/values/strings.xml`.
- [ ] T029 [P] [US2] Translate `empty_calculations` and `empty_reference` in `values-de` (Noch keine Rechner. / Noch keine Nachschlageseiten.), `values-fr` (Aucun calculateur pour l'instant. / Aucune page de référence pour l'instant.), `values-es` (Aún no hay calculadoras. / Aún no hay páginas de referencia.), `values-ja` (計算ツールはまだありません。 / リファレンスはまだありません。) and `values-uk` (Калькуляторів поки немає. / Довідкових сторінок поки немає.), each under `app/src/main/res/`.
- [ ] T030 [US2] Create `app/src/main/kotlin/io/github/kolod/circuitopedia/catalog/CatalogOrdering.kt` with the pure generic `entriesFor` from T026 (`filter` then `sortedWith(compareBy(collator, titleOf))`). No `android.*` imports. T026 now passes.
- [ ] T031 [US2] Implement `CategoryPage` in `app/src/main/kotlin/io/github/kolod/circuitopedia/catalog/CatalogScreen.kt`:
  - Resolve titles with `LocalContext.current.getString(titleRes)`. Build `collator = remember(locale) { Collator.getInstance(locale) }` with `locale = LocalConfiguration.current.locales[0]`. Compute the entries with `entriesFor`, inside `remember(modules, locale)`.
  - If the result is empty, show a centered `Text` with the category's empty-message resource (a private `Category.emptyRes()` mapping).
  - Otherwise show a `LazyColumn(state = listState, modifier = Modifier.testTag("module-list-${category.name}"))` keyed by `id.value`, with one `ListItem` per entry (tag `module-entry`): `leadingContent` is `Icon(painterResource(iconRes), contentDescription = null)`, `headlineContent` is the title with `maxLines = 2` and ellipsis, `supportingContent` is the description with `maxLines = 1` and ellipsis. The whole item is `Modifier.clickable { onOpen(id) }`.
- [ ] T032 [US2] Hoist the list states in `app/src/main/kotlin/io/github/kolod/circuitopedia/catalog/CatalogScreen.kt`. Add `val listStates = Category.entries.map { rememberLazyListState() }` at the `CatalogScreen` level and pass `listStates[page]` to `CategoryPage`, so scroll positions survive pager page disposal (research R4). T027 now passes.

**Checkpoint**: US1 and US2 both work. The release app shows both empty messages.

---

## Phase 5: User Story 3 - Open a resource and return (Priority: P2)

**Goal**: Tapping an entry opens the module's screen, framed by a shell top bar with its title
and an "up" button. Back or up returns to the same tab and scroll position. Back on the catalog
exits the app.

**Independent Test**: With sample modules, open an entry, go back, and check that the tab and
scroll position are kept (spec US3).

### Tests for User Story 3 ⚠️

- [ ] T033 [US3] Write `app/src/androidTest/kotlin/io/github/kolod/circuitopedia/NavigationTest.kt` with `createAndroidComposeRule<ComponentActivity>()` and `CircuitopediaApp(sampleModules + manySampleCalculations(40))`. Cases:
  1. Clicking "Ohm's law" shows the top bar title "Ohm's law" and the node `content-sample-ohm`.
  2. `Espresso.pressBack()` (or `activity.onBackPressedDispatcher.onBackPressed()`) returns to the catalog with "Calculations" selected.
  3. On the Reference tab, opening "Pinouts" and pressing the "up" button (content description `navigate_up`) returns with "Reference" still selected.
  4. Scroll the Calculations list to index 30, open that entry, go back, and assert `firstVisibleItemIndex` is still about 30 (the entry at index 30 is displayed).
  5. A double `performClick()` on one entry shows exactly one module screen, and a single back returns to the catalog.
  6. Using `StateRestorationTester`, an open module screen survives state restoration.
  7. Restoring with `openModuleId = "missing-id"` shows the catalog. Test this by making the root take an optional `initialModuleId` parameter that is only used in tests, or by restoring the state with a registry that lacks the id.
  8. Back on the catalog finishes the activity (`activity.isFinishing`).

  Confirm the tests fail.

### Implementation for User Story 3

- [ ] T034 [P] [US3] Add `navigate_up` ("Navigate up") to `app/src/main/res/values/strings.xml`, with translations in `values-de` (Nach oben), `values-fr` (Remonter), `values-es` (Subir), `values-ja` (上へ移動) and `values-uk` (Угору).
- [ ] T035 [P] [US3] Create `app/src/main/res/drawable/ic_arrow_back.xml`, a 24dp Material Symbols "arrow_back" vector (Apache 2.0) with `android:autoMirrored="true"` and `android:tint="?attr/colorControlNormal"`.
- [ ] T036 [US3] Create `app/src/main/kotlin/io/github/kolod/circuitopedia/module/ModuleScreen.kt`: `@Composable fun ModuleScreen(module: AppModule, onUp: () -> Unit)`. It is a `Scaffold` with a `TopAppBar(title = stringResource(module.titleRes), navigationIcon = IconButton(onUp) { Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.navigate_up)) })`, and its content `Box(Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) { module.content() }`.
- [ ] T037 [US3] Add navigation to `app/src/main/kotlin/io/github/kolod/circuitopedia/CircuitopediaApp.kt`:
  - `var openModuleId by rememberSaveable { mutableStateOf<String?>(null) }`.
  - `val open = modules.firstOrNull { it.id.value == openModuleId }`.
  - If `open != null`, show `ModuleScreen(open, onUp = { openModuleId = null })` together with `BackHandler { openModuleId = null }`. Otherwise show `CatalogScreen(modules, onOpen = { openModuleId = it.value })`. An unknown restored id falls through to the catalog.
  - Keep `CatalogScreen` state (pager and list states) above the `if`, or wrap the catalog in a `SaveableStateHolder` keyed `"catalog"`, so the tab and scroll survive a round trip to a module screen.

  T033 now passes.

**Checkpoint**: US1–US3 work. Sample modules open and return correctly.

---

## Phase 6: User Story 4 - Add a module without touching the shell (Priority: P2)

**Goal**: The integration contract is enforced. Duplicate ids fail tests, missing default or
locale strings fail lint, and adding a module touches only `modules/<name>/` plus one registry
line.

**Independent Test**: The dry run from quickstart §5 (spec US4).

### Tests for User Story 4 ⚠️

- [ ] T038 [P] [US4] Write `app/src/test/kotlin/io/github/kolod/circuitopedia/ModuleRegistryTest.kt` (JVM) first, against `fun duplicateIds(ids: List<ModuleId>): Set<ModuleId>`. Cases:
  - `duplicateIds(listOf(a, b, a, c, b))` gives `{a, b}`, and unique input gives an empty set.
  - `registryIdsAreUnique`: `assertTrue("Duplicate module ids: …", duplicateIds(ModuleRegistry.modules.map { it.id }).isEmpty())`, with a message listing the `value`s.

  Confirm it fails to compile or fails.

### Implementation for User Story 4

- [ ] T039 [US4] Create `app/src/main/kotlin/io/github/kolod/circuitopedia/RegistryChecks.kt` with the pure `duplicateIds` (`groupingBy { it }.eachCount().filterValues { it > 1 }.keys`). T038 now passes.
- [ ] T040 [US4] Add a `lint { }` block to both `app/build.gradle.kts` and `module-api/build.gradle.kts`: `error += setOf("MissingTranslation", "MissingDefaultResource", "ExtraTranslation")` and `abortOnError = true`. In `app`, also `checkDependencies = true`, so lint on `:app` covers the strings of every `:modules:*` project (research R9).
- [ ] T041 [US4] Run the dry run from `specs/001-app-shell-navigation/quickstart.md` §5 and do not commit it:
  1. Create `modules/demo/` from the `modules/README.md` sample, with strings in all six locales, an icon, and `val demoModule`.
  2. Add `demoModule,` to `ModuleRegistry.kt`.
  3. Check that `git status` shows only `modules/demo/**` and `ModuleRegistry.kt`.
  4. Check that Demo appears in its tab and opens.
  5. Duplicate the line and check that `./gradlew :app:testDebugUnitTest` fails naming `demo`.
  6. Remove `values-ja/strings.xml` and check that `./gradlew lint` fails with `MissingTranslation`.
  7. Revert all of it with `git restore` and `git clean` on `modules/demo`.

  Fix `modules/README.md`, `settings.gradle.kts` or `app/build.gradle.kts` if any step deviates.

**Checkpoint**: All four stories are complete. The contract is proven by a real (throwaway) module.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T042 [P] Create the root `README.md` (the constitution's Sync Impact Report flags it as missing). It covers:
  - What Circuitopedia is, and that it is free, ad-free and offline.
  - Build prerequisites (JDK 25, Android SDK 37).
  - `./gradlew :app:assembleDebug`, `test`, `lint` and `connectedDebugAndroidTest`.
  - A link to `modules/README.md` for adding modules.
  - The GPLv3 license.
- [ ] T043 [P] Check for hardcoded user-facing strings: search `app/src/main` and `module-api/src/main` for `Text("` and `contentDescription = "`. The expected result is none.
- [ ] T044 Check the merged release manifest `app/build/intermediates/merged_manifest/release/processReleaseManifest/AndroidManifest.xml` (after `./gradlew :app:processReleaseManifest`). It has no `<uses-permission>` (Principle I). Also check that `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` contains no `com.google.android.gms`, `com.android.billingclient` or `com.google.android.play` artifact (Principle VII).
- [ ] T045 Confirm the source-citation gate is N/A. This feature adds no formulas or reference values; note this in the PR description.
- [ ] T046 Run `./gradlew test lint :app:assembleDebug :app:assembleRelease`. Everything must pass. The release APK is unsigned without `app/keystore.properties`, which is expected.
- [ ] T047 Run `./gradlew :app:connectedDebugAndroidTest` on an API 26+ emulator or device. All tests must pass.
- [ ] T048 Do the manual checks from `specs/001-app-shell-navigation/quickstart.md` §4:
  - The launcher name.
  - Cold start time via `adb shell am start -W -n io.github.kolod.circuitopedia/.MainActivity`: `TotalTime` under 1000 ms (SC-002).
  - Rotation in 10 cycles (SC-006).
  - Each of the six locales, plus Italian falling back to English (SC-005).
  - Dark theme, 200% font size, and the process-death restore.
- [ ] T049 Push branch `001-app-shell-navigation` and open a PR to `main`. Merge only after CI (`test.yml`) is green and T042–T048 are done (Principle VI).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies. T001 runs before T002–T005. T003 runs before the first Gradle sync.
- **Foundational (Phase 2)**: needs Setup. Blocks all stories.
- **US1 (Phase 3)**: needs Foundational.
- **US2 (Phase 4)**: needs US1's `CatalogScreen` (T024), because it fills in `CategoryPage`. T026 and T030 (the pure ordering logic) can start right after Foundational.
- **US3 (Phase 5)**: needs US2's clickable entries (T031). T034–T036 can start after Foundational.
- **US4 (Phase 6)**: T038–T040 need only Foundational. T041 (dry run) needs US3, so that a demo module can be opened.
- **Polish (Phase 7)**: needs all stories.

### Within Each Story

- Tests are written first and confirmed failing (T021, T026/T027, T033, T038).
- Strings and translations come before the UI that uses them, and can run in parallel with the tests.
- Pure logic (`entriesFor`, `duplicateIds`, `ModuleId`) comes before the composables that call it.

### Parallel Opportunities

- Setup: T004, T006, T007 and T008 run in parallel after T001–T003.
- Foundational: T011, T012, T015, T016 and T019 run in parallel. T013 follows T011 and T012. T014 follows T013. T017 follows T016. T018 follows T015 and T017. T020 follows T014 and T019.
- US1: T022 and T023 run in parallel with T021.
- US2: T026, T028 and T029 run in parallel. T026 and T030 can even start alongside US1.
- US3: T034 and T035 run in parallel with T033.
- US4: T038–T040 can run alongside US2 and US3.

---

## Parallel Example: User Story 2

```text
Task: "T026 Write CatalogOrderingTest in app/src/test/kotlin/io/github/kolod/circuitopedia/catalog/CatalogOrderingTest.kt"
Task: "T028 Add empty_* strings to app/src/main/res/values/strings.xml"
Task: "T029 Translate empty_* strings in app/src/main/res/values-{de,fr,es,ja,uk}/strings.xml"
```

## Parallel Example: Foundational

```text
Task: "T011 Write ModuleIdTest in module-api/src/test/kotlin/io/github/kolod/circuitopedia/module/ModuleIdTest.kt"
Task: "T012 Create Category.kt in module-api/src/main/kotlin/io/github/kolod/circuitopedia/module/Category.kt"
Task: "T016 Create Theme.kt in app/src/main/kotlin/io/github/kolod/circuitopedia/theme/Theme.kt"
Task: "T019 Create debug sample resources in app/src/debug/res/"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 Setup, then Phase 2 Foundational.
2. Phase 3 (US1): the app builds and launches with working tabs.
3. **Stop and validate**: T021 green on an emulator, plus a manual tap, swipe and rotate.

### Incremental Delivery

1. Add US2: lists and empty states. This is what the release actually shows.
2. Add US3: opening a module, back and up.
3. Add US4: contract enforcement and the dry run.
4. Polish, then PR to `main`.

Commit after each task or logical group, with small English commit messages (Principle VI).

---

## Notes

- [P] means different files with no dependency on incomplete tasks.
- Every new user-facing string is added in all six locales in the same story phase that
  introduces it. Lint (T040) enforces this from Phase 6 on.
- Instrumented tests (T021, T027, T033, T047) need an emulator. JVM tests and lint run in CI.
