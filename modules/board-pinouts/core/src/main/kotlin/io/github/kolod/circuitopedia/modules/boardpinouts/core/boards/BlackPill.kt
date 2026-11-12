package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardLayout
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardNote
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardUse
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BootEffect
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Direction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupLimit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Header
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Side
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Source
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ViewSide
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max
import io.github.kolod.circuitopedia.modules.boardpinouts.core.range

// WeAct "Black Pill" STM32F411CEU6. Sources:
// - WeAct Studio schematic "MiniF4 ... V3.1" (MiniF4x1Cx_V31 SchDoc, 4/17/2021) and board shape:
//   P1 (5V, GND, 3.3V, PB10, PB2, PB1, PB0, PA7-PA0, NRST, PC15, PC14, PC13, VB) along one long edge and
//   P2 (PB12-PB15, PA8-PA12, PA15, PB3-PB9, 5V, GND, 3.3V) along the other, both with pin 1 at the USB
//   end; P3 SWD (3.3V, SWDIO=PA13, SWCLK=PA14, GND). KEY on PA0 to GND through 330Ω; blue LED on PC13
//   (1.5kΩ to 3.3V); 32.768kHz crystal on PC14/PC15; USB-C on PA11/PA12; PB2 to GND through 10kΩ (R3);
//   optional SPI flash footprint on PA4 (CS), PA5 (SCK), PA6 (MISO), PA7 (MOSI); VB is VBAT through a
//   BAT54C. Hardware README: V3.1 keeps the V3.0 pin definition.
// - ST DS10314 "STM32F411xC STM32F411xE", DocID026289 Rev 7 (14-Dec-2017), the copy in WeAct's
//   repository (st.com was unreachable when transcribed; the user approved the copy):
//   - Table 8, UFQFPN48: I/O structure (PA0 and PB5 are TC, the others FT), additional functions
//     (ADC1_0..9, BOOT1 on PB2), notes 2 (PC13-PC15 sink only 3mA, max 2MHz, not a current source)
//     and 4 (FT is not 5V tolerant in analog mode);
//   - Table 9: alternate functions AF00-AF15 (transcribed as printed, header pins only);
//   - Table 12: 25mA per I/O, 120mA for all I/Os together (absolute maximum);
//   - Table 14: VBAT 1.65V to 3.6V.
// - ST DS10086 "STM32F401xB/C STM32F401xD/E", DocID025644 Rev 3, the copy in WeAct's repository,
//   for the F401 variant note.

private val blackPillSources = listOf(
    Source("WeAct Studio", "MiniF4x1Cx_V31", "MiniSTM32F4x1 V3.1 schematic and board shape", "V3.1, 2021-04-17"),
    Source("STMicroelectronics", "DS10314", "STM32F411xC STM32F411xE datasheet", "DocID026289 Rev 7, 14-Dec-2017 (WeAct copy)"),
    Source("STMicroelectronics", "DS10086", "STM32F401xB/C STM32F401xD/E datasheet", "DocID025644 Rev 3 (WeAct copy)"),
)

private val table8 = SourceRef("DS10314", "Table 8")
private val table9 = SourceRef("DS10314", "Table 9")

