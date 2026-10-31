# Feature Specification: Inductor Color Code Calculator

**Feature Branch**: `003-inductor-color-code`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "Write a module like the previous one, but for inductors." The
previous module is the resistor color code calculator (spec 002). This feature reuses its design
and interaction, adapted to color-coded inductors (molded axial RF chokes).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Read a 4-band inductor (Priority: P1)

A hobbyist holds a small axial inductor with four color bands. They open "Inductor color code"
from the Calculations tab. The calculator opens on the "4 bands" tab and looks like the resistor
calculator:

- the current value in large text (for example "47µH ±10%");
- below it, a drawing of an inductor whose bands show the selected colors. Each band is joined
  by a leader line to its column header in the table;
- below, a table with one row per color and one column per band (Band 1, Band 2, Multiplier,
  Tolerance). Its header stays fixed while the color rows scroll.

Each cell is filled with its color and shows what that color means in that band: a digit, a
multiplier such as "×10µH", or a tolerance such as "±10%". Cells that a color cannot take in that
band are empty. The user taps the matching color in each column. The selected cell of each
column is outlined, the drawing recolors, and the value updates immediately.

**Why this priority**: The 4-band code (two digits, multiplier, tolerance) is the most common
marking on color-coded inductors. This alone delivers the calculator's core value.

**Independent Test**: Open the calculator, tap Yellow in Band 1, Violet in Band 2, Black in
Multiplier and Silver in Tolerance. The screen shows "47µH ±10%" and the drawing shows yellow,
violet, black and silver bands.

**Acceptance Scenarios**:

1. **Given** the calculator is opened for the first time, **When** it appears, **Then** the "4 bands"
   tab is selected and the default selection Yellow, Violet, Black, Silver shows "47µH ±10%".
2. **Given** the 4-band tab, **When** the user taps Brown, Black, Brown, Gold, **Then** the value
   reads "100µH ±5%".
3. **Given** the 4-band tab, **When** the user taps Red, Red, Gold, Gold, **Then** the value reads
   "2.2µH ±5%". Given Brown, Black, Silver, Silver, it reads "100nH ±10%".
4. **Given** a selection, **When** the user taps a different cell in one column, **Then** only that
   column's outline moves, the matching band of the drawing changes color, and the value updates.
5. **Given** the 4-band tab, **When** the user taps an empty cell, **Then** nothing changes.

---

### User Story 2 - Other band layouts (Priority: P1)

Below the top bar there are tabs for the supported band layouts. Switching tabs changes the
table columns, the drawing and the value format, as in the resistor calculator.

| Tab     | Columns (bands)                        | Value shown                  |
|---------|----------------------------------------|------------------------------|
| 3 bands | Band 1, Band 2, Multiplier             | value, tolerance always ±20% |
| 4 bands | Band 1, Band 2, Multiplier, Tolerance  | value ±tolerance             |

On both tabs, small values may be marked with a **decimal point**: Gold in Band 1 or Band 2
stands for the point instead of a digit (decided 2026-10-31). The first three bands then read
as two digits around the point, and the third band is a digit instead of a multiplier:

| Bands 1–3           | Reading            | Value  |
|---------------------|--------------------|--------|
| Yellow, Violet, Gold | 47 × 0.1 µH        | 4.7µH  |
| Yellow, Gold, Violet | 4 . 7 µH           | 4.7µH  |
| Gold, Red, Red       | . 2 2 µH           | 220nH  |
| Red, Red, Silver     | 22 × 0.01 µH       | 220nH  |

When a decimal point is selected, the third column's header changes from "Mult." to "Band 3",
its cells show digits instead of multipliers, and its band in the drawing still follows it.

**Why this priority**: Inductors without a tolerance band exist, and small values are often
marked with a decimal point. Without both, real parts are misread.

**Independent Test**: On each tab, set a known color combination and compare the result with
the Acceptance Scenarios.

**Acceptance Scenarios**:

