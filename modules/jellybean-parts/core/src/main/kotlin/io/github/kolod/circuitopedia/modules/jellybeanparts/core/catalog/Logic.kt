package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.FamilyMember
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputHighVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputLowVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.PropagationDelay
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p

internal val logic = listOf(
    // TI SCLS181H (SN74HC00, the family's reference gate): 6.3 Recommended Operating Conditions;
    // 6.5 Electrical Characteristics - Commercial (74xx), TA = 25°C; 6.7 Switching Characteristics -
    // Commercial (74xx), TA = 25°C. A family entry: no single package or pinout (spec FR-005).
    PartEntry(
        id = PartId("74hc"), name = "74HC", category = Category.Logic,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "2", typ = "5", max = "6", unit = "V")),
            p(OutputHighVoltage, Value(min = "3.98", typ = "4.3", unit = "V"), "VCC = 4.5 V, IOH = −4 mA"),
            p(OutputLowVoltage, Value(typ = "0.17", max = "0.26", unit = "V"), "VCC = 4.5 V, IOL = 4 mA"),
            p(PropagationDelay, Value(typ = "9", max = "18", unit = "ns"), "SN74HC00, A or B to Y, VCC = 4.5 V"),
            p(SupplyCurrent, Value(max = "2", unit = "µA"), "SN74HC00, VI = VCC or 0, VCC = 6 V"),
        ),
        packageType = null, pins = emptyList(),
        // Common members; their functions are named in the description strings.
        members = listOf("74HC00", "74HC02", "74HC04", "74HC08", "74HC14", "74HC32", "74HC74", "74HC595")
            .map { FamilyMember(it, emptyMap()) },
        source = Source("Texas Instruments", "SCLS181 — SN74HC00, SN54HC00", "SCLS181H, revised August 2021"),
    ),
)
