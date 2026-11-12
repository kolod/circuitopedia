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
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupLimit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Header
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.NoteKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Side
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Source
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ViewSide
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max

// Raspberry Pi 4 Model B, 40-pin header J8. Sources:
// - Raspberry Pi "Raspberry Pi 4 Model B Datasheet", Release 1.1 (2024): Figure 3 J8 pinout (ID_SD
//   and ID_SC are reserved for the HAT ID EEPROM, which is interrogated at boot); Table 3 DC
//   characteristics (maximum drive strength 16mA); §4.1 5V supply; Table 5 GPIO alternate functions;
// - Raspberry Pi documentation "GPIO on Raspberry Pi" (raspberrypi/documentation, commit 0fb4163,
//   2026-10-29): GPIO pins are 3.3V and not 5V tolerant, GP2/GP3 have fixed 1.8kΩ pull-ups to 3.3V,
//   up to 16mA per pin and 50mA for all GPIO together;
// - raspberrypi/firmware boot/overlays/README (fetched 2026-11-12): the pin pairs of i2c0/1/3-6,
//   spi0-6, uart0-5 and the legal pwm pins (PWM0: 12 Alt0, 18 Alt5; PWM1: 13 Alt0, 19 Alt5); the
//   onboard analogue audio uses both PWM channels; disable-bt "restores UART0/ttyAMA0 over GPIOs 14 & 15"
//   (so Bluetooth holds UART0 by default).
// The overlays fix which pins each extra bus uses, so each pin set is one placement that claims its
// pins together. Drawn rotated, USB and Ethernet at the bottom.

private val pi4Sources = listOf(
    Source("Raspberry Pi", "rpi4-datasheet", "Raspberry Pi 4 Model B Datasheet", "Release 1.1, 2024"),
    Source("Raspberry Pi", "gpio-on-raspberry-pi", "Raspberry Pi documentation: GPIO on Raspberry Pi", "commit 0fb4163, 2026-10-29"),
    Source("Raspberry Pi", "overlays-readme", "Raspberry Pi firmware boot/overlays/README", "fetched 2026-11-12"),
)

private val table5 = SourceRef("rpi4-datasheet", "Table 5")
private val overlays = SourceRef("overlays-readme", "i2c, spi, uart, pwm overlays")

private val j8 = listOf(
    "3V3", "5V", "GPIO2", "5V", "GPIO3", "GND", "GPIO4", "GPIO14", "GND", "GPIO15",
    "GPIO17", "GPIO18", "GPIO27", "GND", "GPIO22", "GPIO23", "3V3", "GPIO24", "GPIO10", "GND",
    "GPIO9", "GPIO25", "GPIO11", "GPIO8", "GND", "GPIO7", "GPIO0", "GPIO1", "GPIO5", "GND",
    "GPIO6", "GPIO12", "GPIO13", "GND", "GPIO19", "GPIO16", "GPIO26", "GPIO20", "GND", "GPIO21",
)

/** Figure 3 labels GPIO0/GPIO1 by their reserved names. */
private val j8Labels = mapOf("GPIO0" to "ID_SD", "GPIO1" to "ID_SC")

private val pi4Power = mapOf(
    "3V3" to PowerPin(PowerRole.Supply3V3, Value(typ = "3.3", unit = "V"), condition = ConditionKey.Nominal),
    "5V" to PowerPin(PowerRole.Supply5V, Value(typ = "5", unit = "V"), condition = ConditionKey.Nominal),
    "GND" to PowerPin(PowerRole.Ground),
)

private val pi4Gpios = (0..27).map { "GPIO$it" }

