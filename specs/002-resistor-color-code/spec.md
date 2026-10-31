# Feature Specification: Resistor Color Code Calculator

**Feature Branch**: `002-resistor-color-code`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "The first calculator: compute a resistor's resistance from its color bands. Design as in the image." Reference design: [design/reference-4-bands.png](design/reference-4-bands.png)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Read a 4-band resistor (Priority: P1)

A hobbyist holds a resistor with four color bands. They open "Resistor color code" from the
Calculations tab. The calculator opens on the "4 bands" tab. It shows:

- the current value in large text (for example "4.7kΩ ±5%");
- next to it, a drawing of a resistor whose bands show the selected colors, labeled
  "1st", "2nd", "MUL" and "TOL";
- below, a table with one row per color (Black … Silver) and one column per band (Band 1,
  Band 2, Multiplier, Tolerance).

Each table cell is filled with its color and shows what that color means in that band: a digit,
a multiplier such as "×100Ω", or a tolerance such as "±5%". Cells that a color cannot take in
that band are empty. The user taps the cell of the matching color in each column. The selected
cell of each column is outlined, the drawing recolors, and the value updates immediately.

**Why this priority**: 4-band resistors are the most common (E6–E24 carbon and metal film). This
alone delivers the calculator's core value.

**Independent Test**: Open the calculator, tap Yellow in Band 1, Violet in Band 2, Red in
Multiplier and Gold in Tolerance. The screen shows "4.7kΩ ±5%" and the drawing shows yellow,
violet, red and gold bands.

**Acceptance Scenarios**:

1. **Given** the calculator is opened for the first time, **When** it appears, **Then** the "4 bands"
   tab is selected and the default selection Yellow, Violet, Red, Gold shows "4.7kΩ ±5%".
2. **Given** the 4-band tab, **When** the user taps Brown in Band 1, Black in Band 2, Orange in
   Multiplier and Brown in Tolerance, **Then** the value reads "10kΩ ±1%".
3. **Given** a selection, **When** the user taps a different cell in one column, **Then** only that
   column's outline moves, the matching band of the drawing changes color, and the value
   updates without any extra action.
4. **Given** the 4-band tab, **When** the user taps an empty cell (for example Black in Band 1),
   **Then** nothing changes.
5. **Given** Red, Red, Gold, Gold, **When** shown, **Then** the value reads "2.2Ω ±5%". Given Red,
   Red, Silver, Silver, it reads "0.22Ω ±10%".

---

### User Story 2 - Switch between 3, 4, 5 and 6 bands (Priority: P1)

Below the top bar there are four tabs: "3 bands", "4 bands", "5 bands", "6 bands". Switching tabs
changes the table columns, the drawing (number of bands and labels) and the value format:

| Tab     | Columns (bands)                                   | Value shown                     |
|---------|---------------------------------------------------|---------------------------------|
| 3 bands | Band 1, Band 2, Multiplier                        | value, tolerance always ±20%    |
| 4 bands | Band 1, Band 2, Multiplier, Tolerance             | value ±tolerance                |
| 5 bands | Band 1, Band 2, Band 3, Multiplier, Tolerance     | value ±tolerance                |
| 6 bands | Band 1, Band 2, Band 3, Multiplier, Tolerance, TCR | value ±tolerance, TCR in ppm/K |

**Why this priority**: Precision (1% and better) resistors use 5 or 6 bands. Without them the
calculator can't be used for a large share of real parts.

**Independent Test**: On each tab, set a known color combination and compare the result with
the expected values in the Acceptance Scenarios.

**Acceptance Scenarios**:

1. **Given** the "3 bands" tab with defaults Yellow, Violet, Red, **When** shown, **Then** the value
   reads "4.7kΩ ±20%" and the drawing shows three bands labeled "1st", "2nd", "MUL".
2. **Given** the "5 bands" tab, **When** the user selects Brown, Black, Black, Red, Brown, **Then**
   the value reads "10kΩ ±1%".
3. **Given** the "5 bands" tab with defaults Yellow, Violet, Black, Brown, Brown, **When** shown,
   **Then** the value reads "4.7kΩ ±1%".
