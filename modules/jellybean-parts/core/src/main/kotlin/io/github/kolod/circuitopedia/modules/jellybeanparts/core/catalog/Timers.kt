package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputLowVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.ControlVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Discharge
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Ground
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Output
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.PositiveSupply
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Reset
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Threshold
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Trigger
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

internal val timers = listOf(
    // TI SLFS022K: 5.3 Recommended Operating Conditions; 5.5 Electrical Characteristics, NE555
    // (VCC = 5 V to 15 V, TA = 25°C); pin functions table, P package.
    PartEntry(
        id = PartId("ne555"), name = "NE555", category = Category.Timers,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "4.5", max = "16", unit = "V")),
            p(OutputCurrent, Value(max = "200", unit = "mA", plusMinus = true)),
            p(OutputLowVoltage, Value(typ = "0.1", max = "0.25", unit = "V"), "VCC = 15 V, IOL = 10 mA"),
            p(SupplyCurrent, Value(typ = "3", max = "6", unit = "mA"), "output low, no load, VCC = 5 V"),
            p(SupplyCurrent, Value(typ = "10", max = "15", unit = "mA"), "output low, no load, VCC = 15 V"),
        ),
        packageType = Package.Dip8,
        pins = pins(Ground, Trigger, Output, Reset, ControlVoltage, Threshold, Discharge, PositiveSupply),
        source = Source("Texas Instruments", "SLFS022 — xx555 Precision Timers", "SLFS022K, revised March 2026"),
    ),
)
