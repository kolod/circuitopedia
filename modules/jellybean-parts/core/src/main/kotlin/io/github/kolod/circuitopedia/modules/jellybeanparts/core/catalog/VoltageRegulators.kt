package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.FamilyMember
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.AdjustCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Dropout
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ReferenceVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyCurrent
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Adjust
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Ground
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Input
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Output
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

private fun member(name: String, min: String, typ: String, max: String, vinMin: String? = null, vinMax: String? = null) =
    FamilyMember(
        name,
        buildMap {
            put(OutputVoltage, Value(min = min, typ = typ, max = max, unit = "V"))
            if (vinMin != null || vinMax != null) put(InputVoltage, Value(min = vinMin, max = vinMax, unit = "V"))
        },
    )

internal val voltageRegulators = listOf(
    // TI SLVS056P: 7.3 Recommended Operating Conditions; 7.5–7.10 Electrical Characteristics
    // (output voltage at TJ = 25°C, IO = 5 mA to 1 A); Pin Functions, KCS (TO-220) package.
    PartEntry(
        id = PartId("78xx"), name = "78xx", category = Category.VoltageRegulators,
        parameters = listOf(
            p(OutputCurrent, Value(max = "1.5", unit = "A")),
            p(Dropout, Value(typ = "2", unit = "V"), "IO = 1 A, TJ = 25°C"),
            p(SupplyCurrent, Value(typ = "4.2", max = "8", unit = "mA"), "bias current, µA7805, TJ = 25°C"),
        ),
        packageType = Package.To220, pins = pins(Input, Ground, Output),
        members = listOf(
            member("7805", "4.8", "5", "5.2", "7", "25"),
            member("7808", "7.7", "8", "8.3", "10.5", "25"),
            member("7810", "9.6", "10", "10.4", "12.5", "28"),
            member("7812", "11.5", "12", "12.5", "14.5", "30"),
            member("7815", "14.4", "15", "15.6", "17.5", "30"),
            member("7824", "23", "24", "25", "27", "38"),
        ),
        source = Source("Texas Instruments", "SLVS056 — µA7800 Series Positive-Voltage Regulators", "SLVS056P, revised January 2015"),
    ),
    // onsemi MC7900/D: Maximum Ratings; Electrical Characteristics of MC7905C, MC7912C and MC7915C
    // (output voltage at TJ = 25°C); TO-220 pin assignment.
    PartEntry(
        id = PartId("79xx"), name = "79xx", category = Category.VoltageRegulators,
        parameters = listOf(
            p(InputVoltage, Value(max = "-35", unit = "V"), "−5.0 V ≥ VO ≥ −18 V"),
            p(Dropout, Value(typ = "1.3", unit = "V"), "MC7905, IO = 1.0 A, TJ = 25°C"),
            p(SupplyCurrent, Value(typ = "4.3", max = "8.0", unit = "mA"), "input bias current, MC7905, TJ = 25°C"),
        ),
        packageType = Package.To220, pins = pins(Ground, Input, Output),
        members = listOf(
            member("7905", "-4.8", "-5.0", "-5.2"),
            member("7912", "-11.5", "-12", "-12.5"),
            member("7915", "-14.4", "-15", "-15.6"),
            member("7924", "-23", "-24", "-25"),
        ),
        source = Source("onsemi", "MC7900/D — Negative Voltage Regulators 1.0 A", "Rev. 21, October 2021"),
    ),
    // TI SLVS044Z: 6.3 Recommended Operating Conditions; 6.6 Electrical Characteristics
    // (TJ = 25°C, VIN − VOUT = 5 V, IOUT = 10 mA); Pin Functions, KCS (TO-220, legacy chip).
    PartEntry(
        id = PartId("lm317"), name = "LM317", category = Category.VoltageRegulators,
        parameters = listOf(
            p(ReferenceVoltage, Value(min = "1.2", typ = "1.25", max = "1.3", unit = "V"), "3 V ≤ (VIN − VOUT) ≤ 40 V, 10 mA ≤ IOUT ≤ 1500 mA, PD ≤ 20 W"),
            p(OutputVoltage, Value(min = "1.25", max = "37", unit = "V")),
            p(InputVoltage, Value(min = "3", max = "40", unit = "V"), "input-to-output differential"),
            p(OutputCurrent, Value(min = "0.01", max = "1.5", unit = "A")),
            p(AdjustCurrent, Value(typ = "50", max = "100", unit = "µA")),
        ),
        packageType = Package.To220, pins = pins(Adjust, Output, Input),
        source = Source("Texas Instruments", "SLVS044 — LM317 3-Pin Adjustable Regulator", "SLVS044Z, revised April 2025"),
    ),
    // TI SLVS047L: 7.3 Recommended Operating Conditions; 7.5 Electrical Characteristics, LM337 column
    // (|VI − VO| = 5 V, IO = 0.5 A); Pin Functions, KCS (TO-220).
    PartEntry(
        id = PartId("lm337"), name = "LM337", category = Category.VoltageRegulators,
        parameters = listOf(
            p(ReferenceVoltage, Value(min = "-1.213", typ = "-1.25", max = "-1.287", unit = "V"), "VI − VO = −3 V to −40 V, IO = 10 mA to 1.5 A, TJ = 25°C"),
            p(OutputCurrent, Value(min = "10", max = "1500", unit = "mA"), "|VI − VO| ≤ 40 V, P ≤ 15 W"),
            p(OutputCurrent, Value(min = "1.5", typ = "2.2", unit = "A"), "peak, |VI − VO| ≤ 15 V"),
            p(AdjustCurrent, Value(typ = "65", max = "100", unit = "µA")),
        ),
        packageType = Package.To220, pins = pins(Adjust, Input, Output),
        source = Source("Texas Instruments", "SLVS047 — LM337 3-Terminal Adjustable Regulator", "SLVS047L, revised January 2015"),
    ),
)