4. **Given** the "6 bands" tab with defaults Yellow, Violet, Black, Brown, Brown, Brown, **When**
   shown, **Then** the value reads "4.7kΩ ±1% 100ppm/K", and the drawing has six bands
   labeled "1st", "2nd", "3rd", "MUL", "TOL", "TCR".
5. **Given** a selection on one tab, **When** the user switches to another tab and back,
   **Then** each tab still shows its own last selection.
6. **Given** any tab, **When** the user taps another tab or swipes horizontally across the
   content, **Then** the adjacent tab is selected.

---

### User Story 3 - Use the calculator in any supported language and setting (Priority: P2)

A user whose phone is set to Ukrainian (or German, French, Spanish, Japanese) sees translated
color names, column headers, band labels and tab names. The number uses the locale's decimal
separator (for example "4,7kΩ ±5%" in Ukrainian and German). The screen works in dark theme, in
landscape, and with a large system font. It is also usable with a screen reader.

**Why this priority**: The constitution requires every feature in all six languages with
locale formatting. Accessibility matters for a reference tool, but the calculation itself is
already valuable without it.

**Independent Test**: Switch the device language to each supported locale and check the texts
and the decimal separator. Turn on TalkBack and select a band by touch exploration.

**Acceptance Scenarios**:

1. **Given** the device language is Ukrainian, **When** the calculator shows 4.7 kΩ ±5%,
   **Then** the value reads "4,7kΩ ±5%", the tabs read "3 смуги", "4 смуги", "5 смуг", "6 смуг"
   (plural-aware), and the color names are Ukrainian.
2. **Given** TalkBack is on, **When** the user focuses a cell, **Then** it is announced with band,
   color, meaning and selection state (for example "Band 1, Yellow, 4, selected").
3. **Given** dark theme, **When** the calculator is shown, **Then** all color cells keep their real
   band colors, the text in each cell stays readable, and the selection outline is visible
   on every color, including Black and White.
4. **Given** landscape orientation or 200% font size, **When** the calculator is shown, **Then** the
   whole table can be reached by scrolling and no cell text is clipped beyond recognition.

---

### Edge Cases

- **Black as the first digit**: not selectable. The Band 1 cell for Black is empty, so a value
  with a leading zero can't be formed.
- **Smallest values**: Brown, Black, Silver (3/4-band) is 0.1Ω. Brown, Black, Black, Silver
  (5/6-band) is 1Ω. Values under 1Ω are shown in Ω with a decimal, never in mΩ.
- **Largest values**: White, White, White multiplier is 99GΩ (3/4-band). White, White, White,
  White is 999GΩ (5/6-band).
- **Floating-point artifacts**: results are exact. Red, Red, Silver must read "0.22Ω", never
  "0.22000000000000003Ω".
- **Trailing zeros**: dropped. Brown, Black, Red is "1kΩ", not "1.0kΩ" or "1.00kΩ". Brown,
  Black, Black, Brown (5-band) is "1kΩ".
- **Three significant digits**: kept. On 5 bands, Brown, Red, Brown, Black is "121Ω", and
  Yellow, Violet, Green, Brown is "4.75kΩ".
- **Process recreation or rotation**: the selected tab and every tab's selection are kept.
- **Very narrow screens (about 320dp)**: in the 6-band table (7 columns) cells shrink and the text
  may use a smaller size, but each cell stays a tappable target of at least 48dp height.

## Requirements *(mandatory)*

### Functional Requirements

**Module**

