# Feature Specification: Development Board Pinouts

**Feature Branch**: `007-board-pinouts`

**Created**: 2026-11-12

**Status**: Draft

**Input**: User description: "A new module to look up the pinouts of popular development boards:
Arduino Nano, Arduino Uno, Raspberry Pi Pico (Wi-Fi), Raspberry Pi Pico 2 (Wi-Fi), Raspberry Pi 4
Model B, STM32F1 (Blue Pill), STM32F4 (Black Pill). It must show alternate functions, and make
clear that some pins are remapped as a whole group, so that the user cannot pick an invalid
option (for example I2C on the Raspberry Pi 4 Model B). It must show usage limits, for example
highlight the pins that tolerate 5V TTL."

Scope decided with the user on 2026-11-12:
- **Alternate functions:** a planner. The user assigns functions to pins. Choosing a function
  claims every pin it needs at once, and options that would conflict are shown as unavailable,
  with the reason.
- **Board revisions:** the classic ones (see FR-002).
- **Pin limits to mark:**
  - 5V tolerant, and 3.3V only;
  - analog capable;
  - pins already used on the board;
  - input-only and other special pins;
  - current limits and power pins;
  - pins with a special role during boot, which must be used with care.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - See a board's pinout and each pin's limits (Priority: P1)

A hobbyist is wiring a sensor to a Blue Pill and needs to know which header pin is PB7, and
whether it survives a 5V signal. They open the Reference tab, choose "Board pinouts" and then
"STM32F1 (Blue Pill)". They see:
- a drawing of the board's headers, with every pin numbered and labeled with its main name;
- the side the board is viewed from, and the position of the USB connector;
- the limits of every pin, marked so they can be told apart at a glance: 5V tolerant, 3.3V only,
  analog, used on the board, boot role, power.

Tapping a pin opens its details:
- its names (header label and chip pin name);
- all its alternate functions;
- its limits, each with a short explanation, for example "5V tolerant (FT): accepts up to 5V as
  an input when not in analog mode";
- the current it may source or sink.

**Why this priority**: Finding the right pin and knowing whether it is safe is what people look
up most at the bench. It is useful on its own, without the planner.

**Independent Test**: Open each of the seven boards and compare every header pin, its names, its
alternate functions and its limits with the cited documents.

**Acceptance Scenarios**:

1. **Given** the Reference tab, **When** the user opens "Board pinouts", **Then** the seven boards
   of FR-002 are listed in that order, each with its chip.
2. **Given** a board page, **Then** every header pin of the board is drawn in its physical place,
   with its number and main name, and the page states the view side.
3. **Given** the Blue Pill page, **When** the user turns on the "5V tolerant" mark, **Then** only
   the pins that the chip datasheet marks as 5V tolerant are highlighted. On the Raspberry Pi 4 the
   same mark highlights no GPIO pin, and the page says that all GPIO pins are 3.3V only.
4. **Given** a pin, **When** the user taps it, **Then** its details list every alternate function
   from the cited documents and every limit that applies to it.
5. **Given** a pin with a boot role (for example the Raspberry Pi 4 ID EEPROM pins GPIO0 and GPIO1,
   or BOOT1 on the Blue Pill), **Then** its details warn what happens at start-up if it is driven
   or loaded.
6. **Given** a power pin (for example VIN, 5V, 3V3, VSYS, VBUS), **Then** its details give its
   allowed voltage range and the current it can supply or must receive, as the cited document
   states.

---

### User Story 2 - Plan pin assignments without invalid combinations (Priority: P1)

The user wants I2C, a UART and SPI on a Raspberry Pi 4. On the board page they open the planner and
add "I2C". The planner offers every valid placement of the I2C peripherals on the header, for
example "I2C1 on GPIO2/GPIO3 (pins 3 and 5)", with the placement's notes. When they pick one, both
pins are claimed together and shown as assigned on the drawing. Options that would need a pin that
is already claimed are shown as unavailable, with the reason, for example "Uses GPIO8, already
assigned to SPI0 CE0".

On a Blue Pill, picking the remapped I2C1 (PB8/PB9) moves both signals together. The planner
explains that the STM32F1 remaps a peripheral's pins as a group. On a Pico, each signal of a
peripheral can be placed on any of its allowed pins, but both signals must belong to the same
peripheral instance (I2C0 or I2C1).

