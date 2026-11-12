package io.github.kolod.circuitopedia.modules.boardpinouts.core

import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.boards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StateCodecTest {

    private val pi = BoardId("rpi4b")
    private val bluePill = BoardId("blue-pill")

    private val sample = mapOf(
        pi to listOf(
            Assignment(PlacementId("i2c1"), mapOf("SDA" to ChipPin("GPIO2"), "SCL" to ChipPin("GPIO3"))),
            Assignment(PlacementId("uart0"), mapOf("TXD" to ChipPin("GPIO14"), "RXD" to ChipPin("GPIO15"), "CTS" to null, "RTS" to null)),
        ),
        bluePill to listOf(Assignment(PlacementId("i2c1-remap"), mapOf("SCL" to ChipPin("PB8"), "SDA" to null, "SMBA" to null))),
    )

    @Test
    fun roundTrip() = assertEquals(sample, decode(encode(sample)))

    @Test
    fun emptyRoundTrip() = assertEquals(emptyMap<BoardId, List<Assignment>>(), decode(encode(emptyMap())))

    @Test
    fun everyDefaultAssignmentRoundTrips() {
        val all = boards.associate { b -> b.id to b.placements.mapNotNull { b.defaultAssignment(emptyList(), it) } }
        assertEquals(all, decode(encode(all)))
    }

    @Test
    fun unknownBoardsPlacementsAndPinsAreDropped() {
        val text = encode(sample) + ";nope=x:A@B" + ";rpi4b=ghost:SDA@GPIO2"
        val decoded = decode(text)
        assertEquals(sample[bluePill], decoded[bluePill])
        assertEquals(sample[pi], decoded[pi]!!.filter { it.placement.value != "ghost" })
        assertTrue(BoardId("nope") !in decoded)
    }

    @Test
    fun malformedTextGivesAnEmptyMap() {
        listOf("", "garbage", "rpi4b", "rpi4b=", "=;=", "rpi4b=i2c1:SDA").forEach { text ->
            assertTrue(text, decode(text).values.all { it.isEmpty() })
        }
    }
}
