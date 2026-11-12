package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardFunction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardLayout
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardNote
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardUse
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BootEffect
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Direction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.FunctionKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Header
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.NoteKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Side
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SignalSlot
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Source
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ViewSide
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max
import io.github.kolod.circuitopedia.modules.boardpinouts.core.range

// STM32F103C8T6 "Blue Pill". The board has no official maker; it follows the original vcc-gnd
// schematic ("STM32F103C8T6核心板原理图", 2016/1/26) published by stm32-base.org. Sources:
// - the schematic: P3 (VBAT, PC13, PC14, PC15, PA0-PA7, PB0, PB1, PB10, PB11, NRST, VCC3V3, GND, GND),
//   P4 (VCC3V3, GND, 5V, PB9-PB3, PA15, PA12-PA8, PB15-PB12), SWD P2 (VCC3V3, SWDIO, SWCLK, GND); LED D2
//   from VCC3V3 through 510Ω to PC13; 32.768kHz crystal on PC14/PC15; USB D-/D+ on PA11/PA12 with
//   R10 4.7kΩ from PA12 to 5V; BOOT0/BOOT1 (PB2) only on the jumper block, not on the headers;
// - stm32-base.org "STM32F103C8T6 Blue Pill" page (fetched 2026-11-12): the +5V pins connect straight
//   to the USB connector without protection; the D+ resistor is 10kΩ or 4.7kΩ instead of 1.5kΩ;
// - ST DS5319 "STM32F103x8 STM32F103xB", Rev 20 (28-Jul-2025): Table 5 (LQFP48 pins, FT column: PA0-PA7,
//   PB0, PB1, PB5 and PC13-PC15 are not FT; main function after reset JTMS/SWDIO, JTCK/SWCLK, JTDI,
//   JTDO, JNTRST on PA13, PA14, PA15, PB3, PB4; ADC12_IN0..9; note 5: PC13-PC15 sink only 3mA, max
//   2MHz, not a current source), Table 7 (25mA per I/O), Table 9 (VBAT 1.8V to 3.6V);
// - ST RM0008 Rev 21: Tables 34, 43-46 and 52-56 (AFIO remaps; options needing pins absent from the
//   48-pin package are left out), §23.2 (USB and CAN share a 512-byte SRAM and cannot be used
//   concurrently);
// - ST ES096 Rev 15 (30-Mar-2022) §2.3: GPIO and AFIO errata. 2.3.7 ("do not use SPI1 remapped in
//   master mode and I2C1 together") is a planner conflict; the others are pin errata.
// Drawn from the top with USB at the top: P3 on the left (VBAT at the USB end), P4 on the right (PB12
// at the USB end), SWD at the far end.

private val bluePillSources = listOf(
    Source("vcc-gnd (via stm32-base.org)", "bluepill-schematic", "STM32F103C8T6 core board schematic", "2016-01-26"),
    Source("stm32-base.org", "stm32-base-blue-pill", "STM32F103C8T6 Blue Pill board page", "fetched 2026-11-12"),
    Source("STMicroelectronics", "DS5319", "STM32F103x8 STM32F103xB datasheet", "Rev 20, 28-Jul-2025"),
    Source("STMicroelectronics", "RM0008", "STM32F101xx-F107xx reference manual", "Rev 21, 2021"),
    Source("STMicroelectronics", "ES096", "STM32F101x8/B, STM32F102x8/B and STM32F103x8/B errata sheet", "Rev 15, 30-Mar-2022"),
)

private fun rm(table: String) = SourceRef("RM0008", table)
private val table5 = SourceRef("DS5319", "Table 5")
private const val SRAM = "usb-can-sram"
private const val ERRATUM_237 = "es096-2.3.7"

private fun remap(id: String, instance: String, source: SourceRef, vararg slots: SignalSlot, resources: Set<String> = emptySet(), notes: List<NoteKey> = emptyList()) =
    placement(id, instance, source, *slots, group = true, resources = resources, notes = notes)

