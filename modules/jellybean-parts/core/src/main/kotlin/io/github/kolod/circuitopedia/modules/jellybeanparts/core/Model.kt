package io.github.kolod.circuitopedia.modules.jellybeanparts.core

/** Categories in spec FR-002 order. */
enum class Category {
    OpAmps, Comparators, Timers, BipolarTransistors, Mosfets, Diodes,
    VoltageRegulators, VoltageReferences, Logic, TemperatureSensors, PowerControllers,
}

/** A stable lowercase key, used for string resource names and saved state. */
@JvmInline
value class PartId(val value: String)

/** What a parameter row measures. Each kind has a localized name in the UI. */
enum class ParameterKind {
    // Op-amps, comparators, timer, logic, sensors, controllers
    SupplyVoltage, SupplyCurrent, PowerDissipationTotal, InputOffsetVoltage, InputBiasCurrent,
    GainBandwidth, SlewRate, ResponseTime, OutputSinkCurrent, OutputCurrent, OutputLowVoltage,
    OutputHighVoltage, PropagationDelay, ScaleFactor, OutputAt25C, Accuracy, OperatingTemperature,
    OscillatorFrequency, SwitchCurrent, UvloOn, UvloOff, MaxDutyCycle, PowerEfficiency,
    ConversionEfficiency, OutputResistance,

    // Bipolar transistors
    Vceo, Ic, Pd, Hfe, Ft, VceSat,

    // MOSFETs
    Vds, Id, RdsOn, VgsTh,

    // Diodes
    Vrrm, IfAv, Ifsm, Vf, Trr, Ir,

    // Regulators and references
    OutputVoltage, InputVoltage, Dropout, ReferenceVoltage, AdjustCurrent, CathodeVoltage,
    CathodeCurrent, MinCathodeCurrent,
}

/**
 * A value as printed in the datasheet: exact decimal text, never converted or rounded. [plusMinus]
 * marks values printed with "±" (for example OP07 input offset voltage).
 */
data class Value(
    val min: String? = null,
    val typ: String? = null,
    val max: String? = null,
    val unit: String,
    val plusMinus: Boolean = false,
)

/** One row of the parameter table. [condition] is symbolic and untranslated. */
data class Parameter(val kind: ParameterKind, val value: Value, val condition: String? = null)

/** The common package drawn for an entry, with its pin count. */
enum class Package(val pinCount: Int) {
    To92(3), Sot23(3), Dip8(8), Dip14(14), Do35(2), Do41(2), To126(3), To220(3),
}

/** A pin's function, with the short symbol drawn next to the pin. */
enum class PinFunction(val symbol: String) {
    Collector("C"), Base("B"), Emitter("E"),
    Gate("G"), Drain("D"), Source("S"),
    Anode("A"), Cathode("K"),
    Output("OUT"), InvertingInput("IN−"), NonInvertingInput("IN+"),
    PositiveSupply("V+"), NegativeSupply("V−"), Ground("GND"),
    Input("IN"), Adjust("ADJ"), Reference("REF"),
    Trigger("TRIG"), Threshold("THRES"), Discharge("DISCH"), ControlVoltage("CONT"), Reset("RESET"),
    NoConnect("NC"), OffsetNull("NULL"),
    Compensation("COMP"), VoltageFeedback("VFB"), CurrentSense("ISENSE"), RtCt("RT/CT"),
    ReferenceOutput("VREF"),
    CapPlus("CAP+"), CapMinus("CAP−"), Oscillator("OSC"), LowVoltage("LV"),
    SwitchCollector("SWC"), SwitchEmitter("SWE"), TimingCapacitor("CT"), DriverCollector("DRC"),
    PeakCurrentSense("IPK"), ComparatorInvertingInput("CII"),
}

/** A numbered pin. [channel] is A, B, C or D for multi-channel parts. */
data class Pin(val number: Int, val function: PinFunction, val channel: Char? = null)

/** A member of a family entry (78xx, 1N4001–1N4007, UC384x …) with its distinguishing values. */
data class FamilyMember(val name: String, val values: Map<ParameterKind, Value>)

/** The datasheet the entry's values and pinout were copied from. */
data class Source(val maker: String, val document: String, val revision: String)

data class PartEntry(
    val id: PartId,
    val name: String,
    val category: Category,
    val parameters: List<Parameter>,
    val packageType: Package?,
    val pins: List<Pin>,
    val makerVariesPinout: Boolean = false,
    val members: List<FamilyMember> = emptyList(),
    val equivalent: PartId? = null,
    val source: Source,
)

/** Pins numbered 1…n in order. */
internal fun pins(vararg functions: PinFunction): List<Pin> =
    functions.mapIndexed { i, f -> Pin(i + 1, f) }

internal fun p(kind: ParameterKind, value: Value, condition: String? = null) =
    Parameter(kind, value, condition)
