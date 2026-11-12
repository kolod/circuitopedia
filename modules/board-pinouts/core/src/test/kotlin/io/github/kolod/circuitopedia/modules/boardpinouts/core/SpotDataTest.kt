package io.github.kolod.circuitopedia.modules.boardpinouts.core

import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Known facts per board, each checked against the source table named in the test. */
class SpotDataTest {

    private fun b(id: String) = board(BoardId(id))!!
    private fun Board.p(id: String) = placement(PlacementId(id))!!
    private fun Board.has(pin: String, kind: LimitKind) = limitsOf(ChipPin(pin)).any { it.kind == kind }
    private fun Placement.pins(signal: String) = slot(signal)!!.candidates.map { it.name }

    @Test
    fun pi4I2c1OnGpio2And3WithBoardPullUps_Table5_GpioDoc() {
        val pi = b("rpi4b")
        val i2c1 = pi.p("i2c1")
        assertEquals(listOf("GPIO2"), i2c1.pins("SDA"))
        assertEquals(listOf("GPIO3"), i2c1.pins("SCL"))
        val pull = pi.limitsOf(ChipPin("GPIO2")).single { it.kind == LimitKind.BoardPull }
        assertEquals(Value(typ = "1.8", unit = "kΩ"), pull.value)
        assertEquals(listOf("3", "5"), pi.headerPinsOf(ChipPin("GPIO2")).map { it.number } + pi.headerPinsOf(ChipPin("GPIO3")).map { it.number })
    }

    @Test
    fun pi4I2c3HasTwoPinPairsOfOneInstance_OverlaysReadme() {
        val pi = b("rpi4b")
        assertEquals(listOf("GPIO2", "GPIO3"), pi.p("i2c3-2-3").slots.flatMap { it.candidates }.map { it.name })
        assertEquals(listOf("GPIO4", "GPIO5"), pi.p("i2c3-4-5").slots.flatMap { it.candidates }.map { it.name })
        assertEquals(pi.p("i2c3-2-3").instance, pi.p("i2c3-4-5").instance)
        assertTrue(pi.chipPins.none { pi.has(it.name, LimitKind.FiveVoltTolerant) })
    }

    @Test
    fun bluePillI2c1RemapMovesAsGroup_RM0008Table55() {
        val i2c = b("blue-pill").p("i2c1-remap")
        assertTrue(i2c.movesAsGroup)
        assertEquals(listOf("PB8"), i2c.pins("SCL"))
        assertEquals(listOf("PB9"), i2c.pins("SDA"))
    }

    @Test
    fun bluePillFiveVoltTolerance_DS5319Table5() {
        val bp = b("blue-pill")
        assertTrue(bp.has("PA0", LimitKind.ThreeVoltOnly))
        assertTrue(bp.has("PB5", LimitKind.ThreeVoltOnly))
        assertTrue(bp.has("PB10", LimitKind.FiveVoltTolerant))
        assertTrue(bp.has("PA12", LimitKind.FiveVoltTolerant))
    }

    @Test
    fun bluePillUsbAndCanShareTheSram_RM0008Section23_2() {
        val bp = b("blue-pill")
        val usb = bp.p("usb").resources
        assertTrue(usb.isNotEmpty())
        assertTrue(bp.p("can").resources.containsAll(usb))
        assertTrue(bp.p("can-remap").resources.containsAll(usb))
    }

    @Test
    fun nanoA6AndA7AreInputOnlyAnalog_DS40002061B_1_1_9() {
        val nano = b("nano")
        listOf("ADC6", "ADC7").forEach {
            assertTrue(nano.has(it, LimitKind.InputOnly))
            assertTrue(nano.has(it, LimitKind.Analog))
        }
        assertEquals(listOf("A6"), nano.headerPinsOf(ChipPin("ADC6")).map { it.label })
    }

    @Test
    fun unoSdaHeaderPinIsA4_A000066Section5_2() {
        val uno = b("uno-r3")
        assertEquals(listOf("A4", "SDA"), uno.headerPinsOf(ChipPin("PC4")).map { it.label })
    }

    @Test
    fun picoWPwm0AOnGp0AndGp16_RP2040Table279() {
        val pwm0 = b("pico-w").p("pwm0")
        assertEquals(listOf("GP0", "GP16"), pwm0.pins("A"))
        assertFalse(pwm0.slot("A")!!.required)
    }

    @Test
    fun pico2WFiveVoltToleranceExceptAdcPins_RP2350Table1674() {
        val pico2 = b("pico2-w")
        val ft = pico2.limitsOf(ChipPin("GP0")).single { it.kind == LimitKind.FiveVoltTolerant }
        assertEquals(ConditionKey.IovddPowered, ft.condition)
        assertTrue(pico2.has("GP26", LimitKind.ThreeVoltOnly))
        assertTrue(b("pico-w").has("GP0", LimitKind.ThreeVoltOnly))
    }

    @Test
    fun pico2WUartTxAlsoOnCtsPins_RP2350Table677() {
        assertEquals(listOf("GP0", "GP2", "GP12", "GP14", "GP16", "GP18", "GP28"), b("pico2-w").p("uart0").pins("TX"))
    }

    @Test
    fun blackPillBoardUses_WeActV31Schematic() {
        val bp = b("black-pill")
        assertEquals(BoardUse.Led, bp.limitsOf(ChipPin("PC13")).single { it.kind == LimitKind.BoardUse }.use)
        assertEquals(BoardUse.Button, bp.limitsOf(ChipPin("PA0")).single { it.kind == LimitKind.BoardUse }.use)
        assertTrue(bp.has("PA0", LimitKind.ThreeVoltOnly))
        assertTrue(bp.has("PB5", LimitKind.ThreeVoltOnly))
    }

    @Test
    fun blackPillI2c1PerSignal_DS10314Table9() {
        val i2c1 = b("black-pill").p("i2c1")
        assertEquals(listOf("PB6", "PB8"), i2c1.pins("SCL"))
        assertEquals(listOf("PB7", "PB9"), i2c1.pins("SDA"))
        assertFalse(i2c1.movesAsGroup)
    }
}
