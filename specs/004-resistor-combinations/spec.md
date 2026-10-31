# Feature Specification: Resistor Combination Finder

**Feature Branch**: `004-resistor-combinations`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "A module that picks resistors from a given series to match a target
value. The module tries different ways of connecting resistors: two or three in parallel; two or
three in series; combined, two in parallel and then one more in series. Results are sorted by
deviation."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Find combinations for a target value (Priority: P1)

An engineer needs a 3.3kΩ resistor in a design but wants to build it from the values they stock
(for example the E12 series), or needs a value that no standard series has, such as 1.234kΩ. They
open "Resistor combinations" from the Calculations tab, choose the series, and type the target
value. The screen lists combinations of standard resistors that come closest to the target:

- a single resistor;
- two or three resistors in series;
- two or three resistors in parallel;
- two resistors in parallel, with a third one in series.

Each result shows how the resistors are connected, their values, the resulting resistance and its
deviation from the target in percent. The best match is at the top.

**Why this priority**: This is the whole purpose of the module. Without it there is nothing to
show.

**Independent Test**: Choose E24 and enter 1.234kΩ. The list fills with combinations sorted by
deviation, and each line's resistance and deviation can be recomputed by hand from its values.

**Acceptance Scenarios**:

1. **Given** the E12 series, **When** the user enters 4.7k, **Then** the first result is the single
   resistor 4.7kΩ with deviation 0%.
2. **Given** the E12 series, **When** the user enters 5k, **Then** an exact combination is listed
   first, for example 10kΩ ∥ 10kΩ = 5kΩ (0%). An exact two-resistor result ranks above any
   three-resistor result with the same deviation.
3. **Given** the E24 series and the target 1.234kΩ, **When** the results appear, **Then** every
   listed result's deviation is no worse than the one below it, each kind of connection appears
   at most 5 times, and no combination left out of the list is closer to the target than the
   worst listed result of its own kind.
4. **Given** results are shown, **When** the user changes the target or the series, **Then** the
   list updates immediately, without a separate "calculate" action.
5. **Given** a combination, **When** it differs from another one only in the order of the
   resistors (for example 1kΩ + 2.2kΩ and 2.2kΩ + 1kΩ), **Then** it is listed only once.

---

### User Story 2 - Choose which connections to allow (Priority: P2)

Some users only want series connections (easy on a breadboard), or want to avoid three-resistor
combinations. They can switch each kind of connection on or off, and the list keeps only the
allowed ones.

**Why this priority**: The core search already works with every connection allowed. Filtering
makes the results more practical but is not essential.

**Independent Test**: Turn off everything except "2 in series". Every listed result is a single
resistor or a two-resistor series connection.

**Acceptance Scenarios**:

1. **Given** all connections are allowed, **When** the user turns off "3 in parallel", **Then** no
   result with three resistors in parallel is listed, and the list is still sorted by deviation.
2. **Given** only series connections are allowed, **When** the target is 1.234kΩ in E24, **Then**
   every result is a single resistor or a series connection.
3. **Given** the user turns off every kind of connection, **Then** only single resistors are
   listed (a single resistor is always allowed).

---

### User Story 3 - Use it in any supported language and setting (Priority: P2)

As in the other calculators: all six languages, locale decimal separators in values and
deviations, dark theme, 200% font, portrait only, and screen reader support.

**Why this priority**: Required by the constitution. The search is valuable without it.

**Independent Test**: In Ukrainian, enter "1,234k". The results use decimal commas, and a screen
reader reads each result as one item with its connection, values, resistance and deviation.

**Acceptance Scenarios**:

1. **Given** the Ukrainian language, **When** the user enters "1,234k" or "1.234k", **Then** both
   are accepted as 1.234kΩ, and results show "1,234kΩ".
2. **Given** TalkBack is on, **When** a result is focused, **Then** it is read as one item, for
   example "10 kilohm parallel 10 kilohm, 5 kilohm, deviation 0 percent".
3. **Given** 200% font size, **When** results are shown, **Then** no value or deviation is cut off.

---

### Edge Cases

- **Empty or invalid target** (letters, several decimal separators, a negative number, zero):
  no results, and a short message under the field explains the expected format, for example
  "4.7k, 4k7 or 4700".
- **Target outside the reachable range**: the smallest reachable value is three of the smallest
  resistors in parallel, the largest is three of the largest in series. Beyond that range the list
  still shows the closest results, and their large deviation makes the situation clear. Targets
  above 1GΩ or below 0.1Ω are rejected as invalid.
- **Exact matches**: several combinations may hit the target exactly; all of them are listed, with
  fewer resistors first.
- **Very fine series (E192)**: the search still finishes quickly (see SC-002), and the results
  still respect the "nothing closer was left out" rule.
- **Same value used more than once** (for example 10kΩ ∥ 10kΩ): allowed, since a stock usually
  holds several resistors of each value.
- **Process recreation or rotation**: the target text, the series and the allowed connections are
  kept.

## Requirements *(mandatory)*

### Functional Requirements

**Module**

- **FR-001**: The module MUST be listed in the Calculations category with a localized title
  ("Resistor combinations") and description ("Match a value with 1–3 standard resistors"),
  following the module contract of spec 001.

**Input**

- **FR-002**: The user MUST be able to enter the target resistance as text. These notations are
  accepted, with an optional "Ω" or "R" suffix and surrounding spaces:
  - a plain number: "4700";
  - a number with an SI prefix (m, k, M, G): "4.7k";
  - the resistor code notation, where the prefix letter replaces the decimal point: "4k7", "R47"
    (IEC 60062).

  Both "." and "," are accepted as the decimal separator.
