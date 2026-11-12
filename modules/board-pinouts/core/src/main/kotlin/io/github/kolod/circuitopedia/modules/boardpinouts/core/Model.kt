package io.github.kolod.circuitopedia.modules.boardpinouts.core

// The board pinout model (specs/007-board-pinouts/data-model.md). It holds stable keys only: every
// word shown to the user is a string resource in the Android module, looked up from these keys.

@JvmInline value class BoardId(val value: String)

/** A chip pin by its printed name: "PB7", "GP2", "GPIO2", "PD3". */
@JvmInline value class ChipPin(val name: String)

@JvmInline value class PlacementId(val value: String)

/** Something two placements cannot both use besides a pin, for example a shared SRAM. */
@JvmInline value class ResourceId(val value: String)

/** A value as printed in its source: each bound is decimal text or null, at least one is set. */
data class Value(val min: String? = null, val typ: String? = null, val max: String? = null, val unit: String) {
    init {
        require(min != null || typ != null || max != null) { "a value needs a bound" }
        listOfNotNull(min, typ, max).forEach { require(it.toBigDecimalOrNull() != null) { "not a decimal: $it" } }
    }
}

fun max(value: String, unit: String) = Value(max = value, unit = unit)
fun range(min: String, max: String, unit: String) = Value(min = min, max = max, unit = unit)

data class Source(val maker: String, val document: String, val title: String, val revision: String)

/** A table or section of a source, for the audit: "DS5319 Table 5". */
data class SourceRef(val document: String, val section: String)

// Layout -----------------------------------------------------------------------------------------

enum class Direction { Down, Up, Right }

enum class Side { Top, Bottom, Left, Right }

/** The side a board is drawn from; each has a localized note. */
enum class ViewSide { TopUsbUp, TopRotatedUsbUp, TopRotatedUsbDown }

/**
 * One straight row of pins. Coordinates are in header pitch units (2.54mm = 1), from the board's
 * top-left corner as drawn; pin 1 of the row is at [x], [y], the next ones follow [direction].
 */
data class Header(val id: String, val x: Float, val y: Float, val direction: Direction, val count: Int)

data class BoardLayout(
    val width: Float,
    val height: Float,
    val headers: List<Header>,
    val usb: Side,
    val view: ViewSide,
)

// Pins -------------------------------------------------------------------------------------------

enum class PowerRole { Ground, NotConnected, Supply3V3, Supply5V, Vin, Vsys, Vbus, Vbat, Aref, AdcVref, Ioref, Reset, Run, Enable }

/** [condition] qualifies the range, for example "up to AVCC" for an ADC reference. */
data class PowerPin(
    val role: PowerRole,
    val range: Value? = null,
    val current: Value? = null,
    val condition: ConditionKey? = null,
)

data class HeaderPin(
    val header: String,
    val position: Int,
    val label: String,
    val number: String? = null,
    val pin: ChipPin? = null,
    val power: PowerPin? = null,
)

enum class LimitKind {
    FiveVoltTolerant, ThreeVoltOnly, Analog, BoardUse, InputOnly, OpenDrain, BoardPull, BootRole,
    CurrentLimit, Erratum,
}

enum class BoardUse { Usb, UsbSerial, Crystal, Led, Button, Debug, IdEeprom, Flash }

enum class BootEffect {
    /** Raspberry Pi: the firmware probes a HAT ID EEPROM on these pins at boot. */
    HatEepromProbe,
    /** STM32: the level at reset selects the boot memory together with BOOT0. */
    BootMemorySelect,
    /** Debug pins are in their debug function after reset until software releases them. */
    DebugAfterReset,
}

enum class ConditionKey {
    NotAnalogMode, IovddPowered, PullUp, PullDown, AbsoluteMaximum, UpToAvcc, ConnectedTo5V, Nominal, FiveVoltSupply, DriveSetting, SinkOnly,
}

data class Limit(
    val kind: LimitKind,
    val value: Value? = null,
    val condition: ConditionKey? = null,
    val use: BoardUse? = null,
    val effect: BootEffect? = null,
    val erratum: String? = null,
)

enum class GroupKey {
    /** Sum of the currents the listed pins source (drive high). */
    SourceSum,
    /** Sum of the currents the listed pins sink (drive low). */
    SinkSum,
    /** Current through one supply or ground pin of the chip. */
    SupplyPin,
    /** Sum of the currents of all the listed pins together. */
    TotalSum,
}

data class GroupLimit(val pins: Set<ChipPin>, val value: Value, val key: GroupKey)

// Functions and placements -----------------------------------------------------------------------

enum class FunctionKind { Uart, I2c, Spi, Pwm, Adc, Can, I2s, Usb }

data class SignalSlot(val signal: String, val candidates: List<ChipPin>, val required: Boolean = true)

/** Software or wiring facts shown with a placement; each has a localized text. */
enum class NoteKey {
    NeedsOverlay, BluetoothUsesUart0, Timer0UsedByMillis, SsMustBeOutput, JtagMustBeReleased,
    UsbSerialShared, AnalogAudioUsesPwm, UsbPullupOnBoard,
}

data class Placement(
    val id: PlacementId,
    val instance: String,
    val slots: List<SignalSlot>,
    val source: SourceRef,
    val movesAsGroup: Boolean = false,
    val resources: Set<ResourceId> = emptySet(),
    val notes: List<NoteKey> = emptyList(),
) {
    fun slot(signal: String): SignalSlot? = slots.firstOrNull { it.signal == signal }
}

data class BoardFunction(val kind: FunctionKind, val placements: List<Placement>)

/** Board-level facts shown under the drawing; each has a localized text. */
enum class BoardNote {
    PicoVariant, Pico2Variant, F401Variant, ReferenceSchematic, ClonesMayDiffer, NoFiveVoltGpio,
    UsbPullupValue, FiveVoltRailUnprotected,
}

data class Board(
    val id: BoardId,
    val name: String,
    val chip: String,
    val layout: BoardLayout,
    val pins: List<HeaderPin>,
    val limits: Map<ChipPin, List<Limit>>,
    val groupLimits: List<GroupLimit>,
    val functions: List<BoardFunction>,
    val notes: List<BoardNote>,
    val sources: List<Source>,
    /** Functions the planner does not cover, as printed: "PCINT5", "INT0", "AIN0", "PIO0". */
    val otherFunctions: Map<ChipPin, List<String>> = emptyMap(),
) {
    val placements: List<Placement> get() = functions.flatMap { it.placements }
    val chipPins: Set<ChipPin> get() = pins.mapNotNull { it.pin }.toSet()
}
