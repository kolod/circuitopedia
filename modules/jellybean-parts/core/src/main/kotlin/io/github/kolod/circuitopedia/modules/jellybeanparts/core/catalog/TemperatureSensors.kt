package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Accuracy
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OperatingTemperature
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ScaleFactor
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
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
)