- **FR-003**: The user MUST be able to choose the series: E3, E6, E12, E24, E48, E96 or E192. E24
  is selected the first time.
- **FR-004**: The available resistors MUST be every value of the chosen series in each decade from
  1Ω up to and including 10MΩ.
- **FR-005**: The user MUST be able to allow or forbid each kind of connection separately:
  - 2 in series;
  - 3 in series;
  - 2 in parallel;
  - 3 in parallel;
  - 2 in parallel plus 1 in series.

  All of them are allowed the first time. A single resistor is always considered.

**Search and results**

- **FR-006**: For a valid target, the module MUST consider every single resistor and every allowed
  combination of the available resistors. A value may be used more than once in a combination.
  Combinations that differ only in the order of resistors that can be swapped without changing
  the circuit MUST be listed only once.
- **FR-007**: Results MUST be sorted by the absolute value of the relative deviation
  `(R − target) / target`. Ties go to the combination with fewer resistors, then to the one with
  the smaller largest resistor value.
- **FR-008**: The module MUST show, for each considered kind of connection (single resistor
  included), its 5 best results under the order of FR-007, merged into one list sorted by FR-007
  (at most 30 results). Within each kind, no combination outside the list may come before the
  last one of that kind in the list. This keeps simple combinations visible next to the more
  precise three-resistor ones (decided 2026-10-31).
- **FR-009**: Each result MUST show:
  - the connection, using the symbols "+" for series and "∥" for parallel, for example
    "4.7kΩ ∥ 10kΩ + 220Ω";
  - the resulting resistance with 4 significant digits;
  - the deviation in percent with sign and 3 significant digits, or "0%" when exact.
- **FR-010**: The list MUST update immediately whenever the target, the series or the allowed
  connections change.

**State and accessibility**

- **FR-011**: The target text, the series and the allowed connections MUST survive rotation and
  process recreation. After a cold start the defaults apply (empty target, E24, all connections
  allowed).
- **FR-012**: Each result MUST be read by screen readers as one item containing the connection,
  the values, the resulting resistance and the deviation. Its unit symbols are spoken as words.

### Key Entities

- **Series**: one of E3 … E192. It has an ordered list of base values in one decade.
- **Available resistor**: a series value placed in a decade from 1Ω to 10MΩ.
- **Connection kind**: Single, 2 series, 3 series, 2 parallel, 3 parallel, or 2 parallel + 1 series.
- **Combination**: a connection kind plus its resistor values. It yields a resistance.
- **Result**: a combination with its resistance and its deviation from the target.

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- E-series values: **IEC 60063:2015**, *Preferred number series for resistors and capacitors*. The
  planning phase MUST verify all values against the standard (or an authoritative reproduction)
  and record the known irregularities, such as E24's 2.7, 3.0, 3.3, 3.6, 3.9, 4.3, 4.7, 8.2 against
  the rounded geometric series, and the fact that E24 is not a subset of E48.
- Series: `R = R1 + R2 (+ R3)`. Parallel: `1/R = 1/R1 + 1/R2 (+ 1/R3)`. These are basic circuit
  laws (Ohm's and Kirchhoff's laws), cited in the plan from a standard textbook.
- Resistor code notation in input ("4k7", "R47"): IEC 60062:2016, §4 (letter and digit code), the
  same standard as spec 002.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: The target is in ohms, entered with an optional SI prefix or the
  resistor-code letter. Valid range: 0.1Ω to 1GΩ, greater than zero. Invalid input shows a
  localized message and no results (Edge Cases).
- **Internal representation**: Series and single resistor values are exact. Parallel results are
  generally not finite decimals. They are computed with enough precision that the order of the
  results and the shown digits are correct, as defined in the plan.
- **Precision/rounding**:
  - resistance shown with 4 significant digits, deviation with 3;
  - SI prefix chosen so that the mantissa is in [1, 1000) (Ω, kΩ, MΩ, GΩ; values below 1Ω stay in
    Ω, as in spec 002);
  - decimal separator per locale.

### New Dependencies *(Principle III)*

- None. The module reuses the shared color code libraries only if the plan finds a real overlap
  (for example SI formatting); otherwise it stands alone.

### Permissions & Network *(Principle I)*

- None.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk:
  - title and description;
  - field label and hint;
  - error message;
  - series selector;
  - names of the connection kinds;
  - empty-list message;
  - spoken forms for screen readers ("parallel", "kilohm", "deviation" …).
- Input parsing accepts both "." and "," (Principle V). Output uses the locale's separator.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can get a ranked list for a new target in under 10 seconds from opening the
  module (type the value, read the top result).
- **SC-002**: For any valid target and series up to E192 with all connections allowed, the list
  appears within 1 second on a mid-range phone, and typing stays responsive.
- **SC-003**: For every tested target, the listed results are exactly the best 5 of each kind
  under FR-007, merged as FR-008 says. This is checked automatically against a slow, obviously correct search over all
  combinations.
- **SC-004**: Every shown resistance and deviation matches a hand calculation from the listed
  values, to the shown number of digits.
- **SC-005**: All texts appear in each of the six supported languages, with no missing or raw-key
  strings.

## Assumptions

- "A certain series" means the IEC 60063 E-series. A user-defined list of resistors in stock is out
  of scope for this release.
- The resistor tolerance is not taken into account. The deviation is that of the nominal values.
- 1Ω to 10MΩ covers the parts that hobbyists and most designs use. A configurable range is out of
  scope.
- Other topologies (two in series in parallel with a third, four or more resistors) are out of
  scope. The user listed exactly these five combined kinds.
- Results are not saved between sessions.