1. **Given** the "3 bands" tab with defaults Yellow, Violet, Black, **When** shown, **Then** the
   value reads "47µH ±20%" and the drawing shows three bands, each joined to its header.
2. **Given** the 4-band tab, **When** the user selects Yellow, Gold, Violet, Gold, **Then** the value
   reads "4.7µH ±5%", the third column is headed "Band 3", and its cells show digits.
3. **Given** the 4-band tab, **When** the user selects Gold, Red, Red, Silver, **Then** the value
   reads "220nH ±10%".
4. **Given** Yellow, Gold, Violet, **When** the user taps Violet in Band 2, **Then** the decimal point
   is gone, the third column is headed "Mult." again, and its Violet (not a valid multiplier) is
   replaced by the band's default, Black, so the value reads "47µH".
5. **Given** Yellow, Gold, Violet, **When** the user taps Gold in Band 1, **Then** the decimal point
   moves to Band 1 and Band 2 gets its default color, Violet: Gold, Violet, Violet = 770nH.
6. **Given** a selection on one tab, **When** the user switches to another tab and back, **Then**
   each tab still shows its own last selection.
7. **Given** any tab, **When** the user taps another tab or swipes horizontally across the
   content, **Then** the adjacent tab is selected.

---

### User Story 3 - Use the calculator in any supported language and setting (Priority: P2)

As in the resistor calculator: translated texts in all six languages, the locale's decimal
separator ("4,7µH ±5%" in Ukrainian and German), dark theme, 200% font size and screen reader
support. The app is portrait-only (constitution 1.1.0).

**Why this priority**: Required by the constitution. The calculation is already valuable
without it.

**Independent Test**: Switch the device language to each supported locale and check texts and
the decimal separator. Turn on TalkBack and select a band by touch exploration.

**Acceptance Scenarios**:

1. **Given** the device language is Ukrainian, **When** the calculator shows 4.7µH ±5%, **Then**
   the value reads "4,7µH ±5%", the tabs are plural-aware ("3 смуги", "4 смуги"), and the color names are Ukrainian.
2. **Given** TalkBack is on, **When** the user focuses a cell, **Then** it is announced with band,
   color, meaning and selection state (for example "Band 1, Yellow, 4, selected").
3. **Given** dark theme, **When** the calculator is shown, **Then** all cells keep their real band
   colors, cell text stays readable and the selection outline is visible on every color.
4. **Given** 200% font size, **When** the calculator is shown, **Then** the whole table can be
   reached by scrolling and no cell text is cut off.

---

### Edge Cases

- **Black as the first digit**: not selectable, so a value with a leading zero can't be formed.
  With the decimal point in Band 1, Band 2 holds the first digit, so Black is not selectable
  there either (no ".0x" values, and no zero inductance).
- **Two decimal points**: impossible. Selecting Gold in one of Band 1 and Band 2 moves the point
  there, and the other band gets its default color.
- **Smallest value**: Brown, Black, Silver and Gold, Brown, Black are both 0.1µH, shown as
  "100nH".
- **Largest value**: White, White, Orange = 99 × 1000µH, shown as "99mH".
- **Floating-point artifacts**: results are exact. Red, Red, Silver must read "220nH", never a
  value like "220.00000000000003nH".
- **Trailing zeros**: dropped. Brown, Black, Brown is "100µH", Brown, Black, Red is "1mH".
- **Process recreation or rotation**: the selected tab and every tab's selection are kept.
- **Very narrow screens (about 320dp)**: cells shrink and text may get smaller, but each cell stays
  a tappable target of at least 48dp height and no label is cut off.

## Requirements *(mandatory)*

### Functional Requirements

**Module**

- **FR-001**: The calculator MUST be a module in the Calculations category with a localized title
  ("Inductor color code") and a one-line description ("Inductance from color bands"). It follows
  the module contract of spec 001 and is independent of the resistor module (FR-013 of spec 001).

**Layout and interaction** (same as spec 002)