/** Table 5: ALT0-ALT5 per GPIO as printed ("-" cells left out). */
private val table5Alt = mapOf(
    "GPIO0" to listOf("SDA0", "SA5", "PCLK", "SPI3 CE0 N", "TXD2", "SDA6"),
    "GPIO1" to listOf("SCL0", "SA4", "DE", "SPI3 MISO", "RXD2", "SCL6"),
    "GPIO2" to listOf("SDA1", "SA3", "LCD VSYNC", "SPI3 MOSI", "CTS2", "SDA3"),
    "GPIO3" to listOf("SCL1", "SA2", "LCD HSYNC", "SPI3 SCLK", "RTS2", "SCL3"),
    "GPIO4" to listOf("GPCLK0", "SA1", "DPI D0", "SPI4 CE0 N", "TXD3", "SDA3"),
    "GPIO5" to listOf("GPCLK1", "SA0", "DPI D1", "SPI4 MISO", "RXD3", "SCL3"),
    "GPIO6" to listOf("GPCLK2", "SOE N", "DPI D2", "SPI4 MOSI", "CTS3", "SDA4"),
    "GPIO7" to listOf("SPI0 CE1 N", "SWE N", "DPI D3", "SPI4 SCLK", "RTS3", "SCL4"),
    "GPIO8" to listOf("SPI0 CE0 N", "SD0", "DPI D4", "TXD4", "SDA4"),
    "GPIO9" to listOf("SPI0 MISO", "SD1", "DPI D5", "RXD4", "SCL4"),
    "GPIO10" to listOf("SPI0 MOSI", "SD2", "DPI D6", "CTS4", "SDA5"),
    "GPIO11" to listOf("SPI0 SCLK", "SD3", "DPI D7", "RTS4", "SCL5"),
    "GPIO12" to listOf("PWM0", "SD4", "DPI D8", "SPI5 CE0 N", "TXD5", "SDA5"),
    "GPIO13" to listOf("PWM1", "SD5", "DPI D9", "SPI5 MISO", "RXD5", "SCL5"),
    "GPIO14" to listOf("TXD0", "SD6", "DPI D10", "SPI5 MOSI", "CTS5", "TXD1"),
    "GPIO15" to listOf("RXD0", "SD7", "DPI D11", "SPI5 SCLK", "RTS5", "RXD1"),
    "GPIO16" to listOf("FL0", "SD8", "DPI D12", "CTS0", "SPI1 CE2 N", "CTS1"),
    "GPIO17" to listOf("FL1", "SD9", "DPI D13", "RTS0", "SPI1 CE1 N", "RTS1"),
    "GPIO18" to listOf("PCM CLK", "SD10", "DPI D14", "SPI6 CE0 N", "SPI1 CE0 N", "PWM0"),
    "GPIO19" to listOf("PCM FS", "SD11", "DPI D15", "SPI6 MISO", "SPI1 MISO", "PWM1"),
    "GPIO20" to listOf("PCM DIN", "SD12", "DPI D16", "SPI6 MOSI", "SPI1 MOSI", "GPCLK0"),
    "GPIO21" to listOf("PCM DOUT", "SD13", "DPI D17", "SPI6 SCLK", "SPI1 SCLK", "GPCLK1"),
    "GPIO22" to listOf("SD0 CLK", "SD14", "DPI D18", "SD1 CLK", "ARM TRST", "SDA6"),
    "GPIO23" to listOf("SD0 CMD", "SD15", "DPI D19", "SD1 CMD", "ARM RTCK", "SCL6"),
    "GPIO24" to listOf("SD0 DAT0", "SD16", "DPI D20", "SD1 DAT0", "ARM TDO", "SPI3 CE1 N"),
    "GPIO25" to listOf("SD0 DAT1", "SD17", "DPI D21", "SD1 DAT1", "ARM TCK", "SPI4 CE1 N"),
    "GPIO26" to listOf("SD0 DAT2", "TE0", "DPI D22", "SD1 DAT2", "ARM TDI", "SPI5 CE1 N"),
    "GPIO27" to listOf("SD0 DAT3", "TE1", "DPI D23", "SD1 DAT3", "ARM TMS", "SPI6 CE1 N"),
)

/** Table 5 signals the planner covers; everything else is shown as printed. */
private val plannedSignals = Regex("""^(SDA\d|SCL\d|TXD\d|RXD\d|CTS\d|RTS\d|SPI\d .*|PWM\d|PCM .*)$""")

private fun i2c(id: String, instance: String, sda: String, scl: String, vararg notes: NoteKey) =
    placement(id, instance, table5, slot("SDA", sda), slot("SCL", scl), group = true, notes = notes.toList())

private fun uart(id: String, instance: String, tx: String, rx: String, cts: String, rts: String, vararg notes: NoteKey) =
    placement(id, instance, table5, slot("TXD", tx), slot("RXD", rx), opt("CTS", cts), opt("RTS", rts), notes = notes.toList())

private fun spi(id: String, sclk: String, mosi: String, miso: String, vararg ce: Pair<String, String>, notes: List<NoteKey> = emptyList()) =
    placement(
        id, id.uppercase(), table5,
        slot("SCLK", sclk), slot("MOSI", mosi), opt("MISO", miso), *ce.map { (name, pin) -> opt(name, pin) }.toTypedArray(),
        notes = notes,
    )

private val overlay = NoteKey.NeedsOverlay

