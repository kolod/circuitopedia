# Quickstart: Jellybean Parts Reference

**Feature**: [spec.md](spec.md) | **Date**: 2026-11-04

Prerequisites are the same as in [spec 001's quickstart](../001-app-shell-navigation/quickstart.md).

## 1. JVM tests

```powershell
./gradlew :modules:jellybean-parts:core:test
```

Expected: the catalog integrity checks pass ([research R6](research.md)):
- 34 entries in FR-002 order;
- every entry has a source with a revision;
- every parameter has a unit and consistent bounds;
- every pinout is complete;
- the spot values match their datasheet tables.

## 2. Whole project checks

```powershell
./gradlew test lint :app:assembleDebug :app:assembleRelease
```

Expected: green. `LocaleCoverageTest` sees every new string in six locales.

## 3. Instrumented UI tests

```powershell
./gradlew connectedDebugAndroidTest
```

## 4. Manual checks

1. The Reference tab lists "Jellybean parts". The empty-category message is gone.
2. Open it. The 11 category headers appear in FR-002 order, the credit line is at the bottom, and
   the headers stay at the top while scrolling.
3. Open BC548 and check:
   - the parameters V_CEO, I_C, hFE range and P_D;
   - the TO-92 pinout with its view side;
   - the pin table;
   - the maker warning;
   - the source line.

   Then go back to the same position.
4. Open 78xx (family members table), 74HC (no pinout), 1N914 (equivalent of 1N4148) and LM324
   (DIP-14).
5. Ukrainian at 200% font: translated texts, decimal commas, tables wrap, nothing cut off. TalkBack
   reads "Pin 1, колектор".
6. The audit checklist `checklists/datasheet-audit.md` has every entry ticked with the revision
   checked (SC-003).
