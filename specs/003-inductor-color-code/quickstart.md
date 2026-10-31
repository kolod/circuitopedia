# Quickstart: Inductor Color Code Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

Prerequisites are the same as in [spec 001's quickstart](../001-app-shell-navigation/quickstart.md).

## 1. JVM tests (test-first)

```powershell
./gradlew :shared:color-code:test :modules:inductor-color-code:core:test :modules:resistor-color-code:core:test
```

Expected: all green.
- The inductor code table matches [research R1](research.md).
- All 2,160 valid selections match the oracle.
- The decimal-point rules hold ([data-model](data-model.md)).
- Formatting in `en`, `uk`, `de` and `ja`.
- The resistor's existing tests still pass after the refactor.

## 2. Whole project checks

```powershell
./gradlew test lint :app:assembleDebug :app:assembleRelease
```

Expected: green. `ModuleRegistryTest` sees `inductor-color-code`, `LocaleCoverageTest` covers
`shared/color-code-ui` and the new module, and both SVG contract tests pass.

## 3. Instrumented UI tests

```powershell
./gradlew connectedDebugAndroidTest
```

Expected: the app (16), resistor (8) and inductor screen tests all pass on an emulator.

## 4. Manual checks

1. `./gradlew :app:installDebug`. Calculations lists "Inductor color code" and "Resistor color
   code", sorted alphabetically.
2. Open the inductor calculator: "4 bands" is selected, "47µH ±10%", yellow-violet-black-silver
   in the drawing.
3. Tap Gold in Band 2, then Violet in Band 3: "4.7µH ±10%". The third header reads "Band 3".
4. Tap Gold in Band 1: Band 2 turns Violet, and it reads "770nH ±10%".
5. Tap Yellow in Band 1: the point is gone, the header reads "Mult.", Band 3 is Black, and it reads
   "47µH ±10%".
6. Check the 3-band tab ("47µH ±20%"), Ukrainian ("4,7µH"), dark theme, 200% font (nothing cut
   off) and TalkBack ("Band 2, Gold, Decimal point, selected").
7. Open the resistor calculator: it looks and behaves exactly as before.
