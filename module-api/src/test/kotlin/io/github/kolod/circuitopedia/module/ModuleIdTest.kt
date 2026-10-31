package io.github.kolod.circuitopedia.module

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ModuleIdTest {

    @Test
    fun acceptsKebabCaseIds() {
        for (value in listOf("a", "ohms-law", "555-timer", "a".repeat(64))) {
            assertEquals(value, ModuleId(value).value)
        }
    }

    @Test
    fun rejectsMalformedIds() {
        val invalid = listOf("", "Ohms", "ohms_law", "-x", "x-", "a--b", "ом", "a b", "a".repeat(65))
        for (value in invalid) {
            assertThrows("\"$value\" must be rejected", IllegalArgumentException::class.java) {
                ModuleId(value)
            }
        }
    }
}