**Why this priority**: Group remapping is the main trap the user wants to avoid. The planner makes
an invalid choice impossible instead of leaving the check to the user.

**Independent Test**: On each board, assign a set of peripherals that is known to fit and check
the result. Then try to add one that conflicts, and check that it is offered as unavailable with
the right reason.

**Acceptance Scenarios**:

1. **Given** a board page, **When** the user adds a function in the planner, **Then** every valid
   placement of that function on the header is offered, with the pins it uses.
2. **Given** a placement that remaps a peripheral as a group, **When** it is picked, **Then** all
   the pins of that group are claimed together, and the planner states that they move as a group.
3. **Given** a placement whose pins are already claimed, **Then** it is shown as unavailable and
   names the pin and the function that holds it. It cannot be picked.
4. **Given** a placement that uses a pin with a boot role or a pin used on the board (for example
   PA11/PA12 for USB on the Blue Pill), **Then** it can be picked, but it carries a warning that
   explains the risk.
5. **Given** an assigned function, **When** the user marks one of its signals as unused (for
   example UART TX only), **Then** that signal's pin is released. On boards that remap as a group,
   the planner notes that the peripheral stays in the chosen mapping.
6. **Given** assignments on a board, **When** the user removes one or clears all, **Then** the pins
   are released and the options that depended on them become available again.
7. **Given** assignments on a board, **When** the device rotates or the app is recreated, **Then**
   the assignments are kept. Each board keeps its own assignments while the app runs.

---

### User Story 3 - Use it in any supported language and setting (Priority: P2)

As in the other modules: six languages, dark theme, 200% font, portrait only, and screen-reader
support. Because the header drawing is dense, the same pins are also available as a list, so a
screen-reader user or a user with a large font can read and assign every pin without the drawing.

**Why this priority**: Required by the constitution.

**Independent Test**: With TalkBack on, read every pin of the Pico 2 W in header order and assign
I2C through the planner. At 200% font, do the same with the list.

**Acceptance Scenarios**:

1. **Given** TalkBack is on, **When** a pin is focused in the drawing or the list, **Then** it is
   read as its number, its name, its assignment if any, and its limits, for example "Pin 3, GPIO2,
   assigned to I2C1 SDA, 3.3V only, has pull-up".
2. **Given** the Ukrainian language, **Then** limit names, explanations, warnings and planner
   messages are in Ukrainian. Pin names, signal names and peripheral names stay as printed
   ("PB7", "I2C1 SDA").
3. **Given** 200% font size, **Then** every pin, function and limit stays readable through the
   list without horizontal scrolling of the page.
4. **Given** the limit marks, **Then** each is told apart by a symbol or pattern as well as by
   color, so it reads in both themes and with color blindness.

---

### Edge Cases

- **Variants sharing a header** (Pico and Pico W, Pico 2 and Pico 2 W, Black Pill with F401 or
  F411): one entry per board as listed in FR-002. The page names the variants that share its
  header and lists their differences, for example the Pico W onboard LED driven by the wireless
  chip, or the peripherals missing on the F401.
- **Pins not on the header** (crystal pins, the Pico W GPIO23/24/25/29 used by the wireless chip):
  they are not drawn and cannot be planned. The page mentions them where they explain a missing
  number or function.
- **Functions that also depend on software** (Raspberry Pi 4 I2C3–I2C6, SPI3–SPI6 and the extra
  UARTs that need a device-tree overlay): the placement says so, as a note.
