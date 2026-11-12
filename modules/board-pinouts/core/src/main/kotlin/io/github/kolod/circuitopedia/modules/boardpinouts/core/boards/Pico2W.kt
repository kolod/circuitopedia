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

// Raspberry Pi Pico 2 W (RP2350A, QFN-60). Sources:
// - Raspberry Pi "Raspberry Pi Pico 2 W Datasheet", Release 2 (build 03/07/2026): Figure 2 pinout
//   (same layout as the Pico W), §2.1 power pins;
// - Raspberry Pi "Raspberry Pi Pico 2 Datasheet", Release 5 (build 03/07/2026), §2.1: the Pico 2
//   uses GPIO23/24/25/29 as the Pico does;
// - Raspberry Pi "RP2350 Datasheet", Release 8 (build 01/10/2026):
//   - Table 677: GPIO functions F0-F11 (SIO left out); F11 adds UART TX/RX on CTS/RTS pins;
//   - Tables 1673/1674: GPIO0-25 are "Digital IO (FT)", tolerating up to 5.5V provided IOVDD is
//     powered to 3.3V; GPIO26-29 (ADC) are standard "Digital IO / Analogue";
//   - Table 1680: VPIN (standard) up to IOVDD + 0.5V;
//   - Table 1683: drive settings 2/4/8/12mA, IIOVDD_MAX and IIOVSS_MAX 100mA;
//   - §6.1.5: ADC_AVDD nominal 1.8V to 3.3V;
//   - Appendix D.5.1, RP2350-E9: increased leakage on Bank 0 GPIO inputs, affects stepping A2 only.

private val pico2WSources = listOf(
    Source("Raspberry Pi", "pico-2-w-datasheet", "Raspberry Pi Pico 2 W Datasheet", "Release 2, 03/07/2026"),
    Source("Raspberry Pi", "pico-2-datasheet", "Raspberry Pi Pico 2 Datasheet", "Release 5, 03/07/2026"),
    Source("Raspberry Pi", "rp2350-datasheet", "RP2350 Datasheet", "Release 8, 01/10/2026"),
)

private val rp2350Functions = SourceRef("rp2350-datasheet", "Table 677")

