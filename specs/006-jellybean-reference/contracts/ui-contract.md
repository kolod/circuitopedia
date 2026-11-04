# Contract: Jellybean Parts Screens

The module has one module screen with two levels: the list and a part page. The state is a saveable
`PartId?`.

## List

```text
┌──────────────────────────────────────────┐
│ ←  Jellybean parts                       │  shell top bar
│ Op-amps                                  │  sticky category header
│   LM358    Dual op-amp, single supply    │  row: part name + purpose
│   LM324    Quad op-amp, single supply    │
│ Comparators                              │
│   …                                      │
│ Selection: EEVblog forum "Jellybean list"│  footer credit (FR-006)
└──────────────────────────────────────────┘
```

- The list is a `LazyColumn` with `stickyHeader` per category. The rows are in FR-002 order.
- Tapping a row opens its page. Back from the page returns to the same list position, through a
  saveable `LazyListState`.

## Part page

```text
┌──────────────────────────────────────────┐
│ ←  Jellybean parts                       │
│ BC548                                    │  title
│ Bipolar transistor · NPN, general purpose│  category · purpose
│ Description text …                       │
│ Key parameters                           │
│ Parameter      Min    Typ    Max   Unit  │  table; cells wrap at large fonts
│ V_CEO                        30    V     │
│ I_C                          100   mA    │
│ hFE            110           800   —     │  condition under the name
│ Pinout · TO-92, flat side facing you     │
│      [ drawing with 1 C  2 B  3 E ]      │  Canvas, decorative
│ 1  Collector                             │  pin table (spoken)
│ 2  Base                                  │
│ 3  Emitter                               │
│ ⚠ Pinout varies between makers: check …  │  only if makerVariesPinout
│ Members (family entries only)            │
│ Source: onsemi, BC546/D, Rev. …          │
└──────────────────────────────────────────┘
```

- The page is a scrollable `Column` (or `LazyColumn`) whose content stacks at 200% font.
- **Parameter rows** are each one semantics node. They are read as "<name>, <min> to <max> <unit
  words>, <condition>" when there is a range, or "<name>, maximum 100 milliampere".
- **Pin table rows** are read as "Pin 1, collector". The drawing is
  `clearAndSetSemantics {}`.

## Strings (prefix `jellybean_parts_`, six locales)

- `title`, `description`, `credit`, `source_label`, `key_parameters`, `pinout`, `members`,
  `equivalent_of`, `warning_pinout_varies`, `col_parameter`, `col_min`, `col_typ`, `col_max`,
  `col_unit`
- `category_<name>` (11)
- `param_<kind>` (one per `ParameterKind`)
- `pin_<function>` (one per `PinFunction`)
- `view_<package>` (8)
- `<id>_purpose` and `<id>_description` (34 × 2)
- spoken words for kinds and units

## Test tags

`jb-list`, `jb-header-<Category>`, `jb-row-<id>`, `jb-page`, `jb-param-<kind>`, `jb-pin-<n>`,
`jb-source`, `jb-warning`.
