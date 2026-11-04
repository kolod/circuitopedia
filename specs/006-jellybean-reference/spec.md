# Feature Specification: Jellybean Parts Reference

**Feature Branch**: `006-jellybean-reference`

**Created**: 2026-11-04

**Status**: Draft

**Input**: User description: "In the Reference tab, add information on jellybean components by category, from https://www.eevblog.com/forum/chat/jellybean-list/"

Scope decided with the user on 2026-11-04:
- **Parts:** the parts and families of the thread's first post (Phaedrus, 2014-04-15), plus the
  replies' additions that fill real gaps. Regional and niche parts are left out.
- **Entry content:** each entry shows its category, its purpose, its key datasheet parameters with
  the datasheet cited, and the pinout of its common package.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse jellybean parts by category (Priority: P1)

A hobbyist is about to order parts, or is looking for something already in the drawer. They open
the Reference tab and choose "Jellybean parts". They see the common, cheap, widely stocked parts
grouped by category:
- op-amps;
- comparators;
- timers;
- bipolar transistors;
- MOSFETs;
- diodes;
- voltage regulators;
- voltage references;
- logic;
- temperature sensors;
- power and PWM controllers.

Each row shows the part number and a one-line purpose, for example "LM358, dual op-amp, single
supply".

**Why this priority**: The categorized list is the heart of the reference. It answers "what is
the usual part for this job?" even without opening an entry.

**Independent Test**: Open the reference and check that every category of the list below appears
with its parts in a fixed order, each with its purpose in the device language.

**Acceptance Scenarios**:

1. **Given** the Reference tab, **When** the user opens "Jellybean parts", **Then** the categories
   appear in the order listed above, each with its parts.
2. **Given** the list, **When** the user scrolls, **Then** the category headers make it clear which
   category each part belongs to.
3. **Given** the device language is Ukrainian, **Then** category names and purposes are in
   Ukrainian, while part numbers stay as written ("LM358").

---

### User Story 2 - Look up a part's key parameters and pinout (Priority: P1)

The user needs to know whether the BC548 can switch 200mA, and which leg is the base. They tap the
BC548 row. Its page shows:
- its category and purpose;
- a table of key datasheet parameters with their conditions, for example "Collector current (DC):
  100mA max";
- a drawing of its common package with each pin numbered and named, as seen from a stated side;
- the datasheet the values come from: maker, document and revision.

**Why this priority**: Pinout and limits are what people look up most at the bench. Without them,
the list is only names.

**Independent Test**: Open five parts from different categories, and compare every shown parameter
and pin with the cited datasheet.

**Acceptance Scenarios**:

1. **Given** the BC548 page, **Then** the parameters include the collector-emitter voltage, the
   collector current, the DC current gain range and the power dissipation, each with its unit and
   condition. The pinout shows the TO-92 package with pins 1–3 labeled C, B, E (or as the cited
   datasheet states) and the side it is viewed from.
2. **Given** a family entry (78xx, 1N400x, 74HC), **Then** the page explains the naming (for example
   "7805 = 5V") and lists the members' differing values in a small table.
3. **Given** a part whose pinout differs between makers (for example some TO-92 transistors),
   **Then** the page warns: "Pinout varies between makers: check the datasheet of your part".
4. **Given** any parameter on any page, **Then** the source of the value is shown on the page: the
   datasheet's maker, document number or title, and revision or date.
5. **Given** a page, **When** the user presses back, **Then** the list returns at the same scroll
   position.

---

### User Story 3 - Use it in any supported language and setting (Priority: P2)

As in the calculators: six languages, the locale's decimal separator in values, dark theme, 200%
font, portrait only, and screen-reader support. Pinout drawings are described to screen readers
pin by pin, for example "Pin 1, collector".

**Why this priority**: Required by the constitution.

**Independent Test**: In Ukrainian, check that a parameter value reads "0,7V". With TalkBack, hear
the pins of a pinout drawing in order.

**Acceptance Scenarios**:

1. **Given** the Ukrainian language, **Then** decimal values use a comma, units use the
   international symbols as elsewhere in the app, and pin names are translated (колектор, база,
   емітер).
2. **Given** TalkBack is on, **When** a pinout is focused, **Then** each pin is read with its number
   and name, in pin order.
