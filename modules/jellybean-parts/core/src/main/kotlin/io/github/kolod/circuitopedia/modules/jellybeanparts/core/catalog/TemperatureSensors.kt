package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Accuracy
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OperatingTemperature
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputAt25C
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ScaleFactor
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Ground
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Output
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.PositiveSupply
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

internal val temperatureSensors = listOf(
    // TI SNIS159H: 6.3 Recommended Operating Conditions; 6.7 Electrical Characteristics: LM35,
    // LM35C, LM35D Limits, LM35 columns (VS = 5 V, ILOAD = 50 µA); Pin Functions, TO-92.
    PartEntry(
        id = PartId("lm35"), name = "LM35", category = Category.TemperatureSensors,
        parameters = listOf(
            p(ScaleFactor, Value(min = "9.8", typ = "10", max = "10.2", unit = "mV/°C"), "sensor gain (average slope), TMIN ≤ TA ≤ TMAX"),
            p(Accuracy, Value(typ = "0.4", max = "1", unit = "°C", plusMinus = true), "TA = 25°C"),
            p(Accuracy, Value(typ = "0.8", max = "1.5", unit = "°C", plusMinus = true), "TA = TMAX"),
            p(SupplyCurrent, Value(typ = "56", max = "80", unit = "µA"), "quiescent, VS = 5 V, 25°C"),
            p(OperatingTemperature, Value(min = "-55", max = "150", unit = "°C")),
        ),
        packageType = Package.To92, pins = pins(PositiveSupply, Output, Ground),
        source = Source("Texas Instruments", "SNIS159 — LM35 Precision Centigrade Temperature Sensors", "SNIS159H, revised December 2017"),
    ),
    // ADI TMP35/TMP36/TMP37 Rev. E: Table 1 Specifications (VS = 2.7 V to 5.5 V, F grade accuracy);
    // Figure 4, T-3 (TO-92): pin 1 +VS, pin 2 VOUT, pin 3 GND. The text layer drops ±, µ and °.
    PartEntry(
        id = PartId("tmp36"), name = "TMP36", category = Category.TemperatureSensors,
        parameters = listOf(
            p(ScaleFactor, Value(typ = "10", unit = "mV/°C"), "−40°C ≤ TA ≤ +125°C"),
            p(OutputAt25C, Value(typ = "750", unit = "mV"), "TA = 25°C"),
            p(Accuracy, Value(typ = "1", max = "2", unit = "°C", plusMinus = true), "F grade, TA = 25°C"),
            p(Accuracy, Value(typ = "2", max = "3", unit = "°C", plusMinus = true), "F grade, over rated temperature"),
            p(SupplyVoltage, Value(min = "2.7", max = "5.5", unit = "V")),
            p(SupplyCurrent, Value(max = "50", unit = "µA"), "ISY (ON), unloaded"),
            p(OperatingTemperature, Value(min = "-40", max = "125", unit = "°C"), "specified range"),
        ),
        packageType = Package.To92, pins = pins(PositiveSupply, Output, Ground),
        source = Source("Analog Devices", "TMP35/TMP36/TMP37 — Low Voltage Temperature Sensors", "Rev. E, August 2008"),
    ),
)
