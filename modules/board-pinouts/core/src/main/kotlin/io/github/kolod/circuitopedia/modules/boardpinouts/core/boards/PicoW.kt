package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardNote
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupLimit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Source
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max
import io.github.kolod.circuitopedia.modules.boardpinouts.core.range

// Raspberry Pi Pico W (RP2040). Sources:
// - Raspberry Pi "Raspberry Pi Pico W Datasheet", Release 7 (build 03/07/2026): Figure 2 pinout, §2.1
//   power pins; GPIO23/24/25/29 are used by the wireless chip and are not on the header;
// - Raspberry Pi "Raspberry Pi Pico Datasheet", Release 21 (build 03/07/2026), §2.1: on the Pico the
//   same GPIOs drive the LED (25), sense VBUS (24), control the SMPS (23) and read VSYS/3 (29);
// - Raspberry Pi "RP2040 Datasheet" (build 2025-02-20, 3184e62): Table 279 GPIO functions (F1-F9; SIO
//   on every pin is left out), §2.9.5 ADC_AVDD nominal 1.8V to 3.3V, Table 622 voltage at IO up to
//   IOVDD + 0.5V, Table 625 drive settings 2/4/8/12mA and IIOVDD_MAX/IIOVSS_MAX 50mA.

private val picoWSources = listOf(
    Source("Raspberry Pi", "pico-w-datasheet", "Raspberry Pi Pico W Datasheet", "Release 7, 03/07/2026"),
    Source("Raspberry Pi", "pico-datasheet", "Raspberry Pi Pico Datasheet", "Release 21, 03/07/2026"),
    Source("Raspberry Pi", "rp2040-datasheet", "RP2040 Datasheet", "Build 3184e62, 2025-02-20"),
)

private val rp2040Functions = SourceRef("rp2040-datasheet", "Table 279")

/** RP2040 Table 279, rows for the header GPIOs, F1-F4 and F6-F9 as printed. */
private val rp2040Table = mapOf(
    "GP0" to listOf("SPI0 RX", "UART0 TX", "I2C0 SDA", "PWM0 A", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP1" to listOf("SPI0 CSn", "UART0 RX", "I2C0 SCL", "PWM0 B", "PIO0", "PIO1", "USB VBUS DET"),
    "GP2" to listOf("SPI0 SCK", "UART0 CTS", "I2C1 SDA", "PWM1 A", "PIO0", "PIO1", "USB VBUS EN"),
    "GP3" to listOf("SPI0 TX", "UART0 RTS", "I2C1 SCL", "PWM1 B", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP4" to listOf("SPI0 RX", "UART1 TX", "I2C0 SDA", "PWM2 A", "PIO0", "PIO1", "USB VBUS DET"),
    "GP5" to listOf("SPI0 CSn", "UART1 RX", "I2C0 SCL", "PWM2 B", "PIO0", "PIO1", "USB VBUS EN"),
    "GP6" to listOf("SPI0 SCK", "UART1 CTS", "I2C1 SDA", "PWM3 A", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP7" to listOf("SPI0 TX", "UART1 RTS", "I2C1 SCL", "PWM3 B", "PIO0", "PIO1", "USB VBUS DET"),
    "GP8" to listOf("SPI1 RX", "UART1 TX", "I2C0 SDA", "PWM4 A", "PIO0", "PIO1", "USB VBUS EN"),
    "GP9" to listOf("SPI1 CSn", "UART1 RX", "I2C0 SCL", "PWM4 B", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP10" to listOf("SPI1 SCK", "UART1 CTS", "I2C1 SDA", "PWM5 A", "PIO0", "PIO1", "USB VBUS DET"),
    "GP11" to listOf("SPI1 TX", "UART1 RTS", "I2C1 SCL", "PWM5 B", "PIO0", "PIO1", "USB VBUS EN"),
    "GP12" to listOf("SPI1 RX", "UART0 TX", "I2C0 SDA", "PWM6 A", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP13" to listOf("SPI1 CSn", "UART0 RX", "I2C0 SCL", "PWM6 B", "PIO0", "PIO1", "USB VBUS DET"),
    "GP14" to listOf("SPI1 SCK", "UART0 CTS", "I2C1 SDA", "PWM7 A", "PIO0", "PIO1", "USB VBUS EN"),
    "GP15" to listOf("SPI1 TX", "UART0 RTS", "I2C1 SCL", "PWM7 B", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP16" to listOf("SPI0 RX", "UART0 TX", "I2C0 SDA", "PWM0 A", "PIO0", "PIO1", "USB VBUS DET"),
    "GP17" to listOf("SPI0 CSn", "UART0 RX", "I2C0 SCL", "PWM0 B", "PIO0", "PIO1", "USB VBUS EN"),
    "GP18" to listOf("SPI0 SCK", "UART0 CTS", "I2C1 SDA", "PWM1 A", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP19" to listOf("SPI0 TX", "UART0 RTS", "I2C1 SCL", "PWM1 B", "PIO0", "PIO1", "USB VBUS DET"),
    "GP20" to listOf("SPI0 RX", "UART1 TX", "I2C0 SDA", "PWM2 A", "PIO0", "PIO1", "CLOCK GPIN0", "USB VBUS EN"),
    "GP21" to listOf("SPI0 CSn", "UART1 RX", "I2C0 SCL", "PWM2 B", "PIO0", "PIO1", "CLOCK GPOUT0", "USB OVCUR DET"),
    "GP22" to listOf("SPI0 SCK", "UART1 CTS", "I2C1 SDA", "PWM3 A", "PIO0", "PIO1", "CLOCK GPIN1", "USB VBUS DET"),
    "GP26" to listOf("SPI1 SCK", "UART1 CTS", "I2C1 SDA", "PWM5 A", "PIO0", "PIO1", "USB VBUS EN"),
    "GP27" to listOf("SPI1 TX", "UART1 RTS", "I2C1 SCL", "PWM5 B", "PIO0", "PIO1", "USB OVCUR DET"),
    "GP28" to listOf("SPI1 RX", "UART0 TX", "I2C0 SDA", "PWM6 A", "PIO0", "PIO1", "USB VBUS DET"),
)

private val picoAdc = mapOf("GP26" to "ADC0", "GP27" to "ADC1", "GP28" to "ADC2")

val picoW = Board(
    id = BoardId("pico-w"),
    name = "Raspberry Pi Pico W",
    chip = "RP2040",
    layout = picoLayout,
    pins = picoPins(picoPower(adcVref = range("1.8", "3.3", "V"))),
    limits = limits {
        add(picoGpios, Limit(LimitKind.ThreeVoltOnly))
        add(picoGpios, Limit(LimitKind.CurrentLimit, max("12", "mA"), condition = ConditionKey.DriveSetting))
        add(picoAdc.keys, Limit(LimitKind.Analog))
    },
    groupLimits = listOf(
        GroupLimit(pins(*picoGpios.toTypedArray()).toSet(), max("50", "mA"), GroupKey.SourceSum),
        GroupLimit(pins(*picoGpios.toTypedArray()).toSet(), max("50", "mA"), GroupKey.SinkSum),
    ),
    functions = rpFunctions(rp2040Table, rp2040Functions, picoAdc),
    notes = listOf(BoardNote.PicoVariant),
    sources = picoWSources,
    otherFunctions = rpOthers(rp2040Table),
)
