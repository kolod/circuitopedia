package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.entry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PartsScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = rule.setContent { MaterialTheme { PartsScreen() } }

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun scrollTo(container: String, tag: String) =
        rule.onNodeWithTag(container).performScrollToNode(hasTestTag(tag))

    private fun spoken(tag: String): String =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()

    private fun open(id: String) {
        scrollTo("jb-list", "jb-row-$id")
        rule.onNodeWithTag("jb-row-$id").performClick()
        rule.onNodeWithTag("jb-page").assertIsDisplayed()
    }

    // US1

    @Test
    fun everyHeaderAndPartIsListedWithItsPurpose() {
        launch()
        // The category order itself is checked by CatalogTest; here each header and row must show.
        Category.entries.forEach { category ->
            val tag = "jb-header-${category.name}"
            scrollTo("jb-list", tag)
            rule.onNodeWithTag(tag).assertIsDisplayed()
            catalog.filter { it.category == category }.forEach { part ->
                val row = "jb-row-${part.id.value}"
                scrollTo("jb-list", row)
                rule.onNodeWithTag(row, useUnmergedTree = false).assertIsDisplayed()
                val texts = rule.onNodeWithTag(row).fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text }
                assertEquals(listOf(part.name, string(partTexts(part.id).purpose)), texts)
            }
        }
        scrollTo("jb-list", "jb-credit")
        rule.onNodeWithTag("jb-credit").assertIsDisplayed()
    }

    @Test
    fun everyPartHasItsTexts() {
        launch()
        catalog.forEach { part ->
            val texts = partTexts(part.id)
            assertTrue(part.id.value, string(texts.purpose).isNotBlank())
            assertTrue(part.id.value, string(texts.description).isNotBlank())
        }
    }

    // US2

    @Test
    fun bc548PageShowsParametersPinsWarningAndSource() {
        launch()
        open("bc548")
        listOf("jb-param-Vceo", "jb-param-Ic", "jb-param-Hfe").forEach {
            scrollTo("jb-page", it)
            rule.onNodeWithTag(it).assertIsDisplayed()
        }
        val pins = listOf(R.string.jellybean_parts_pin_collector, R.string.jellybean_parts_pin_base, R.string.jellybean_parts_pin_emitter)
        pins.forEachIndexed { i, name ->
            scrollTo("jb-page", "jb-pin-${i + 1}")
            assertEquals(string(R.string.jellybean_parts_spoken_pin, i + 1, string(name)), spoken("jb-pin-${i + 1}"))
        }
        scrollTo("jb-page", "jb-warning")
        rule.onNodeWithTag("jb-warning").assertIsDisplayed()
        scrollTo("jb-page", "jb-source")
        val source = entry(PartId("bc548"))!!.source
        rule.onNodeWithTag("jb-source").assertIsDisplayed()
        val text = rule.onNodeWithTag("jb-source").fetchSemanticsNode().config[SemanticsProperties.Text].single().text
        assertTrue(text, text.contains(source.document) && text.contains(source.revision))
    }

    @Test
    fun familiesNoPinoutAndEquivalent() {
        launch()
        open("78xx")
        scrollTo("jb-page", "jb-member-7805")
        rule.onNodeWithTag("jb-member-7805").assertIsDisplayed()
        Espresso.pressBack()

        open("74hc")
        scrollTo("jb-page", "jb-no-pinout")
        rule.onNodeWithTag("jb-no-pinout").assertIsDisplayed()
        Espresso.pressBack()

        open("1n914")
        scrollTo("jb-page", "jb-equivalent")
        rule.onNodeWithTag("jb-equivalent").assertIsDisplayed()
    }

    @Test
    fun backReturnsToTheSameListPosition() {
        launch()
        scrollTo("jb-list", "jb-row-lm317")
        val before = rule.onNodeWithTag("jb-row-lm317").fetchSemanticsNode().positionInRoot
        rule.onNodeWithTag("jb-row-lm317").performClick()
        rule.onNodeWithTag("jb-page").assertIsDisplayed()
        Espresso.pressBack()
        rule.waitForIdle()
        assertEquals(before, rule.onNodeWithTag("jb-row-lm317").fetchSemanticsNode().positionInRoot)
    }

    @Test
    fun openPageSurvivesStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { PartsScreen() } }
        open("ne555")
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("jb-page").assertIsDisplayed()
        scrollTo("jb-page", "jb-pin-8")
        rule.onNodeWithTag("jb-pin-8").assertIsDisplayed()
    }

    // US3

    @Test
    fun parameterRowIsSpokenWithBoundsAndUnit() {
        launch()
        open("lm358")
        scrollTo("jb-page", "jb-param-SupplyVoltage")
        val expected = listOf(
            string(R.string.jellybean_parts_param_supplyvoltage),
            string(R.string.jellybean_parts_spoken_min, "3") + ", " +
                string(R.string.jellybean_parts_spoken_max, "30") + " " + string(R.string.jellybean_parts_unit_v),
        ).joinToString(", ")
        assertEquals(expected, spoken("jb-param-SupplyVoltage"))
    }

    @Test
    fun negativeValuesAreSpokenAsMinus() {
        launch()
        open("lm337")
        scrollTo("jb-page", "jb-param-ReferenceVoltage")
        val text = spoken("jb-param-ReferenceVoltage")
        assertTrue(text, text.contains(string(R.string.jellybean_parts_spoken_negative, "1.25")))
    }
}
