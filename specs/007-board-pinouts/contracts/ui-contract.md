# Contract: Board Pinouts Screens

The module has one screen with three levels: the board list, a board page, and sheets for pin
details and the planner (research R7).

## Board list

```text
┌──────────────────────────────────────────┐
│ ←  Board pinouts                         │  shell top bar
│ Arduino Uno R3            ATmega328P     │  row: board name, chip
│ Arduino Nano              ATmega328P     │
│ Raspberry Pi Pico W       RP2040         │
│ Raspberry Pi Pico 2 W     RP2350         │
│ Raspberry Pi 4 Model B    BCM2711        │
│ STM32F1 "Blue Pill"       STM32F103C8T6  │
│ STM32F4 "Black Pill"      STM32F411CEU6  │
└──────────────────────────────────────────┘
```

- This is a `LazyColumn` with tag `bp-list`. Each row has tag `bp-row-<boardId>`.
- The list position is saved.

## Board page

```text
┌──────────────────────────────────────────┐
│ ←  Board pinouts                         │
│ STM32F1 "Blue Pill" · STM32F103C8T6      │  title
│ [ Drawing | List ]          [Planner (2)]│  view switch; planner button with count
│ Marks: [5V ✓] [3.3V ✓] [~ ADC] [⚡ Power]│  filter chips, scrollable row
│        [● Board] [⏻ Boot] [⚠ Special]    │
│ Top view, USB at the top                 │  view-side note
│ ┌──────── board drawing ───────┐         │  Canvas, tag bp-drawing
│ │ PB12 ▣ ●             ● ▣ GND  │         │  pads + labels + mark symbols
│ │ …                             │         │  assigned pins: filled + signal
│ └───────────────────────────────┘         │
│ Variants and notes …                     │
│ Sources: ST DS5319 Rev. …; …             │  tag bp-sources
└──────────────────────────────────────────┘
```

- **List view** (tag `bp-pins`): one row per header pin, in header order, with tag
  `bp-pin-<header>-<position>`. A row shows the number, label, chip pin, assignment and limit
  symbols.
  - Each row is one semantics node. It reads: "Pin 3, GPIO2, assigned to I2C1 SDA, 3.3 volt
    only, board pull-up 1.8 kilohm".
  - Tapping a row opens the pin sheet.
- **Drawing**: tapping a pad or its label opens the pin sheet. The canvas uses
  `clearAndSetSemantics { contentDescription = <board> pinout drawing, use the list to read
  pins }`.
- **Default view**: the drawing, or the list when fontScale > 1.3.
- **Onboard parts** (FR-017): a pin wired to an onboard part carries its short tag ("LED",
  "button", "crystal", "USB", "SWD", "ID EEPROM", "flash") on the drawing and in the list. Onboard
  LEDs and buttons are also drawn as schematic symbols in the middle of the board, tagged with their
  pin and joined to its pad by a dashed right-angle wire.
- **Keep the screen on** (FR-018, tag `bp-keep-screen-on`): a switch row under the controls. The
  choice is held by the module screen; `View.keepScreenOn` is set only while a board is shown.
- **Placement names**: when an instance has several placements, its name carries the pins of its
  required slots, for example "USART1 (PB6, PB7)" or "I2C3 (GPIO4, GPIO5)".
- **Marks** (tags `bp-mark-<LimitKind>`): each is a toggle chip, saved. A mark shows on the
  drawing as a symbol and a pattern, not only a color. In the list, it shows as symbols.

## Pin sheet (tag `bp-pin-sheet`)

- The header label, chip pin and any other header positions of the same pin.
- **Limits**, one row each (tag `bp-limit-<kind>`): the symbol, the name and the explanation,
  with value and condition. Group limits follow.
- **Functions**: every placement signal on this pin, as "I2C1 SDA (remap)". Each shows its
  verdict, and tapping it opens the planner at that placement.
- **Current assignment**, if any, with "Remove".

## Planner sheet (tag `bp-planner`)

```text
Assignments
  I2C1 (remap) · SDA PB9, SCL PB8   ⓘ moves as a group     [Edit] [Remove]
  USART2 · TX PA2, RX PA3                                  [Edit] [Remove]
[+ Add function]                                           [Clear all]
```

- **Add function** shows the `FunctionKind`s the board has. Choosing one lists its placements
  (tags `bp-placement-<id>`), each with:
  - its instance and pins;
  - "moves as a group" when it applies;
  - its notes;
  - its verdict:
    - **Available:** it can be picked.
    - **Warned:** ⚠ and the warning texts. It can be picked.
    - **Blocked:** disabled, shown with the reasons, for example "Uses GPIO8, already assigned to
      SPI0 CE0". The node is announced as disabled, followed by the reasons.
- **Edit**: one row per slot.
  - In per-signal placements, a slot offers its candidate pins, each with its verdict.
  - Optional slots have a "used" switch.
  - Required slots in a group placement may be switched off. The note "the remap stays" is then
    shown.
- **Clear all** asks for confirmation.

## Strings (prefix `board_pinouts_`, six locales)

- `title`, `description`, `keep_screen_on`, `tag_<BoardUse>`, `drawing`, `list`, `planner`, `marks`, `sources_label`, `view_<key>`,
  `add_function`, `clear_all`, `clear_all_confirm`, `remove`, `edit`, `used`, `moves_as_group`,
  `remap_stays`
- `function_<kind>` (8), `limit_<kind>` and `limit_<kind>_explain` (11 + 11), `use_<BoardUse>`,
  `boot_<effect>`, `condition_<key>`, `group_<key>`, `note_<key>`, `variant_<key>`,
  `board_<id>_description`
- `conflict_same_instance`, `conflict_pin_taken`, `conflict_resource_taken`,
  `resource_<id>`, `warning_board_use`, `warning_boot_role`
- `spoken_pin`, `spoken_assigned`, `spoken_blocked`, `drawing_description`

Pin, signal, instance, board and chip names are not localized. Values use the locale decimal
separator through `formatDecimal`, and keep the printed digits.
