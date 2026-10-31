package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.DecimalPoint
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.Digit
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.Multiplier
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole.Tolerance
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Green
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionTest {
    private fun four(vararg colors: BandColor) = Selection(BandLayout.Four, colors.toList())
    private fun three(vararg colors: BandColor) = Selection(BandLayout.Three, colors.toList())
    private fun Selection.roles() = List(layout.bandCount) { roleOf(it) }

    // US1

    @Test
    fun defaults() {
        assertEquals(listOf(Yellow, Violet, Black, Silver), Selection.default(BandLayout.Four).colors)
        assertEquals(listOf(Yellow, Violet, Black), Selection.default(BandLayout.Three).colors)
    }

    @Test
    fun rolesWithoutAPoint() {
        val selection = Selection.default(BandLayout.Four)
        assertNull(selection.pointIndex)
        assertEquals(listOf(Digit, Digit, Multiplier, Tolerance), selection.roles())
    }

    @Test
    fun withSetsAValidBand() {
        assertEquals(listOf(Brown, Violet, Black, Silver), Selection.default(BandLayout.Four).with(0, Brown).colors)
        assertEquals(listOf(Yellow, Violet, Gold, Silver), Selection.default(BandLayout.Four).with(2, Gold).colors)
    }

    @Test
    fun invalidTapsAreIgnored() {
        val selection = Selection.default(BandLayout.Four)
        assertSame(selection, selection.with(0, Black))
        assertSame(selection, selection.with(0, Silver))
        assertSame(selection, selection.with(2, Green))
        assertSame(selection, selection.with(3, Red))
        assertFalse(selection.canSelect(0, Black))
        assertTrue(selection.canSelect(0, Brown))
    }

    @Test
    fun invalidSelectionsCannotBeBuilt() {
        assertThrows(IllegalArgumentException::class.java) { four(Black, Violet, Black, Silver) }
        assertThrows(IllegalArgumentException::class.java) { four(Gold, Gold, Black, Silver) }
        assertThrows(IllegalArgumentException::class.java) { four(Yellow, Violet, Green, Silver) }
        assertThrows(IllegalArgumentException::class.java) { four(Yellow, Violet, Black, Red) }
        assertThrows(IllegalArgumentException::class.java) { three(Yellow, Violet) }
    }

    // US2: the decimal point

    @Test
    fun rolesWithThePointInBandTwo() {
        val selection = three(Yellow, Gold, Violet)
        assertEquals(1, selection.pointIndex)
        assertEquals(listOf(Digit, DecimalPoint, Digit), selection.roles())
    }

    @Test
    fun rolesWithThePointInBandOne() {
        val selection = four(Gold, Red, Red, Silver)
        assertEquals(0, selection.pointIndex)
        assertEquals(listOf(DecimalPoint, Digit, Digit, Tolerance), selection.roles())
    }

    @Test
    fun removingThePointResetsAnInvalidMultiplier() {
        assertEquals(listOf(Yellow, Violet, Black, Silver), four(Yellow, Gold, Violet, Silver).with(1, Violet).colors)
    }

    @Test
    fun movingThePointToBandOneResetsBandTwo() {
        assertEquals(listOf(Gold, Violet, Violet, Silver), four(Yellow, Gold, Violet, Silver).with(0, Gold).colors)
    }

    @Test
    fun movingThePointToBandTwoResetsBandOne() {
        assertEquals(listOf(Yellow, Gold, Red, Silver), four(Gold, Red, Red, Silver).with(1, Gold).colors)
    }

    @Test
    fun settingThePointKeepsAValidDigitInBandThree() {
        // Black is a valid digit, so it stays; Gold as a multiplier would not.
        assertEquals(listOf(Yellow, Gold, Black), three(Yellow, Violet, Black).with(1, Gold).colors)
        assertEquals(listOf(Yellow, Gold, Black), three(Yellow, Violet, Gold).with(1, Gold).colors)
    }

    @Test
    fun firstSignificantDigitIsNeverBlack() {
        val pointFirst = four(Gold, Red, Red, Silver)
        assertFalse(pointFirst.canSelect(1, Black))
        assertTrue(pointFirst.canSelect(2, Black))
        assertThrows(IllegalArgumentException::class.java) { four(Gold, Black, Red, Silver) }
        // Setting the point in Band 1 while Band 2 is Black replaces Band 2 with its default.
        assertEquals(listOf(Gold, Violet, Black), three(Brown, Black, Black).with(0, Gold).colors)
    }

    @Test
    fun bandThreeOffersDigitsOnlyWithAPoint() {
        val withPoint = three(Yellow, Gold, Violet)
        assertTrue(withPoint.canSelect(2, Green))
        assertFalse(withPoint.canSelect(2, Gold))
        assertFalse(withPoint.canSelect(2, Silver))
        val withoutPoint = three(Yellow, Violet, Black)
        assertFalse(withoutPoint.canSelect(2, Green))
        assertTrue(withoutPoint.canSelect(2, Silver))
    }

    @Test
    fun bothPointCellsStaySelectable() {
        val selection = four(Yellow, Gold, Violet, Silver)
        assertTrue(selection.canSelect(0, Gold))
        assertTrue(selection.canSelect(1, Gold))
    }
}
