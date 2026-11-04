# Data Model: Jellybean Parts Reference

**Feature**: [spec.md](spec.md) | **Date**: 2026-11-04

All types are in `:modules:jellybean-parts:core` and are pure Kotlin. Enum ordinals are not saved,
since the saved state is `PartId.value`.

## Category (enum, in FR-002 order)

`OpAmps, Comparators, Timers, BipolarTransistors, Mosfets, Diodes, VoltageRegulators,
VoltageReferences, Logic, TemperatureSensors, PowerControllers`

## PartId (value class)

A stable lowercase key: `lm358`, `bc548`, `1n4001_1n4007`, `78xx`, `74hc`, `uc384x`. It is used for
string resource names (`jellybean_parts_<id>_purpose`) and for saved state.

## PartEntry

| Field | Type | Rule |
|-------|------|------|
| `id` | PartId | unique |
| `name` | String | as printed: "LM358", "2N2222 (PN2222A)", "78xx", "UC3842–UC3845" |
| `category` | Category | |
| `parameters` | List<Parameter> | ≥ 3 |
| `packageType` | Package? | null only for the 74HC family |
| `pins` | List<Pin> | size = `packageType.pinCount`, numbers 1…n; empty only if `packageType` is null |
| `makerVariesPinout` | Boolean | shows the warning of spec US2 |
| `members` | List<FamilyMember> | non-empty for families (78xx, 79xx, 1N4001–1N4007, UC384x, 74HC) |
| `equivalent` | PartId? | for example 1N914 → 1N4148 |
| `source` | Source | required |

## Parameter

| Field | Type | Rule |
|-------|------|------|
| `kind` | ParameterKind | localized name in the UI |
| `value` | Value | |
| `condition` | String? | symbolic, untranslated ("I_C = 2mA, V_CE = 1V") |

## Value

| Field | Type | Rule |
|-------|------|------|
| `min`, `typ`, `max` | String? | exact decimal text as printed. At least one is present, and where present min ≤ typ ≤ max |
| `unit` | String | printed symbol with its prefix: "V", "mA", "µA", "nA", "Ω", "mW", "W", "MHz", "ns", "mV/°C", "%" |

A range such as hFE 110–800 is `min = 110, max = 800`.

## FamilyMember

| Field | Type | Example |
|-------|------|---------|
| `name` | String | "7805", "1N4004", "UC3843" |
| `values` | Map<ParameterKind, Value> | VOUT 5V; VRRM 400V; UVLO on 8.4V and off 7.6V |

## Package (enum)

| Entry | Pins | View note key |
|-------|------|---------------|
| `To92` | 3 | flat side facing you, leads down |
| `Sot23` | 3 | top view |
| `Dip8` | 8 | top view, notch up |
| `Dip14` | 14 | top view, notch up |
| `Do35` | 2 | cathode band on the right |
| `Do41` | 2 | cathode band on the right |
| `To126` | 3 | marking facing you, leads down |
| `To220` | 3 | marking facing you, leads down |

## Pin

| Field | Type |
|-------|------|
| `number` | Int (1…n) |
| `function` | PinFunction |
| `channel` | Char? (A, B, C, D for multi-channel parts) |

`PinFunction` is one of: Collector, Base, Emitter, Gate, Drain, Source, Anode, Cathode, Output,
InvertingInput, NonInvertingInput, PositiveSupply, NegativeSupplyOrGround, Ground, Input, Adjust,
Reference, Trigger, Threshold, Discharge, Control, Reset, NoConnect, OffsetNull, Comp, VoltageFeedback,
CurrentSense, RtCt, Vref, Vcc, Cap+, Cap−, Osc, LowVoltage, SwitchCollector, SwitchEmitter,
TimingCapacitor, DriverCollector, IpkSense, ComparatorInvertingInput. The list is completed at
transcription, so that every pin name in the cited datasheets maps to one entry.

Each `PinFunction` has a short symbol (C, B, E, OUT, IN−, IN+, V+, GND, ADJ, REF, …) for the drawing,
and a localized full name.

## Source

| Field | Type | Example |
|-------|------|---------|
| `maker` | String | "Texas Instruments" |
| `document` | String | "SLOS068 — LMx58, LM2904" |
| `revision` | String | "Rev. AE, 2024-02", recorded at transcription |

## Catalog

`val catalog: List<PartEntry>` holds the 34 entries in FR-002 order. `byCategory()` groups them in
`Category` order.
