package io.github.kolod.circuitopedia.modules.boardpinouts.core

import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.boards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Integrity of the transcribed board data (research R8, SC-003). */
class BoardDataTest {

    private fun each(check: (Board) -> Unit) = boards.forEach { board ->
        try {
            check(board)
        } catch (e: AssertionError) {
            throw AssertionError("${board.id.value}: ${e.message}", e)
        }
    }

    @Test
    fun sevenBoardsInSpecOrder() {
        assertEquals(
            listOf("uno-r3", "nano", "pico-w", "pico2-w", "rpi4b", "blue-pill", "black-pill"),
            boards.map { it.id.value },
        )
    }

    @Test
    fun everyLayoutPositionHasExactlyOnePin() = each { board ->
        val positions = board.pins.map { it.header to it.position }
        assertEquals("duplicate positions", positions.size, positions.toSet().size)
        val expected = board.layout.headers.flatMap { h -> (1..h.count).map { h.id to it } }.toSet()
        assertEquals("positions vs layout", expected, positions.toSet())
    }

    @Test
    fun everyPinIsNamedAndIsEitherAChipPinOrPower() = each { board ->
        board.pins.forEach { pin ->
            val where = "${pin.header}/${pin.position}"
            assertTrue("$where label", pin.label.isNotBlank())
            assertTrue("$where chip pin xor power", (pin.pin == null) != (pin.power == null))
            pin.power?.let {
                if (it.role !in setOf(PowerRole.Ground, PowerRole.NotConnected, PowerRole.Run, PowerRole.Enable, PowerRole.Reset)) assertTrue("$where range", it.range != null)
            }
        }
    }

    @Test
    fun everyChipPinHasExactlyOneVoltageClass() = each { board ->
        board.chipPins.forEach { pin ->
            val kinds = board.limitsOf(pin).map { it.kind }
            val classes = kinds.count { it == LimitKind.FiveVoltTolerant || it == LimitKind.ThreeVoltOnly }
            assertEquals("${pin.name} 5V/3.3V class", 1, classes)
        }
        assertTrue("limits only for header pins", board.chipPins.containsAll(board.limits.keys))
    }

    @Test
    fun everyLimitHasItsRequiredFields() = each { board ->
        board.limits.forEach { (pin, limits) ->
            limits.forEach { limit ->
                val ok = when (limit.kind) {
                    LimitKind.BoardUse -> limit.use != null
                    LimitKind.BoardPull -> limit.value != null && limit.condition != null
                    LimitKind.BootRole -> limit.effect != null
                    LimitKind.CurrentLimit -> limit.value != null
                    LimitKind.Erratum -> !limit.erratum.isNullOrBlank()
                    else -> true
                }
                assertTrue("${pin.name} ${limit.kind} fields", ok)
            }
        }
    }

    @Test
    fun placementsAreWellFormed() = each { board ->
        val ids = board.placements.map { it.id }
        assertEquals("unique placement ids", ids.size, ids.toSet().size)
        board.placements.forEach { placement ->
            val name = placement.id.value
            val signals = placement.slots.map { it.signal }
            assertEquals("$name unique signals", signals.size, signals.toSet().size)
            assertTrue("$name has slots", placement.slots.isNotEmpty())
            placement.slots.forEach { slot ->
                assertTrue("$name ${slot.signal} candidates", slot.candidates.isNotEmpty())
                slot.candidates.forEach { assertTrue("$name ${slot.signal} ${it.name} on header", it in board.chipPins) }
                if (placement.movesAsGroup) assertEquals("$name ${slot.signal} group = one pin", 1, slot.candidates.size)
            }
            assertTrue("$name source", placement.source.document.isNotBlank() && placement.source.section.isNotBlank())
            assertTrue("$name source is listed", board.sources.any { it.document == placement.source.document })
        }
        board.functions.forEach { assertTrue("${it.kind} has placements", it.placements.isNotEmpty()) }
        assertEquals("one entry per function kind", board.functions.size, board.functions.map { it.kind }.toSet().size)
    }

    @Test
    fun everyBoardCitesBoardAndChipDocuments() = each { board ->
        assertTrue("two sources", board.sources.size >= 2)
        board.sources.forEach {
            if (it.maker.isBlank() || it.document.isBlank() || it.title.isBlank() || it.revision.isBlank()) {
                fail("incomplete source $it")
            }
        }
    }
}
