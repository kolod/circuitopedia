package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Black
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Brown
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Gold
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Red
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Silver
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Violet
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.White
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor.Yellow
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Digit
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Multiplier
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.TemperatureCoefficient
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole.Tolerance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionTest {

    @Test
    fun layoutRoles() {
        assertEquals(listOf(Digit, Digit, Multiplier), BandLayout.Three.roles)
        assertEquals(listOf(Digit, Digit, Multiplier, Tolerance), BandLayout.Four.roles)
        assertEquals(listOf(Digit, Digit, Digit, Multiplier, Tolerance), BandLayout.Five.roles)
        assertEquals(listOf(Digit, Digit, Digit, Multiplier, Tolerance, TemperatureCoefficient), BandLayout.Six.roles)
        assertEquals(listOf(3, 4, 5, 6), BandLayout.entries.map { it.bandCount })
    }

    @Test
    fun blackIsNeverTheFirstDigit() {
        for (layout in BandLayout.entries) {
            assertFalse("$layout", layout.accepts(0, Black))
            assertTrue("$layout", layout.accepts(1, Black))
        }
        assertTrue(BandLayout.Five.accepts(2, Black))
    }

    @Test
    fun goldAndSilverAreNotDigits() {
        for (layout in BandLayout.entries) {
            layout.roles.forEachIndexed { i, role ->
                if (role == Digit) {
                    assertFalse(layout.accepts(i, Gold))
                    assertFalse(layout.accepts(i, Silver))
                }
            }
        }
    }

    @Test
    fun multiplierAcceptsEveryColor() {
        for (color in BandColor.entries) assertTrue("$color", BandLayout.Four.accepts(2, color))
    }

    @Test
    fun toleranceAndTcrBandsRejectColorsWithoutAMeaning() {
        assertFalse(BandLayout.Four.accepts(3, Black))
        assertFalse(BandLayout.Four.accepts(3, White))
        assertTrue(BandLayout.Four.accepts(3, Gold))
        assertFalse(BandLayout.Six.accepts(5, White))
        assertFalse(BandLayout.Six.accepts(5, Gold))
        assertFalse(BandLayout.Six.accepts(5, Silver))
        assertTrue(BandLayout.Six.accepts(5, Black))
    }

    @Test
    fun outOfRangeIndexIsRejected() {
        assertFalse(BandLayout.Three.accepts(3, Red))
        assertFalse(BandLayout.Three.accepts(-1, Red))
    }

    @Test
    fun defaultsMatchTheSpec() {
        assertEquals(listOf(Yellow, Violet, Red), Selection.default(BandLayout.Three).colors)
        assertEquals(listOf(Yellow, Violet, Red, Gold), Selection.default(BandLayout.Four).colors)
        assertEquals(listOf(Yellow, Violet, Black, Brown, Brown), Selection.default(BandLayout.Five).colors)
        assertEquals(listOf(Yellow, Violet, Black, Brown, Brown, Brown), Selection.default(BandLayout.Six).colors)
    }

    @Test
    fun withChangesOnlyTheGivenBand() {
        val changed = Selection.default(BandLayout.Four).with(2, Brown)
        assertEquals(listOf(Yellow, Violet, Brown, Gold), changed.colors)
    }

    @Test
    fun withIgnoresAnInvalidColor() {
        val default = Selection.default(BandLayout.Four)
        assertEquals(default, default.with(0, Black))
        assertEquals(default, default.with(3, White))
    }

    @Test
    fun constructorRejectsInvalidSelections() {
        assertThrows(IllegalArgumentException::class.java) { Selection(BandLayout.Four, listOf(Yellow, Violet, Red)) }
        assertThrows(IllegalArgumentException::class.java) { Selection(BandLayout.Four, listOf(Black, Violet, Red, Gold)) }
    }
}