- **Shared resources other than pins** (for example a timer used for PWM on two pins, or ADC
  channels shared with a pin's digital use): where the cited documents state such a link, the
  planner treats it like a pin conflict and gives the reason.
- **Chip errata affecting pin use** (for example an RP2350 erratum about GPIO input pull-downs):
  noted in the pin details with the erratum number.
- **No placement available**: when every placement of a function is blocked, the function is still
  listed, marked unavailable, with the reasons.
- **Board clones** (Blue Pill, Black Pill): the page states which reference board the drawing
  follows, and that clones may differ in onboard parts.

## Requirements *(mandatory)*

### Functional Requirements

**Module and content**

- **FR-001**: The module MUST be listed in the Reference category with a localized title ("Board
  pinouts") and description ("Dev board headers, pin functions and limits"), following the module
  contract of spec 001.
- **FR-002**: The module MUST contain these boards, in this order:
  1. Arduino Uno R3 (ATmega328P);
  2. Arduino Nano (ATmega328P, classic);
  3. Raspberry Pi Pico W (RP2040);
  4. Raspberry Pi Pico 2 W (RP2350);
  5. Raspberry Pi 4 Model B (BCM2711, 40-pin header);
  6. STM32F1 "Blue Pill" (STM32F103C8T6);
  7. STM32F4 "Black Pill" (WeAct, STM32F411CEU6).
- **FR-003**: Each board page MUST show a drawing of all its headers in their physical layout.
  Every pin has its number and main name, and the page states the view side and where the USB
  connector is.
- **FR-004**: Each board MUST also offer the same pins as a list in header order, with the same
  information and actions as the drawing.
- **FR-005**: Each pin's details MUST show:
  - its header label and its chip pin name;
  - all its alternate functions on that board, as given by the cited documents;
  - every limit of FR-006 that applies, each with a one-line explanation;
  - its current limit, and any group limit it shares (for example the total current of a port).
- **FR-006**: The following pin limits MUST be supported, each marked so it can be shown or
  hidden on the drawing:
  - **5V tolerant:** with the conditions under which the datasheet allows it;
  - **3.3V only:** not 5V tolerant;
  - **Analog:** ADC channel, or DAC output where the chip has one;
  - **Used on the board:** USB, crystal, onboard LED, button, debug port, wireless chip, ID EEPROM;
  - **Special:** input-only, open-drain, fixed pull-up or pull-down on the board;
  - **Boot role:** pins whose level or load at reset changes how the board starts, with the
    effect;
  - **Power:** supply and ground pins, with the allowed voltage range and current.

**Planner**

- **FR-007**: Each board MUST have a planner with the functions its chip offers on the header:
  UART, I2C, SPI, PWM, ADC, CAN, I2S and USB, as applicable. Each function lists its valid
  placements: a peripheral instance and the pins its signals use.
- **FR-008**: The planner MUST follow each chip's routing rules:
  - where a chip remaps a peripheral's pins as a group (STM32F1), a placement claims the whole
    group;
  - where each signal can be routed on its own (RP2040, RP2350, STM32F4), each signal is placed
    separately among its allowed pins, within the same peripheral instance;
  - where the board fixes the routing (ATmega328P), there is one placement;
  - where a function needs an alternate mode of a pin set (BCM2711), the placement claims those
    pins.
- **FR-009**: A placement that needs a pin or a shared resource already claimed MUST be shown as
  unavailable, with the pin or resource and the function that holds it, and MUST NOT be pickable.
- **FR-010**: A placement that uses a pin with a boot role, or a pin used on the board, MUST be
  pickable, but MUST show a warning that explains the risk.
- **FR-011**: The user MUST be able to mark a signal of an assigned function as unused, remove one
  assignment, or clear all assignments of the board. Released pins become available at once.
- **FR-012**: The drawing and the list MUST show each assigned pin with its function and signal.
  The planner MUST show a summary of the board's assignments.

**Navigation and state**

- **FR-013**: The board list opens on the boards in FR-002 order. Tapping a board opens its page,
  and back returns to the list at the same position. The open board, the open pin details, the
  shown limit marks and each board's assignments survive rotation and process recreation.
  Assignments are not saved after the app is closed by the user.

**Accessibility and localization**

- **FR-014**: All text except board names, chip names, pin names, signal names and peripheral
  names MUST be in the six supported languages. This covers limit names and explanations,
  warnings, notes, planner messages and view-side notes. Numbers use the locale's decimal
  separator.
- **FR-015**: Each pin MUST be read by screen readers as its number, name, assignment and limits.
  Every planner action MUST be reachable without the drawing.
- **FR-016**: Limit marks and assignments MUST be distinguishable without color, by symbol or
  pattern.

**Added with the user on 2026-11-12**

- **FR-017**: The drawing and the list MUST always name the onboard part wired to a pin (LED,
  button, crystal, USB, debug port, ID EEPROM, flash footprint), whatever marks are enabled.
- **FR-018**: A board page MUST offer a "Keep the screen on" switch. While it is on and a board is
  shown, the screen does not dim or turn off. Going back to the board list or leaving the module
  releases the screen; opening a board again restores the user's choice. No permission is needed.

### Key Entities

- **Board**: a name, its chip, its variants with their differences, its headers, its sources, and
  notes (reference board for clones, view side).
- **Header pin**: a header and position, a number, a main label, the chip pin it connects to (or a
  power or ground role), and its limits.
- **Pin limit**: a kind (FR-006), an optional condition or value (voltage range, current), and a
  localized explanation.
- **Alternate function**: a peripheral instance and signal (for example "USART1 TX") available on a
  chip pin, with its source.
- **Placement**: a function's option on a board: the peripheral instance, the pins of each signal,
  whether the pins move as a group, the shared resources it uses, and its notes.
- **Assignment**: a chosen placement on a board, with the signals marked used or unused.
- **Source**: a document's maker, title or number, and revision or date.

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

Every pin, alternate function, limit and current value MUST come from a cited official document.
The planning phase MUST pick the current revision of each, record its title, number and revision,
transcribe the data and verify it, and note conflicts between documents. Expected sources:

- **Arduino Uno R3 and Nano:** Arduino's board datasheets and schematics (A000066, A000005), plus
  the Microchip ATmega328P datasheet for alternate functions, limits and currents.
- **Raspberry Pi Pico W and Pico 2 W:** Raspberry Pi's "Pico W datasheet" and "Pico 2 W
  datasheet", plus the RP2040 and RP2350 datasheets (GPIO function tables, electrical
  characteristics, errata).
- **Raspberry Pi 4 Model B:** Raspberry Pi's "Raspberry Pi 4 Model B datasheet" and "BCM2711 ARM
  Peripherals" (alternate function table), plus the official documentation of the 40-pin header
  and its overlays.
- **Blue Pill:** STMicroelectronics STM32F103x8/xB datasheet (DS5319) and the reference manual
  RM0008 (AFIO remapping). The board has no official maker, so the board layout follows a
  published reference schematic, which the plan names.
- **Black Pill:** STMicroelectronics STM32F411xC/xE datasheet (DS10314), plus WeAct Studio's
  published schematic of the board.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: There is no numeric input. The user only navigates, toggles marks and picks
  placements. Values carry their units as printed in the source (V, mA).
- **Invalid input**: Invalid pin combinations cannot be picked (FR-009). The reason is shown
  instead of an error.
- **Precision/rounding**: Values are shown as the source gives them, without rounding or
  conversion. The decimal separator follows the locale.

### New Dependencies *(Principle III)*

- None. Drawings are made with the existing UI toolkit.

### Permissions & Network *(Principle I)*

- None. All data ships with the app, and sources are cited by name, not linked or downloaded.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk:
  - the module title and description;
  - board descriptions, variant differences and notes;
  - limit names and explanations;
  - boot-role effects and warnings;
  - planner labels and messages;
  - view-side notes.
- Board, chip, pin, signal and peripheral names are not translated. Decimal separators follow the
  locale.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: From the Reference tab, a user can find any header pin of any listed board and read
  whether it is 5V tolerant in under 15 seconds.
- **SC-002**: For every board, the planner never accepts a set of assignments that the cited
  documents forbid. An automated check covers every placement of every function against every
  other.
- **SC-003**: For every board, an automated check confirms that every header pin has a name and a
  source, that every function has at least one placement, and that every power pin has a voltage
  range.
- **SC-004**: A manual audit of every board against its cited documents finds no transcription
  error before release.
- **SC-005**: A user can assign three common peripherals (one I2C, one UART, one SPI) on any board
  that has them, in under one minute and without an invalid result.
- **SC-006**: All text appears in each of the six supported languages, with no missing or raw-key
  strings. Every pin and planner action works with a screen reader and at 200% font size.

## Assumptions

- "Classic" revisions were agreed with the user. Arduino Uno R4, Nano Every, Nano 33 and Nano ESP32
  are not included, nor the Raspberry Pi 5 or other models; they may come in a later spec.
- The Pico W and Pico 2 W pages also serve the Pico and Pico 2, which share their headers.
- The Black Pill page follows WeAct's STM32F411CEU6 board, and notes the STM32F401 variant's
  differences.
- Only the pins on the board's headers are shown. Debug, test or pad-only pins are mentioned only
  where they matter (for example the Pico SWD pads, or the Blue Pill SWD header).
- The planner covers pin routing and the shared resources stated in the cited documents. It does
  not check timing, clock trees, DMA channels or power budgets.
- Assignments are a working aid during a session. Saving or sharing named configurations is out of
  scope.
