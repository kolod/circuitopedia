# Research: Development Board Pinouts

**Feature**: [spec.md](spec.md) | **Date**: 2026-11-12

## R1. Sources (Principle II)

- **Decision**: For each board, the board maker's documents fix the layout, the headers and the
  board's own use of pins. The chip maker's datasheet, reference manual and errata fix the
  alternate functions, the limits and the currents.
- **Recording**: as in spec 006 (R2), the document number and title are fixed here. The
  **revision and date are recorded when the data is transcribed**, from the PDF actually used,
  and stored in the board's `Source` list. An entry marked "confirm" must be checked against the
  maker's website at transcription. If it differs, this table is updated in the same commit.
- **Downloads**: PDFs are fetched with the agent's fetch tool or supplied by the user. Bot checks
  are never bypassed. If a site refuses, the user is asked for the file.

| Board | Board documents | Chip documents |
|-------|-----------------|----------------|
| Arduino Uno R3 | Arduino, A000066 datasheet; UNO R3 schematic | Microchip DS40002061, ATmega48A/PA/88A/PA/168A/PA/328/P datasheet (confirm) |
| Arduino Nano | Arduino, A000005 datasheet; Nano schematic | same as Uno |
| Pico W | Raspberry Pi, "Raspberry Pi Pico W Datasheet" | Raspberry Pi, "RP2040 Datasheet" |
| Pico 2 W | Raspberry Pi, "Raspberry Pi Pico 2 W Datasheet" | Raspberry Pi, "RP2350 Datasheet" (including its errata appendix) |
| Raspberry Pi 4 Model B | Raspberry Pi, "Raspberry Pi 4 Model B Datasheet"; Raspberry Pi documentation, "GPIO and the 40-pin header", and the firmware overlay README for I2C3–6, SPI3–6, UART2–5 | Raspberry Pi, "BCM2711 ARM Peripherals" (GPIO alternate function table) |
| Blue Pill | No official maker. The reference schematic is the one published by stm32-base.org for "STM32F103C8T6 Blue Pill" (confirm). The page states it. | ST DS5319, STM32F103x8/xB datasheet; RM0008 reference manual (AFIO remap tables); ES096 errata (confirm the number) |
| Black Pill | WeAct Studio, "MiniSTM32F4x1" schematic (GitHub, board V3.0 for STM32F411CEU6) (confirm) | ST DS10314, STM32F411xC/xE datasheet (alternate function table); RM0383 reference manual; ES0287 errata (confirm) |

- **Conflicts**: where a board document and a chip document disagree (for example a current
  limit), the stricter value is shown and both are named in the pin note.
- **Alternatives considered**: community pinout diagrams (for example pinout.xyz). They are fine
  for checking the result, but they are not primary sources and are not cited.

## R2. Board revisions and variants

- **Decision**: the boards are Uno R3, the classic Nano, Pico W, Pico 2 W, Pi 4 Model B (40-pin
  header only), Blue Pill (STM32F103C8T6) and WeAct Black Pill V3.0 (STM32F411CEU6).
- **Variants**: each board lists the variants that share its header, with their differences as
  localized notes:
  - Pico W also covers the Pico: on the Pico, GP25 drives the LED and GP23/GP24/GP29 have board
    roles; on the W, the wireless chip uses them.
  - Pico 2 W also covers the Pico 2, in the same way.
  - Black Pill: the STM32F401CCU6/CEU6 variants. Their differences are named from DS10086 (F401)
    against DS10314, but they are not planned.
- **Headers shown**: every header a user wires to. For the Uno these are the power, analog and two
  digital headers, including the SDA/SCL/AREF/GND row, plus the 6-pin ICSP header of the
  ATmega328P. For the Nano, its two rows and the ICSP pads. For the Pico boards, the two
  20-pin rows; the 3-pin debug header is mentioned as a note. For the Pi 4, the 40-pin header. For
  the Blue Pill, its two rows and the 4-pin SWD header. For the Black Pill, its two rows and the
  4-pin SWD header. USB connectors are not pins.

## R3. Planner model (FR-007–FR-012)

