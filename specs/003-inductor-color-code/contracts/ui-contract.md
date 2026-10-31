# Contract: Inductor Screen

Same layout as the resistor screen (spec 002 `contracts/ui-contract.md`), built with
`ColorCodeScaffold` ([shared-ui.md](shared-ui.md)).

```text
┌──────────────────────────────────────────┐
│ ←  Inductor color code                   │  shell top bar
│  3 bands │ 4 bands                       │  tabs (plurals), 4 bands selected first
│               47µH ±10%                  │  value, centered, live region
│        ─[▌ ▌ ▌      ▌]─                  │  inductor SVG, leader lines to the header
│       │Band 1│Band 2│ Mult.│Toler.│      │  fixed header ("Band 3" while a point is set)
│ Black │      │  0   │ ×1µH │      │      │  only the rows scroll
│ …     │      │      │      │      │      │
│ Gold  │  .   │  .   │×0.1µH│ ±5%  │      │
│ Silver│      │      │×0.01µH│±10% │      │
└──────────────────────────────────────────┘
```

## Behavior

- A cell is selectable iff `Selection.canSelect(column, color)`. Its text is
  `roleOf(column).cellLabel(color)` for the *current* selection, except that Gold in Bands 1–2
  always shows ".". Non-selectable cells are empty and hidden from screen readers.
- A tap calls `Selection.with`; every band replaced by the rules updates in the same frame,
  including the third header ("Mult." ↔ "Band 3").
- Screen-reader text of a cell: "<band name>, <color>, <meaning>", where the band name is
  "Band 1", "Band 2", "Band 3", "Multiplier" or "Tolerance", and the meaning of the point is
  "Decimal point".

## Strings (module, prefix `inductor_color_code_`, six locales)

| Key | en |
|-----|----|
| `title` | Inductor color code |
| `description` | Inductance from color bands |
| `decimal_point` | Decimal point |

Everything else comes from the shared library.

## Test tags

`icc-tab-3`, `icc-tab-4`, `icc-pager`, `icc-page-3`, `icc-page-4`, `icc-value`,
`icc-cell-<column>-<Color>`. The resistor keeps `rcc-*`.

## Drawing

`res/raw/inductor_color_code_inductor.svg` per research R5. `shownBands` = [1, 2, 3] for 3
bands and [1, 2, 3, 4] for 4 bands.