3. **Given** 200% font size, **Then** every parameter, value and pin name stays readable without
   horizontal scrolling of the page. Tables may wrap their cells.

---

### Edge Cases

- **Families** (78xx, 79xx, 1N400x, 74HC, UC384x): one entry per family. Values that differ
  between members (output voltage, reverse voltage, start-up threshold) are listed per member.
  The 74HC family has no single pinout, so its page explains the family and shows no package
  drawing.
- **Equivalent parts** (1N914 and 1N4148; 2N2222 and PN2222A; 1N5817 and 1N5819): each keeps its
  own entry if it is in the list. The page names the equivalent and the difference.
- **Pinout differences between makers or packages**: the page shows the pinout of the cited
  datasheet, with the warning of User Story 2.
- **Obsolete or single-source parts**: the cited datasheet is the most recent one available from
  an active maker. If none is active, this is noted on the page.
- **Long translated parameter names** (German): table cells wrap and are never cut.
- **Process recreation**: the open page and the list scroll position are kept.

## Requirements *(mandatory)*

### Functional Requirements

**Module and content**

- **FR-001**: The module MUST be listed in the Reference category with a localized title
  ("Jellybean parts") and description ("Common cheap parts: specs and pinouts"), following the
  module contract of spec 001.
- **FR-002**: The reference MUST contain these entries, grouped in these categories, in this
  order:
  - Op-amps: LM358, LM324, TL072, OP07.
  - Comparators: LM393, LM339.
  - Timers: NE555.
  - Bipolar transistors: 2N3904, 2N3906, 2N2222 (PN2222A), PN2907 (2N2907), BC548, BD139, BD140.
  - MOSFETs: 2N7000, 2N7002.
  - Diodes: 1N4148, 1N914, 1N4001–1N4007, 1N5817, 1N5819, BAT54, SD103.
  - Voltage regulators: 78xx, 79xx, LM317, LM337.
  - Voltage references: TL431.
  - Logic: 74HC family.
  - Temperature sensors: LM35, TMP36.
  - Power and PWM controllers: MC34063, UC3842–UC3845, ICL7660.

  The first post supplied the op-amps except OP07, the comparators, the timer, the transistors
  2N3904, 2N3906, 2N2222, PN2907 and BC548, the diodes 1N4148, 1N914, 1N400x, 1N5817 and 1N5819,
  the regulators 78xx and LM317, the reference TL431, the logic family and MC34063. The replies
  supplied the rest.
- **FR-003**: Each entry MUST show:
  - its part number or family name;
  - its category;
  - a one-line purpose;
  - a short description of what it is typically used for;
  - its key parameters (FR-004);
  - its pinout (FR-005);
  - its source (FR-006).
- **FR-004**: Key parameters MUST be the values a designer checks first for that category, each
  with its unit, its kind (min, typ or max) and its test condition where the datasheet gives one:
  - **Op-amps and comparators:** supply range, input offset voltage, gain-bandwidth or response
    time, output type, channels.
  - **Bipolar transistors:** polarity, V_CEO, I_C, P_D, hFE range, f_T.
  - **MOSFETs:** channel, V_DS, I_D, R_DS(on) at a stated V_GS, V_GS(th).
  - **Diodes:** V_RRM, I_F(AV), V_F at a stated current, t_rr where relevant, I_R.
  - **Regulators and references:** output or reference voltage and its tolerance, output current,
    dropout, input range.
  - **Timer, logic, sensors and power controllers:** the equivalent essentials (supply range,
    output current, scale factor, accuracy, frequency range).
- **FR-005**: Each entry except the 74HC family MUST show a drawing of its common package:
  TO-92, SOT-23, DIP-8, DIP-14, DO-35, DO-41, TO-126, TO-220 or SOD-123, depending on the part. The
  drawing shows every pin with its number and its name, and states the side it is viewed from (for
  example "TO-92, top view, flat side down"). A part whose pinout differs between makers
  shows the warning of User Story 2.
- **FR-006**: Every parameter value and every pinout MUST come from a cited manufacturer
  datasheet. The page names the datasheet's maker, document number or title, and revision or date.
  The reference also credits the EEVblog thread as the source of the part selection.

**Navigation and state**