- **FR-002**: The screen MUST show, from top to bottom: the band tabs, the formatted value, the
  inductor drawing, the color table header and the color rows. Only the color rows scroll,
  vertically. Nothing scrolls horizontally, and the table uses minimal side margins.
- **FR-003**: The "4 bands" tab MUST be selected the first time the calculator is opened.
- **FR-004**: Users MUST be able to switch band tabs by tap and by horizontal swipe.
- **FR-005**: The color table MUST have a first column of localized color names, one row per
  color used by the inductor code (Black … White, Gold, Silver), and one column per band of the
  selected tab with localized headers (Band 1, Band 2, Mult., Toler.). While a decimal point is
  selected, the third header reads "Band 3".
- **FR-006**: Each cell valid for its band MUST be filled with that row's color and MUST show the
  band meaning: a digit, "." for the decimal point, a multiplier with unit ("×0.01µH", "×0.1µH",
  "×1µH", "×10µH", "×100µH", "×1mH"), or a tolerance ("±5%", "±10%"). Invalid cells MUST be empty and not selectable.
- **FR-007**: Text in each colored cell MUST have a contrast ratio of at least 4.5:1 against its
  background. The band colors are the same as in the resistor calculator.
- **FR-008**: Exactly one cell per band column MUST be selected, shown with the same outline as
  in the resistor calculator.
- **FR-009**: The inductor drawing MUST show a color-coded axial inductor with its leads and one
  band per column in the selected colors, in band order from left to right, with the tolerance
  band set apart toward the right end. It comes from a bundled, editable vector image, and each
  band is joined by a leader line (vertical and slanted segments only) to the top edge of its
  header column. The drawing has no text labels.

**Calculation** (see Constitution Constraints for the sources and table)

- **FR-010**: Without a decimal point, inductance MUST be computed as `digits × multiplier` in
  microhenries (µH), where the digits are Band 1 and Band 2 read as a decimal integer.
- **FR-010a**: Gold MUST be selectable in Band 1 and Band 2 as a decimal point ("." in the cell).
  With the point in Band 2, the value is `<Band 1>.<Band 3>` µH; with the point in Band 1, it is
  `0.<Band 2><Band 3>` µH. In this mode the third band is a digit (Black … White), and Black is
  not selectable in the band that holds the first digit.
- **FR-010b**: After any tap, every band whose color is no longer valid for its current role MUST
  be replaced by that band's default color (FR-014). This keeps at most one decimal point and
  never shows an invalid reading.
- **FR-011**: The tolerance MUST come from the tolerance band. On the 3-band tab it is fixed at
  ±20%.
- **FR-012**: The result MUST update immediately on every selection change.
- **FR-013**: The value MUST be shown with the SI prefix that puts the mantissa in [1, 1000): nH,
  µH or mH. Format: `<value><prefix>H ±<tolerance>%`, with no space between number and unit, as
  in the resistor calculator.

**Defaults and state**

- **FR-014**: The default selection is Yellow, Violet, Black (3 bands) and Yellow, Violet, Black,
  Silver (4 bands). Both show 47µH.
- **FR-015**: Each tab MUST keep its own selection while the user switches tabs. The selected tab
  and all selections MUST survive rotation and process recreation. After a cold start the
  defaults apply.

**Accessibility**

- **FR-016**: Every selectable cell MUST be at least 48dp tall and expose band name, color name,
  meaning and selected state to screen readers. The drawing is decorative.

### Key Entities

- **Band color**: One of the colors used by the inductor code. Each has a display color and a
  localized name, shared in meaning with the resistor calculator.
- **Band role**: Digit, Multiplier or Tolerance. It decides which colors are valid and what each
  color means. Band 1 and Band 2 may also hold the decimal point, which turns the third band
  from Multiplier into Digit.
- **Band layout**: The ordered list of roles for one tab, for example 4 bands = Digit, Digit,
  Multiplier, Tolerance.
