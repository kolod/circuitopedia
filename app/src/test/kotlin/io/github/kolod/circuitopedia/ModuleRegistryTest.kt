package io.github.kolod.circuitopedia

import io.github.kolod.circuitopedia.module.ModuleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleRegistryTest {

    @Test
    fun duplicateIdsNamesEveryRepeatedId() {
        val (a, b, c) = listOf(ModuleId("a"), ModuleId("b"), ModuleId("c"))
        assertEquals(setOf(a, b), duplicateIds(listOf(a, b, a, c, b)))
        assertEquals(emptySet<ModuleId>(), duplicateIds(listOf(a, b, c)))
    }

    @Test
    fun registryIdsAreUnique() {
        val duplicates = duplicateIds(ModuleRegistry.modules.map { it.id })
        assertTrue("Duplicate module ids: ${duplicates.joinToString()}", duplicates.isEmpty())
    }
}
