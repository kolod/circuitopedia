# Feature Specification: Voltage Divider Calculator

**Feature Branch**: `005-voltage-divider`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "A voltage divider calculator. The user chooses which values are known
and which must be found: U1 (input voltage), U2 (output voltage), R1 (upper resistor), R2 (lower
resistor), R (total resistance). Circuit: U1 — R1 — U2 — R2 — GND. A resistance obtained here can
be passed quickly to the resistor combination finder."

Clarified 2026-10-31:
- Standard-resistor builds keep their total resistance within ±10% of R.
- Empty fields are the ones to compute, and a "Calculate" button is enabled once enough data is
  entered.
- Instead of handing a value to the combination finder, the divider picks standard resistors for
  R1 and R2 together, in place. Matching the divider as a whole, rather than each arm separately,
  gives a more accurate output voltage, often with fewer resistors.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Find the missing values of a divider (Priority: P1)

An engineer needs 3.3V from a 5V rail and has a 10kΩ resistor for the upper position. They open
"Voltage divider" from the Calculations tab. A schematic shows U1 — R1 — U2 — R2 — GND with each
quantity labeled. Below it are five fields: U1, U2, R1, R2 and R. They fill in U1 = 5V, U2 = 3.3V
and R1 = 10kΩ, and leave R2 and R empty. "Calculate" becomes enabled. Tapping it shows R2 =
19.41kΩ and R = 29.41kΩ in the empty fields, styled as computed values.

Any three independent quantities can be entered, which gives nine cases. The three resistances
alone are not enough, because they fix no voltage.

**Why this priority**: This is the core of the module.

**Independent Test**: For each of the nine valid sets of three entered quantities, enter values
from a known divider, tap Calculate, and check the two computed values against hand calculation.

**Acceptance Scenarios**:

1. **Given** U1 = 5V, U2 = 3.3V, R1 = 10kΩ are entered, **When** Calculate is tapped, **Then**
   R2 = 19.41kΩ and R = 29.41kΩ are shown as computed values.
2. **Given** U1 = 12V, R1 = 10kΩ, R2 = 4.7kΩ, **Then** U2 = 3.837V and R = 14.7kΩ.
3. **Given** U2 = 2.5V, R1 = 1kΩ, R = 3kΩ, **Then** R2 = 2kΩ and U1 = 3.75V.
4. **Given** U1 = 9V, U2 = 3V, R = 30kΩ, **Then** R1 = 20kΩ and R2 = 10kΩ.
5. **Given** fewer or more than three fields are filled, or only R1, R2 and R are filled, **Then**
   Calculate is disabled, and a short note says what is missing or extra.
6. **Given** the entered values cannot form a divider (for example U2 ≥ U1, or R1 ≥ R), **When**
   Calculate is tapped, **Then** no result is shown, and a short message names the problem.
7. **Given** results are shown, **When** the user edits any entered value, **Then** the computed
   values and the standard-value list are cleared until Calculate is tapped again.
8. **Given** results are shown, **When** the user starts typing in a computed field, **Then** that
   field becomes an entered value, and the other results are cleared.

---

### User Story 2 - Build the divider from standard resistors (Priority: P1)

R2 = 19.41kΩ is not a standard value. Below the results, the user picks a series (E3 … E192, E24
first). The calculator lists ways to build the divider from standard resistors. Each arm is one
resistor, or two in series or in parallel. Choices for both arms are made together, so that the
**output voltage** is as close as possible to the target. Each entry shows the two arms, the
resulting U2 with its error, and the resulting total resistance with its deviation.

An entered R1 or R2 is kept as entered, and only the other arm is chosen. When both R1 and R2 were
computed, both arms are chosen.

**Why this priority**: A computed divider is rarely buildable as is. Picking both arms together
gives a more accurate divider than rounding each arm separately, often with fewer parts.

**Independent Test**: For U1 = 5V and U2 = 3.3V with R = 30kΩ, choose E24, and check that the list
is sorted by U2 error. Recompute each entry's U2 and total resistance by hand. Check that no pair
of the allowed shapes, with a total within ±10% of R, left out of the list has a smaller U2 error
than the last entry of the same resistor count.

**Acceptance Scenarios**:

1. **Given** U1 = 5V, U2 = 3.3V, R1 = 10kΩ entered and R2 computed as 19.41kΩ, **When** E24 is
   chosen, **Then** R1 stays 10kΩ in every entry, and R2 is built from 1–2 E24 resistors.
2. **Given** U1 = 9V, U2 = 3V, R = 30kΩ entered (both arms computed), **When** E24 is chosen,
   **Then** the first entry is 20kΩ / 10kΩ with U2 = 3V (0%), using two resistors.
3. **Given** both arms are chosen, **Then** for each total resistor count (2, 3 and 4), up to 5 of
   the best entries are listed, merged and sorted by the absolute U2 error. Ties go to fewer
   resistors, then to the smaller total-resistance deviation.
