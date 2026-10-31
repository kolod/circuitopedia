# Quickstart: Resistor Combination Finder

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

Prerequisites are the same as in [spec 001's quickstart](../001-app-shell-navigation/quickstart.md).

## 1. JVM tests (test-first)

```powershell
./gradlew :shared:si-format:test :modules:resistor-combinations:core:test
```

Expected: all green.
- The E-series tables match [research R1](research.md), including E24's irregular values,
  E192's 9.20 and the subset chains.
- The parser table passes.
- The fast search equals the exhaustive reference search on E3/E6/E12 grids and on random E24
  targets.
- The benchmark median for E192 with every kind is under 150 ms.

## 2. Whole project checks

```powershell
./gradlew test lint :app:assembleDebug :app:assembleRelease
```

Expected: green. The color code calculators still pass after the `si-format` move.

## 3. Instrumented UI tests

```powershell
./gradlew connectedDebugAndroidTest
```

Expected: the app, both color code modules and the combinations screen tests all pass.

## 4. Manual checks

1. Calculations lists "Resistor combinations".
2. E12 and "4.7k": the first result is 4.7kΩ, 0%.
3. E12 and "5k": an exact two-resistor result (10kΩ ∥ 10kΩ) comes before any exact
   three-resistor result.
4. E24 and "1,234k" in Ukrainian: decimal commas, each kind at most 5 times, sorted by deviation.
5. Turn off everything except "2 in series": only single resistors and 2-resistor series
   connections remain.
6. E192 and a few targets: the list appears without a visible delay, and typing stays smooth.
7. "abc", "0" and "2G": an error and no list.
8. Dark theme, 200% font (nothing cut off), and TalkBack reading a result as one item.
