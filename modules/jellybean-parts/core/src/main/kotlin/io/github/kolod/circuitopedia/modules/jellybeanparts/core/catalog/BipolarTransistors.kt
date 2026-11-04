package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Ft
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Hfe
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Ic
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Pd
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.VceSat
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vceo
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Base
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Collector
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Emitter
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

private val ebc = pins(Emitter, Base, Collector)

// TO-92 bipolar transistors are made in EBC and CBE orders by different makers (research R3), so
// all of them carry the maker warning.
internal val bipolarTransistors = listOf(
    // onsemi 2N3904/D: Maximum Ratings, Thermal Characteristics, Electrical Characteristics (TA = 25°C).
    PartEntry(
        id = PartId("2n3904"), name = "2N3904", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "40", unit = "V")),
            p(Ic, Value(max = "200", unit = "mA"), "continuous"),
            p(Pd, Value(max = "625", unit = "mW"), "TA = 25°C"),
            p(Hfe, Value(min = "100", max = "300", unit = "—"), "IC = 10 mA, VCE = 1.0 V"),
            p(Ft, Value(min = "300", unit = "MHz"), "IC = 10 mA, VCE = 20 V, f = 100 MHz"),
            p(VceSat, Value(max = "0.2", unit = "V"), "IC = 10 mA, IB = 1.0 mA"),
        ),
        packageType = Package.To92, pins = ebc, makerVariesPinout = true,
        source = Source("onsemi", "2N3904/D — NPN General-Purpose Amplifier", "Rev. 3, October 2024"),
    ),
    // onsemi 2N3906/D (Fairchild format): Absolute Maximum Ratings, Thermal Characteristics (2N3906
    // column), Electrical Characteristics (TA = 25°C).
    PartEntry(
        id = PartId("2n3906"), name = "2N3906", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "-40", unit = "V")),
            p(Ic, Value(max = "-200", unit = "mA"), "continuous"),
            p(Pd, Value(max = "625", unit = "mW")),
            p(Hfe, Value(min = "100", max = "300", unit = "—"), "IC = −10 mA, VCE = −1.0 V"),
            p(Ft, Value(min = "250", unit = "MHz"), "IC = −10 mA, VCE = −20 V, f = 100 MHz"),
            p(VceSat, Value(max = "-0.25", unit = "V"), "IC = −10 mA, IB = −1.0 mA"),
        ),
        packageType = Package.To92, pins = ebc, makerVariesPinout = true,
        source = Source("onsemi", "2N3906/D — 2N3906, MMBT3906, PZT3906 PNP General-Purpose Amplifier", "Rev. 1.2.2"),
    ),
    // onsemi PN2222A/D (Fairchild format): Absolute Maximum Ratings, Thermal Characteristics,
    // Electrical Characteristics (TA = 25°C).
    PartEntry(
        id = PartId("pn2222a"), name = "2N2222 (PN2222A)", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "40", unit = "V")),
            p(Ic, Value(max = "1.0", unit = "A")),
            p(Pd, Value(max = "625", unit = "mW")),
            p(Hfe, Value(min = "100", max = "300", unit = "—"), "IC = 150 mA, VCE = 10 V"),
            p(Ft, Value(min = "300", unit = "MHz"), "IC = 20 mA, VCE = 20 V, f = 100 MHz"),
            p(VceSat, Value(max = "0.3", unit = "V"), "IC = 150 mA, IB = 15 mA"),
        ),
        packageType = Package.To92, pins = ebc, makerVariesPinout = true,
        source = Source("onsemi", "PN2222A/D — NPN General-Purpose Amplifier", "Rev. 1.1.0"),
    ),
    // onsemi PN2907A/D: Maximum Ratings, Electrical Characteristics (TA = 25°C).
    PartEntry(
        id = PartId("pn2907a"), name = "PN2907 (2N2907)", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "-60", unit = "V")),
            p(Ic, Value(max = "-600", unit = "mA"), "continuous"),
            p(Pd, Value(max = "625", unit = "mW"), "TA = 25°C"),
            p(Hfe, Value(min = "100", max = "300", unit = "—"), "IC = −150 mA, VCE = −10 V"),
            p(Ft, Value(min = "200", unit = "MHz"), "IC = −50 mA, VCE = −20 V, f = 100 MHz"),
            p(VceSat, Value(max = "-0.4", unit = "V"), "IC = −150 mA, IB = −15 mA"),
        ),
        packageType = Package.To92, pins = ebc, makerVariesPinout = true,
        source = Source("onsemi", "PN2907A/D — General Purpose Transistor, PNP", "Rev. 3, May 2007"),
    ),
    // onsemi BC546/D: Maximum Ratings and Electrical Characteristics, BC548 rows (TA = 25°C);
    // TO-92 Style 17.
    PartEntry(
        id = PartId("bc548"), name = "BC548", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "30", unit = "V")),
            p(Ic, Value(max = "100", unit = "mA"), "continuous"),
            p(Pd, Value(max = "625", unit = "mW"), "TA = 25°C"),
            p(Hfe, Value(min = "110", max = "800", unit = "—"), "IC = 2.0 mA, VCE = 5.0 V"),
            p(Ft, Value(min = "150", typ = "300", unit = "MHz"), "IC = 10 mA, VCE = 5.0 V, f = 100 MHz"),
            p(VceSat, Value(typ = "0.09", max = "0.25", unit = "V"), "IC = 10 mA, IB = 0.5 mA"),
        ),
        packageType = Package.To92, pins = pins(Collector, Base, Emitter), makerVariesPinout = true,
        source = Source("onsemi", "BC546/D — Amplifier Transistors, NPN Silicon", "Rev. 8, August 2021"),
    ),
    // onsemi BD135/D: Maximum Ratings and Electrical Characteristics, BD139G rows (TC = 25°C);
    // TO-225 Style 1, the same outline as TO-126.
    PartEntry(
        id = PartId("bd139"), name = "BD139", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "80", unit = "V")),
            p(Ic, Value(max = "1.5", unit = "A")),
            p(Pd, Value(max = "12.5", unit = "W"), "TC = 25°C"),
            p(Pd, Value(max = "1.25", unit = "W"), "TA = 25°C"),
            p(Hfe, Value(min = "40", max = "250", unit = "—"), "IC = 0.15 A, VCE = 2 V"),
            p(VceSat, Value(max = "0.5", unit = "V"), "IC = 0.5 A, IB = 0.05 A"),
        ),
        packageType = Package.To126, pins = pins(Emitter, Collector, Base),
        source = Source("onsemi", "BD135/D — Plastic Medium-Power Silicon NPN Transistors", "Rev. 18, June 2024"),
    ),
    // onsemi BD136/D: Absolute Maximum Ratings and Electrical Characteristics, BD140 rows
    // (TC = 25°C); TO-126.
    PartEntry(
        id = PartId("bd140"), name = "BD140", category = Category.BipolarTransistors,
        parameters = listOf(
            p(Vceo, Value(max = "-80", unit = "V")),
            p(Ic, Value(max = "-1.5", unit = "A"), "DC"),
            p(Pd, Value(max = "12.5", unit = "W"), "TC = 25°C"),
            p(Pd, Value(max = "1.25", unit = "W"), "TA = 25°C"),
            p(Hfe, Value(min = "63", max = "160", unit = "—"), "BD14010, VCE = −2 V, IC = −150 mA"),
            p(Hfe, Value(min = "100", max = "250", unit = "—"), "BD14016, VCE = −2 V, IC = −150 mA"),
            p(VceSat, Value(max = "-0.5", unit = "V"), "IC = 500 mA, IB = 50 mA"),
        ),
        packageType = Package.To126, pins = pins(Emitter, Collector, Base),
        source = Source("onsemi", "BD136/D — BD136, BD138, BD140 PNP Epitaxial Silicon Transistor", "Rev. 2, May 2024"),
    ),
)
