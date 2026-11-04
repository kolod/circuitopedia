package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.FamilyMember
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.IfAv
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Ifsm
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Ir
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Pd
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Trr
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vf
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vrrm
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Anode
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Cathode
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.NoConnect
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Source
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.p
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.pins

/** Axial diodes are drawn with the cathode band on the right: pin 1 anode, pin 2 cathode. */
private val axial = pins(Anode, Cathode)

// Where a PDF's text layer drops the "µ" glyph of a unit, the unit is recorded as printed on the
// page (µA) and the row is flagged for the visual check in checklists/datasheet-audit.md.
internal val diodes = listOf(
    // Vishay 81857: Absolute Maximum Ratings, Electrical Characteristics (Tamb = 25°C).
    PartEntry(
        id = PartId("1n4148"), name = "1N4148", category = Category.Diodes,
        parameters = listOf(
            p(Vrrm, Value(max = "100", unit = "V")),
            p(IfAv, Value(max = "150", unit = "mA"), "VR = 0"),
            p(Vf, Value(max = "1", unit = "V"), "IF = 10 mA"),
            p(Trr, Value(max = "4", unit = "ns"), "IF = 10 mA, VR = 6 V, iR = 0.1 × IR, RL = 100 Ω"),
            p(Ir, Value(max = "25", unit = "nA"), "VR = 20 V"),
        ),
        packageType = Package.Do35, pins = axial,
        source = Source("Vishay", "81857 — 1N4148 Small Signal Fast Switching Diode", "Rev. 1.6, 07-Nov-2024"),
    ),
    // onsemi 1N914/D: Absolute Maximum Ratings, Thermal Characteristics, Electrical Characteristics
    // (TA = 25°C), 1N914 rows.
    PartEntry(
        id = PartId("1n914"), name = "1N914", category = Category.Diodes,
        parameters = listOf(
            p(Vrrm, Value(max = "100", unit = "V")),
            p(IfAv, Value(max = "200", unit = "mA"), "average rectified forward current"),
            p(Vf, Value(max = "1.0", unit = "V"), "IF = 10 mA"),
            p(Trr, Value(max = "4.0", unit = "ns"), "IF = 10 mA, VR = 6.0 V"),
            p(Ir, Value(max = "0.025", unit = "µA"), "VR = 20 V"),
            p(Pd, Value(max = "500", unit = "mW")),
        ),
        packageType = Package.Do35, pins = axial, equivalent = PartId("1n4148"),
        source = Source("onsemi", "1N914/D — Small Signal Diode", "Rev. 6, September 2024"),
    ),
    // Vishay 88503: Maximum Ratings and Electrical Characteristics (TA = 25°C).
    PartEntry(
        id = PartId("1n4001_1n4007"), name = "1N4001–1N4007", category = Category.Diodes,
        parameters = listOf(
            p(IfAv, Value(max = "1.0", unit = "A"), "0.375\" (9.5 mm) lead length, TL = 75°C"),
            p(Ifsm, Value(max = "30", unit = "A"), "8.3 ms single half sine-wave"),
            p(Vf, Value(max = "1.1", unit = "V"), "IF = 1.0 A"),
            p(Ir, Value(max = "5.0", unit = "µA"), "at rated DC blocking voltage, TA = 25°C"),
        ),
        packageType = Package.Do41, pins = axial,
        members = listOf(
            "1N4001" to "50", "1N4002" to "100", "1N4003" to "200", "1N4004" to "400",
            "1N4005" to "600", "1N4006" to "800", "1N4007" to "1000",
        ).map { (name, v) -> FamilyMember(name, mapOf(Vrrm to Value(max = v, unit = "V"))) },
        source = Source("Vishay", "88503 — 1N4001 thru 1N4007 General Purpose Plastic Rectifier", "Revision 29-Apr-2020"),
    ),
    // Vishay 88525: Maximum Ratings and Electrical Characteristics (TA = 25°C), 1N5817 column.
    PartEntry(
        id = PartId("1n5817"), name = "1N5817", category = Category.Diodes,
        parameters = listOf(
            p(Vrrm, Value(max = "20", unit = "V")),
            p(IfAv, Value(max = "1.0", unit = "A"), "0.375\" (9.5 mm) lead length, TL = 90°C"),
            p(Ifsm, Value(max = "25", unit = "A"), "8.3 ms single half sine-wave"),
            p(Vf, Value(max = "0.450", unit = "V"), "IF = 1.0 A"),
            p(Vf, Value(max = "0.750", unit = "V"), "IF = 3.1 A"),
            p(Ir, Value(max = "1.0", unit = "mA"), "at rated DC blocking voltage, TA = 25°C"),
        ),
        packageType = Package.Do41, pins = axial,
        source = Source("Vishay", "88525 — 1N5817, 1N5818, 1N5819 Schottky Barrier Plastic Rectifier", "Revision 01-Jul-2020"),
    ),
    // Vishay 88525, 1N5819 column.
    PartEntry(
        id = PartId("1n5819"), name = "1N5819", category = Category.Diodes,
        parameters = listOf(
            p(Vrrm, Value(max = "40", unit = "V")),
            p(IfAv, Value(max = "1.0", unit = "A"), "0.375\" (9.5 mm) lead length, TL = 90°C"),
            p(Ifsm, Value(max = "25", unit = "A"), "8.3 ms single half sine-wave"),
            p(Vf, Value(max = "0.600", unit = "V"), "IF = 1.0 A"),
            p(Vf, Value(max = "0.900", unit = "V"), "IF = 3.1 A"),
            p(Ir, Value(max = "1.0", unit = "mA"), "at rated DC blocking voltage, TA = 25°C"),
        ),
        packageType = Package.Do41, pins = axial,
        source = Source("Vishay", "88525 — 1N5817, 1N5818, 1N5819 Schottky Barrier Plastic Rectifier", "Revision 01-Jul-2020"),
    ),
    // onsemi BAT54LT1/D: Maximum Ratings, Electrical Characteristics (TA = 25°C); SOT-23 Style 8.
    PartEntry(
        id = PartId("bat54"), name = "BAT54", category = Category.Diodes,
        parameters = listOf(
            p(Vrrm, Value(max = "30", unit = "V"), "reverse voltage VR"),
            p(IfAv, Value(max = "200", unit = "mA"), "forward current (DC)"),
            p(Vf, Value(typ = "0.29", max = "0.32", unit = "V"), "IF = 1.0 mA"),
            p(Vf, Value(typ = "0.35", max = "0.40", unit = "V"), "IF = 10 mA"),
            p(Trr, Value(max = "5.0", unit = "ns"), "IF = IR = 10 mA, IR(REC) = 1.0 mA"),
            p(Ir, Value(typ = "0.2", max = "2.0", unit = "µA"), "VR = 25 V"),
        ),
        packageType = Package.Sot23, pins = pins(Anode, NoConnect, Cathode),
        source = Source("onsemi", "BAT54LT1/D — Schottky Barrier Diodes", "Rev. 18, November 2024"),
    ),
    // Vishay 85754: Absolute Maximum Ratings and Electrical Characteristics (Tamb = 25°C).
    PartEntry(
        id = PartId("sd103"), name = "SD103", category = Category.Diodes,
        parameters = listOf(
            p(Vrrm, Value(max = "40", unit = "V"), "SD103A, peak inverse voltage"),
            p(Vrrm, Value(max = "30", unit = "V"), "SD103B, peak inverse voltage"),
            p(Vrrm, Value(max = "20", unit = "V"), "SD103C, peak inverse voltage"),
            p(IfAv, Value(max = "200", unit = "mA"), "forward continuous current"),
            p(Vf, Value(max = "370", unit = "mV"), "IF = 20 mA"),
            p(Vf, Value(max = "600", unit = "mV"), "IF = 200 mA"),
            p(Trr, Value(typ = "10", unit = "ns"), "IF = IR = 50 mA to 200 mA, recover to 0.1 IR"),
            p(Ir, Value(max = "5", unit = "µA"), "SD103A at VR = 30 V, SD103B at 20 V, SD103C at 10 V"),
        ),
        packageType = Package.Do35, pins = axial,
        source = Source("Vishay", "85754 — SD103A, SD103B, SD103C Small Signal Schottky Diodes", "Rev. 2.2, 08-Nov-2024"),
    ),
)