/** DS10314 Table 9, header pins, cells as printed ("-" cells left out). */
private val f411Af = mapOf(
    "PA0" to listOf("TIM2_CH1/TIM2_ETR", "TIM5_CH1", "USART2_CTS", "EVENTOUT"),
    "PA1" to listOf("TIM2_CH2", "TIM5_CH2", "SPI4_MOSI/I2S4_SD", "USART2_RTS", "EVENTOUT"),
    "PA2" to listOf("TIM2_CH3", "TIM5_CH3", "TIM9_CH1", "I2S2_CKIN", "USART2_TX", "EVENTOUT"),
    "PA3" to listOf("TIM2_CH4", "TIM5_CH4", "TIM9_CH2", "I2S2_MCK", "USART2_RX", "EVENTOUT"),
    "PA4" to listOf("SPI1_NSS/I2S1_WS", "SPI3_NSS/I2S3_WS", "USART2_CK", "EVENTOUT"),
    "PA5" to listOf("TIM2_CH1/TIM2_ETR", "SPI1_SCK/I2S1_CK", "EVENTOUT"),
    "PA6" to listOf("TIM1_BKIN", "TIM3_CH1", "SPI1_MISO", "I2S2_MCK", "SDIO_CMD", "EVENTOUT"),
    "PA7" to listOf("TIM1_CH1N", "TIM3_CH2", "SPI1_MOSI/I2S1_SD", "EVENTOUT"),
    "PA8" to listOf("MCO_1", "TIM1_CH1", "I2C3_SCL", "USART1_CK", "USB_FS_SOF", "SDIO_D1", "EVENTOUT"),
    "PA9" to listOf("TIM1_CH2", "I2C3_SMBA", "USART1_TX", "USB_FS_VBUS", "SDIO_D2", "EVENTOUT"),
    "PA10" to listOf("TIM1_CH3", "SPI5_MOSI/I2S5_SD", "USART1_RX", "USB_FS_ID", "EVENTOUT"),
    "PA11" to listOf("TIM1_CH4", "SPI4_MISO", "USART1_CTS", "USART6_TX", "USB_FS_DM", "EVENTOUT"),
    "PA12" to listOf("TIM1_ETR", "SPI5_MISO", "USART1_RTS", "USART6_RX", "USB_FS_DP", "EVENTOUT"),
    "PA13" to listOf("JTMS-SWDIO", "EVENTOUT"),
    "PA14" to listOf("JTCK-SWCLK", "EVENTOUT"),
    "PA15" to listOf("JTDI", "TIM2_CH1/TIM2_ETR", "SPI1_NSS/I2S1_WS", "SPI3_NSS/I2S3_WS", "USART1_TX", "EVENTOUT"),
    "PB0" to listOf("TIM1_CH2N", "TIM3_CH3", "SPI5_SCK/I2S5_CK", "EVENTOUT"),
    "PB1" to listOf("TIM1_CH3N", "TIM3_CH4", "SPI5_NSS/I2S5_WS", "EVENTOUT"),
    "PB2" to listOf("EVENTOUT"),
    "PB3" to listOf("JTDO-SWO", "TIM2_CH2", "SPI1_SCK/I2S1_CK", "SPI3_SCK/I2S3_CK", "USART1_RX", "I2C2_SDA", "EVENTOUT"),
    "PB4" to listOf("JTRST", "TIM3_CH1", "SPI1_MISO", "SPI3_MISO", "I2S3ext_SD", "I2C3_SDA", "SDIO_D0", "EVENTOUT"),
    "PB5" to listOf("TIM3_CH2", "I2C1_SMBA", "SPI1_MOSI/I2S1_SD", "SPI3_MOSI/I2S3_SD", "SDIO_D3", "EVENTOUT"),
    "PB6" to listOf("TIM4_CH1", "I2C1_SCL", "USART1_TX", "EVENTOUT"),
    "PB7" to listOf("TIM4_CH2", "I2C1_SDA", "USART1_RX", "SDIO_D0", "EVENTOUT"),
    "PB8" to listOf("TIM4_CH3", "TIM10_CH1", "I2C1_SCL", "SPI5_MOSI/I2S5_SD", "I2C3_SDA", "SDIO_D4", "EVENTOUT"),
    "PB9" to listOf("TIM4_CH4", "TIM11_CH1", "I2C1_SDA", "SPI2_NSS/I2S2_WS", "I2C2_SDA", "SDIO_D5", "EVENTOUT"),
    "PB10" to listOf("TIM2_CH3", "I2C2_SCL", "SPI2_SCK/I2S2_CK", "I2S3_MCK", "SDIO_D7", "EVENTOUT"),
    "PB12" to listOf("TIM1_BKIN", "I2C2_SMBA", "SPI2_NSS/I2S2_WS", "SPI4_NSS/I2S4_WS", "SPI3_SCK/I2S3_CK", "EVENTOUT"),
    "PB13" to listOf("TIM1_CH1N", "SPI2_SCK/I2S2_CK", "SPI4_SCK/I2S4_CK", "EVENTOUT"),
    "PB14" to listOf("TIM1_CH2N", "SPI2_MISO", "I2S2ext_SD", "SDIO_D6", "EVENTOUT"),
    "PB15" to listOf("RTC_50Hz", "TIM1_CH3N", "SPI2_MOSI/I2S2_SD", "SDIO_CK", "EVENTOUT"),
)

/** Table 8 "Additional functions" of the header pins (ADC channels go to the planner). */
private val f411Additional = mapOf(
    "PA0" to listOf("WKUP1"),
    "PA9" to listOf("OTG_FS_VBUS"),
    "PB2" to listOf("BOOT1"),
    "PB15" to listOf("RTC_REFIN"),
    "PC13" to listOf("RTC_AMP1", "RTC_OUT", "RTC_TS"),
    "PC14" to listOf("OSC32_IN"),
    "PC15" to listOf("OSC32_OUT"),
)

private val f411Adc = listOf("PA0", "PA1", "PA2", "PA3", "PA4", "PA5", "PA6", "PA7", "PB0", "PB1")
    .mapIndexed { i, pin -> pin to "ADC1_$i" }.toMap()

