# Data Model: Development Board Pinouts

**Feature**: [spec.md](spec.md) | **Date**: 2026-11-12

All types live in the pure-Kotlin core, `modules/board-pinouts/core`, and hold no display text
(research R6). The normative signatures are in [contracts/core-api.md](contracts/core-api.md).

## Board

| Field | Type | Rule |
|-------|------|------|
| `id` | `BoardId` | stable, unique: `uno-r3`, `nano`, `pico-w`, `pico2-w`, `rpi4b`, `blue-pill`, `black-pill` |
| `name` | String | as marketed, not localized ("Arduino Uno R3") |
| `chip` | String | as printed ("ATmega328P", "STM32F103C8T6") |
| `layout` | `BoardLayout` | outline, headers and view side |
| `pins` | `List<HeaderPin>` | every header position, unique `(header, position)` |
| `limits` | `Map<ChipPin, List<Limit>>` | the limits of each chip pin |
| `groupLimits` | `List<GroupLimit>` | port or total current limits |
| `functions` | `List<BoardFunction>` | in the order shown by the planner |
| `notes` | `List<BoardNote>` | variants sharing the header ("Pico variant", "F401 variant"), "reference schematic", "clones may differ", board warnings |
| `sources` | `List<Source>` | at least one board document and one chip document |
| `otherFunctions` | `Map<ChipPin, List<String>>` | functions the planner does not cover, as printed ("PCINT5", "PIO0", "TIM1_BKIN") |

The catalog is `boards: List<Board>`, in FR-002 order.

## BoardLayout

- `width` and `height` are in header pitch units (2.54mm = 1).
- `headers` is a list of `Header(id, origin, direction, count, label)`. The direction is `Down` or
  `Right`. Two-row headers (ICSP, the Pi 4's 40 pins) are two `Header` entries, or one header
  with `rows = 2` and zig-zag numbering, as the data needs.
- `usb` is the side and offset of the USB connector.
- `view` is a `ViewSide` key ("top view, USB at the top"; "top view, rotated: USB on the left").

## HeaderPin

| Field | Type | Rule |
|-------|------|------|
| `header` | header id | exists in the layout |
| `position` | Int | 1…count of that header |
| `number` | String | printed board pin number, if any ("3" on the Pi 4; "D2" style boards use the label) |
| `label` | String | silkscreen label ("D13", "GP2", "PB7", "3V3", "GND") |
| `pin` | `ChipPin?` | null for power and ground |
| `power` | `PowerPin?` | non-null exactly when `pin` is null |

`PowerPin(role, range: Value?, current: Value?)`. The role is one of `Ground`, `Supply3V3`,
`Supply5V`, `Vin`, `Vsys`, `Vbus`, `Vbat`, `Aref`, `Ioref`, `Reset`, `Enable`, `RunEnable`. A
`Ground` pin has no range. Every other role has a range (SC-003).

## ChipPin

A value class over the printed name ("PB7", "GP2", "GPIO2", "PD3", "ADC_VREF").

## Limit

`Limit(kind: LimitKind, value: Value?, condition: ConditionKey?, use: BoardUse?, effect: BootEffect?,
erratum: String?)`

| `kind` | Required extra |
|--------|----------------|
| `FiveVoltTolerant` | optional `condition` |
| `ThreeVoltOnly` | none |
| `Analog` | `value` is not used. The ADC or DAC channel is shown from the functions. |
| `BoardUse` | `use` |
| `InputOnly`, `OpenDrain` | none |
| `BoardPull` | `value` (resistance) and `condition` (up or down) |
| `BootRole` | `effect` |
| `CurrentLimit` | `value` (mA) |
| `Erratum` | `erratum` ("RP2350-E9") |

- A pin may not have both `FiveVoltTolerant` and `ThreeVoltOnly`.
- Every chip pin on the header has exactly one of the two.

`GroupLimit(pins: Set<ChipPin>, value: Value, key: GroupKey)`, for example the sum of output
currents of a port.

## Value

`Value(min, typ, max, unit)`, as in spec 006. Each bound is the printed decimal text or null. At
least one bound is set.

## BoardFunction and Placement

```text
BoardFunction(kind: FunctionKind, placements: List<Placement>)
FunctionKind = Uart | I2c | Spi | Pwm | Adc | Can | I2s | Usb   # FR-007

Placement(
  id: PlacementId,           # unique per board, for example "i2c1-remap"
  instance: String,          # as printed: "I2C1", "USART2", "TIM3", "PWM0", "ADC"
  slots: List<SignalSlot>,
  movesAsGroup: Boolean,     # STM32F1 AFIO remap
  resources: Set<ResourceId>,
  notes: List<NoteKey>,
  source: SourceRef,         # which source table, for the audit
)

SignalSlot(signal: String, candidates: List<ChipPin>, required: Boolean)
```

Rules:
- every candidate is a chip pin of a header pin of the same board;
- `movesAsGroup` implies one candidate per slot;
- slot signals are unique within a placement;
- placement ids are unique per board.

## Assignment (UI state, saved)

`Assignment(placement: PlacementId, chosen: Map<String, ChipPin?>)`. For each slot signal, the
chosen pin, or null if the slot is unused.

- A required slot cannot be unused, except in group placements, where the user may unuse any
  slot (spec US2 scenario 5).
- At least one slot is used.

## Availability (planner result)

```text
Availability = Available
             | Warned(warnings: List<Warning>)
             | Blocked(reasons: List<Conflict>)

Conflict = SameInstance(assignment)
         | PinTaken(pin, assignment, signal)
         | ResourceTaken(resource, assignment)
Warning  = PinBoardUse(pin, use) | PinBootRole(pin, effect)
```

Checks:
- `availability(board, assignments, placement)` checks the placement with its default candidate
  choice (the first free candidate per required slot).
- `pinAvailability(board, assignments, placement, slot, pin)` checks one candidate pin of a
  per-signal slot.

## Source

`Source(maker, document, title, revision)`. `SourceRef` points to a source and a table or section
("DS5319 Table 5", "RM0008 §9.3").

## State transitions (board page)

```text
list ──tap board──▶ board page ──tap pin──▶ pin sheet
                    │   ▲  └──planner──▶ planner sheet ──add──▶ placement picker
                    │   │                   ▲                     │ pick (not Blocked)
                    ◀back                   └─────────────────────┘
```

Back closes the top sheet, then the board page. Assignments persist per board (research R7).
