# Feature Specification: Voltage Divider Calculator

**Feature Branch**: `005-voltage-divider`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "A voltage divider calculator. The user chooses which values are known
and which must be found: U1 (input voltage), U2 (output voltage), R1 (upper resistor), R2 (lower
resistor), R (total resistance). Circuit: U1 — R1 — U2 — R2 — GND. A resistance obtained here can
be passed quickly to the resistor combination finder."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Find the missing values of a divider (Priority: P1)

An engineer needs 3.3V from a 5V rail and has a 10kΩ resistor for the upper position. They open
"Voltage divider" from the Calculations tab. A schematic shows U1 — R1 — U2 — R2 — GND with each
quantity labeled. Below it are five fields: U1, U2, R1, R2 and R. They enter U1 = 5V, U2 = 3.3V
and R1 = 10kΩ. The calculator shows R2 = 19.41kΩ and R = 29.41kΩ.

Any three independent quantities can be known, and the other two are computed. That gives nine
cases. The three resistances alone are not enough, because they fix no voltage.

**Why this priority**: This is the whole purpose of the module.

**Independent Test**: For each of the nine valid sets of three known quantities, enter values
from a known divider and check the two computed values against hand calculation.

**Acceptance Scenarios**:

1. **Given** U1 = 5V, U2 = 3.3V, R1 = 10kΩ are known, **When** they are entered, **Then** R2 =
   19.41kΩ and R = 29.41kΩ are shown.
2. **Given** U1 = 12V, R1 = 10kΩ, R2 = 4.7kΩ are known, **Then** U2 = 3.837V and R = 14.7kΩ.
3. **Given** U2 = 2.5V, R1 = 1kΩ, R = 3kΩ are known, **Then** R2 = 2kΩ and U1 = 3.75V.
4. **Given** U1 = 9V, U2 = 3V, R = 30kΩ are known, **Then** R1 = 20kΩ and R2 = 10kΩ.
5. **Given** any known value changes, **When** it is edited, **Then** the computed values update
   immediately, without a separate "calculate" action.
6. **Given** the known values cannot form a divider (for example U2 ≥ U1, or R1 ≥ R), **Then** no
   result is shown and a short message names the problem.

---

### User Story 2 - Send a resistance to the combination finder (Priority: P2)

The computed R2 = 19.41kΩ is not a standard value. Next to each resistance (R1, R2 and R), a
button opens "Resistor combinations" with that resistance already filled in as the target. The
user picks a combination there and goes back, and the divider still shows their values.

**Why this priority**: It saves retyping, and it joins the two calculators into one workflow.
The divider is useful without it.

**Independent Test**: Compute R2 = 19.41kΩ, tap its "Find combinations" button, and check that
the finder opens with 19.41kΩ as its target and lists results. Press back and check that the
divider shows the same values.

**Acceptance Scenarios**:

1. **Given** R2 = 19.41kΩ is computed, **When** the user taps its "Find combinations" button,
   **Then** the combination finder opens with the target "19.41k" (4 significant digits), and its
   results are listed.
2. **Given** the finder was opened from the divider, **When** the user presses back, **Then** the
   divider returns with all its values and its choice of known quantities unchanged.
3. **Given** a resistance field is empty or invalid, **Then** its button is disabled.
4. **Given** the finder is opened normally from the catalog, **Then** it starts empty as before.
   Only an explicit hand-over fills the target.

---

### User Story 3 - Use it in any supported language and setting (Priority: P2)

As in the other calculators: all six languages, the locale's decimal separator in input and
output, dark theme, 200% font, portrait only, and screen-reader support. Each field announces its
name, value and whether it is known or computed.

**Why this priority**: Required by the constitution.

**Independent Test**: In Ukrainian, enter "3,3" for U2 and check that the computed values use
decimal commas. With TalkBack, focus a computed field and hear its name, its value with unit words,
and "computed".

**Acceptance Scenarios**:

1. **Given** the Ukrainian language, **When** the user enters "4,7k" for R1, **Then** it is
   accepted, and the results use decimal commas.
2. **Given** TalkBack is on, **When** a computed field is focused, **Then** it is read as, for
   example, "R2, 19.41 kilohm, computed".
3. **Given** 200% font size, **Then** the schematic, the fields and the buttons stay usable, and no
   value is cut off.

---

### Edge Cases

- **Only two values entered**: nothing is computed yet. The message says which quantities are still
  needed.
- **The three resistances are known**: this does not determine a divider, because no voltage is
  given. This set cannot be chosen, or a message explains it, depending on the answer to FR-004.
- **U2 equal to U1**: this would need R1 = 0, so it is invalid. **U2 = 0** would need R2 = 0, so it
  is invalid as well. **U2 greater than U1** is invalid for a passive divider.
- **R1 or R2 not smaller than R** (when R is known): invalid.
- **Negative voltages**: both U1 and U2 may be negative if they have the same sign, so that
  0 < U2/U1 < 1. Mixed signs are invalid.
- **Very large or small values**: voltages from 1mV to 100kV and resistances from 0.1Ω to 1GΩ are
  accepted. Values outside these ranges are invalid.
- **Process recreation or rotation**: all entered values and the choice of known quantities are
  kept.

## Requirements *(mandatory)*

### Functional Requirements

**Module**

- **FR-001**: The module MUST be listed in the Calculations category with a localized title
  ("Voltage divider") and description ("Find U1, U2, R1, R2 or R of a resistive divider"),
  following the module contract of spec 001.
