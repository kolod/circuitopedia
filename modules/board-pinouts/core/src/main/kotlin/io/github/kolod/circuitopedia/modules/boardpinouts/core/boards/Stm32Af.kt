package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardFunction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.FunctionKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Placement
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PlacementId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ResourceId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SignalSlot
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef

// Per-signal placements from an STM32F4 "Alternate function mapping" table: each cell is printed as
// PERIPHERAL_SIGNAL, several joined by "/" ("SPI1_NSS/I2S1_WS"). SPIx and I2Sx are one peripheral,
// so their placements share a resource and cannot both be used.

private data class Signal(val instance: String, val signal: String)

private fun parse(cell: String): List<Signal> = cell.split('/').mapNotNull { part ->
    val i = part.indexOf('_')
    if (i <= 0) null else Signal(part.substring(0, i), part.substring(i + 1))
}

private fun kindOf(s: Signal): FunctionKind? = when {
    s.instance.startsWith("USART") -> FunctionKind.Uart.takeIf { s.signal in setOf("TX", "RX", "CTS", "RTS", "CK") }
    s.instance.startsWith("I2C") -> FunctionKind.I2c.takeIf { s.signal in setOf("SCL", "SDA", "SMBA") }
    s.instance.startsWith("SPI") -> FunctionKind.Spi.takeIf { s.signal in setOf("SCK", "MOSI", "MISO", "NSS") }
    s.instance.matches(Regex("I2S\\d")) -> FunctionKind.I2s.takeIf { s.signal in setOf("CK", "SD", "WS", "MCK") }
    s.instance.startsWith("TIM") -> FunctionKind.Pwm.takeIf { s.signal.matches(Regex("CH\\dN?")) }
    s.instance == "USB" -> FunctionKind.Usb.takeIf { s.signal in setOf("FS_DM", "FS_DP", "FS_VBUS", "FS_ID", "FS_SOF") }
    else -> null
}

private fun required(kind: FunctionKind, signal: String) = when (kind) {
    FunctionKind.Uart -> signal == "TX" || signal == "RX"
    FunctionKind.I2c -> signal == "SCL" || signal == "SDA"
    FunctionKind.Spi -> signal == "SCK" || signal == "MOSI"
    FunctionKind.I2s -> signal == "CK" || signal == "SD" || signal == "WS"
    FunctionKind.Usb -> signal == "FS_DM" || signal == "FS_DP"
    else -> false
}

private val kindOrder = listOf(
    FunctionKind.Uart, FunctionKind.I2c, FunctionKind.Spi, FunctionKind.I2s, FunctionKind.Adc, FunctionKind.Pwm, FunctionKind.Usb,
)

/** [table] maps a header pin to its alternate function cells as printed; [adc] maps a pin to its ADC channel. */
internal fun stm32f4Functions(table: Map<String, List<String>>, source: SourceRef, adc: Map<String, String>, adcSource: SourceRef): List<BoardFunction> {
    val instances = linkedMapOf<Pair<FunctionKind, String>, LinkedHashMap<String, MutableList<String>>>()
    table.forEach { (pin, cells) ->
        cells.flatMap(::parse).forEach { s ->
            val kind = kindOf(s) ?: return@forEach
            instances.getOrPut(kind to s.instance) { linkedMapOf() }.getOrPut(s.signal) { mutableListOf() } += pin
        }
    }
    val placements = instances.map { (key, signals) ->
        val (kind, instance) = key
        val slots = signals.map { (signal, pins) -> SignalSlot(signal, pins(*pins.toTypedArray()), required(kind, signal)) }
            .sortedBy { if (it.required) 0 else 1 }
        val shared = when (kind) {
            FunctionKind.Spi -> setOf("spi${instance.removePrefix("SPI")}")
            FunctionKind.I2s -> setOf("spi${instance.removePrefix("I2S")}")
            else -> emptySet()
        }
        val id = if (kind == FunctionKind.Usb) "usb-otg-fs" else instance.lowercase()
        kind to Placement(
            PlacementId(id),
            if (kind == FunctionKind.Usb) "USB OTG FS" else instance,
            slots, source, resources = shared.map(::ResourceId).toSet(),
        )
    }
    val adcPlacement = placement("adc1", "ADC1", adcSource, *adc.map { (pin, channel) -> opt(channel, pin) }.toTypedArray())
    val byKind = placements.groupBy({ it.first }, { it.second }) + (FunctionKind.Adc to listOf(adcPlacement))
    return kindOrder.mapNotNull { kind -> byKind[kind]?.let { BoardFunction(kind, it) } }
}

/** Cells the planner does not cover (timers' ETR/BKIN, MCO, SDIO, JTAG/SWD, EVENTOUT...), as printed. */
internal fun stm32Others(table: Map<String, List<String>>): Map<ChipPin, List<String>> = table.mapNotNull { (pin, cells) ->
    val rest = cells.flatMap { cell ->
        val parts = cell.split('/')
        val signals = parse(cell)
        if (signals.size == parts.size && signals.all { kindOf(it) != null }) emptyList() else parts.filter { part ->
            parse(part).singleOrNull()?.let { kindOf(it) } == null
        }
    }
    if (rest.isEmpty()) null else ChipPin(pin) to rest
}.toMap()
