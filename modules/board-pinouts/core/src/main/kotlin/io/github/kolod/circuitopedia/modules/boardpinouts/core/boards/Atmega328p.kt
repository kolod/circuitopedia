package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardFunction
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.FunctionKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupLimit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.NoteKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Source
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef
import io.github.kolod.circuitopedia.modules.boardpinouts.core.max
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value

// ATmega328P facts shared by the Uno R3 and the Nano. Source: Microchip DS40002061B, "ATmega48A/PA/
// 88A/PA/168A/PA/328/P megaAVR Data Sheet", Rev. B 09/2020:
// - Tables 14-3, 14-6 and 14-9: alternate functions of ports B, C and D;
// - §29.1: absolute maximum ratings (40.0mA per I/O pin, 200.0mA per VCC and GND pin);
// - Table 30-1, notes 3 and 4: the port current sums (the only common DC table in this revision);
// - Table 29-15: ADC reference voltage 1.0V to AVCC.

internal val atmega328pSource = Source(
    maker = "Microchip",
    document = "DS40002061B",
    title = "ATmega48A/PA/88A/PA/168A/PA/328/P megaAVR Data Sheet",
    revision = "Rev. B, 09/2020",
)

private val portB = SourceRef("DS40002061B", "Table 14-3")
private val portC = SourceRef("DS40002061B", "Table 14-6")
private val portD = SourceRef("DS40002061B", "Table 14-9")

/** The ATmega328P port pins brought out on both boards (PB6/PB7 carry the crystal, PC6 is RESET). */
internal val atmegaIo = listOf(
    "PD0", "PD1", "PD2", "PD3", "PD4", "PD5", "PD6", "PD7",
    "PB0", "PB1", "PB2", "PB3", "PB4", "PB5",
    "PC0", "PC1", "PC2", "PC3", "PC4", "PC5",
)

internal val atmegaAdc = listOf("PC0", "PC1", "PC2", "PC3", "PC4", "PC5")

/** Limits common to both boards; the board adds its own board uses. */
internal fun Limits.atmegaLimits() {
    add(atmegaIo, Limit(LimitKind.FiveVoltTolerant, condition = ConditionKey.FiveVoltSupply))
    add(atmegaIo, Limit(LimitKind.CurrentLimit, max("40.0", "mA"), condition = ConditionKey.AbsoluteMaximum))
    add(atmegaAdc, Limit(LimitKind.Analog))
}

internal val atmegaGroupLimits = listOf(
    GroupLimit(pins("PC0", "PC1", "PC2", "PC3", "PC4", "PC5", "PD0", "PD1", "PD2", "PD3", "PD4").toSet(), max("150", "mA"), GroupKey.SourceSum),
    GroupLimit(pins("PB0", "PB1", "PB2", "PB3", "PB4", "PB5", "PD5", "PD6", "PD7").toSet(), max("150", "mA"), GroupKey.SourceSum),
    GroupLimit(pins("PC0", "PC1", "PC2", "PC3", "PC4", "PC5").toSet(), max("100", "mA"), GroupKey.SinkSum),
    GroupLimit(pins("PB0", "PB1", "PB2", "PB3", "PB4", "PB5", "PD5", "PD6", "PD7").toSet(), max("100", "mA"), GroupKey.SinkSum),
    GroupLimit(pins("PD0", "PD1", "PD2", "PD3", "PD4").toSet(), max("100", "mA"), GroupKey.SinkSum),
    GroupLimit(pins(*atmegaIo.toTypedArray()).toSet(), max("200.0", "mA"), GroupKey.SupplyPin),
)

/** Table 29-15: V_REF from 1.0V up to AVCC (shown with the UpToAvcc condition). */
internal val atmegaAref = Value(min = "1.0", unit = "V")

/** Planner functions; [extraAdc] adds the Nano's ADC6/ADC7. Routing is fixed: one placement each. */
internal fun atmegaFunctions(extraAdc: List<String> = emptyList()) = listOf(
    BoardFunction(
        FunctionKind.Uart,
        listOf(placement("usart0", "USART0", portD, slot("RXD", "PD0"), slot("TXD", "PD1"), opt("XCK", "PD4"), notes = listOf(NoteKey.UsbSerialShared))),
    ),
    BoardFunction(FunctionKind.I2c, listOf(placement("twi", "TWI", portC, slot("SDA", "PC4"), slot("SCL", "PC5")))),
    BoardFunction(
        FunctionKind.Spi,
        listOf(
            placement(
                "spi", "SPI", portB,
                slot("SCK", "PB5"), slot("MOSI", "PB3"), opt("MISO", "PB4"), opt("SS", "PB2"),
                notes = listOf(NoteKey.SsMustBeOutput),
            ),
        ),
    ),
    BoardFunction(
        FunctionKind.Pwm,
        listOf(
            placement("timer0", "Timer/Counter0", portD, opt("OC0A", "PD6"), opt("OC0B", "PD5"), notes = listOf(NoteKey.Timer0UsedByMillis)),
            placement("timer1", "Timer/Counter1", portB, opt("OC1A", "PB1"), opt("OC1B", "PB2")),
            placement("timer2", "Timer/Counter2", portB, opt("OC2A", "PB3"), opt("OC2B", "PD3")),
        ),
    ),
    BoardFunction(
        FunctionKind.Adc,
        listOf(
            placement(
                "adc", "ADC", portC,
                *(atmegaAdc.mapIndexed { i, pin -> opt("ADC$i", pin) } + extraAdc.map { opt(it, it) }).toTypedArray(),
            ),
        ),
    ),
)

internal val atmegaOthers = others(
    "PB0" to listOf("ICP1", "CLKO", "PCINT0"),
    "PB1" to listOf("PCINT1"),
    "PB2" to listOf("PCINT2"),
    "PB3" to listOf("PCINT3"),
    "PB4" to listOf("PCINT4"),
    "PB5" to listOf("PCINT5"),
    "PC0" to listOf("PCINT8"),
    "PC1" to listOf("PCINT9"),
    "PC2" to listOf("PCINT10"),
    "PC3" to listOf("PCINT11"),
    "PC4" to listOf("PCINT12"),
    "PC5" to listOf("PCINT13"),
    "PD0" to listOf("PCINT16"),
    "PD1" to listOf("PCINT17"),
    "PD2" to listOf("INT0", "PCINT18"),
    "PD3" to listOf("INT1", "PCINT19"),
    "PD4" to listOf("T0", "PCINT20"),
    "PD5" to listOf("T1", "PCINT21"),
    "PD6" to listOf("AIN0", "PCINT22"),
    "PD7" to listOf("AIN1", "PCINT23"),
)