- **FR-001**: The calculator MUST be a module in the Calculations category with a localized
  title ("Resistor color code") and a one-line description ("Resistance from 3–6 color
  bands"). It follows the module contract of spec 001.

**Layout** (per [design/reference-4-bands.png](design/reference-4-bands.png))

- **FR-002**: The screen MUST show, from top to bottom:
  1. tabs "3 bands", "4 bands", "5 bands", "6 bands";
  2. a summary area with the formatted value on the left and the resistor drawing on the right;
  3. the color table.
- **FR-003**: The "4 bands" tab MUST be selected the first time the calculator is opened.
- **FR-004**: Users MUST be able to switch band tabs by tap and by horizontal swipe.
- **FR-005**: The color table MUST have a first column of localized color names. It has one row
  per color in this order: Black, Brown, Red, Orange, Yellow, Green, Blue, Violet, Gray, White,
  Gold, Silver. There is one further column per band of the selected tab, with localized
  headers (Band 1, Band 2, Band 3, Mult., Toler., TCR).
- **FR-006**: Each cell that is valid for its band MUST be filled with that row's color (with a
  subtle vertical shading as in the reference) and MUST show the band meaning: a digit, a
  multiplier with unit ("×1Ω", "×10Ω", "×100Ω", "×1kΩ" … "×1GΩ", "×0.1Ω", "×0.01Ω"), a tolerance
  ("±5%"), or a temperature coefficient ("100ppm/K"). Invalid cells MUST be empty and not
  selectable.
- **FR-007**: Text in each colored cell MUST have a contrast ratio of at least 4.5:1 against its
  background. Use white text on dark colors and black text on light colors (Yellow, White,
  Gold, Silver, Orange, Green as applicable).
- **FR-008**: Exactly one cell per band column MUST be selected, shown with a clearly visible
  outline in an accent color that contrasts with every band color.
- **FR-009**: The resistor drawing MUST show the body, the leads and one band per column in the
  selected colors, in band order from left to right. The tolerance band (and TCR band) is set
  apart toward the right end, as in the reference. Above the bands are localized leader-line
  labels: "1st", "2nd", "3rd" (5/6-band), "MUL", "TOL" (4–6-band) and "TCR" (6-band).

**Calculation** (see Constitution Constraints for the sources and tables)

- **FR-010**: Resistance MUST be computed as `digits × multiplier`. Digits are the 2 (3/4-band) or
  3 (5/6-band) digit bands read as a decimal integer. The multiplier comes from the multiplier
  band.
- **FR-011**: The tolerance MUST come from the tolerance band (4–6-band). On the 3-band tab it is
  fixed at ±20% (no tolerance band).
- **FR-012**: On the 6-band tab, the temperature coefficient MUST come from the TCR band and is
  shown in ppm/K.
- **FR-013**: The result MUST update immediately on every selection change.

**Defaults & state**

- **FR-014**: The default selection for each tab is:
  - 3 bands: Yellow, Violet, Red;
  - 4 bands: Yellow, Violet, Red, Gold;
  - 5 bands: Yellow, Violet, Black, Brown, Brown;
  - 6 bands: Yellow, Violet, Black, Brown, Brown, Brown.

  All of these show 4.7kΩ.
- **FR-015**: Each tab MUST keep its own selection while the user switches tabs. The selected tab
  and all selections MUST survive rotation and process recreation. After a cold start the
  defaults apply.

**Accessibility**

- **FR-016**: Every selectable cell MUST be at least 48dp tall and expose band name, color name,
  meaning and selected state to screen readers. The drawing is decorative (the value text
  carries the information).

### Key Entities

- **Band color**: One of 12 colors (Black … Silver). Each has a display color and a localized name.
- **Band role**: Digit, Multiplier, Tolerance or Temperature coefficient. It decides which colors
  are valid and what each color means.
- **Band layout**: 3, 4, 5 or 6 bands, giving the ordered list of roles. For example, 5 bands =
  Digit, Digit, Digit, Multiplier, Tolerance.
- **Reading**: The selected color per band of one layout. It yields resistance (Ω), tolerance (%)
  and, for 6 bands, the temperature coefficient (ppm/K).

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- Color code meaning for digits, multipliers, tolerances and temperature coefficients:
  **IEC 60062:2016**, *Marking codes for resistors and capacitors*, §5 (color code) and
  Tables 1–3. Cross-checked against the reproduction in Wikipedia, "Electronic color code",
  section "Resistors" (retrieved 2026-10-31). The planning phase MUST verify every value
  below against these sources and record any discrepancy.

