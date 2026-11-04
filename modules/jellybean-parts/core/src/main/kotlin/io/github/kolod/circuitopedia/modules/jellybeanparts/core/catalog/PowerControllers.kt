package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.FamilyMember
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ConversionEfficiency
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.MaxDutyCycle
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OscillatorFrequency
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputResistance
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.PowerEfficiency
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ReferenceVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SwitchCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.UvloOff
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.UvloOn
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.VceSat
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.CapMinus
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.CapPlus
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.ComparatorInvertingInput
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Compensation
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.CurrentSense
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.DriverCollector
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Ground
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.LowVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.NoConnect
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Oscillator
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Output
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.PeakCurrentSense
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.PositiveSupply
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.ReferenceOutput
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.RtCt
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.SwitchCollector
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.SwitchEmitter
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.TimingCapacitor
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.VoltageFeedback
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

private fun uc384x(name: String, on: List<String>, off: List<String>, duty: List<String>) = FamilyMember(
    name,
    mapOf(
        UvloOn to Value(on[0], on[1], on[2], "V"),
        UvloOff to Value(off[0], off[1], off[2], "V"),
        MaxDutyCycle to Value(duty[0], duty[1], duty[2], "%"),
    ),
)

internal val powerControllers = listOf(
    // TI SLLS636N: 7.3 Recommended Operating Conditions; 7.5–7.8 Electrical Characteristics
    // (VCC = 5 V); Pin Functions, P package.
    PartEntry(
        id = PartId("mc34063"), name = "MC34063", category = Category.PowerControllers,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "3", max = "40", unit = "V")),
            p(SwitchCurrent, Value(max = "1.5", unit = "A"), "absolute maximum"),
            p(OscillatorFrequency, Value(min = "24", typ = "33", max = "42", unit = "kHz"), "VPIN5 = 0 V, CT = 1 nF, TA = 25°C"),
            p(ReferenceVoltage, Value(min = "1.225", typ = "1.25", max = "1.275", unit = "V"), "comparator threshold, TA = 25°C"),
            p(VceSat, Value(typ = "1", max = "1.3", unit = "V"), "Darlington connection, ISW = 1 A"),
            p(SupplyCurrent, Value(max = "4", unit = "mA"), "VCC = 5 V to 40 V, CT = 1 nF"),
        ),
        packageType = Package.Dip8,
        pins = pins(
            SwitchCollector, SwitchEmitter, TimingCapacitor, Ground,
            ComparatorInvertingInput, PositiveSupply, PeakCurrentSense, DriverCollector,
        ),
        source = Source("Texas Instruments", "SLLS636 — MC33063A, MC34063A", "SLLS636N, revised January 2015"),
    ),
    // TI SLUS223H: 4 Device Comparison Table; 6.3 Recommended Operating Conditions; 6.5 Electrical
    // Characteristics (UC384x, VVCC = 15 V); Table 5-1 Pin Functions, PDIP (8).
    PartEntry(
        id = PartId("uc384x"), name = "UC3842–UC3845", category = Category.PowerControllers,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "12", max = "28", unit = "V")),
            p(ReferenceVoltage, Value(min = "4.95", typ = "5", max = "5.05", unit = "V"), "IVREF = 1 mA, TJ = 25°C"),
            p(OscillatorFrequency, Value(min = "47", typ = "52", max = "57", unit = "kHz"), "initial accuracy, TJ = 25°C"),
            p(SupplyCurrent, Value(max = "25", unit = "mA"), "externally limited"),
        ),
        packageType = Package.Dip8,
        pins = pins(Compensation, VoltageFeedback, CurrentSense, RtCt, Ground, Output, PositiveSupply, ReferenceOutput),
        members = listOf(
            uc384x("UC3842", listOf("14.5", "16", "17.5"), listOf("8.5", "10", "11.5"), listOf("92", "97", "100")),
            uc384x("UC3843", listOf("7.8", "8.4", "9"), listOf("7", "7.6", "8.2"), listOf("92", "97", "100")),
            uc384x("UC3844", listOf("14.5", "16", "17.5"), listOf("8.5", "10", "11.5"), listOf("47", "48", "50")),
            uc384x("UC3845", listOf("7.8", "8.4", "9"), listOf("7", "7.6", "8.2"), listOf("47", "48", "50")),
        ),
        source = Source("Texas Instruments", "SLUS223 — UCx84x Current-Mode PWM Controllers", "SLUS223H, revised October 2024"),
    ),
    // Renesas FN3072: Electrical Specifications, ICL7660 columns (V+ = 5 V, TA = 25°C); Pinouts.
    PartEntry(
        id = PartId("icl7660"), name = "ICL7660", category = Category.PowerControllers,
        parameters = listOf(
            p(SupplyVoltage, Value(min = "1.5", max = "3.5", unit = "V"), "LV to GND"),
            p(SupplyVoltage, Value(min = "3.0", max = "10.0", unit = "V"), "LV open"),
            p(SupplyCurrent, Value(typ = "170", max = "500", unit = "µA"), "RL = ∞"),
            p(OutputResistance, Value(typ = "55", max = "100", unit = "Ω"), "IOUT = 20 mA"),
            p(OscillatorFrequency, Value(typ = "10", unit = "kHz")),
            p(PowerEfficiency, Value(min = "95", typ = "98", unit = "%"), "RL = 5 kΩ"),
            p(ConversionEfficiency, Value(min = "97", typ = "99.9", unit = "%"), "RL = ∞"),
        ),
        packageType = Package.Dip8,
        pins = pins(NoConnect, CapPlus, Ground, CapMinus, Output, LowVoltage, Oscillator, PositiveSupply),
        source = Source("Renesas", "FN3072 — ICL7660, ICL7660A CMOS Voltage Converters", "Rev. 7.00, Oct 5, 2010"),
    ),
)