private val p1 = listOf("5V", "GND", "3.3V", "PB10", "PB2", "PB1", "PB0", "PA7", "PA6", "PA5", "PA4", "PA3", "PA2", "PA1", "PA0", "NRST", "PC15", "PC14", "PC13", "VB")
private val p2 = listOf("PB12", "PB13", "PB14", "PB15", "PA8", "PA9", "PA10", "PA11", "PA12", "PA15", "PB3", "PB4", "PB5", "PB6", "PB7", "PB8", "PB9", "5V", "GND", "3.3V")
private val p3 = listOf("3.3V", "SWDIO", "SWCLK", "GND")
private val swd = mapOf("SWDIO" to "PA13", "SWCLK" to "PA14")

private val blackPillPower = mapOf(
    "5V" to PowerPin(PowerRole.Supply5V, Value(typ = "5", unit = "V"), condition = ConditionKey.Nominal),
    "3.3V" to PowerPin(PowerRole.Supply3V3, Value(typ = "3.3", unit = "V"), condition = ConditionKey.Nominal),
    "GND" to PowerPin(PowerRole.Ground),
    "NRST" to PowerPin(PowerRole.Reset),
    "VB" to PowerPin(PowerRole.Vbat, range("1.65", "3.6", "V")),
)

private fun row(header: String, labels: List<String>) = labels.mapIndexed { i, label ->
    blackPillPower[label]?.let { power(header, i + 1, label, it, number = "${i + 1}") }
        ?: gpio(header, i + 1, label, swd[label] ?: label, number = "${i + 1}")
}

private val headerIo = (p1 + p2).filter { it.startsWith("P") } + swd.values
private val tcPins = listOf("PA0", "PB5")

val blackPill = Board(
    id = BoardId("black-pill"),
    name = "STM32F4 \"Black Pill\"",
    chip = "STM32F411CEU6",
    layout = BoardLayout(
        width = 8.3f,
        height = 23f,
        headers = listOf(
            Header("P2", 1.15f, 1f, Direction.Down, 20),
            Header("P1", 7.15f, 1f, Direction.Down, 20),
            Header("P3", 2.65f, 22f, Direction.Right, 4),
        ),
        usb = Side.Top,
        view = ViewSide.TopUsbUp,
    ),
    pins = row("P1", p1) + row("P2", p2) + row("P3", p3),
    limits = limits {
        add(headerIo - tcPins.toSet(), Limit(LimitKind.FiveVoltTolerant, condition = ConditionKey.NotAnalogMode))
        add(tcPins, Limit(LimitKind.ThreeVoltOnly))
        add(headerIo, Limit(LimitKind.CurrentLimit, max("25", "mA"), condition = ConditionKey.AbsoluteMaximum))
        add(f411Adc.keys, Limit(LimitKind.Analog))
        add(listOf("PC13", "PC14", "PC15"), Limit(LimitKind.CurrentLimit, max("3", "mA"), condition = ConditionKey.SinkOnly))
        add("PA0", Limit(LimitKind.BoardUse, use = BoardUse.Button))
        add("PC13", Limit(LimitKind.BoardUse, use = BoardUse.Led))
        add(listOf("PC14", "PC15"), Limit(LimitKind.BoardUse, use = BoardUse.Crystal))
        add(listOf("PA11", "PA12"), Limit(LimitKind.BoardUse, use = BoardUse.Usb))
        add(listOf("PA4", "PA5", "PA6", "PA7"), Limit(LimitKind.BoardUse, use = BoardUse.Flash))
        add(listOf("PA13", "PA14"), Limit(LimitKind.BoardUse, use = BoardUse.Debug))
        add("PB2", Limit(LimitKind.BoardPull, Value(typ = "10", unit = "kΩ"), condition = ConditionKey.PullDown))
        add("PB2", Limit(LimitKind.BootRole, effect = BootEffect.BootMemorySelect))
    },
    groupLimits = listOf(
        GroupLimit(pins(*headerIo.toTypedArray()).toSet(), max("120", "mA"), GroupKey.SinkSum),
        GroupLimit(pins(*headerIo.toTypedArray()).toSet(), max("120", "mA"), GroupKey.SourceSum),
    ),
    functions = stm32f4Functions(f411Af, table9, f411Adc, table8),
    notes = listOf(BoardNote.F401Variant, BoardNote.ClonesMayDiffer),
    sources = blackPillSources,
    otherFunctions = stm32Others(f411Af).let { af ->
        (af.keys.map { it.name } + f411Additional.keys).distinct()
            .associate { ChipPin(it) to af[ChipPin(it)].orEmpty() + f411Additional[it].orEmpty() }
    },
)
