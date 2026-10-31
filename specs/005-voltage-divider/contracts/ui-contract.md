# Contract: Divider Screen

One `LazyColumn`; everything scrolls together (the 200% font lesson from spec 004).

```text
┌──────────────────────────────────────────┐
│ ←  Voltage divider                       │  shell top bar
│      U1 ●                                │  schematic (Canvas, decorative)
│         [R1]  ┐                          │
│      U2 ●     ├ R                        │
│         [R2]  ┘                          │
│         ⏚                                │
│   Unloaded divider (no current from U2)  │  note
│ ┌ U1 ─────────────── V ┐ ┌ U2 ──────── V ┐│  OutlinedTextFields, unit suffix
│ ┌ R1 ─────────────── Ω ┐ ┌ R2 ──────── Ω ┐│  computed: tinted, supporting "computed"
│ ┌ R ──────────────────────────────── Ω ┐ │
│   Enter one more value                   │  note / error (FR-004, FR-006)
│ [        Calculate        ]              │  enabled per canCalculate
├──────────────────────────────────────────┤  only if R1 or R2 computed:
│ Standard resistors                       │
│ [E3][E6][E12][E24][E48][E96][E192]       │
│ R1: 5.1kΩ   R2: 10kΩ                     │  one item per build
│ U2 = 3.311V (+0.333%)   R = 15.1kΩ (…)   │
└──────────────────────────────────────────┘
```

On a narrow screen, or at a large font, the fields stack in a single column (`FlowRow` with a
minimum width).

## Behavior

- As implemented:
  - the five fields are stacked in one column;
  - a computed field shows the number with its SI prefix ("19.41k"), because the suffix already shows the unit, and the text stays parseable;
  - the schematic labels have a fixed size.

- **Calculate**: enabled iff `canCalculate`. It fills the empty fields with `formatVolts` or
  `formatOhms` text and marks them computed.
- **Editing an entered field**: clears every computed field (their text and origin become Empty)
  and the builds.
- **Typing into a computed field**: makes it Entered and clears the other computed fields.
- **Builds**: computed on `Dispatchers.Default` with `ensureActive`, when the result is Solved
  with R1 or R2 computed, and on every series change.
- **Messages**: under the button, by priority:
  1. a parse error ("Check U2: e.g. 3.3 or 3,3");
  2. `Invalid` reasons;
  3. "Enter N more" or "Leave N empty";
  4. "R1, R2 and R alone fix no voltage";
  5. "No build within ±10% of R" when the list is empty.

## Strings (prefix `voltage_divider_`, six locales)

- `title`, `description`
- `note_unloaded`
- `label_u1`, `label_u2`, `label_r1`, `label_r2`, `label_r` (short names: "U1 (input)", "U2 (output)", "R1 (upper)", "R2 (lower)", "R (total)")
- `computed`
- `calculate`
- `need_more` (plurals), `too_many` (plurals), `no_voltage`
- `error_parse` (%1$s = field name), `error_ratio`, `error_signs`, `error_total`
- `builds_header`, `series_label`, `no_builds`
- `build_u2` ("U2 = %1$s (%2$s)"), `build_r` ("R = %1$s (%2$s)")
- `state_entered`, `state_computed`
- spoken: `spoken_volt`, `spoken_millivolt`, `spoken_kilovolt`, `spoken_ohm`, `spoken_kilohm`, `spoken_megohm`, `spoken_gigohm`, `spoken_series`, `spoken_parallel`, `spoken_deviation`

## Test tags

`vd-field-<Quantity>`, `vd-calculate`, `vd-message`, `vd-series-<E24>`, `vd-build-<index>`,
`vd-list`.