4. **Given** the series changes, **Then** the list updates immediately.
5. **Given** U2 and both arms are computed from entered R1 and R2 (as in scenario 2 of User Story 1),
   **Then** no standard-value list is shown, because both resistors were given.

---

### User Story 3 - Use it in any supported language and setting (Priority: P2)

As in the other calculators: all six languages, the locale's decimal separator in input and
output, dark theme, 200% font, portrait only, and screen-reader support. Each field announces its
name, value and whether it was entered or computed. Each standard-value entry is read as one item.

**Why this priority**: Required by the constitution.

**Independent Test**: In Ukrainian, enter "3,3" for U2 and check that the results use decimal
commas. With TalkBack, focus a computed field and hear its name, its value with unit words, and
"computed".

**Acceptance Scenarios**:

1. **Given** the Ukrainian language, **When** the user enters "4,7k" for R1, **Then** it is
   accepted, and the results use decimal commas.
2. **Given** TalkBack is on, **When** a computed field is focused, **Then** it is read as, for
   example, "R2, 19.41 kilohm, computed".
3. **Given** 200% font size, **Then** the schematic, the fields, the button and the list stay
   usable, and no value is cut off.

---

### Edge Cases

- **U2 equal to U1** would need R1 = 0, and **U2 = 0** would need R2 = 0, so both are invalid.
  **U2 greater than U1** is invalid for a passive divider.
- **R1 or R2 not smaller than R** (when R is entered): invalid.
- **Negative voltages**: U1 and U2 may both be negative if they have the same sign
  (0 < U2/U1 < 1). Mixed signs are invalid.
- **Ranges**: voltages from 1mV to 100kV in magnitude, resistances from 0.1Ω to 1GΩ. The search for
  standard resistors uses resistor values from 1Ω to 10MΩ, as in spec 004.
- **Target ratio that standard resistors cannot reach well** (for example U2/U1 = 0.999): the list
  still shows the closest entries, and their error makes the situation clear.
- **Exact matches**: several builds may reach the ratio exactly. All of them are listed, with fewer
  resistors first.
- **Process recreation or rotation**: the entered values, the computed results and the chosen series
  are kept.

## Requirements *(mandatory)*

### Functional Requirements

**Module**

- **FR-001**: The module MUST be listed in the Calculations category with a localized title
  ("Voltage divider") and description ("Solve a resistive divider and pick standard resistors"),
  following the module contract of spec 001.
- **FR-002**: The screen MUST show a schematic of the divider, U1 — R1 — U2 — R2 — GND, with the
  five quantities labeled (U1, U2, R1, R2, R). It also shows a short note that the divider is
  assumed unloaded.

**Input and computation**

- **FR-003**: Each of the five quantities MUST have a field. Voltages are entered in volts with an
  optional SI prefix (m, k), and resistances in ohms with the notations of spec 004 FR-002 ("4.7k",
  "4k7", "R47"). Both "." and "," are accepted as the decimal separator.
- **FR-004**: A field the user has filled is an **entered** value. An empty field is a value to
  **compute**. "Calculate" MUST be enabled exactly when three fields are entered, they parse, and
  they are not the set {R1, R2, R}. Otherwise a short note says what is missing or extra.
- **FR-005**: Calculate MUST compute the two empty quantities with `U2 = U1 · R2 / (R1 + R2)` and
  `R = R1 + R2`, and show them in their fields, styled clearly differently from entered values.
- **FR-006**: Entered values that cannot form a divider MUST NOT produce results. A localized message
  names the problem: U2 not strictly between 0 and U1 (same sign), a resistance not smaller than
  R, or a value out of range.
- **FR-007**: Computed values MUST be shown with 4 significant digits and the SI prefix that puts the
  mantissa in [1, 1000), for example "19.41kΩ" or "3.837V".
- **FR-008**: Editing an entered field MUST clear the computed results and the standard-value list.
  Typing in a computed field turns it into an entered field, and the other results are cleared.

**Standard resistors**

- **FR-009**: After a calculation in which R1, R2 or both were computed, the module MUST offer a
  series choice (E3, E6, E12, E24, E48, E96, E192; E24 first; values from 1Ω to 10MΩ, IEC 60063 as in
  spec 004). It lists builds of the divider from standard resistors:
  - each computed arm is one resistor, or two in series or in parallel;
  - an entered arm is kept exactly as entered;
  - the total R1 + R2 of a build MUST lie within ±10% of R, whether R was entered or computed. Without this limit, decade-scaled pairs with a tiny ratio error but a useless total, such as a few ohms, would fill the list (clarified 2026-10-31).

  If no build fits the window, a short message says so.
