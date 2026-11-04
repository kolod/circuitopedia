package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.CathodeCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.CathodeVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputBiasCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.MinCathodeCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputResistance
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ReferenceVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Anode
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Cathode
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Reference
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

internal val voltageReferences = listOf(
    // TI SLVS543S: 6.4 Recommended Operating Conditions; 6.5 Electrical Characteristics, TL431C
    // (TA = 25°C). Pinout: the legs are the same at every maker, but the numbering is not. TI
    // (Table 5-1, LP) numbers them 1 Cathode, 2 Anode, 3 REF; onsemi TL431/D Rev. 45 numbers them
    // 1 Reference, 2 Anode, 3 Cathode. The pins follow onsemi, because like the other TO-92 entries
    // its pin 1 is the left leg with the flat side facing the viewer (REF, A, K from the left).
    PartEntry(
        id = PartId("tl431"), name = "TL431", category = Category.VoltageReferences,
        parameters = listOf(
            p(ReferenceVoltage, Value(min = "2440", typ = "2495", max = "2550", unit = "mV"), "TL431C, VKA = Vref, IKA = 10 mA"),
            p(CathodeVoltage, Value(max = "36", unit = "V"), "from Vref"),
            p(CathodeCurrent, Value(min = "1", max = "100", unit = "mA"), "continuous"),
            p(MinCathodeCurrent, Value(typ = "0.4", max = "1", unit = "mA"), "VKA = Vref"),
            p(InputBiasCurrent, Value(typ = "2", max = "4", unit = "µA"), "reference input current, IKA = 10 mA, R1 = 10 kΩ"),
            p(OutputResistance, Value(typ = "0.2", max = "0.5", unit = "Ω"), "dynamic impedance, VKA = Vref, f ≤ 1 kHz, IKA = 1 mA to 100 mA"),
        ),
        packageType = Package.To92, pins = pins(Reference, Anode, Cathode),
        source = Source("Texas Instruments", "SLVS543 — TL431, TL432 Precision Programmable Reference", "SLVS543S, revised May 2024"),
    ),
)