- **Decision**: one model covers all four routing styles. A function on a board is a list of
  **placements**. A placement is one peripheral instance, with **signal slots**. Each slot has one
  or more **candidate chip pins**:
  - **Group remap** (STM32F1 AFIO): one placement per remap option (no remap, partial remap, full
    remap). Each slot has exactly one candidate, so all signals move together. The placement is
    flagged `movesAsGroup`, and the UI explains it.
  - **Fixed routing** (ATmega328P): one placement per instance, one candidate per slot.
  - **Pin-set alternate mode** (BCM2711): one placement per instance and pin set (for example
    I2C3 on GPIO2/3 ALT5, or on GPIO4/5 ALT5). Each slot has one candidate.
  - **Per-signal routing** (RP2040, RP2350, STM32F4): one placement per instance. A slot lists every
    pin that can carry that signal, and the user picks one per used slot.
- **Rules**, pure functions in the core, tested first:
  1. An instance is assigned at most once per board.
  2. A chip pin carries at most one used signal.
  3. Two placements that need the same **shared resource** conflict. Resources are named in the
     data from the cited documents:
     - the STM32F103 USB and CAN share a dedicated SRAM;
     - the STM32F1 JTAG release is needed for PA15/PB3/PB4. This is a warning, not a resource:
       see rule 5;
     - erratum conflicts, as the errata state them.
  4. A slot is `required` (for example I2C SDA and SCL, UART TX/RX) or `optional` (CTS, RTS, NSS,
     each ADC channel, each PWM channel). Optional slots start unused. A placement with no used
     slot cannot be confirmed.
  5. Pins whose limits include "used on the board" or "boot role" give **warnings**, not blocks.
     Each warning names the pin and the reason.
  6. Unusing a slot releases its pin. In a group placement, the other slots stay on their pins
     (the remap stays), and the UI says so.
- **Availability**: for a candidate placement against the current assignments, the result is one of:
  - `Available`;
  - `AvailableWithWarnings(reasons)`;
  - `Blocked(reasons)`: the reasons name the pin or resource and the assignment holding it.

  For per-signal placements, each candidate pin of a slot gets the same verdict.
- **PWM and ADC**: PWM is planned per timer or slice instance, with each channel as a slot. Two
  pins that output the same channel (RP2040 GP0 and GP16 = PWM0 A) are two candidates of one slot,
  so they cannot both be used. On the Pi 4, the same applies to PWM0 channel 0 on GPIO12 and
  GPIO18. ADC is one instance whose channels are optional slots with one candidate each.
- **Software notes**: placements may carry note keys, which are localized in the UI and are not
  rules:
  - needs a device-tree overlay (Pi 4 I2C3–6, SPI3–6, UART2–5);
  - Bluetooth uses UART0 by default (Pi 4);
  - Timer0 is used by the Arduino core for `millis()`;
  - JTAG must be disabled to use PA15/PB3/PB4 (STM32F1);
  - SS must stay an output in SPI master mode (ATmega328P).
- **Alternatives considered**:
  - A constraint solver that proposes whole configurations: out of scope, and harder to explain.
  - Highlighting only: rejected by the user.

## R4. Pin limits (FR-006)

- **Decision**: the limits are a closed enum, `LimitKind`, with an optional value (voltage range or
  current) and an optional condition key:
  - `FiveVoltTolerant` (condition, for example "not in analog mode", or "IOVDD powered");
  - `ThreeVoltOnly`;
  - `Analog` (ADC channel or DAC);
  - `BoardUse` (with a `BoardUse` key: USB, crystal, LED, button, debug, wireless, ID EEPROM,
    USB-serial);
  - `InputOnly`;
  - `OpenDrain`;
  - `BoardPull` (pull-up or pull-down on the board, with its value);
  - `BootRole` (with an effect key);
  - `Power` (supply or ground, with a voltage range and current);
  - `CurrentLimit` (per pin);
  - `Erratum` (with its number).
- **Port and group limits** (for example "total of ports C0–C5", or the Pi's 50mA total for all
  GPIO, when the source states it) are a board-level list. Each pin's details list the groups it
  belongs to.
- **Values**: exact decimal text as printed, with its unit ("40", "mA"; "3.3", "V"). They are
  formatted for the locale by `formatDecimal` from `:shared:si-format`, which keeps the printed
  digits, as `printed()` does in spec 006.
- **Marks**: each limit kind has a symbol and a pattern as well as a color (FR-016). The drawing
  shows the marks of the enabled kinds. By default, 5V tolerant, 3.3V only and power are enabled.

## R5. Drawing and list