| Color  | Digit | Multiplier | Tolerance | TCR (ppm/K) |
|--------|-------|------------|-----------|-------------|
| Black  | 0 (not as 1st digit) | ×1     | —       | 250 |
| Brown  | 1     | ×10        | ±1%       | 100 |
| Red    | 2     | ×100       | ±2%       | 50  |
| Orange | 3     | ×1k        | ±0.05%    | 15  |
| Yellow | 4     | ×10k       | ±0.02%    | 25  |
| Green  | 5     | ×100k      | ±0.5%     | 20  |
| Blue   | 6     | ×1M        | ±0.25%    | 10  |
| Violet | 7     | ×10M       | ±0.1%     | 5   |
| Gray   | 8     | ×100M      | ±0.01%    | 1   |
| White  | 9     | ×1G        | —         | —   |
| Gold   | —     | ×0.1       | ±5%       | —   |
| Silver | —     | ×0.01      | ±10%      | —   |
| (none) | —     | —          | ±20% (3-band only) | — |

- Display colors for the bands: use common practical RGB approximations. IEC 60062 defines color
  names, not RGB values. The chosen RGB values are recorded in the plan.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: There is no free-text input. Every input is a selection from a fixed set,
  so invalid input can't be entered. Invalid cells (for example Black as the 1st digit, Gold as
  a digit) are not selectable.
- **Internal representation**: Resistance is computed exactly in decimal (digits × 10^k with
  k ∈ [−2, 9]) and carries the unit Ω across module boundaries. There is no binary
  floating-point rounding.
- **Precision/rounding**: No rounding is needed. Each result has at most 3 significant digits.
  - Display shows all significant digits from the digit bands, with trailing zeros dropped.
  - The SI prefix is Ω, kΩ, MΩ or GΩ, chosen so that the mantissa is in [1, 1000). Values
    below 1Ω stay in Ω (for example 0.1Ω, 0.47Ω).
  - Tolerance is shown as given ("±0.05%"), and TCR as an integer with "ppm/K".
  - Decimal separators follow the app locale.
- **Format**: `<value><prefix>Ω ±<tolerance>%`, plus ` <tcr>ppm/K` on 6 bands. There is no space
  between number and unit, as in the reference design.

### New Dependencies *(Principle III)*

- None.

### Permissions & Network *(Principle I)*

- None.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk: module title and description, 12 color names, column
  headers (Band 1/2/3, Mult., Toler., TCR), drawing labels (1st, 2nd, 3rd, MUL, TOL, TCR), tab
  labels, and screen-reader descriptions.
- The tab label "N bands" MUST use plurals resources (for example uk "3 смуги" / "5 смуг",
  ja without plural forms).
- Unit symbols Ω, k, M, G, %, ppm/K are not translated. Only the decimal separator follows the
  locale.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can read the value of a 4-band resistor in at most 4 taps after opening the
  calculator (one per band). On defaults that match, it takes zero taps.
- **SC-002**: For every one of the 4 layouts and every valid combination of colors, the shown
  value equals the value computed from the source table. Unit tests cover representative and
  boundary combinations, and an exhaustive check runs over all combinations.
- **SC-003**: The value updates within one frame of a tap. There is no visible delay.
- **SC-004**: All texts on the screen are translated in the 6 supported languages, and decimal
  separators follow the locale in each of them.
- **SC-005**: Every text-on-color cell meets a 4.5:1 contrast ratio, verified for all 12 colors in
  light and dark theme.

## Assumptions

- "Ranges" in the reference image means "bands". The UI uses "bands".
- The reference image shows the multiplier column only down to Green and the tolerance column
  only for Gold and Silver. Under Principle II (correctness) this spec follows the full IEC
  60062 table: multipliers up to White (×1G) and all standard tolerances. The visual design
  otherwise follows the image.
- Pink (×0.001) and the 4-band "none = ±20%" variant are not offered. Pink is rare, and the
  3-band tab covers the no-tolerance case.
- Reverse lookup (value → colors), E-series checks and showing the min/max range from tolerance
  are out of scope and may come in later specs.
- Band colors look the same in light and dark theme (they represent physical colors). Only the
  surrounding surfaces follow the theme.
- Selections are not persisted across cold starts (consistent with the shell in spec 001).