private val bluePillFunctions = listOf(
    BoardFunction(
        FunctionKind.Uart,
        listOf(
            remap("usart1", "USART1", rm("Table 54"), slot("TX", "PA9"), slot("RX", "PA10"), opt("CK", "PA8"), opt("CTS", "PA11"), opt("RTS", "PA12")),
            remap("usart1-remap", "USART1", rm("Table 54"), slot("TX", "PB6"), slot("RX", "PB7"), opt("CK", "PA8"), opt("CTS", "PA11"), opt("RTS", "PA12")),
            placement("usart2", "USART2", rm("Table 53"), slot("TX", "PA2"), slot("RX", "PA3"), opt("CK", "PA4"), opt("CTS", "PA0"), opt("RTS", "PA1")),
            placement("usart3", "USART3", rm("Table 52"), slot("TX", "PB10"), slot("RX", "PB11"), opt("CK", "PB12"), opt("CTS", "PB13"), opt("RTS", "PB14")),
        ),
    ),
    BoardFunction(
        FunctionKind.I2c,
        listOf(
            remap("i2c1", "I2C1", rm("Table 55"), slot("SCL", "PB6"), slot("SDA", "PB7"), opt("SMBA", "PB5"), resources = setOf(ERRATUM_237)),
            remap("i2c1-remap", "I2C1", rm("Table 55"), slot("SCL", "PB8"), slot("SDA", "PB9"), opt("SMBA", "PB5"), resources = setOf(ERRATUM_237)),
            placement("i2c2", "I2C2", table5, slot("SCL", "PB10"), slot("SDA", "PB11"), opt("SMBA", "PB12")),
        ),
    ),
    BoardFunction(
        FunctionKind.Spi,
        listOf(
            remap("spi1", "SPI1", rm("Table 56"), slot("SCK", "PA5"), slot("MOSI", "PA7"), opt("MISO", "PA6"), opt("NSS", "PA4")),
            remap(
                "spi1-remap", "SPI1", rm("Table 56"), slot("SCK", "PB3"), slot("MOSI", "PB5"), opt("MISO", "PB4"), opt("NSS", "PA15"),
                resources = setOf(ERRATUM_237), notes = listOf(NoteKey.JtagMustBeReleased),
            ),
            placement("spi2", "SPI2", table5, slot("SCK", "PB13"), slot("MOSI", "PB15"), opt("MISO", "PB14"), opt("NSS", "PB12")),
        ),
    ),
    BoardFunction(
        FunctionKind.Adc,
        listOf(
            placement(
                "adc12", "ADC12", table5,
                *listOf("PA0", "PA1", "PA2", "PA3", "PA4", "PA5", "PA6", "PA7", "PB0", "PB1")
                    .mapIndexed { i, pin -> opt("IN$i", pin) }.toTypedArray(),
            ),
        ),
    ),
    BoardFunction(
        FunctionKind.Pwm,
        listOf(
            remap("tim1", "TIM1", rm("Table 46"), opt("CH1", "PA8"), opt("CH2", "PA9"), opt("CH3", "PA10"), opt("CH4", "PA11"), opt("CH1N", "PB13"), opt("CH2N", "PB14"), opt("CH3N", "PB15")),
            remap("tim1-partial", "TIM1", rm("Table 46"), opt("CH1", "PA8"), opt("CH2", "PA9"), opt("CH3", "PA10"), opt("CH4", "PA11"), opt("CH1N", "PA7"), opt("CH2N", "PB0"), opt("CH3N", "PB1")),
            remap("tim2", "TIM2", rm("Table 45"), opt("CH1", "PA0"), opt("CH2", "PA1"), opt("CH3", "PA2"), opt("CH4", "PA3")),
            remap("tim2-partial-1", "TIM2", rm("Table 45"), opt("CH1", "PA15"), opt("CH2", "PB3"), opt("CH3", "PA2"), opt("CH4", "PA3"), notes = listOf(NoteKey.JtagMustBeReleased)),
            remap("tim2-partial-2", "TIM2", rm("Table 45"), opt("CH1", "PA0"), opt("CH2", "PA1"), opt("CH3", "PB10"), opt("CH4", "PB11")),
            remap("tim2-full", "TIM2", rm("Table 45"), opt("CH1", "PA15"), opt("CH2", "PB3"), opt("CH3", "PB10"), opt("CH4", "PB11"), notes = listOf(NoteKey.JtagMustBeReleased)),
            remap("tim3", "TIM3", rm("Table 44"), opt("CH1", "PA6"), opt("CH2", "PA7"), opt("CH3", "PB0"), opt("CH4", "PB1")),
            remap("tim3-partial", "TIM3", rm("Table 44"), opt("CH1", "PB4"), opt("CH2", "PB5"), opt("CH3", "PB0"), opt("CH4", "PB1"), notes = listOf(NoteKey.JtagMustBeReleased)),
            placement("tim4", "TIM4", rm("Table 43"), opt("CH1", "PB6"), opt("CH2", "PB7"), opt("CH3", "PB8"), opt("CH4", "PB9")),
        ),
    ),
    BoardFunction(
        FunctionKind.Can,
        listOf(
            remap("can", "CAN", rm("Table 34"), slot("RX", "PA11"), slot("TX", "PA12"), resources = setOf(SRAM)),
            remap("can-remap", "CAN", rm("Table 34"), slot("RX", "PB8"), slot("TX", "PB9"), resources = setOf(SRAM)),
        ),
    ),
    BoardFunction(
        FunctionKind.Usb,
        listOf(placement("usb", "USB", rm("§23.2"), slot("DM", "PA11"), slot("DP", "PA12"), resources = setOf(SRAM), notes = listOf(NoteKey.UsbPullupOnBoard))),
    ),
)

