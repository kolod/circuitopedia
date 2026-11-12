package io.github.kolod.circuitopedia.modules.boardpinouts.core

import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.boards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The planner rules of research R3 on a small hand-made board, then swept over the real boards. */
class PlannerTest {

    private val src = SourceRef("test", "t")
    private fun s(signal: String, vararg pins: String, required: Boolean = true) = SignalSlot(signal, pins.map(::ChipPin), required)
    private fun p(id: String, instance: String, vararg slots: SignalSlot, group: Boolean = false, resources: Set<String> = emptySet()) =
        Placement(PlacementId(id), instance, slots.toList(), src, group, resources.map(::ResourceId).toSet())

    // A tiny board: P1..P6 on one header.
    private val uartA = p("uart-a", "UART1", s("TX", "P1"), s("RX", "P2"), group = true)
    private val uartB = p("uart-b", "UART1", s("TX", "P3"), s("RX", "P4"), group = true)
    private val i2c = p("i2c", "I2C1", s("SDA", "P2", "P5"), s("SCL", "P6"))
    private val can = p("can", "CAN", s("RX", "P3"), s("TX", "P4"), resources = setOf("sram"))
    private val usb = p("usb", "USB", s("DM", "P5"), s("DP", "P6"), resources = setOf("sram"))
    private val pwm = p("pwm", "PWM0", s("A", "P1", "P5", required = false), s("B", "P6", required = false))

    private val board = Board(
        id = BoardId("test"),
        name = "Test",
        chip = "T",
        layout = BoardLayout(2f, 7f, listOf(Header("h", 0f, 0f, Direction.Down, 6)), Side.Top, ViewSide.TopUsbUp),
        pins = (1..6).map { HeaderPin("h", it, "P$it", pin = ChipPin("P$it")) },
        limits = mapOf(
            ChipPin("P5") to listOf(Limit(LimitKind.BoardUse, use = BoardUse.Led)),
            ChipPin("P6") to listOf(Limit(LimitKind.BootRole, effect = BootEffect.BootMemorySelect)),
        ),
        groupLimits = emptyList(),
        functions = listOf(
            BoardFunction(FunctionKind.Uart, listOf(uartA, uartB)),
            BoardFunction(FunctionKind.I2c, listOf(i2c)),
            BoardFunction(FunctionKind.Can, listOf(can)),
            BoardFunction(FunctionKind.Usb, listOf(usb)),
            BoardFunction(FunctionKind.Pwm, listOf(pwm)),
        ),
        notes = emptyList(),
        sources = emptyList(),
    )

    private fun assign(vararg a: Pair<Placement, Map<String, String?>>) =
        a.map { (placement, chosen) -> Assignment(placement.id, chosen.mapValues { (_, v) -> v?.let(::ChipPin) }) }

    private fun default(placement: Placement, assignments: List<Assignment> = emptyList()) =
        board.defaultAssignment(assignments, placement)!!

    @Test
    fun aFreePlacementIsAvailable() {
        assertEquals(Availability.Available, board.availability(emptyList(), uartA))
    }

    @Test
    fun theSameInstanceCannotBeAssignedTwice() {
        val a = listOf(default(uartA))
        val v = board.availability(a, uartB) as Availability.Blocked
        assertTrue(Conflict.SameInstance(uartA.id) in v.reasons)
    }

    @Test
    fun aPinTakenByAnotherAssignmentBlocksAGroupPlacement() {
        val a = listOf(default(uartA))
        // i2c SDA can still use P5 instead of P2, so i2c stays available (with the P5 LED warning)
        assertTrue(board.availability(a, i2c) is Availability.Warned)
        val b = assign(uartA to mapOf("TX" to "P1", "RX" to "P2"), pwm to mapOf("A" to "P5", "B" to null))
        val v = board.availability(b, i2c) as Availability.Blocked
        assertTrue(Conflict.PinTaken(ChipPin("P2"), uartA.id, "RX") in v.reasons)
        assertTrue(Conflict.PinTaken(ChipPin("P5"), pwm.id, "A") in v.reasons)
    }

    @Test
    fun aSharedResourceBlocks() {
        val a = listOf(default(can))
        val v = board.availability(a, usb) as Availability.Blocked
        assertEquals(listOf(Conflict.ResourceTaken(ResourceId("sram"), can.id)), v.reasons)
    }

    @Test
    fun boardUseAndBootPinsWarnOnlyWhenNothingBlocks() {
        val v = board.availability(emptyList(), usb) as Availability.Warned
        assertEquals(
            listOf(Warning.PinBoardUse(ChipPin("P5"), BoardUse.Led), Warning.PinBootRole(ChipPin("P6"), BootEffect.BootMemorySelect)),
            v.warnings,
        )
    }

