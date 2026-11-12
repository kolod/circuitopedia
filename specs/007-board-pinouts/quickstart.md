# Quickstart: Development Board Pinouts

**Feature**: [spec.md](spec.md) | **Date**: 2026-11-12

The prerequisites are the same as in [spec 001's quickstart](../001-app-shell-navigation/quickstart.md).

## 1. JVM tests

```powershell
./gradlew :modules:board-pinouts:core:test
```

Expected: `BoardDataTest`, `PlannerTest` and `SpotDataTest` pass ([research R8](research.md)):
- seven boards in FR-002 order;
- every pin is named and sourced;
- every power pin has a range;
- every function has a placement;
- the pairwise planner sweep agrees with the conflict rules;
- the spot facts match their source tables.

## 2. Whole project checks

```powershell
./gradlew test lint :app:assembleDebug :app:assembleRelease
```

Expected: green. `LocaleCoverageTest` sees every new string in six locales.

## 3. Instrumented UI tests

```powershell
./gradlew connectedDebugAndroidTest
./gradlew :app:installDebug   # connected tests uninstall the app
```

## 4. Manual checks

1. **Board list.** The Reference tab lists "Board pinouts". Open it: the seven boards appear in
   FR-002 order.
2. **Pin sheet.** On the Blue Pill:
   - with the "5V" mark, PB10 is highlighted and PA0 is not;
   - tap PA12: the pin sheet shows the USB board use and the 5V tolerance with its condition.
3. **I2C conflict.** On the Pi 4:
   - add I2C and pick I2C1 on GPIO2/GPIO3; pins 3 and 5 show "I2C1 SDA/SCL";
   - add I2C again: the I2C3 placement on GPIO2/3 is blocked with "already assigned to I2C1",
     while I2C3 on GPIO4/5 is available, with the overlay note.
4. **Group remap.** On the Blue Pill:
   - add I2C1 (remap): PB8 and PB9 are claimed, with "moves as a group";
   - add CAN: if USB is assigned, CAN is blocked by the shared SRAM.
5. **Per-signal routing.** On the Pico 2 W, add I2C0 and pick SDA GP4 and SCL GP5. The UART1
   placement that would need GP4 shows that candidate as blocked.
6. **Saved state.** Rotate the device or recreate the app (`adb shell am kill`): assignments, the
   open board and the marks are kept.
7. **Language, font and screen reader.** In Ukrainian at 200% font, the list is the default view,
   the texts are translated and nothing is cut off. TalkBack reads each pin with its number, name,
   assignment and limits, and the planner can be used without the drawing.
8. **Source audit.** `checklists/source-audit.md` has every board and document ticked, with the
   revision checked (SC-004).
