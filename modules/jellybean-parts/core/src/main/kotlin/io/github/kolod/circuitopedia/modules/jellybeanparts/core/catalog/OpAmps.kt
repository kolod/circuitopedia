package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.GainBandwidth
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputBiasCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputOffsetVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.PowerDissipationTotal
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SlewRate
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Pin
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.InvertingInput
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.NegativeSupply
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.NoConnect
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.NonInvertingInput
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.OffsetNull
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Output
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.PositiveSupply
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

/** The standard dual op-amp/comparator DIP-8 pinout: OUT A, IN− A, IN+ A, V−, IN+ B, IN− B, OUT B, V+. */
internal fun dualPins(): List<Pin> = listOf(
    Pin(1, Output, 'A'), Pin(2, InvertingInput, 'A'), Pin(3, NonInvertingInput, 'A'),
    Pin(4, NegativeSupply), Pin(5, NonInvertingInput, 'B'), Pin(6, InvertingInput, 'B'),
    Pin(7, Output, 'B'), Pin(8, PositiveSupply),
)

/** The standard quad DIP-14 pinout of LM324-style op-amps: V+ on pin 4, V− on pin 11. */
private fun quadOpAmpPins(): List<Pin> = listOf(
    Pin(1, Output, 'A'), Pin(2, InvertingInput, 'A'), Pin(3, NonInvertingInput, 'A'),
    Pin(4, PositiveSupply), Pin(5, NonInvertingInput, 'B'), Pin(6, InvertingInput, 'B'),
    Pin(7, Output, 'B'), Pin(8, Output, 'C'), Pin(9, InvertingInput, 'C'),
    Pin(10, NonInvertingInput, 'C'), Pin(11, NegativeSupply), Pin(12, NonInvertingInput, 'D'),
    Pin(13, InvertingInput, 'D'), Pin(14, Output, 'D'),
)

internal val opAmps = listOf(
    // TI SLOS068AB: 5.3 Recommended Operating Conditions; 5.7 Electrical Characteristics: LM358, LM358A
    // (VS = 5 V, TA = 25°C); Table 4-1 Pin Functions, P package.
    PartEntry(
        id = PartId("lm358"), name = "LM358", category = Category.OpAmps,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "3", max = "30", unit = "V")),
            p(InputOffsetVoltage, Value(typ = "3", max = "7", unit = "mV"), "VS = 5 V to 30 V, VCM = 0 V, VO = 1.4 V"),
            p(InputBiasCurrent, Value(typ = "-20", max = "-250", unit = "nA"), "VO = 1.4 V"),
            p(GainBandwidth, Value(typ = "0.7", unit = "MHz")),
            p(SlewRate, Value(typ = "0.3", unit = "V/µs"), "G = +1"),
            p(SupplyCurrent, Value(typ = "350", max = "600", unit = "µA"), "per amplifier, VO = 2.5 V, IO = 0 A, TA = 0°C to 70°C"),
        ),
        packageType = Package.Dip8, pins = dualPins(),
        source = Source("Texas Instruments", "SLOS068 — LMx58, LM2904", "SLOS068AB, revised October 2024"),
    ),
    // TI SLOS066AE: 6.3 Recommended Operating Conditions; 6.7 Electrical Characteristics for LM324
    // (VCC = 5 V); 6.10 Operating Conditions (VCC = ±15 V); Table 5-1 Pin Functions, N package.
    PartEntry(
        id = PartId("lm324"), name = "LM324", category = Category.OpAmps,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "3", max = "30", unit = "V")),
            p(InputOffsetVoltage, Value(typ = "3", max = "7", unit = "mV"), "VCC = 5 V to MAX, VIC = VICRmin, VO = 1.4 V, TA = 25°C"),
            p(InputBiasCurrent, Value(typ = "-20", max = "-250", unit = "nA"), "VO = 1.4 V, TA = 25°C"),
            p(GainBandwidth, Value(typ = "1.2", unit = "MHz"), "unity gain, VCC = ±15 V, RL = 1 MΩ, CL = 20 pF"),
            p(SlewRate, Value(typ = "0.5", unit = "V/µs"), "unity gain, VCC = ±15 V, RL = 1 MΩ, CL = 30 pF"),
            p(SupplyCurrent, Value(typ = "0.7", max = "1.2", unit = "mA"), "four amplifiers, VO = 2.5 V, no load, full range"),
        ),
        packageType = Package.Dip14, pins = quadOpAmpPins(),
        source = Source("Texas Instruments", "SLOS066 — LMx24, LM2902", "SLOS066AE, revised September 2025"),
    ),
    // TI SLOS080W: 5.3 Recommended Operating Conditions (all other devices); 5.8 and 5.9 Electrical
    // Characteristics for TL07xC (VS = ±15 V, TA = 25°C); Table 4-3 Pin Functions: TL072x.
    PartEntry(
        id = PartId("tl072"), name = "TL072", category = Category.OpAmps,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "4.5", max = "40", unit = "V")),
            p(InputOffsetVoltage, Value(typ = "3", max = "10", unit = "mV"), "TL072C, VO = 0 V, RS = 50 Ω"),
            p(GainBandwidth, Value(typ = "5.25", unit = "MHz")),
            p(SlewRate, Value(min = "8", typ = "20", unit = "V/µs"), "TL072C, VI = 10 V, CL = 100 pF, RL = 2 kΩ"),
            p(SupplyCurrent, Value(typ = "1.4", max = "2.5", unit = "mA"), "per amplifier, VO = 0 V, no load"),
        ),
        packageType = Package.Dip8, pins = dualPins(),
        source = Source("Texas Instruments", "SLOS080 — TL07xx", "SLOS080W, revised July 2025"),
    ),
    // TI SLOS099H: 6.3 Recommended Operating Conditions; 6.5 Electrical Characteristics, OP07C
    // (TA = 25°C, VS = ±15 V); Table 5-1 Pin Functions.
    PartEntry(
        id = PartId("op07"), name = "OP07", category = Category.OpAmps,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "6", max = "36", unit = "V"), "single supply; dual supply ±3 V to ±18 V"),
            p(InputOffsetVoltage, Value(typ = "60", unit = "µV", plusMinus = true), "OP07C"),
            p(InputBiasCurrent, Value(typ = "1.8", unit = "nA", plusMinus = true), "OP07C"),
            p(GainBandwidth, Value(min = "0.4", typ = "0.6", unit = "MHz"), "unity gain"),
            p(SlewRate, Value(typ = "0.3", unit = "V/µs"), "VS = 5 V, RL = 2 kΩ"),
            p(PowerDissipationTotal, Value(typ = "80", max = "150", unit = "mW"), "no load"),
        ),
        packageType = Package.Dip8,
        pins = pins(
            OffsetNull, InvertingInput, NonInvertingInput, NegativeSupply,
            NoConnect, Output, PositiveSupply, OffsetNull,
        ),
        source = Source("Texas Instruments", "SLOS099 — OP07x", "SLOS099H, revised March 2023"),
    ),
)