- **Decision**: a Compose `Canvas` per board, drawn from a **layout description in the data**:
  - the board outline size;
  - the header rows, each with origin, direction and pitch;
  - the USB position;
  - the view side.

  There are no per-board drawing functions, because one layout model fits all seven boards. Pins
  are pads with numbers. Labels sit outside the rows. The board is drawn upright, with USB at the
  top for the Pico, Nano, Blue Pill and Black Pill. The Uno and Pi 4 are drawn rotated so the
  long side is vertical, and the view note says so.
- **Interaction**: tapping a pad opens that pin's details. On the drawing, pad hit areas are at
  least 48dp wide. Where the pitch is smaller than 48dp, the label column is the tap target. The
  canvas is `clearAndSetSemantics`, and the **list** carries the semantics (FR-015).
- **List**: a tab or segmented switch, "Drawing | List", on the board page. The list shows one row
  per header pin, in header order: number, label, chip pin, assignment and limit symbols. At
  fontScale > 1.3 the list is the default.
- **Alternatives considered**: SVG assets per board, with the library approved in spec 002. They
  would need hit-testing and recoloring of SVG paths, which is more work than drawing from data.

## R6. Data representation (Principle III)

- **Decision**: pure-Kotlin data in `modules/board-pinouts/core`, one file per board, with stable
  keys only:
  - `BoardId`;
  - `ChipPin` names (as printed: "PB7", "GP2", "GPIO2", "PD3");
  - `HeaderPin`, a header id and position mapped to a chip pin or a power role;
  - `Placement` lists;
  - `LimitKind` values, `NoteKey` enums and `Source`.

  Localized text goes in string resources: board descriptions, variant notes, limit names and
  explanations, note keys, boot effects and planner messages. Signal and peripheral names are not
  localized.
- **Header duplicates**: several header pins may map to one chip pin (Uno SDA/A4, Uno ICSP
  MISO/D12). Claims are made on chip pins, so every header position of that pin shows the
  assignment.
- **Size**: about 200 header pins and 300–400 placements in total. Kotlin data of this size
  compiles fine. If a file exceeds the JVM method size limit, the board's data is split into
  functions per header.
- **Alternatives considered**: JSON assets. These need a parser and are not type-checked
  (spec 006 R4).

## R7. Navigation and state

- **Decision**: one module screen with three levels: board list, board page, and pin details as
  a modal bottom sheet. The planner is a second sheet. A `BackHandler` closes the sheets, then the
  board page, as in spec 006, and the up arrow goes through the back dispatcher (since 710c5b1).
- **Saved state** (`rememberSaveable`):
  - the open board id;
  - the open pin;
  - the drawing or list choice;
  - the enabled limit kinds;
  - the assignments of **all** boards, as a `Map<BoardId, List<Assignment>>` with a custom `Saver`
    that writes compact strings (`instance|slot=pin,…`).

  These survive rotation and process death, but not a user's close (FR-013).

## R8. Testing strategy

- **JVM (core), written first**:
  - `BoardDataTest`:
    - the seven boards in FR-002 order;
    - every header pin has a number and a label;
    - every non-power header pin maps to a chip pin that exists;
    - every power pin has a range;
    - every board has sources with revisions;
    - every placement candidate is a chip pin on that board's headers;
    - every function has at least one placement (SC-003).
  - `PlannerTest`:
    - the rules of R3 on small hand-made boards;
    - a pairwise sweep over every placement of every real board: two placements conflict exactly
      when they share an instance, a chip pin in required slots, or a resource (SC-002).
  - `SpotDataTest`: known facts per board, each naming its source table. Examples:
    - Pi 4 I2C1 is on GPIO2/GPIO3 ALT0 and the board has 1.8kΩ pull-ups;
    - Blue Pill I2C1 remap is on PB8/PB9;
    - Blue Pill PA0 is not 5V tolerant, but PB10 is;
    - Nano A6/A7 are input-only analog;
    - RP2350 GPIO are 5V tolerant except the ADC pins.
- **Instrumented**:
  - board list order;
  - opening a board, then back;
  - pin details;
  - the 5V mark toggle;
  - planner: add I2C on the Pi 4, then see a conflicting SPI placement as blocked, with its
    reason;
  - unuse a slot;
  - state restoration of assignments;
  - every pin's spoken text in the list.
- **Manual (SC-004)**: `checklists/source-audit.md` gets one line per board and document, with the
  revision checked.
