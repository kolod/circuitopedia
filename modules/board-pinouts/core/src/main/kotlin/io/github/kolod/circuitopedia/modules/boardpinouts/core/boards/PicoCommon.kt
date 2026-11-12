package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardFunction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardLayout
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Direction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.FunctionKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Header
import io.github.kolod.circuitopedia.modules.boardpinouts.core.HeaderPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Side
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SignalSlot
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ViewSide
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max
import io.github.kolod.circuitopedia.modules.boardpinouts.core.range

// The 40-pin layout shared by the Pico W and Pico 2 W. Both datasheets (Figure 2, "The pinout of the
// Pico W Rev3 board" / "Pico 2 W") number pins 1-20 down the left side and 21-40 up the right side,
// seen from the top with the USB connector at the top, and list the seven non-GPIO pins of §2.1:
// VBUS (40), VSYS (39), 3V3_EN (37), 3V3 (36), ADC_VREF (35), AGND (33) and RUN (30).

internal val picoLayout = BoardLayout(
    width = 8.3f,
    height = 21f,
    headers = listOf(
        Header("left", 0.65f, 1f, Direction.Down, 20),
        Header("right", 7.65f, 20f, Direction.Up, 20),
    ),
    usb = Side.Top,
    view = ViewSide.TopUsbUp,
)

/** Physical pin → label and chip pin (null for power, filled from [picoPower]). */
private val picoPinout = listOf(
    "GP0", "GP1", "GND", "GP2", "GP3", "GP4", "GP5", "GND", "GP6", "GP7",
    "GP8", "GP9", "GND", "GP10", "GP11", "GP12", "GP13", "GND", "GP14", "GP15",
    "GP16", "GP17", "GND", "GP18", "GP19", "GP20", "GP21", "GND", "GP22", "RUN",
    "GP26", "GP27", "AGND", "GP28", "ADC_VREF", "3V3(OUT)", "3V3_EN", "GND", "VSYS", "VBUS",
)

/** The power pins, with the ranges both board datasheets give in §2.1 (VSYS 1.8V to 5.5V, VBUS nominally 5V,
 *  3V3 load recommended under 300mA). [adcVref] comes from the chip datasheet's ADC supply section. */
internal fun picoPower(adcVref: Value) = mapOf(
    "GND" to PowerPin(PowerRole.Ground),
    "AGND" to PowerPin(PowerRole.Ground),
    "RUN" to PowerPin(PowerRole.Run),
    "3V3_EN" to PowerPin(PowerRole.Enable),
    "ADC_VREF" to PowerPin(PowerRole.AdcVref, adcVref),
    "3V3(OUT)" to PowerPin(PowerRole.Supply3V3, Value(typ = "3.3", unit = "V"), current = max("300", "mA")),
    "VSYS" to PowerPin(PowerRole.Vsys, range("1.8", "5.5", "V")),
    "VBUS" to PowerPin(PowerRole.Vbus, Value(typ = "5", unit = "V"), condition = ConditionKey.Nominal),
)

internal fun picoPins(power: Map<String, PowerPin>): List<HeaderPin> = picoPinout.mapIndexed { i, label ->
    val number = i + 1
    val header = if (number <= 20) "left" else "right"
    val position = if (number <= 20) number else number - 20
    val pwr = power[label]
    if (pwr != null) {
        power(header, position, label, pwr, number = "$number")
    } else {
        gpio(header, position, label, label, number = "$number")
    }
}

/** The GPIOs on the 40-pin header, in GPIO order. */
internal val picoGpios = (0..22).map { "GP$it" } + listOf("GP26", "GP27", "GP28")

/**
 * Builds per-signal placements from a chip's GPIO function table: one placement per peripheral
 * instance, each signal listing every header GPIO that can carry it (research R3). [table] maps a
 * GPIO to its functions as printed ("SPI0 RX", "UART1 CTS", "PWM3 A").
 */
internal fun rpFunctions(table: Map<String, List<String>>, source: SourceRef, adc: Map<String, String>): List<BoardFunction> {
    // instance -> signal -> candidate pins, in table order
    val instances = linkedMapOf<String, LinkedHashMap<String, MutableList<String>>>()
    table.forEach { (pin, functions) ->
        functions.forEach { function ->
            val (instance, signal) = function.split(' ', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            instances.getOrPut(instance) { linkedMapOf() }.getOrPut(signal) { mutableListOf() } += pin
        }
    }
    fun kind(instance: String) = when {
        instance.startsWith("UART") -> FunctionKind.Uart
        instance.startsWith("I2C") -> FunctionKind.I2c
        instance.startsWith("SPI") -> FunctionKind.Spi
        instance.startsWith("PWM") -> FunctionKind.Pwm
        else -> null
    }
    fun required(kind: FunctionKind, signal: String) = when (kind) {
        FunctionKind.Uart -> signal == "TX" || signal == "RX"
        FunctionKind.I2c -> true
        FunctionKind.Spi -> signal == "SCK" || signal == "TX"
        else -> false
    }
    val byKind = linkedMapOf<FunctionKind, MutableList<io.github.kolod.circuitopedia.modules.boardpinouts.core.Placement>>()
    instances.forEach { (instance, signals) ->
        val kind = kind(instance) ?: return@forEach
        val slots = signals.map { (signal, pins) -> SignalSlot(signal, pins(*pins.toTypedArray()), required(kind, signal)) }
            .sortedBy { if (it.required) 0 else 1 }
        byKind.getOrPut(kind) { mutableListOf() } += placement(instance.lowercase(), instance, source, *slots.toTypedArray())
    }
    val adcPlacement = placement("adc", "ADC", source, *adc.map { (pin, channel) -> opt(channel, pin) }.toTypedArray())
    return listOf(FunctionKind.Uart, FunctionKind.I2c, FunctionKind.Spi, FunctionKind.Pwm).mapNotNull { kind ->
        byKind[kind]?.let { BoardFunction(kind, it) }
    } + BoardFunction(FunctionKind.Adc, listOf(adcPlacement))
}

/** Functions outside the planner (PIO, clocks, USB power control), per pin, as printed. */
internal fun rpOthers(table: Map<String, List<String>>): Map<io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin, List<String>> =
    table.mapNotNull { (pin, functions) ->
        val rest = functions.filter { f -> listOf("UART", "I2C", "SPI", "PWM").none { f.startsWith(it) } }
        if (rest.isEmpty()) null else io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin(pin) to rest
    }.toMap()
