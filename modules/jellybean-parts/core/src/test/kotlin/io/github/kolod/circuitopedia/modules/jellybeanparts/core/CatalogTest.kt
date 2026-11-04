package io.github.kolod.circuitopedia.modules.jellybeanparts.core

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Research R6: catalog integrity checks. They cannot catch a mistyped value; the audit does that. */
class CatalogTest {
    private val expectedIds = listOf(
        "lm358", "lm324", "tl072", "op07",
        "lm393", "lm339",
        "ne555",
        "2n3904", "2n3906", "pn2222a", "pn2907a", "bc548", "bd139", "bd140",
        "2n7000", "2n7002",
        "1n4148", "1n914", "1n4001_1n4007", "1n5817", "1n5819", "bat54", "sd103",
        "78xx", "79xx", "lm317", "lm337",
        "tl431",
        "74hc",
        "lm35", "tmp36",
        "mc34063", "uc384x", "icl7660",
    )

    private val families = setOf("1n4001_1n4007", "78xx", "79xx", "74hc", "uc384x")

    @Test
    fun entriesAreInSpecOrder() {
        assertEquals(expectedIds, catalog.map { it.id.value })
    }

    @Test
    fun categoriesFollowEnumOrder() {
        val ordinals = catalog.map { it.category.ordinal }
        assertEquals(ordinals.sorted(), ordinals)
        assertEquals(Category.entries.toList(), catalogByCategory().keys.toList())
        assertEquals(catalog, catalogByCategory().values.flatten())
    }

    @Test
    fun idsAreUniqueAndResolvable() {
        assertEquals(catalog.size, catalog.map { it.id }.toSet().size)
        catalog.forEach {
            assertTrue(it.id.value, it.id.value.matches(Regex("[a-z0-9_]+")))
            assertEquals(it, entry(it.id))
        }
        assertNull(entry(PartId("nope")))
    }

    @Test
    fun everySourceIsComplete() {
        catalog.forEach {
            assertTrue(it.id.value, it.source.maker.isNotBlank())
            assertTrue(it.id.value, it.source.document.isNotBlank())
            assertTrue(it.id.value, it.source.revision.isNotBlank())
        }
    }

    @Test
    fun everyValueHasUnitBoundAndOrder() {
        catalog.forEach { e ->
            assertTrue("${e.id.value} has < 3 parameters", e.parameters.size >= 3)
            val values = e.parameters.map { it.kind.name to it.value } +
                e.members.flatMap { m -> m.values.map { (k, v) -> "${m.name} ${k.name}" to v } }
            values.forEach { (what, v) -> checkValue("${e.id.value} $what", v) }
        }
    }

    private fun checkValue(what: String, v: Value) {
        assertTrue("$what unit", v.unit.isNotBlank())
        val bounds = listOfNotNull(v.min, v.typ, v.max)
        assertTrue("$what bound", bounds.isNotEmpty())
        val numbers = bounds.map { BigDecimal(it) }
        // Negative quantities (PNP, 79xx) are printed with min/max by magnitude, so compare magnitudes.
        val ordered = if (numbers.all { it.signum() <= 0 }) numbers.map { it.abs() } else numbers
        assertEquals("$what order", ordered.sorted(), ordered)
    }

    @Test
    fun pinoutsAreComplete() {
        catalog.forEach { e ->
            val pkg = e.packageType
            if (e.id.value == "74hc") {
                assertNull(pkg)
                assertTrue(e.pins.isEmpty())
            } else {
                assertNotNull(e.id.value, pkg)
                assertEquals(e.id.value, (1..pkg!!.pinCount).toList(), e.pins.map { it.number })
            }
        }
    }

    @Test
    fun familiesHaveMembers() {
        catalog.forEach { e ->
            assertEquals(e.id.value, e.id.value in families, e.members.isNotEmpty())
        }
    }

    @Test
    fun equivalentsExist() {
        catalog.mapNotNull { it.equivalent }.forEach { assertNotNull(it.value, entry(it)) }
        assertEquals(PartId("1n4148"), entry(PartId("1n914"))!!.equivalent)
    }
}