private val p3 = listOf("VBAT", "PC13", "PC14", "PC15", "PA0", "PA1", "PA2", "PA3", "PA4", "PA5", "PA6", "PA7", "PB0", "PB1", "PB10", "PB11", "NRST", "VCC3V3", "GND", "GND")
private val p4 = listOf("VCC3V3", "GND", "5V", "PB9", "PB8", "PB7", "PB6", "PB5", "PB4", "PB3", "PA15", "PA12", "PA11", "PA10", "PA9", "PA8", "PB15", "PB14", "PB13", "PB12")
private val p2 = listOf("VCC3V3", "SWDIO", "SWCLK", "GND")
private val swd = mapOf("SWDIO" to "PA13", "SWCLK" to "PA14")

private val bluePillPower = mapOf(
    "VBAT" to PowerPin(PowerRole.Vbat, range("1.8", "3.6", "V")),
    "NRST" to PowerPin(PowerRole.Reset),
    "VCC3V3" to PowerPin(PowerRole.Supply3V3, Value(typ = "3.3", unit = "V"), condition = ConditionKey.Nominal),
    "5V" to PowerPin(PowerRole.Supply5V, Value(typ = "5", unit = "V"), condition = ConditionKey.Nominal),
    "GND" to PowerPin(PowerRole.Ground),
)

private fun row(header: String, labels: List<String>) = labels.mapIndexed { i, label ->
    bluePillPower[label]?.let { power(header, i + 1, label, it, number = "${i + 1}") }
        ?: gpio(header, i + 1, label, swd[label] ?: label, number = "${i + 1}")
}

private val bluePillIo = (p3 + p4).filter { it.startsWith("P") } + swd.values
private val notFt = listOf("PC13", "PC14", "PC15", "PA0", "PA1", "PA2", "PA3", "PA4", "PA5", "PA6", "PA7", "PB0", "PB1", "PB5")

