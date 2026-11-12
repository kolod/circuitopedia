package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardLayout
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardUse
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Direction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Header
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Side
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Source
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ViewSide
import io.github.kolod.circuitopedia.modules.boardpinouts.core.range

// Arduino UNO R3. Sources:
// - Arduino A000066 "Arduino UNO R3 User Manual", Rev. 4 (20/10/2026): §2.2 VIN 6-20V; §5.1 JANALOG
//   and §5.2 JDIGITAL pin lists (NC, IOREF connected to 5V, A4/A5 duplicated as SDA/SCL);
// - Arduino schematic UNO-TH_Rev3e (06/03/19): header nets, ICSP (1 MISO, 2 +5V, 3 SCK, 4 MOSI,
//   5 RESET, 6 GND), D0/D1 through 1kΩ to the ATmega16U2 USB-serial bridge, D13 (SCK) buffered by
//   the LMV358 to the "L" LED;
// - the ATmega328P datasheet, see Atmega328p.kt.
// The drawing is rotated (USB at the top) and schematic, not to scale.

private val unoSources = listOf(
    Source("Arduino", "A000066", "Arduino UNO R3 User Manual", "Rev. 4, 20/10/2026"),
    Source("Arduino", "UNO-TH_Rev3e", "Arduino UNO Rev3 schematic", "Rev3e, 06/03/2019"),
    atmega328pSource,
)

private val nominal5V = PowerPin(PowerRole.Supply5V, Value(typ = "5", unit = "V"), condition = ConditionKey.Nominal)
private val ground = PowerPin(PowerRole.Ground)
private val reset = PowerPin(PowerRole.Reset, range("-0.5", "13.0", "V"), condition = ConditionKey.AbsoluteMaximum)

val unoR3 = Board(
    id = BoardId("uno-r3"),
    name = "Arduino Uno R3",
    chip = "ATmega328P",
    layout = BoardLayout(
        width = 21f,
        height = 27f,
        headers = listOf(
            Header("power", 1f, 8f, Direction.Down, 8),
            Header("analog", 1f, 17f, Direction.Down, 6),
            Header("digital-high", 20f, 16f, Direction.Up, 10),
            Header("digital-low", 20f, 25f, Direction.Up, 8),
            Header("icsp-odd", 9.5f, 22f, Direction.Down, 3),
            Header("icsp-even", 10.5f, 22f, Direction.Down, 3),
        ),
        usb = Side.Top,
        view = ViewSide.TopRotatedUsbUp,
    ),
    pins = listOf(
        power("power", 1, "NC", PowerPin(PowerRole.NotConnected)),
        power("power", 2, "IOREF", PowerPin(PowerRole.Ioref, Value(typ = "5", unit = "V"), condition = ConditionKey.ConnectedTo5V)),
        power("power", 3, "RESET", reset),
        power("power", 4, "3V3", PowerPin(PowerRole.Supply3V3, Value(typ = "3.3", unit = "V"), condition = ConditionKey.Nominal)),
        power("power", 5, "5V", nominal5V),
        power("power", 6, "GND", ground),
        power("power", 7, "GND", ground),
        power("power", 8, "VIN", PowerPin(PowerRole.Vin, range("6", "20", "V"))),
        gpio("analog", 1, "A0", "PC0"),
        gpio("analog", 2, "A1", "PC1"),
        gpio("analog", 3, "A2", "PC2"),
        gpio("analog", 4, "A3", "PC3"),
        gpio("analog", 5, "A4", "PC4"),
        gpio("analog", 6, "A5", "PC5"),
        gpio("digital-low", 1, "D0", "PD0"),
        gpio("digital-low", 2, "D1", "PD1"),
        gpio("digital-low", 3, "D2", "PD2"),
        gpio("digital-low", 4, "D3", "PD3"),
        gpio("digital-low", 5, "D4", "PD4"),
        gpio("digital-low", 6, "D5", "PD5"),
        gpio("digital-low", 7, "D6", "PD6"),
        gpio("digital-low", 8, "D7", "PD7"),
        gpio("digital-high", 1, "D8", "PB0"),
        gpio("digital-high", 2, "D9", "PB1"),
        gpio("digital-high", 3, "D10", "PB2"),
        gpio("digital-high", 4, "D11", "PB3"),
        gpio("digital-high", 5, "D12", "PB4"),
        gpio("digital-high", 6, "D13", "PB5"),
        power("digital-high", 7, "GND", ground),
        power("digital-high", 8, "AREF", PowerPin(PowerRole.Aref, atmegaAref, condition = ConditionKey.UpToAvcc)),
        gpio("digital-high", 9, "SDA", "PC4"),
        gpio("digital-high", 10, "SCL", "PC5"),
        gpio("icsp-odd", 1, "MISO", "PB4", number = "1"),
        power("icsp-even", 1, "5V", nominal5V, number = "2"),
        gpio("icsp-odd", 2, "SCK", "PB5", number = "3"),
        gpio("icsp-even", 2, "MOSI", "PB3", number = "4"),
        power("icsp-odd", 3, "RESET", reset, number = "5"),
        power("icsp-even", 3, "GND", ground, number = "6"),
    ),
    limits = limits {
        atmegaLimits()
        add(listOf("PD0", "PD1"), Limit(LimitKind.BoardUse, use = BoardUse.UsbSerial))
        add("PB5", Limit(LimitKind.BoardUse, use = BoardUse.Led))
    },
    groupLimits = atmegaGroupLimits,
    functions = atmegaFunctions(),
    notes = emptyList(),
    sources = unoSources,
    otherFunctions = atmegaOthers,
)
