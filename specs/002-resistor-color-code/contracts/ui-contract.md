# Contract: Resistor color code screen

**Feature**: [../spec.md](../spec.md) | **Reference**: [../design/reference-4-bands.png](../design/reference-4-bands.png)

## Layout (inside the shell's top bar titled with the module title)

```text
┌───────────────────────────────────────────┐
│ 3 bands │ 4 bands │ 5 bands │ 6 bands      │  PrimaryTabRow, default 4 bands
├───────────────────────────────────────────┤
│ 4.7kΩ ±5%                                 │  value (headlineSmall), fixed
│          ─[▌ ▌ ▌        ▌]─               │  ResistorDrawing over the band columns, fixed
│             \  |  \        \              │  leader lines: vertical + slanted only, 3dp
├──────────┬────────┬────────┬───────┬──────┤
│          │ Band 1 │ Band 2 │ Mult. │Toler.│  header row, fixed
╞══════════╪════════╪════════╪═══════╪══════╡  ── only below this line scrolls (vertically)
│ Black    │        │   0    │  ×1Ω  │      │  empty cell = not selectable
│ Brown    │   1    │   1    │ ×10Ω  │ ±1%  │
│  …       │        │        │       │      │
│ Silver   │        │        │×0.01Ω │ ±10% │
└──────────┴────────┴────────┴───────┴──────┘
   4dp side margins; no horizontal scrolling; pager swipes between tabs
```

The drawing has no text of its own. It is laid out with the same width and side margins as the
table, so each leader line starts at the horizontal center of its header column
(`bandColumnCenters`, shared with the table).

## Strings (`modules/resistor-color-code/src/main/res/values*/strings.xml`, prefix `resistor_color_code_`)

| Key                                       | English                         |
|-------------------------------------------|---------------------------------|
| `resistor_color_code_title`               | Resistor color code             |
| `resistor_color_code_description`         | Resistance from 3–6 color bands |
| `resistor_color_code_bands` (plurals, %d) | one: %d band / other: %d bands  |
| `resistor_color_code_color_black` … `_silver` (12) | Black … Silver         |
| `resistor_color_code_header_band` (%d)    | Band %d                         |
| `resistor_color_code_header_multiplier`   | Mult.                           |
| `resistor_color_code_header_tolerance`    | Toler.                          |
| `resistor_color_code_header_tcr`          | TCR                             |
| `resistor_color_code_cell_description`    | %1$s, %2$s, %3$s (band, color, meaning) |
| `resistor_color_code_band_multiplier` / `_tolerance` / `_tcr` | Multiplier / Tolerance / Temperature coefficient (screen reader band names) |

Every key exists in en, de, fr, es, ja and uk (`LocaleCoverageTest`, Lint).

## Behavior

- A tap on a valid cell selects it, and the value and drawing update in the same frame.
- A tap on an empty cell does nothing. Empty cells are not focusable by screen readers.
- Selected cell: a 3dp #00E5FF ring with a 1dp inner dark hairline. Semantics:
  `selected = true`, `Role.RadioButton`.
- Band colors are identical in light and dark theme. The header, color-name column and empty
  cells use theme surface colors.
- Minimum cell height is 48dp. The value text is wrapped in `semantics { liveRegion = Polite }`,
  so screen readers announce changes.

## Test tags (for instrumented tests)

`rcc-value`, `rcc-tab-<bands>`, `rcc-cell-<bandIndex>-<color>`, `rcc-pager`, and `rcc-page-<bands>`
on each pager page. The pager can keep a neighboring page composed, so tests look up nodes inside
the current page.