- **FR-002**: The screen MUST show a schematic of the divider, U1 — R1 — U2 — R2 — GND, with the
  five quantities labeled (U1, U2, R1, R2, R).

**Input and computation**

- **FR-003**: Each of the five quantities MUST have a field. Voltages are entered in volts with an
  optional SI prefix (m, k), and resistances in ohms with the notations of spec 004 FR-002 ("4.7k",
  "4k7", "R47"). Both "." and "," are accepted as the decimal separator.
- **FR-004**: [NEEDS CLARIFICATION: How does the user choose which quantities are known and which
  are computed? (a) each field has a "known / computed" switch, and exactly three must be known;
  (b) automatically: the three most recently edited fields are known, and the other two are
  computed; (c) the user picks one of the nine cases from a list (for example "Find R2 and R from
  U1, U2, R1"), and only the three known fields can be edited.]
- **FR-005**: From any valid set of three known quantities, the two others MUST be computed with
  `U2 = U1 · R2 / (R1 + R2)` and `R = R1 + R2`. The results update immediately on every change.
- **FR-006**: Invalid known values MUST NOT produce results. A localized message names the
  problem: missing values, U2 not between 0 and U1, a resistance not smaller than R, a value out
  of range, or an invalid notation.
- **FR-007**: Computed values MUST be shown with 4 significant digits and the SI prefix that puts
  the mantissa in [1, 1000), for example "19.41kΩ" or "3.837V". Computed fields MUST look clearly
  different from known ones.

**Hand-over to the combination finder**

- **FR-008**: Each resistance (R1, R2, R) MUST have a "Find combinations" action when it has a
  valid value, whether entered or computed. The action opens the Resistor combinations module with
  that value, at 4 significant digits, as its target.
- **FR-009**: Returning from the combination finder MUST show the divider unchanged. The finder
  opened this way MUST behave like a normal finder in every other respect.
- **FR-010**: Opening a module with a value MUST go through the app shell. Neither module may
  depend on the other (spec 001 FR-013). Other modules can use the same mechanism later.

**State and accessibility**

- **FR-011**: All entered values and the choice of known quantities MUST survive rotation and
  process recreation. After a cold start the fields are empty.
- **FR-012**: Each field MUST be read by screen readers with its name, its value with unit words,
  and its state (known or computed). The schematic is decorative.

### Key Entities

- **Quantity**: U1, U2, R1, R2 or R. Each has a unit (V or Ω), a value, and a state (known or
  computed).
- **Divider**: a consistent set of the five quantities that satisfies FR-005.
- **Hand-over**: a request to open another module with a value, carried out by the app shell.

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- Unloaded voltage divider: `U2 = U1 · R2 / (R1 + R2)`, from Ohm's law and Kirchhoff's voltage law
  (P. Horowitz, W. Hill, *The Art of Electronics*, 3rd ed., 2015, §1.2.3, voltage dividers). The
  solved forms for each of the nine cases are derived in the plan and tested against the forward
  formula.
- Resistance code notation: IEC 60062:2016 §4, as in spec 004.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: Voltages are in volts, with an optional prefix (mV, kV), from 1mV to 100kV
  in magnitude. Resistances are in ohms, from 0.1Ω to 1GΩ. The validity rules are in FR-006 and
  the Edge Cases.
- **Internal representation**: Inputs are parsed exactly from their decimal text. Computations are
  exact where the formula allows it. The plan defines how divisions that don't terminate are
  rounded before display, so that the shown digits are correct.
- **Precision/rounding**: 4 significant digits for computed values, HALF_UP. Entered values are
  shown as typed. The decimal separator follows the locale.
- **Load**: The divider is assumed unloaded, with no current drawn from U2. The screen states this
  in a short note.

### New Dependencies *(Principle III)*

- None. Parsing and formatting come from shared code. The plan decides whether the resistance
  parser of spec 004 moves to a shared library so that both modules can use it.

### Permissions & Network *(Principle I)*

- None.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk:
  - title and description;
  - field labels;
  - hints;
  - error messages;
  - the "known / computed" wording;
  - the hand-over action;
  - the unloaded note;
  - spoken unit words (volt, millivolt, kilovolt; the ohm words of spec 004).
- Input accepts both "." and ",". Output uses the locale separator.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can compute a missing resistor of a divider in under 15 seconds from opening
  the module (three values typed).
- **SC-002**: All nine valid sets of three known quantities give results that satisfy both
  equations of FR-005 to the shown precision. This is checked automatically, including random
  round trips: compute, then feed the results back in as knowns.
- **SC-003**: Handing a resistance to the combination finder takes one tap. The finder shows
  results for it within 1 second.
- **SC-004**: Going back from the finder restores the divider exactly, in 10 out of 10 attempts.
- **SC-005**: All texts appear in each of the six supported languages, with no missing or raw-key
  strings.

## Assumptions

- The divider is unloaded. Load current, output impedance, power dissipation and tolerance effects
  are out of scope and may come in later specs.
- With only two known quantities (for example only U1 and U2, which is a ratio), nothing is
  computed. Suggesting standard resistor pairs for a ratio is out of scope. The hand-over to the
  combination finder covers one resistor at a time.
- The hand-over is one-way. The chosen combination is not sent back into the divider.
- Results are not saved between sessions.