    @Test
    fun anAssignmentNeverConflictsWithItself() {
        val a = listOf(default(uartA))
        assertEquals(Availability.Available, board.availability(a, uartA))
        assertEquals(Availability.Available, board.pinAvailability(a, uartA, "TX", ChipPin("P1")))
    }

    @Test
    fun defaultAssignmentTakesTheFirstFreeCandidateAndLeavesOptionalSlotsUnused() {
        assertEquals(mapOf("SDA" to ChipPin("P2"), "SCL" to ChipPin("P6")), default(i2c).chosen)
        val taken = listOf(default(uartA))
        assertEquals(ChipPin("P5"), default(i2c, taken).chosen["SDA"])
        // all-optional placements start with the first slot that has a free pin
        assertEquals(mapOf("A" to ChipPin("P1"), "B" to null), default(pwm).chosen)
        assertEquals(mapOf("A" to ChipPin("P5"), "B" to null), default(pwm, taken).chosen)
        assertNull(board.defaultAssignment(listOf(default(can)), uartB)) // CAN holds P3 and P4
    }

    @Test
    fun pinAvailabilityJudgesEachCandidate() {
        val a = listOf(default(uartA))
        val v = board.pinAvailability(a, i2c, "SDA", ChipPin("P2")) as Availability.Blocked
        assertEquals(listOf(Conflict.PinTaken(ChipPin("P2"), uartA.id, "RX")), v.reasons)
        assertTrue(board.pinAvailability(a, i2c, "SDA", ChipPin("P5")) is Availability.Warned)
        // within one assignment, a pin used by another signal is taken too
        val own = assign(pwm to mapOf("A" to "P5", "B" to null))
        val w = board.pinAvailability(own, i2c, "SDA", ChipPin("P5")) as Availability.Blocked
        assertEquals(listOf(Conflict.PinTaken(ChipPin("P5"), pwm.id, "A")), w.reasons)
    }

    @Test
    fun claimsListUsedPinsOnly() {
        val a = assign(uartA to mapOf("TX" to "P1", "RX" to null), i2c to mapOf("SDA" to "P5", "SCL" to "P6"))
        assertEquals(
            mapOf(ChipPin("P1") to (uartA.id to "TX"), ChipPin("P5") to (i2c.id to "SDA"), ChipPin("P6") to (i2c.id to "SCL")),
            board.claims(a),
        )
    }

    @Test
    fun unuseReleasesASlotButNeverTheLastOne() {
        val a = default(uartA)
        val one = a.unuse("RX", uartA)!! // a group placement may release a required slot
        assertEquals(mapOf("TX" to ChipPin("P1"), "RX" to null), one.chosen)
        assertNull(one.unuse("TX", uartA))
        val i = default(i2c)
        assertEquals(i, i.unuse("SDA", i2c)) // a required slot outside a group stays
        assertEquals(ChipPin("P5"), i.use("SDA", ChipPin("P5")).chosen["SDA"])
    }

    // SC-002: pairwise sweep over every real board.

    @Test
    fun everyPairOfPlacementsConflictsExactlyWhenTheRulesSay() = boards.forEach { b ->
        b.placements.forEach { first ->
            val a = b.defaultAssignment(emptyList(), first) ?: return@forEach
            val held = a.chosen.values.filterNotNull().toSet()
            b.placements.filter { it.id != first.id }.forEach { second ->
                val verdict = b.availability(listOf(a), second)
                val sameInstance = second.instance == first.instance
                val sharedResource = second.resources.any { it in first.resources }
                val free = { slot: SignalSlot -> slot.candidates.any { it !in held } }
                val pinsBlock = if (second.slots.any { it.required }) !second.slots.filter { it.required }.all(free) else !second.slots.any(free)
                val shouldBlock = sameInstance || sharedResource || pinsBlock
                val where = "${b.id.value}: ${first.id.value} then ${second.id.value}"
                assertEquals(where, shouldBlock, verdict is Availability.Blocked)
                if (!shouldBlock) {
                    val pins = b.defaultAssignment(listOf(a), second)!!.chosen.values.filterNotNull()
                    val warns = pins.any { pin -> b.limitsOf(pin).any { it.kind == LimitKind.BoardUse || it.kind == LimitKind.BootRole } }
                    assertEquals(where, warns, verdict is Availability.Warned)
                }
            }
        }
    }
}
