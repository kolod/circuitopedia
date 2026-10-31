# Quickstart: Resistor Color Code Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

Prerequisites are the same as in [spec 001's quickstart](../001-app-shell-navigation/quickstart.md).

## 1. Core logic (JVM, test-first)

```powershell
./gradlew :modules:resistor-color-code:core:test
```

Expected: all green. This includes:
- the table check against IEC 60062:2016 ([research R1](research.md));
- an exhaustive check of every valid combination for 3, 4, 5 and 6 bands;
- formatting in `en`, `uk`, `de` and `ja`;
- palette contrast ≥ 4.5:1 (SC-005).

## 2. Whole project checks

```powershell
./gradlew test lint :app:assembleDebug
```

Expected: green. `ModuleRegistryTest` sees the unique id `resistor-color-code`, and
`LocaleCoverageTest` sees all module strings in six locales.

## 3. Instrumented UI tests

```powershell
./gradlew :modules:resistor-color-code:connectedDebugAndroidTest
```

These need an emulator or device. They cover the defaults, taps, tabs, swipes, state
restoration and cell semantics.

## 4. Manual check against the reference

1. `./gradlew :app:installDebug`, then open the app. "Resistor color code" is listed on
   Calculations and the empty message is gone.
2. Open it. The screen matches [design/reference-4-bands.png](design/reference-4-bands.png):
   tabs, "4.7kΩ ±5%", the drawing with yellow-violet-red-gold, and the table with outlined
   Yellow/Violet/Red/Gold. The expected differences from the image are the extra multiplier
   rows (Blue…White) and the extra tolerances (Brown, Red, Green, …).
3. Tap Brown, Black, Orange, Brown. It shows "10kΩ ±1%".
4. Check each tab's defaults: 3 bands "4.7kΩ ±20%", 5 bands "4.7kΩ ±1%", 6 bands
   "4.7kΩ ±1% ±100ppm/K".
5. In Ukrainian, it shows "4,7kΩ ±5%" and the tabs read "3 смуги … 5 смуг".
6. Check dark theme and 200% font size: cells stay readable and the outline is visible
   on White and Black.
7. With TalkBack, a cell reads like "Band 1, Yellow, 4, selected".