private val pi4Functions = listOf(
    BoardFunction(
        FunctionKind.Uart,
        listOf(
            uart("uart0", "UART0", "GPIO14", "GPIO15", "GPIO16", "GPIO17", NoteKey.BluetoothUsesUart0),
            uart("uart1", "UART1", "GPIO14", "GPIO15", "GPIO16", "GPIO17"),
            uart("uart2", "UART2", "GPIO0", "GPIO1", "GPIO2", "GPIO3", overlay),
            uart("uart3", "UART3", "GPIO4", "GPIO5", "GPIO6", "GPIO7", overlay),
            uart("uart4", "UART4", "GPIO8", "GPIO9", "GPIO10", "GPIO11", overlay),
            uart("uart5", "UART5", "GPIO12", "GPIO13", "GPIO14", "GPIO15", overlay),
        ),
    ),
    BoardFunction(
        FunctionKind.I2c,
        listOf(
            i2c("i2c0", "I2C0", "GPIO0", "GPIO1"),
            i2c("i2c1", "I2C1", "GPIO2", "GPIO3"),
            i2c("i2c3-2-3", "I2C3", "GPIO2", "GPIO3", overlay),
            i2c("i2c3-4-5", "I2C3", "GPIO4", "GPIO5", overlay),
            i2c("i2c4-6-7", "I2C4", "GPIO6", "GPIO7", overlay),
            i2c("i2c4-8-9", "I2C4", "GPIO8", "GPIO9", overlay),
            i2c("i2c5-10-11", "I2C5", "GPIO10", "GPIO11", overlay),
            i2c("i2c5-12-13", "I2C5", "GPIO12", "GPIO13", overlay),
            i2c("i2c6-0-1", "I2C6", "GPIO0", "GPIO1", overlay),
            i2c("i2c6-22-23", "I2C6", "GPIO22", "GPIO23", overlay),
        ),
    ),
    BoardFunction(
        FunctionKind.Spi,
        listOf(
            spi("spi0", "GPIO11", "GPIO10", "GPIO9", "CE0" to "GPIO8", "CE1" to "GPIO7"),
            spi("spi1", "GPIO21", "GPIO20", "GPIO19", "CE0" to "GPIO18", "CE1" to "GPIO17", "CE2" to "GPIO16", notes = listOf(overlay)),
            spi("spi3", "GPIO3", "GPIO2", "GPIO1", "CE0" to "GPIO0", "CE1" to "GPIO24", notes = listOf(overlay)),
            spi("spi4", "GPIO7", "GPIO6", "GPIO5", "CE0" to "GPIO4", "CE1" to "GPIO25", notes = listOf(overlay)),
            spi("spi5", "GPIO15", "GPIO14", "GPIO13", "CE0" to "GPIO12", "CE1" to "GPIO26", notes = listOf(overlay)),
            spi("spi6", "GPIO21", "GPIO20", "GPIO19", "CE0" to "GPIO18", "CE1" to "GPIO27", notes = listOf(overlay)),
        ),
    ),
    BoardFunction(
        FunctionKind.Pwm,
        listOf(
            placement(
                "pwm", "PWM", overlays,
                opt("PWM0", "GPIO12", "GPIO18"), opt("PWM1", "GPIO13", "GPIO19"),
                notes = listOf(overlay, NoteKey.AnalogAudioUsesPwm),
            ),
        ),
    ),
    BoardFunction(
        FunctionKind.I2s,
        listOf(placement("pcm", "PCM", table5, slot("CLK", "GPIO18"), slot("FS", "GPIO19"), opt("DIN", "GPIO20"), opt("DOUT", "GPIO21"))),
    ),
)

val raspberryPi4B = Board(
    id = BoardId("rpi4b"),
    name = "Raspberry Pi 4 Model B",
    chip = "BCM2711",
    layout = BoardLayout(
        width = 22f,
        height = 33.5f,
        headers = listOf(
            Header("J8-odd", 19.5f, 1f, Direction.Down, 20),
            Header("J8-even", 20.5f, 1f, Direction.Down, 20),
        ),
        usb = Side.Bottom,
        view = ViewSide.TopRotatedUsbDown,
    ),
    pins = j8.mapIndexed { i, name ->
        val number = i + 1
        val header = if (number % 2 == 1) "J8-odd" else "J8-even"
        val position = (number + 1) / 2
        pi4Power[name]?.let { power(header, position, name, it, number = "$number") }
            ?: gpio(header, position, j8Labels[name] ?: name, name, number = "$number")
    },
    limits = limits {
        add(pi4Gpios, Limit(LimitKind.ThreeVoltOnly))
        add(pi4Gpios, Limit(LimitKind.CurrentLimit, max("16", "mA"), condition = ConditionKey.DriveSetting))
        add(listOf("GPIO0", "GPIO1"), Limit(LimitKind.BoardUse, use = BoardUse.IdEeprom))
        add(listOf("GPIO0", "GPIO1"), Limit(LimitKind.BootRole, effect = BootEffect.HatEepromProbe))
        add(listOf("GPIO2", "GPIO3"), Limit(LimitKind.BoardPull, Value(typ = "1.8", unit = "kΩ"), condition = ConditionKey.PullUp))
    },
    groupLimits = listOf(GroupLimit(pins(*pi4Gpios.toTypedArray()).toSet(), max("50", "mA"), GroupKey.TotalSum)),
    functions = pi4Functions,
    notes = listOf(BoardNote.NoFiveVoltGpio),
    sources = pi4Sources,
    otherFunctions = table5Alt.mapKeys { io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin(it.key) }
        .mapValues { (_, alts) -> alts.filterNot { plannedSignals.matches(it) } },
)