- **Reading**: The selected color per band of one layout. It yields the inductance (µH) and the
  tolerance (%).

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- **Bourns, *Color Code Marking (MIL STD)* and *(EIA STD)***, which reproduce the MIL marking for
  cylindrical RF choke coils (MIL-C-15305 / MIL-PRF-15305 family) and the EIA marking for molded
  inductors. Tolerances cross-checked against a Vishay color chart. Verified in plan research R1
  (2026-10-31), which also records the discrepancies with uncited web tables (extra tolerances
  Brown–Yellow and multipliers Yellow–Blue are not offered):

| Color  | Digit | Multiplier (µH) | Tolerance |
|--------|-------|-----------------|-----------|
| Black  | 0 (not as 1st digit) | ×1     | —    |
| Brown  | 1     | ×10             | —    |
| Red    | 2     | ×100            | —    |
| Orange | 3     | ×1000           | —    |
| Yellow | 4     | —               | —    |
| Green  | 5     | —               | —    |
| Blue   | 6     | —               | —    |
| Violet | 7     | —               | —    |
| Gray   | 8     | —               | —    |
| White  | 9     | —               | —    |
| Gold   | decimal point (Band 1 or 2) | ×0.1 | ±5% |
| Silver | —     | ×0.01           | ±10% |
| (none) | —     | —               | ±20% (3-band only) |

- Display colors: the same RGB values as the resistor calculator (spec 002, research R6).

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: No free-text input. Every input is a selection from a fixed set, so
  invalid input can't be entered; cells invalid for a band are not selectable.
- **Internal representation**: Inductance is computed exactly in decimal and carries the unit
  µH across module boundaries. There is no binary floating-point rounding.
- **Precision/rounding**: No rounding is needed (at most 2 significant digits). Trailing zeros are
  dropped. Decimal separators follow the app locale.
- **Unit symbol**: "H" with SI prefixes n, µ, m, the same international symbols in every locale
  (as "Ω" and "kΩ" in the resistor calculator).

### New Dependencies *(Principle III)*

- None. The calculator reuses the SVG rendering library already approved in spec 002 (AndroidSVG,
  research R10 there). With this second module, the parts both calculators share (color table,
  SVG drawing with leader lines, band colors) are expected to move to a shared place, as the
  YAGNI rule in Principle III allows once two concrete uses exist. The plan decides where.

### Permissions & Network *(Principle I)*

- None.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk: module title and description, column headers (if not
  shared), tab names (plurals), cell descriptions for screen readers. Color names are shared with
  the resistor calculator if the plan moves them to a shared place, otherwise duplicated.
- Number formatting follows the locale (decimal separator).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can read a 4-band inductor's value in under 15 seconds from opening the
  calculator (four taps).
- **SC-002**: Every valid color combination of every offered layout gives the value defined by
  the cited source, checked automatically for all combinations.
- **SC-003**: The value updates within one screen refresh of a tap, with no visible delay.
- **SC-004**: All texts appear in each of the six supported languages, with no missing or raw-key
  strings.
- **SC-005**: Every cell's text has a contrast ratio of at least 4.5:1 against its color.
- **SC-006**: At 200% font size and on a 320dp-wide screen, no cell label is cut off.

## Assumptions

- "Like the previous one" means the same screen structure, interaction and look as the resistor
  calculator (spec 002), with the resistor replaced by an inductor and Ω by H.
- The calculator covers color-coded axial inductors (molded RF chokes). Inductors marked with
  printed digits (for example "4R7" or "101" on SMD parts) and color dots are out of scope, and
  so is the 5-band military marking with a wide silver first band (clarified 2026-10-31).
- The inductor drawing is an editable vector image like the resistor one. The user may supply
  their own; otherwise a first version is drawn during implementation.
- Reverse lookup (value → colors), E-series checks and showing the min/max range from tolerance
  are out of scope, as in spec 002.
- Band colors look the same in light and dark theme. Selections are not persisted across cold
  starts.
