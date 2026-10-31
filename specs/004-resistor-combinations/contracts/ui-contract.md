# Contract: Combinations Screen

```text
┌──────────────────────────────────────────┐
│ ←  Resistor combinations                 │  shell top bar
│ ┌ Target resistance ───────────────────┐ │  OutlinedTextField, text keyboard
│ │ 1.234k                               │ │
│ └──────────────────────────────────────┘ │
│   e.g. 4.7k, 4k7 or 4700  (or error)     │  supporting text
│ ┌ Series ──────────────────────── E24 ▾┐ │  ExposedDropdownMenuBox
│ [2 series][3 series][2 ∥][3 ∥][2 ∥ + 1]  │  FilterChips in a FlowRow, all on first
├──────────────────────────────────────────┤
│ 1.2kΩ + 33Ω + 1Ω                         │  LazyColumn, one item per result
│ 1.234kΩ   0%                             │
│ 2.7kΩ ∥ 2.2kΩ + 22Ω                      │
│ 1.234kΩ   +0.0213%                       │
│ …                                        │
└──────────────────────────────────────────┘
```

## Behavior

- The list updates on every change of the text, the series or the chips (FR-010). The search
  runs off the main thread, and only the latest input's results are shown.
- Empty text shows the hint and an empty-list message. Invalid text shows the error message
  (isError) and no list.
- A chip that is off removes its kind. A single resistor is always included.
- Each result is one semantics node. Its description reads "<connection>, <resistance>,
  deviation <percent>" with spoken unit words (FR-012), built from strings, not symbols.

## Strings (prefix `resistor_combinations_`, six locales)

| Key | en |
|-----|----|
| `title` | Resistor combinations |
| `description` | Match a value with 1–3 standard resistors |
| `target_label` | Target resistance |
| `target_hint` | e.g. 4.7k, 4k7 or 4700 |
| `target_error` | Enter a value from 0.1Ω to 1GΩ, e.g. 4.7k |
| `series_label` | Series |
| `kind_series2` / `kind_series3` | 2 in series / 3 in series |
| `kind_parallel2` / `kind_parallel3` | 2 in parallel / 3 in parallel |
| `kind_parallel_series` | 2 in parallel + 1 |
| `empty` | Enter a target value to see combinations |
| `spoken_series` / `spoken_parallel` | plus / parallel |
| `spoken_deviation` | deviation %1$s percent |
| `spoken_ohm` / `spoken_kilohm` / `spoken_megohm` / `spoken_gigohm` | ohm / kilohm / megohm / gigohm |

## Test tags

`rcomb-target`, `rcomb-series`, `rcomb-series-<E24>`, `rcomb-kind-<Kind>`, `rcomb-list`,
`rcomb-result-<index>`, `rcomb-error`, `rcomb-empty`.
