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
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max
import io.github.kolod.circuitopedia.modules.boardpinouts.core.range

// Arduino Nano (classic, ATmega328P). Sources:
// - Arduino schematic "NanoV3.3" (23/01/19): J1 and J2 nets, ICSP J4 (1 D12/MISO, 2 +5V, 3 D13/SCK,
//   4 D11/MOSI, 5 RESET, 6 GND), D0/D1 through 1kΩ to the FT232RL, D13 through 1kΩ to the "L" LED,
//   3V3 from the FT232RL, VIN into an LM1117IMPX-5.0. The A000005 manual's pin lists (Rev. 5) omit
//   AREF and D13 and repeat a number, so the schematic is followed.
// - TI SNOS412Q "LM1117 800-mA Low-Dropout Linear Regulator", Rev. Q (01/2023), §7.3: input voltage
//   15V max (recommended operating conditions), for VIN.
// - the ATmega328P datasheet, see Atmega328p.kt; §1.1.9: in the TQFP and VQFN packages ADC7:6 serve
//   as analog inputs to the A/D converter (they have no port function).
// Drawn from the top with USB at the top: J2 on the left and J1 on the right, pin 15 at the USB end.

private val nanoSources = listOf(
    Source("Arduino", "NanoV3.3", "Arduino Nano V3.3 schematic", "V3.3, 23/01/2019"),
    Source("Arduino", "A000005", "Arduino Nano User Manual", "Rev. 5, 20/10/2026"),
    Source("Texas Instruments", "SNOS412Q", "LM1117 800-mA Low-Dropout Linear Regulator", "Rev. Q, 01/2023"),
    atmega328pSource,
)

private val ground = PowerPin(PowerRole.Ground)
private val reset = PowerPin(PowerRole.Reset, range("-0.5", "13.0", "V"), condition = ConditionKey.AbsoluteMaximum)
private val nominal5V = PowerPin(PowerRole.Supply5V, Value(typ = "5", unit = "V"), condition = ConditionKey.Nominal)

val nano = Board(
    id = BoardId("nano"),
    name = "Arduino Nano",
    chip = "ATmega328P",
    layout = BoardLayout(
        width = 7f,
        height = 19f,
        headers = listOf(
            Header("J2", 0.5f, 15f, Direction.Up, 15),
            Header("J1", 6.5f, 15f, Direction.Up, 15),
            Header("icsp-odd", 3f, 16.5f, Direction.Right, 3),
            Header("icsp-even", 3f, 17.5f, Direction.Right, 3),
        ),
        usb = Side.Top,
        view = ViewSide.TopUsbUp,
    ),
    pins = listOf(
        gpio("J1", 1, "D1/TX", "PD1", number = "1"),
        gpio("J1", 2, "D0/RX", "PD0", number = "2"),
        power("J1", 3, "RESET", reset, number = "3"),
        power("J1", 4, "GND", ground, number = "4"),
        gpio("J1", 5, "D2", "PD2", number = "5"),
        gpio("J1", 6, "D3", "PD3", number = "6"),
        gpio("J1", 7, "D4", "PD4", number = "7"),
        gpio("J1", 8, "D5", "PD5", number = "8"),
        gpio("J1", 9, "D6", "PD6", number = "9"),
        gpio("J1", 10, "D7", "PD7", number = "10"),
        gpio("J1", 11, "D8", "PB0", number = "11"),
        gpio("J1", 12, "D9", "PB1", number = "12"),
        gpio("J1", 13, "D10", "PB2", number = "13"),
        gpio("J1", 14, "D11/MOSI", "PB3", number = "14"),
        gpio("J1", 15, "D12/MISO", "PB4", number = "15"),
        power("J2", 1, "VIN", PowerPin(PowerRole.Vin, max("15", "V")), number = "1"),
        power("J2", 2, "GND", ground, number = "2"),
        power("J2", 3, "RESET", reset, number = "3"),
        power("J2", 4, "+5V", nominal5V, number = "4"),
        gpio("J2", 5, "A7", "ADC7", number = "5"),
        gpio("J2", 6, "A6", "ADC6", number = "6"),
        gpio("J2", 7, "A5", "PC5", number = "7"),
        gpio("J2", 8, "A4", "PC4", number = "8"),
        gpio("J2", 9, "A3", "PC3", number = "9"),
        gpio("J2", 10, "A2", "PC2", number = "10"),
        gpio("J2", 11, "A1", "PC1", number = "11"),
        gpio("J2", 12, "A0", "PC0", number = "12"),
        power("J2", 13, "AREF", PowerPin(PowerRole.Aref, atmegaAref, condition = ConditionKey.UpToAvcc), number = "13"),
        power("J2", 14, "3V3", PowerPin(PowerRole.Supply3V3, Value(typ = "3.3", unit = "V"), condition = ConditionKey.Nominal), number = "14"),
        gpio("J2", 15, "D13/SCK", "PB5", number = "15"),
        gpio("icsp-odd", 1, "MISO", "PB4", number = "1"),
        gpio("icsp-odd", 2, "SCK", "PB5", number = "3"),
        power("icsp-odd", 3, "RESET", reset, number = "5"),
        power("icsp-even", 1, "+5V", nominal5V, number = "2"),
        gpio("icsp-even", 2, "MOSI", "PB3", number = "4"),
        power("icsp-even", 3, "GND", ground, number = "6"),
    ),
    limits = limits {
        atmegaLimits()
        add(listOf("ADC6", "ADC7"), Limit(LimitKind.FiveVoltTolerant, condition = ConditionKey.FiveVoltSupply))
        add(listOf("ADC6", "ADC7"), Limit(LimitKind.Analog))
        add(listOf("ADC6", "ADC7"), Limit(LimitKind.InputOnly))
        add(listOf("PD0", "PD1"), Limit(LimitKind.BoardUse, use = BoardUse.UsbSerial))
        add("PB5", Limit(LimitKind.BoardUse, use = BoardUse.Led))
    },
    groupLimits = atmegaGroupLimits,
    functions = atmegaFunctions(extraAdc = listOf("ADC6", "ADC7")),
    notes = emptyList(),
    sources = nanoSources,
    otherFunctions = atmegaOthers,
)
