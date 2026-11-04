# Research: Jellybean Parts Reference

**Feature**: [spec.md](spec.md) | **Date**: 2026-11-04

## R1. Part selection (Principle II)

- **Source**: EEVblog forum, "Jellybean list" (General Technical Chat,
  https://www.eevblog.com/forum/chat/jellybean-list/).
  - **First post:** by Phaedrus, 2014-04-15, last edited 2014-04-15 22:40. It covers op-amps,
    comparators, timers, transistors, diodes, regulators, the voltage reference and logic.
  - **Replies #1–#14:** these are where the added parts come from.
- **Decision**: The first post's parts are kept as they are. From the replies, only parts that fill
  a gap left by the first post are added:

  | Added | Gap | Reply |
  |-------|-----|-------|
  | OP07 | a precision op-amp | #3 |
  | BD139, BD140 | a power transistor pair | #5 |
  | 2N7000, 2N7002 | small MOSFETs | #3, #5 |
  | BAT54, SD103 | small-signal Schottky diodes | #12, #14 |
  | 79xx, LM337 | negative regulators | #1, #3 |
  | LM35, TMP36 | temperature sensors | #4 |
  | UC384x | an off-line PWM controller | #10 |
  | ICL7660 | a charge pump | #12 |
- **Excluded**: the spec's Assumptions list the regional, niche and disputed parts. The thread
  supplies only names, and no text is taken from it.

## R2. Datasheets (Principle II)

- **Decision**: For each entry, one current datasheet from an active maker. Texas Instruments,
  onsemi, Nexperia, Vishay, STMicroelectronics, Analog Devices and Renesas are preferred, and the
  maker with the most complete parametric tables is chosen. A family entry uses its family
  datasheet.
- **Recording**: the document number and title are fixed here. The **revision and date are
  recorded when the values are transcribed**, from the PDF actually used, and stored with the entry
  (data-model `Source`). This deviates from the spec's wording ("the planning phase MUST record …
  revision") because the revision must match the document the values are copied from.
- An entry marked "confirm" has a document number that must be confirmed against the maker's
  website at transcription. If it differs, the table is updated in the same commit.

| Entry | Maker | Document |
|-------|-------|----------|
| LM358 | TI | SLOS068, LMx58, LM2904 |
| LM324 | TI | SLOS066, LMx24, LM2902 |
| TL072 | TI | SLOS080, TL07x |
| OP07 | TI | SLOS099, OP07x |
| LM393 | TI | SLOS031, LMx93, LM2903 |
| LM339 | TI | SLCS006, LMx39, LM2901 |
| NE555 | TI | SLFS022, xx555 |
| 2N3904 | onsemi | 2N3904/D |
| 2N3906 | onsemi | 2N3906/D |
| 2N2222 (PN2222A) | onsemi | PN2222A/D (confirm) |
| PN2907 (PN2907A) | onsemi | PN2907A/D (confirm) |
| BC548 | onsemi | BC546/D, BC546–BC548 series (confirm) |
| BD139 | STMicroelectronics | BD135, BD137, BD139 (confirm DS number) |
| BD140 | STMicroelectronics | BD136, BD138, BD140 (confirm DS number) |
| 2N7000 | onsemi | 2N7000/D (confirm) |
| 2N7002 | Nexperia | 2N7002 product data sheet |
| 1N4148 | Vishay | 81857, 1N4148 |
| 1N914 | onsemi | 1N914/D, 1N914–1N4448 (confirm) |
| 1N4001–1N4007 | Vishay | 88503, 1N4001 thru 1N4007 |
| 1N5817 | Vishay | 88525, 1N5817 thru 1N5819 |
| 1N5819 | Vishay | 88525 (same document) |
| BAT54 | Nexperia | BAT54 series product data sheet |
| SD103 | Vishay | SD103A/B/C (confirm document number) |
| 78xx | TI | SLVS056, µA7800 series |
| 79xx | onsemi | MC7900/D (confirm) |
| LM317 | TI | SLVS044, LM317 |
| LM337 | TI | LM337 (confirm literature number) |
| TL431 | TI | SLVS543, TL431, TL432 |
| 74HC family | TI | SCLS181, SN74HC00 (family values: supply range, drive, propagation delay at 4.5V) (confirm) |
| LM35 | TI | SNIS159, LM35 |
| TMP36 | Analog Devices | TMP35/TMP36/TMP37 |
| MC34063 | TI | SLLS636, MC33063A, MC34063A (confirm) |
| UC384x | TI | SLUS223, UC384x |
| ICL7660 | Renesas | FN3072, ICL7660 (confirm) |

- **Transcription rules**:
  - values are copied as printed, with their kind (min, typ or max) and condition;
  - no conversion and no rounding;
  - temperature is 25°C unless the table states otherwise;
  - where a datasheet splits a value by grade (TL431A/B, LM358A, OP07C/D), the entry's grade is
    named in its description.
- **Alternatives considered**: distributor pages or Wikipedia. These are secondary, and their values
  drift.

## R3. Pinouts

- **Decision**: Each pinout is taken from the entry's cited datasheet, for its common package:
  - TO-92: transistors, TL431, LM35, TMP36, 2N7000;
  - SOT-23: 2N7002, BAT54;
  - DIP-8: dual op-amps, comparators, timer, ICL7660, MC34063, UC384x;
  - DIP-14: LM324, LM339;
  - DO-35: 1N4148, 1N914, SD103;
  - DO-41: 1N400x, 1N58xx;
  - TO-126: BD139, BD140;
  - TO-220: 78xx, 79xx, LM317, LM337.

  The 74HC family has no pinout.
- **View sides**, stated on the drawing:

  | Package | View |
  |---------|------|
  | TO-92 | flat side facing you, leads down |
  | SOT-23 | top view, pin 3 at the top |
  | DIP | top view, notch at the top, pin 1 at the top left |
  | TO-126 and TO-220 | marking facing you, leads down |
  | DO-35 and DO-41 | cathode band on the right |
- **Maker-varying pinouts**: the "Pinout varies between makers" warning is set as an explicit flag
  per entry, not guessed. It is set where common sources disagree: TO-92 BJTs, whose order is EBC on
  onsemi parts but CBE on Philips-heritage BC parts and some PN2222 variants, and 2N7000/2N7002 in
  some packages. The final flag list is fixed at transcription.
- **Drawing**: a Compose `Canvas` per package outline, with pins numbered and labeled with short
  symbols (C, B, E; G, D, S; A, K; OUT A, IN A−, IN A+, V+, GND; ADJ; REF). A pin table under the
  drawing gives each pin's full, localized name. AndroidSVG is not needed: there are 8 simple
  outlines and no recoloring.

## R4. Data representation (Principle III: reference data in pure Kotlin)

- **Decision**: The catalog is a pure-Kotlin data file in `modules/jellybean-parts/core`, one
  `PartEntry` per entry. It holds no display text, only stable keys:
  - `PartId` (for example `lm358`);
  - `Category`;
  - parameters as `ParameterKind` (an enum: VCEO, IC, PD, HFE, FT, VDS, ID, RDSON, VGSTH, VRRM, IFAV,
    VF, TRR, IR, VOUT, IOUT, VDROPOUT, VIN, VREF, VOS, GBW, SR, SUPPLY, …) with exact values;
  - pins as `PinFunction` (an enum with an optional channel letter);
  - the `Package`;
  - the `Source`.

  Localized text lives in Android string resources in the module:
  - category names;
  - parameter names;
  - pin names;
  - view-side notes;
  - per-part purpose and description, as `jellybean_parts_<id>_purpose` and `_description`.

  The UI maps enum to resource id with exhaustive `when`s, and part id to resource id with a map. An
  instrumented test checks that every part id has both strings.
- **Values**: `Value(min, typ, max, unit)`, where each bound is an exact `BigDecimal` text as
  printed, or null. `unit` is the printed symbol with its prefix ("mA", "µA", "MHz", "mV/°C").
  Conditions are short symbolic texts that need no translation ("I_C = 2mA, V_CE = 1V";
  "V_GS = 10V"). A condition word that needs translation ("at") is a format string in the UI.
- **Formatting**: the decimal separator follows the locale, through `formatDecimal` from
  `:shared:si-format`. Min, typ and max are shown as separate columns.
- **Alternatives considered**:
  - JSON or XML assets: a parser is needed, and the data is not type-checked;
  - text in the core: not localizable.

## R5. Navigation and state

- **Decision**: One module screen with an internal two-level state: the list, or a part page,
  stored as the `PartId?` in `rememberSaveable`. A `BackHandler`, enabled while a page is open,
  returns to the list. The shell's own back handling keeps working for the list level. The list's
  `LazyListState` is saveable.
- **Rationale**: Same pattern as the shell (spec 001). No navigation library (Principle III).

## R6. Testing strategy

- **JVM (core), written first**, as `CatalogTest`:
  - the 35 entries in FR-002 order and category order;
  - unique ids;
  - every entry has a `Source` with maker, document and revision;
  - every parameter has a unit and at least one bound, and min ≤ typ ≤ max where present;
  - every pinout has exactly the package's pin count, with numbers 1…n and non-null functions;
  - the 74HC family has no pinout, and every other entry has one;
  - the flagged maker-varying entries are as listed.
- **Spot-value tests** for a few entries per category, against the transcribed datasheet (for
  example 1N4148 V_RRM 100V, LM317 V_REF 1.25V typ, TL431 V_REF 2.495V typ, LM35 10mV/°C). Each
  test names the datasheet table it checks.
- **Instrumented**:
  - list order and category headers;
  - opening a page, then back to the same position;
  - state restoration;
  - every part id resolves to strings in the current locale;
  - the pinout semantics are "Pin N, name" in order;
  - a parameter row's spoken text.
- **Manual (SC-003)**: an audit of every entry against its PDF, recorded as a checklist in
  `checklists/datasheet-audit.md`, one line per entry with the revision checked.