- **FR-007**: The list MUST open on the categories in FR-002 order. Tapping a part opens its page,
  and back returns to the list at the same position. The open page and the scroll position survive
  rotation and process recreation.

**Accessibility and localization**

- **FR-008**: All text except part numbers, family names and unit symbols MUST be in the six
  supported languages. This covers category names, purposes, descriptions, parameter names, kinds,
  conditions, pin names, warnings and view-side notes. Numbers use the locale's decimal separator.
- **FR-009**: Pinout drawings MUST expose each pin to screen readers as "Pin N, name", in pin
  order. Parameter tables MUST be read row by row: name, value with unit words, kind.

### Key Entities

- **Category**: an ordered group with a localized name.
- **Part entry**: a part number or family name, its category, purpose, description, parameters,
  package, pinout, source, and optional notes (equivalents, maker differences, family members).
- **Parameter**: a localized name, a value or a range with its unit, a kind (min, typ or max), and
  an optional condition.
- **Package pinout**: a package outline, the view side, and the pins (number and localized name).
- **Source**: the datasheet's maker, title or document number, revision or date.

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- **Part selection:** EEVblog forum, "Jellybean list", General Technical Chat, first post by
  Phaedrus (2014-04-15) and replies #1–#14. The thread is credited in the reference.
- **Values:** every value and pinout comes from the current manufacturer datasheet of each part
  (for example Texas Instruments, onsemi, Nexperia, STMicroelectronics, Vishay, Diodes Inc.,
  Renesas, Analog Devices). The planning phase MUST:
  - pick one datasheet per entry;
  - record its maker, document number and revision;
  - transcribe the values and pins, and verify every value against the datasheet;
  - note conflicts between makers.
- The thread itself provides only part numbers. No text is copied from it.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: There is no user input besides navigation. Values carry SI units and
  prefixes as printed in the datasheet (V, mA, µA, nA, Ω, mW, W, MHz, ns, °C, mV/°C).
- **Precision/rounding**: Values are shown exactly as the datasheet gives them, without rounding or
  conversion, with min, typ and max kept apart. The decimal separator follows the locale.

### New Dependencies *(Principle III)*

- None. Pinout drawings are made with the existing UI toolkit, or with the SVG library already
  approved in spec 002, as the plan decides.

### Permissions & Network *(Principle I)*

- None. All data ships with the app, and datasheets are cited by name, not linked or downloaded.

### Localization *(Principle V)*

- New strings in en, de, fr, es, ja, uk:
  - the module title and description;
  - category names;
  - per-part purposes and descriptions;
  - parameter names, kinds and conditions;
  - pin names;
  - view-side notes and warnings;
  - the source labels.
- Part numbers, family names and unit symbols are not translated. Decimal separators follow the
  locale.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can find the pinout of any listed part in under 10 seconds from the Reference
  tab (two taps and a scroll).
- **SC-002**: 100% of shown parameter values and pins have a source on their page. An automated
  check fails if any entry lacks a source, a parameter lacks a unit, or a pinout lacks a pin name.
- **SC-003**: A manual audit of every entry against its cited datasheet finds no transcription
  error before release.
- **SC-004**: All text appears in each of the six supported languages, with no missing or raw-key
  strings.
- **SC-005**: Every page is usable at 200% font size, and every pinout can be read pin by pin with a
  screen reader.

## Assumptions

- "Jellybean" means common, cheap, widely stocked parts. The thread is a starting point, not an
  authority: values come from datasheets.
- These are left out:
  - regional parts (Tesla KSY21, KC507, KF507, KD502);
  - niche or disputed parts (MPSH10, MPSH81, BF998, PN3563, 2N3055, D44H11/D45H11, 1N34/1N270
    germanium, 2N4401/2N4403, 2N5401/2N5551, IRF510/IRF540, FDP3672, AM1117, 78Lxx/79Lxx, 741,
    TC1047/TC77, PIC12F675, the 4000 series).

  They may come in a later spec.
- SMD variants (MMBT3904, LM358 in SOIC-8) are mentioned in the description, but only the common
  through-hole package gets a pinout drawing. The SOT-23 parts 2N7002 and BAT54 are the exceptions.
- No search, favorites or comparison between parts. Each entry is static reference content.
- Datasheets are cited, not bundled or linked (Principle I: no network).
