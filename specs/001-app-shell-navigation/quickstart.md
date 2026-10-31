# Quickstart: App Shell with Category Navigation

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

This guide shows how to check that the feature works end to end.

## Prerequisites

- JDK 25 (matches `gradle/gradle-daemon-jvm.properties`; Gradle can provision it automatically).
- Android SDK with platform `android-37` (`local.properties` → `sdk.dir`).
- For instrumented tests and manual runs: an emulator (API 26+, for example a Pixel API 37
  image installed through Android Studio's Device Manager) or a USB-connected device.

## 1. Build (SC-001)

```powershell
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`, and the APK at `app/build/outputs/apk/debug/app-debug.apk`.

## 2. JVM unit tests and lint (FR-014, Principles IV/V)

```powershell
./gradlew test lint
```

Expected: all green. This covers:
- `ModuleIdTest`: valid and invalid ids.
- `ModuleRegistryTest`: the registry ids are unique, and `duplicateIds()` names duplicates.
- `CatalogOrderingTest`: filtering by category, locale collation (`uk`: Г < Ґ < Д; `de`: umlauts).
- Lint: no missing default or translated strings for `en, de, fr, es, ja, uk`.

## 3. Instrumented UI tests (US1–US3, edge cases)

```powershell
./gradlew :app:connectedDebugAndroidTest
```

Requires a running emulator or device. These tests use the debug-only sample modules and check:
- The Calculations tab is selected at launch. Tapping or swiping switches tabs.
- Each tab lists only its own samples, sorted by title. An empty category shows its message.
- Tapping a sample opens its screen with its title. Back and "up" return to the same tab and
  scroll position.
- The state survives activity recreation (`ActivityScenario.recreate()`).

## 4. Manual run (US1, US2, SC-002, SC-005)

```powershell
./gradlew :app:installDebug
adb shell am start -n io.github.kolod.circuitopedia/.MainActivity
```

Check:
1. The launcher shows "Circuitopedia". The app opens in under about 1 s on Calculations.
2. Both tabs show their empty-state message, because the release registry is empty.
3. Swipe and tap between tabs. Rotate the device. The tab stays selected.
4. Switch the system language to each of de, fr, es, ja and uk. All shell text is translated.
   With an unsupported language (for example, Italian) the text is English.
5. Turn on dark theme and set font size to the maximum. The UI stays readable and tabs are
   recognizable.
6. Process-death check: open the app, then run `adb shell am kill io.github.kolod.circuitopedia`
   while it is in the background, and reopen it from recents. The same tab is restored.

## 5. Module integration dry run (US4, SC-004)

To check the contract by hand (do not commit this):
1. Create `modules/demo/` with a `build.gradle.kts` (Android library depending on
   `:module-api`), strings in all six locales, an icon, and `val demoModule: AppModule`
   following [contracts/module-contract.md](contracts/module-contract.md).
2. Add `demoModule,` to `ModuleRegistry.modules`.
3. Run `git status`. The only changes are under `modules/demo/` plus one line in
   `ModuleRegistry.kt`.
4. `./gradlew :app:installDebug`. "Demo" appears in its declared tab and opens.
5. Duplicate the registry line and run `./gradlew :app:testDebugUnitTest`. It fails and names
   `demo`.
6. Delete a locale's string and run `./gradlew lint`. It fails with `MissingTranslation`.
