# Data Model: Resistor Color Code Calculator

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

All entities are pure Kotlin in `modules/resistor-color-code/core`. Nothing is persisted.

## BandColor (enum, table row order)

`Black, Brown, Red, Orange, Yellow, Green, Blue, Violet, Gray, White, Gold, Silver`

| Field             | Type              | Notes                                         |
|-------------------|-------------------|-----------------------------------------------|
| digit             | Int?              | 0–9 for Black…White, `null` for Gold/Silver   |
| multiplierExponent| Int?              | −2 (Silver) … 9 (White)                       |
| tolerance         | Percent?          | per research R1, `null` for Black/White       |
| tcr               | PpmPerKelvin?     | per research R1, `null` for White/Gold/Silver |
| argb              | Int               | display base color (research R6)              |
| textOnColor       | Light \| Dark     | chosen for ≥ 4.5:1 contrast                   |

Ordinals are persisted in saved UI state, so the order is append-only.

## BandRole (enum)

`Digit, Multiplier, Tolerance, TemperatureCoefficient`

Valid colors per role:

- **Digit**: colors with a non-null `digit`. For the **first** digit band, Black is excluded.
- **Multiplier**: colors with a non-null `multiplierExponent` (all 12).
- **Tolerance**: colors with a non-null `tolerance`.
- **TemperatureCoefficient**: colors with a non-null `tcr`.

## BandLayout (enum)

| Value | Roles                                                       | Implicit tolerance |
|-------|-------------------------------------------------------------|--------------------|
| Three | Digit, Digit, Multiplier                                    | ±20%               |
| Four  | Digit, Digit, Multiplier, Tolerance                         | —                  |
| Five  | Digit, Digit, Digit, Multiplier, Tolerance                  | —                  |
| Six   | Digit, Digit, Digit, Multiplier, Tolerance, TemperatureCoefficient | —           |

`Four` is the default tab (FR-003).

## Selection

| Field  | Type             | Validation                                              |
|--------|------------------|---------------------------------------------------------|
| layout | BandLayout       |                                                         |
| colors | List<BandColor>  | size = `layout.roles.size`; each valid for its role/index |

- `Selection.default(layout)`: the defaults from FR-014.
- `with(index, color): Selection`: returns a copy with that band changed. If the color is
  invalid for the band, it returns `this` unchanged (spec US1 scenario 4).
- The constructor rejects invalid combinations (`require`).

## Reading (result of `Selection.read()`)

| Field      | Type           | Notes                                           |
|------------|----------------|-------------------------------------------------|
| resistance | Ohms           | exact `BigDecimal`, = digits × 10^exponent      |
| tolerance  | Percent        | from the band, or ±20% for `Three`              |
| tcr        | PpmPerKelvin?  | only for `Six`                                  |

## Unit value classes (Principle II: no unit-less numbers across module boundaries)

- `Ohms(value: BigDecimal)` with `value > 0`
- `Percent(value: BigDecimal)` with `value > 0`
- `PpmPerKelvin(value: Int)` with `value > 0`

## UI state (Android module, saved, not persisted)

| State        | Type                         | Default                      |
|--------------|------------------------------|------------------------------|
| selected tab | BandLayout                   | `Four`                       |
| selections   | Map<BandLayout, Selection>   | `Selection.default` per layout |

Transitions: tapping a valid cell calls `selections[layout] = selections[layout].with(i, c)`.
Tapping an invalid cell does nothing. Changing the tab changes only the selected tab.
