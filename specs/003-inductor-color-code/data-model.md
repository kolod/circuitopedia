# Data Model: Inductor Color Code Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

## Shared (`:shared:color-code`)

### BandColor (enum, moved from the resistor core)

| Field | Type | Notes |
|-------|------|-------|
| `argb` | `Int` | Display color, the spec 002 R6 palette, unchanged |
| `textOnColor` | `TextOnColor` | Light or Dark, ≥ 4.5:1 across the cell gradient |

Entries in this order (ordinals are saved UI state, append-only): Black, Brown, Red, Orange,
Yellow, Green, Blue, Violet, Gray, White, Gold, Silver. The color *meanings* are not here; each
code defines its own (`ResistorCode`, `InductorCode`).

### Percent (value class, moved)

`BigDecimal`, > 0. Used for tolerances by both calculators.

## Inductor core (`:modules:inductor-color-code:core`)

### Microhenries (value class)

`BigDecimal`, > 0, exact. The unit carried across module boundaries.

### InductorCode (functions over BandColor, source research R1)

| Function | Defined for | Value |
|----------|-------------|-------|
| `BandColor.digit: Int?` | Black … White | 0 … 9 |
| `BandColor.multiplierExponent: Int?` | Black, Brown, Red, Orange, Gold, Silver | 0, 1, 2, 3, −1, −2 (power of ten, in µH) |
| `BandColor.tolerance: Percent?` | Gold, Silver | 5, 10 |
| `BandColor.isDecimalPoint: Boolean` | Gold | true |

### BandRole (enum)

`Digit`, `DecimalPoint`, `Multiplier`, `Tolerance`.

### BandLayout (enum)

| Entry | Bands | Default colors (FR-014) |
|-------|-------|-------------------------|
| `Three` | 3 | Yellow, Violet, Black → 47µH ±20% |
| `Four` | 4 | Yellow, Violet, Black, Silver → 47µH ±10% |

### Selection (data class)

| Field | Type | Rule |
|-------|------|------|
| `layout` | `BandLayout` | |
| `colors` | `List<BandColor>` | size = band count; the whole list satisfies the validity rules |

Derived:

- `pointIndex: Int?` = index of Gold among bands 0–1, else `null`.
- `roleOf(index)`:
  - band 0/1: `DecimalPoint` if it is `pointIndex`, else `Digit`;
  - band 2: `Digit` if `pointIndex != null`, else `Multiplier`;
  - band 3: `Tolerance`.

Validity rules (research R3), all must hold:

1. At most one Gold among bands 0–1.
2. Each band's color has a meaning for its role (`digit`, `multiplierExponent`, `tolerance`;
   `DecimalPoint` is Gold).
3. The first significant digit is not Black: band 0 if `pointIndex != 0`, band 1 if
   `pointIndex == 0`.

Transitions:

- `with(index, color)`: set band `index` to `color`. Then, for every other band in order, if
  the rules no longer hold for it, set it to its default color. If the tapped band itself is
  still invalid (for example Silver in Band 1), return the selection unchanged.
- `canSelect(index, color)` = `with(index, color).colors[index] == color`. The UI uses this for
  "selectable" and the role of the *current* selection for the cell labels.

### Reading (data class)

| Field | Type |
|-------|------|
| `inductance` | `Microhenries` |
| `tolerance` | `Percent` (20 when there is no tolerance band) |

`read()`:

- no point: `(d0 × 10 + d1) × 10^exponent` µH;
- point in band 1: `d0 + d2 / 10` µH;
- point in band 0: `(d1 × 10 + d2) / 100` µH.

### Value range

100nH (0.1µH) … 99mH (99 000µH). There are 2,160 valid selections: 720 on 3 bands, 1,440 on
4 bands.

## Inductor UI state (`InductorState`, module)

Selected `BandLayout` plus one `Selection` per layout. It is saved as
`[layout ordinal, color ordinals of Three, color ordinals of Four]`, like the resistor's.
