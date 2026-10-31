# Quickstart: Voltage Divider Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

Prerequisites are the same as in [spec 001's quickstart](../001-app-shell-navigation/quickstart.md).

## 1. JVM tests (test-first)

```powershell
./gradlew :shared:resistors:test :modules:voltage-divider:core:test :modules:resistor-combinations:core:test
```

Expected: all green.
- The nine cases and the round-trip property hold ([research R1](research.md)).
- The build search equals the exhaustive reference on E3/E6/E12 ([R4](research.md)).
- The work budget holds.
- The combinations tests pass unchanged after the library move.

## 2. Whole project checks

```powershell
./gradlew test lint :app:assembleDebug :app:assembleRelease
```

## 3. Instrumented UI tests

```powershell
./gradlew connectedDebugAndroidTest
```

Expected: the app, the resistor color code, the inductor color code, the combinations and the
divider tests all pass.

## 4. Manual checks

1. Calculations lists "Voltage divider".
2. Enter U1 = 5, U2 = 3.3 and R1 = 10k. Calculate is enabled. Tapping it gives R2 = 19.41kΩ and
   R = 29.41kΩ, shown as computed. The builds keep R1 = 10kΩ.
3. Enter U1 = 9, U2 = 3 and R = 30k, then Calculate. On E24 the first build is 20kΩ / 10kΩ, 0%.
   Every build's total is within 27k–33k.
4. Edit U1: the computed values and the builds disappear. Type into a computed field: it becomes
   entered.
5. Enter R1, R2 and R only: Calculate stays disabled, and the note explains why. Enter U2 = 6 with
   U1 = 5: an error appears.
6. E192 with both arms chosen: the list appears without a visible delay. Measure in-process on the
   emulator, as in spec 004.
7. Ukrainian at 200% font: decimal commas, nothing cut off, everything reachable by scrolling.
   TalkBack reads "R2, 19.41 kilohm, computed".
8. The resistor combinations screen still works.