- **FR-010**: Builds MUST be ranked by the absolute relative error of the output voltage,
  `|U2_build − U2_target| / |U2_target|`, which equals the error of the ratio R2 / (R1 + R2). Ties
  go to fewer resistors, then to the smaller absolute deviation of the total resistance from R, then
  to a deterministic order of values.
- **FR-011**: For each total count of chosen resistors, the module MUST list its 5 best builds under
  FR-010, merged into one list sorted by FR-010:
  - one arm chosen: 1 or 2 resistors, so at most 10 entries;
  - both arms chosen: 2, 3 or 4 resistors, so at most 15 entries.

  Within each count, no build left out may rank before the last one listed.
- **FR-012**: Each build MUST show:
  - R1 and R2 as connection text ("4.7kΩ + 220Ω", "10kΩ ∥ 10kΩ", or the entered value);
  - the resulting U2 with 4 significant digits and its signed error in percent with 3 significant
    digits;
  - the resulting R with its signed deviation.
- **FR-013**: The list MUST update immediately when the series changes, and appear within 1 second
  for E192 (SC-003).

**State and accessibility**

- **FR-014**: The entered values, the computed results and the chosen series MUST survive rotation
  and process recreation. After a cold start the fields are empty.
- **FR-015**: Each field MUST be read by screen readers with its name, its value with unit words,
  and its state (entered or computed). Each build is read as one item with unit words. The
  schematic is decorative.

### Key Entities

- **Quantity**: U1, U2, R1, R2 or R. Each has a unit (V or Ω), a value, and a state (entered, computed
  or empty).
- **Divider**: a consistent set of the five quantities that satisfies FR-005. Its ratio R2 / R
  equals U2 / U1.
- **Arm build**: one or two standard resistors in series or in parallel, or an entered value kept as
  is.
- **Divider build**: an arm build for R1 and one for R2, with the resulting U2, U2 error, R and R
  deviation.

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- Unloaded voltage divider `U2 = U1 · R2 / (R1 + R2)`, and the series and parallel formulas, from
  Ohm's and Kirchhoff's laws (P. Horowitz, W. Hill, *The Art of Electronics*, 3rd ed., 2015, §1.2,
  as cited in spec 004). The plan derives the solved form for each of the nine cases and tests it
  against the forward formula.
- E-series values: IEC 60063:2015, the same tables as spec 004.
- Resistance code notation: IEC 60062:2016 §4, as in spec 004.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: Voltages are in volts, with an optional prefix (mV, kV), from 1mV to 100kV
  in magnitude. Resistances are in ohms, from 0.1Ω to 1GΩ. The validity rules are in FR-006 and the
  Edge Cases.
- **Internal representation**: Inputs are parsed exactly from their decimal text. Computations and
  the ranking of builds use exact rational arithmetic, as in spec 004, so that shown digits and the
  order are correct.
- **Precision/rounding**: 4 significant digits for computed values and build voltages, 3 for errors
  and deviations, HALF_UP. The decimal separator follows the locale.
- **Load**: The divider is assumed unloaded, with no current drawn from U2.

### New Dependencies *(Principle III)*

- None. Spec 004's E-series data, exact arithmetic, parsing, formatting and combination search are
  reused through shared libraries. The plan decides how to move them, since a module may not depend
  on another module.

### Permissions & Network *(Principle I)*

- None.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk:
  - title and description;
  - field labels;
  - the Calculate button;
  - the notes about missing or extra values;
  - error messages;
  - "entered" and "computed";
  - the unloaded note;
  - the standard-resistor section header;
  - spoken unit words (volt, millivolt, kilovolt; the ohm words of spec 004).
- Input accepts both "." and ",". Output uses the locale separator.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can compute a missing resistor of a divider in under 15 seconds from opening
  the module (three values and one tap).
- **SC-002**: All nine valid sets of entered quantities give results that satisfy both equations of
  FR-005 to the shown precision. This is checked automatically, including random round trips:
  compute, then feed results back in as entered values.
- **SC-003**: For E192 with both arms chosen, the standard-resistor list appears within 1 second on a
  mid-range phone, and typing stays responsive.
- **SC-004**: The listed builds are exactly the best ones under FR-010 and FR-011. This is checked
  automatically against an exhaustive reference search on small series.
- **SC-005**: All texts appear in each of the six supported languages, with no missing or raw-key
  strings.

## Assumptions

- The divider is unloaded. Load current, power dissipation and tolerance effects are out of scope.
- Arms of three or more resistors are out of scope (clarified 2026-10-31). Two per arm already gives
  close matches, and more would slow the search.
- The total resistance of a build is limited to ±10% of R (FR-009). A user-adjustable window was
  considered and left out (clarified 2026-10-31).
- The hand-over to the combination finder from the original request is dropped in favor of picking
  standard values in place, so no app-shell mechanism for passing values between modules is
  needed.
- Results are not saved between sessions.