/** RP2350 Table 677, rows for the header GPIOs, F0-F4 and F6-F11 as printed. */
private val rp2350Table = mapOf(
    "GP0" to listOf("SPI0 RX", "UART0 TX", "I2C0 SDA", "PWM0 A", "PIO0", "PIO1", "PIO2", "QMI CS1n", "USB OVCUR DET"),
    "GP1" to listOf("SPI0 CSn", "UART0 RX", "I2C0 SCL", "PWM0 B", "PIO0", "PIO1", "PIO2", "TRACECLK", "USB VBUS DET"),
    "GP2" to listOf("SPI0 SCK", "UART0 CTS", "I2C1 SDA", "PWM1 A", "PIO0", "PIO1", "PIO2", "TRACEDATA0", "USB VBUS EN", "UART0 TX"),
    "GP3" to listOf("SPI0 TX", "UART0 RTS", "I2C1 SCL", "PWM1 B", "PIO0", "PIO1", "PIO2", "TRACEDATA1", "USB OVCUR DET", "UART0 RX"),
    "GP4" to listOf("SPI0 RX", "UART1 TX", "I2C0 SDA", "PWM2 A", "PIO0", "PIO1", "PIO2", "TRACEDATA2", "USB VBUS DET"),
    "GP5" to listOf("SPI0 CSn", "UART1 RX", "I2C0 SCL", "PWM2 B", "PIO0", "PIO1", "PIO2", "TRACEDATA3", "USB VBUS EN"),
    "GP6" to listOf("SPI0 SCK", "UART1 CTS", "I2C1 SDA", "PWM3 A", "PIO0", "PIO1", "PIO2", "USB OVCUR DET", "UART1 TX"),
    "GP7" to listOf("SPI0 TX", "UART1 RTS", "I2C1 SCL", "PWM3 B", "PIO0", "PIO1", "PIO2", "USB VBUS DET", "UART1 RX"),
    "GP8" to listOf("SPI1 RX", "UART1 TX", "I2C0 SDA", "PWM4 A", "PIO0", "PIO1", "PIO2", "QMI CS1n", "USB VBUS EN"),
    "GP9" to listOf("SPI1 CSn", "UART1 RX", "I2C0 SCL", "PWM4 B", "PIO0", "PIO1", "PIO2", "USB OVCUR DET"),
    "GP10" to listOf("SPI1 SCK", "UART1 CTS", "I2C1 SDA", "PWM5 A", "PIO0", "PIO1", "PIO2", "USB VBUS DET", "UART1 TX"),
    "GP11" to listOf("SPI1 TX", "UART1 RTS", "I2C1 SCL", "PWM5 B", "PIO0", "PIO1", "PIO2", "USB VBUS EN", "UART1 RX"),
    "GP12" to listOf("HSTX", "SPI1 RX", "UART0 TX", "I2C0 SDA", "PWM6 A", "PIO0", "PIO1", "PIO2", "CLOCK GPIN0", "USB OVCUR DET"),
    "GP13" to listOf("HSTX", "SPI1 CSn", "UART0 RX", "I2C0 SCL", "PWM6 B", "PIO0", "PIO1", "PIO2", "CLOCK GPOUT0", "USB VBUS DET"),
    "GP14" to listOf("HSTX", "SPI1 SCK", "UART0 CTS", "I2C1 SDA", "PWM7 A", "PIO0", "PIO1", "PIO2", "CLOCK GPIN1", "USB VBUS EN", "UART0 TX"),
    "GP15" to listOf("HSTX", "SPI1 TX", "UART0 RTS", "I2C1 SCL", "PWM7 B", "PIO0", "PIO1", "PIO2", "CLOCK GPOUT1", "USB OVCUR DET", "UART0 RX"),
    "GP16" to listOf("HSTX", "SPI0 RX", "UART0 TX", "I2C0 SDA", "PWM0 A", "PIO0", "PIO1", "PIO2", "USB VBUS DET"),
    "GP17" to listOf("HSTX", "SPI0 CSn", "UART0 RX", "I2C0 SCL", "PWM0 B", "PIO0", "PIO1", "PIO2", "USB VBUS EN"),
    "GP18" to listOf("HSTX", "SPI0 SCK", "UART0 CTS", "I2C1 SDA", "PWM1 A", "PIO0", "PIO1", "PIO2", "USB OVCUR DET", "UART0 TX"),
    "GP19" to listOf("HSTX", "SPI0 TX", "UART0 RTS", "I2C1 SCL", "PWM1 B", "PIO0", "PIO1", "PIO2", "QMI CS1n", "USB VBUS DET", "UART0 RX"),
    "GP20" to listOf("SPI0 RX", "UART1 TX", "I2C0 SDA", "PWM2 A", "PIO0", "PIO1", "PIO2", "CLOCK GPIN0", "USB VBUS EN"),
    "GP21" to listOf("SPI0 CSn", "UART1 RX", "I2C0 SCL", "PWM2 B", "PIO0", "PIO1", "PIO2", "CLOCK GPOUT0", "USB OVCUR DET"),
    "GP22" to listOf("SPI0 SCK", "UART1 CTS", "I2C1 SDA", "PWM3 A", "PIO0", "PIO1", "PIO2", "CLOCK GPIN1", "USB VBUS DET", "UART1 TX"),
    "GP26" to listOf("SPI1 SCK", "UART1 CTS", "I2C1 SDA", "PWM5 A", "PIO0", "PIO1", "PIO2", "USB VBUS EN", "UART1 TX"),
    "GP27" to listOf("SPI1 TX", "UART1 RTS", "I2C1 SCL", "PWM5 B", "PIO0", "PIO1", "PIO2", "USB OVCUR DET", "UART1 RX"),
    "GP28" to listOf("SPI1 RX", "UART0 TX", "I2C0 SDA", "PWM6 A", "PIO0", "PIO1", "PIO2", "USB VBUS DET"),
)

private val pico2Adc = mapOf("GP26" to "ADC0", "GP27" to "ADC1", "GP28" to "ADC2")
private val ftGpios = picoGpios - pico2Adc.keys

val pico2W = Board(
    id = BoardId("pico2-w"),
    name = "Raspberry Pi Pico 2 W",
    chip = "RP2350",
    layout = picoLayout,
    pins = picoPins(picoPower(adcVref = range("1.8", "3.3", "V"))),
    limits = limits {
        add(ftGpios, Limit(LimitKind.FiveVoltTolerant, max("5.5", "V"), condition = ConditionKey.IovddPowered))
        add(pico2Adc.keys, Limit(LimitKind.ThreeVoltOnly))
        add(picoGpios, Limit(LimitKind.CurrentLimit, max("12", "mA"), condition = ConditionKey.DriveSetting))
        add(pico2Adc.keys, Limit(LimitKind.Analog))
        add(picoGpios, Limit(LimitKind.Erratum, erratum = "RP2350-E9"))
    },
    groupLimits = listOf(
        GroupLimit(pins(*picoGpios.toTypedArray()).toSet(), max("100", "mA"), GroupKey.SourceSum),
        GroupLimit(pins(*picoGpios.toTypedArray()).toSet(), max("100", "mA"), GroupKey.SinkSum),
    ),
    functions = rpFunctions(rp2350Table, rp2350Functions, pico2Adc),
    notes = listOf(BoardNote.Pico2Variant),
    sources = pico2WSources,
    otherFunctions = rpOthers(rp2350Table),
)
