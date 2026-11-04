package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputBiasCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputOffsetVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputLowVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputSinkCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ResponseTime
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Pin
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Ground
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.InvertingInput
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.NonInvertingInput
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Output
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.PositiveSupply
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p

private const val RESPONSE = "VCC = 5 V, RL to 5 V through 5.1 kΩ, CL = 15 pF, 100 mV input step with 5 mV overdrive"

internal val comparators = listOf(
    // TI SLCS005AH: 5.2 Recommended Operating Conditions (non-V devices); 5.8 Electrical
    // Characteristics, LM393 column (VCC = 5 V, TA = 25°C); 5.11 Switching Characteristics; pin table.
    PartEntry(
        id = PartId("lm393"), name = "LM393", category = Category.Comparators,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "2", max = "30", unit = "V")),
            p(InputOffsetVoltage, Value(typ = "2", max = "5", unit = "mV"), "VCC = 5 V to 30 V, VIC = VICR min, VO = 1.4 V"),
            p(InputBiasCurrent, Value(typ = "-25", max = "-250", unit = "nA"), "VO = 1.4 V"),
            p(ResponseTime, Value(typ = "1.3", unit = "µs"), RESPONSE),
            p(OutputSinkCurrent, Value(min = "6", unit = "mA"), "VOL = 1.5 V, VID = −1 V"),
            p(OutputLowVoltage, Value(typ = "130", max = "400", unit = "mV"), "IOL = 4 mA, VID = −1 V"),
            p(SupplyCurrent, Value(typ = "0.45", max = "1", unit = "mA"), "VCC = 5 V"),
        ),
        packageType = Package.Dip8,
        pins = listOf(
            Pin(1, Output, 'A'), Pin(2, InvertingInput, 'A'), Pin(3, NonInvertingInput, 'A'),
            Pin(4, Ground), Pin(5, NonInvertingInput, 'B'), Pin(6, InvertingInput, 'B'),
            Pin(7, Output, 'B'), Pin(8, PositiveSupply),
        ),
        source = Source("Texas Instruments", "SLCS005 — LMx93, LM2903", "SLCS005AH, revised April 2025"),
    ),
    // TI SLCS006Z: 6.5 Recommended Operating Conditions, Non-B Versions; 6.10 Electrical
    // Characteristics for LMx39 (VCC = 5 V, TA = 25°C); 6.14 Switching Characteristics; Figure 5-1.
    PartEntry(
        id = PartId("lm339"), name = "LM339", category = Category.Comparators,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "2", max = "30", unit = "V")),
            p(InputOffsetVoltage, Value(typ = "2", max = "5", unit = "mV"), "VCC = 5 V to 30 V, VIC = VICR min"),
            p(InputBiasCurrent, Value(typ = "-25", max = "-250", unit = "nA"), "VO = 1.4 V"),
            p(ResponseTime, Value(typ = "1.3", unit = "µs"), RESPONSE),
            p(OutputSinkCurrent, Value(min = "6", typ = "16", unit = "mA"), "VID = −1 V, VOL = 1.5 V"),
            p(OutputLowVoltage, Value(typ = "150", max = "400", unit = "mV"), "VID = −1 V, IOL = 4 mA"),
            p(SupplyCurrent, Value(typ = "0.8", max = "2", unit = "mA"), "VO = 2.5 V, no load"),
        ),
        packageType = Package.Dip14,
        pins = listOf(
            Pin(1, Output, 'A'), Pin(2, Output, 'B'), Pin(3, PositiveSupply),
            Pin(4, InvertingInput, 'B'), Pin(5, NonInvertingInput, 'B'),
            Pin(6, InvertingInput, 'A'), Pin(7, NonInvertingInput, 'A'),
            Pin(8, InvertingInput, 'C'), Pin(9, NonInvertingInput, 'C'),
            Pin(10, InvertingInput, 'D'), Pin(11, NonInvertingInput, 'D'),
            Pin(12, Ground), Pin(13, Output, 'D'), Pin(14, Output, 'C'),
        ),
        source = Source("Texas Instruments", "SLCS006 — LMx39, LM2901", "SLCS006Z, revised May 2025"),
    ),
)