val bluePill = Board(
    id = BoardId("blue-pill"),
    name = "STM32F1 \"Blue Pill\"",
    chip = "STM32F103C8T6",
    layout = BoardLayout(
        width = 8.3f,
        height = 23f,
        headers = listOf(
            Header("P3", 1.15f, 1f, Direction.Down, 20),
            Header("P4", 7.15f, 20f, Direction.Up, 20),
            Header("P2", 2.65f, 22f, Direction.Right, 4),
        ),
        usb = Side.Top,
        view = ViewSide.TopUsbUp,
    ),
    pins = row("P3", p3) + row("P4", p4) + row("P2", p2),
    limits = limits {
        add(bluePillIo - notFt.toSet(), Limit(LimitKind.FiveVoltTolerant))
        add(notFt, Limit(LimitKind.ThreeVoltOnly))
        add(bluePillIo - setOf("PC13", "PC14", "PC15"), Limit(LimitKind.CurrentLimit, max("25", "mA"), condition = ConditionKey.AbsoluteMaximum))
        add(listOf("PC13", "PC14", "PC15"), Limit(LimitKind.CurrentLimit, max("3", "mA"), condition = ConditionKey.SinkOnly))
        add(listOf("PA0", "PA1", "PA2", "PA3", "PA4", "PA5", "PA6", "PA7", "PB0", "PB1"), Limit(LimitKind.Analog))
        add("PC13", Limit(LimitKind.BoardUse, use = BoardUse.Led))
        add(listOf("PC14", "PC15"), Limit(LimitKind.BoardUse, use = BoardUse.Crystal))
        add(listOf("PA11", "PA12"), Limit(LimitKind.BoardUse, use = BoardUse.Usb))
        add("PA12", Limit(LimitKind.BoardPull, Value(typ = "4.7", unit = "kΩ"), condition = ConditionKey.PullUp))
        add(listOf("PA13", "PA14"), Limit(LimitKind.BoardUse, use = BoardUse.Debug))
        add(listOf("PA13", "PA14", "PA15", "PB3", "PB4"), Limit(LimitKind.BootRole, effect = BootEffect.DebugAfterReset))
        add("PA12", Limit(LimitKind.Erratum, erratum = "ES096 2.3.1"))
        add("PA4", Limit(LimitKind.Erratum, erratum = "ES096 2.3.2, 2.3.3"))
        add("PB12", Limit(LimitKind.Erratum, erratum = "ES096 2.3.4, 2.3.5, 2.3.6"))
        add("PB5", Limit(LimitKind.Erratum, erratum = "ES096 2.3.8"))
        add(listOf("PA9", "PA2", "PB10", "PB6"), Limit(LimitKind.Erratum, erratum = "ES096 2.3.9"))
        add("PA9", Limit(LimitKind.Erratum, erratum = "ES096 2.3.10"))
    },
    groupLimits = emptyList(),
    functions = bluePillFunctions,
    notes = listOf(BoardNote.ReferenceSchematic, BoardNote.ClonesMayDiffer, BoardNote.FiveVoltRailUnprotected, BoardNote.UsbPullupValue),
    sources = bluePillSources,
    otherFunctions = others(
        "PA0" to listOf("WKUP", "TIM2_CH1_ETR"),
        "PA8" to listOf("MCO"),
        "PA12" to listOf("TIM1_ETR"),
        "PA13" to listOf("JTMS/SWDIO"),
        "PA14" to listOf("JTCK/SWCLK"),
        "PA15" to listOf("JTDI", "TIM2_CH1_ETR"),
        "PB3" to listOf("JTDO", "TRACESWO"),
        "PB4" to listOf("JNTRST"),
        "PA6" to listOf("TIM1_BKIN"),
        "PB12" to listOf("TIM1_BKIN"),
        "PC13" to listOf("TAMPER-RTC"),
        "PC14" to listOf("OSC32_IN"),
        "PC15" to listOf("OSC32_OUT"),
    ),
)
