package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Id
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Pd
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.RdsOn
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vds
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.VgsTh
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Drain
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Gate
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source as DatasheetSource

internal val mosfets = listOf(
    // onsemi 2N7000/D: Maximum Ratings, Electrical Characteristics (TC = 25°C); TO-92 Style 22.
    PartEntry(
        id = PartId("2n7000"), name = "2N7000", category = Category.Mosfets,
        parameters = listOf(
            p(Vds, Value(max = "60", unit = "V")),
            p(Id, Value(max = "200", unit = "mA"), "continuous"),
            p(Pd, Value(max = "350", unit = "mW"), "TC = 25°C"),
            p(RdsOn, Value(max = "5.0", unit = "Ω"), "VGS = 10 V, ID = 0.5 A"),
            p(RdsOn, Value(max = "6.0", unit = "Ω"), "VGS = 4.5 V, ID = 75 mA"),
            p(VgsTh, Value(min = "0.8", max = "3.0", unit = "V"), "VDS = VGS, ID = 1.0 mA"),
        ),
        packageType = Package.To92, pins = pins(Source, Gate, Drain),
        source = DatasheetSource("onsemi", "2N7000/D — Small Signal MOSFET 200 mA, 60 V", "Rev. 8, April 2011"),
    ),
    // onsemi 2N7002L/D: Maximum Ratings, Thermal Characteristics, Electrical Characteristics
    // (TA = 25°C); SOT-23 Style 21.
    PartEntry(
        id = PartId("2n7002"), name = "2N7002", category = Category.Mosfets,
        parameters = listOf(
            p(Vds, Value(max = "60", unit = "V")),
            p(Id, Value(max = "115", unit = "mA", plusMinus = true), "continuous, TC = 25°C"),
            p(Pd, Value(max = "225", unit = "mW"), "FR-5 board, TA = 25°C"),
            p(RdsOn, Value(max = "7.5", unit = "Ω"), "VGS = 10 V, ID = 500 mA"),
            p(RdsOn, Value(max = "7.5", unit = "Ω"), "VGS = 5.0 V, ID = 50 mA"),
            p(VgsTh, Value(min = "1.0", max = "2.5", unit = "V")),
        ),
        packageType = Package.Sot23, pins = pins(Gate, Source, Drain),
        source = DatasheetSource("onsemi", "2N7002L/D — MOSFET N-Channel, Small Signal, SOT-23", "Rev. 11, June 2024"),
    ),
)
