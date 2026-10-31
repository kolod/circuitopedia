package io.github.kolod.circuitopedia.modules.voltagedivider

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DividerScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = rule.setContent { MaterialTheme { DividerScreen() } }

    private fun field(quantity: Quantity) = rule.onNodeWithTag("vd-field-${quantity.name}")

    private fun type(quantity: Quantity, text: String) = field(quantity).performTextReplacement(text)

    private fun text(quantity: Quantity): String =
        field(quantity).fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun calculate() = rule.onNodeWithTag("vd-calculate").performClick()

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    // US1

    @Test
    fun calculateNeedsThreeValues() {
        launch()
        rule.onNodeWithTag("vd-calculate").assertIsNotEnabled()
        type(Quantity.U1, "5")
        type(Quantity.U2, "3.3")
        rule.onNodeWithTag("vd-calculate").assertIsNotEnabled()
        type(Quantity.R1, "10k")
        rule.onNodeWithTag("vd-calculate").assertIsEnabled()
    }

    @Test
    fun calculateFillsTheEmptyFields() {
        launch()
        type(Quantity.U1, "5")
        type(Quantity.U2, "3.3")
        type(Quantity.R1, "10k")
        calculate()
        assertTrue(text(Quantity.R2), text(Quantity.R2).startsWith("19") && text(Quantity.R2).endsWith("41kΩ"))
        assertTrue(text(Quantity.R), text(Quantity.R).startsWith("29") && text(Quantity.R).endsWith("41kΩ"))
    }

    @Test
    fun editingAnEnteredValueClearsTheResults() {
        launch()
        type(Quantity.U1, "5")
        type(Quantity.U2, "3.3")
        type(Quantity.R1, "10k")
        calculate()
        type(Quantity.U1, "6")
        assertEquals("", text(Quantity.R2))
        assertEquals("", text(Quantity.R))
    }

    @Test
    fun typingIntoAComputedFieldMakesItEntered() {
        launch()
        type(Quantity.U1, "9")
        type(Quantity.U2, "3")
        type(Quantity.R, "30k")
        calculate()
        type(Quantity.R1, "22k")
        // R1 is now entered, so R2 (computed) is cleared and four values would be too many.
        assertEquals("", text(Quantity.R2))
        assertEquals("22k", text(Quantity.R1))
        rule.onNodeWithTag("vd-calculate").assertIsNotEnabled()
    }

    @Test
    fun threeResistancesAloneCannotBeCalculated() {
        launch()
        type(Quantity.R1, "1k")
        type(Quantity.R2, "2k")
        type(Quantity.R, "3k")
        rule.onNodeWithTag("vd-calculate").assertIsNotEnabled()
        rule.onNodeWithTag("vd-message").assertExists()
        val message = rule.onNodeWithTag("vd-message").fetchSemanticsNode().config[SemanticsProperties.Text].single().text
        assertEquals(string(R.string.voltage_divider_no_voltage), message)
    }

    @Test
    fun outputAboveInputIsAnError() {
        launch()
        type(Quantity.U1, "5")
        type(Quantity.U2, "6")
        type(Quantity.R1, "1k")
        val message = rule.onNodeWithTag("vd-message").fetchSemanticsNode().config[SemanticsProperties.Text].single().text
        assertEquals(string(R.string.voltage_divider_error_ratio), message)
    }

    // US2

    private fun awaitBuilds() = rule.waitUntil(10_000) {
        rule.onAllNodes(hasTestTag("vd-build-0")).fetchSemanticsNodes().isNotEmpty()
    }

    private fun description(tag: String): String =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()

    private fun scrollTo(tag: String) = rule.onNodeWithTag("vd-list").performScrollToNode(hasTestTag(tag))

    @Test
    fun buildsAppearAfterCalculateWithTheExactPairFirst() {
        launch()
        type(Quantity.U1, "9")
        type(Quantity.U2, "3")
        type(Quantity.R, "30k")
        calculate()
        scrollTo("vd-series-E24")
        awaitBuilds()
        scrollTo("vd-build-0")
        val first = description("vd-build-0")
        val kilohm = string(R.string.voltage_divider_spoken_kilohm)
        assertTrue(first, first.startsWith("R1 20 $kilohm, R2 10 $kilohm, "))
        assertTrue(first, first.contains(string(R.string.voltage_divider_spoken_error, "0")))
    }

    @Test
    fun switchingTheSeriesUpdatesTheBuilds() {
        launch()
        type(Quantity.U1, "5")
        type(Quantity.U2, "3.3")
        type(Quantity.R, "30k")
        calculate()
        scrollTo("vd-series-E24")
        awaitBuilds()
        scrollTo("vd-build-0")
        val e24 = description("vd-build-0")
        scrollTo("vd-series-E3")
        rule.onNodeWithTag("vd-series-E3").performClick()
        rule.waitUntil(10_000) {
            rule.onAllNodes(hasTestTag("vd-build-0")).fetchSemanticsNodes().firstOrNull()
                ?.config?.get(SemanticsProperties.ContentDescription)?.single()?.let { it != e24 } == true
        }
    }

    @Test
    fun noBuildsWhenBothResistorsAreEntered() {
        launch()
        type(Quantity.U1, "12")
        type(Quantity.R1, "10k")
        type(Quantity.R2, "4k7")
        calculate()
        rule.onNodeWithTag("vd-series-E24").assertDoesNotExist()
    }

    // US3

    @Test
    fun computedFieldIsSpokenWithUnitWordsAndState() {
        launch()
        type(Quantity.U1, "5")
        type(Quantity.U2, "3.3")
        type(Quantity.R1, "10k")
        calculate()
        val node = field(Quantity.R2).fetchSemanticsNode().config
        val label = string(R.string.voltage_divider_label_r2)
        assertEquals(listOf("$label, 19.41 ${string(R.string.voltage_divider_spoken_kilohm)}"), node[SemanticsProperties.ContentDescription])
        assertEquals(string(R.string.voltage_divider_state_computed), node[SemanticsProperties.StateDescription])
    }

    @Test
    fun valuesSurviveStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { DividerScreen() } }
        field(Quantity.U1).performTextInput("12")
        field(Quantity.R1).performTextInput("10k")
        field(Quantity.R2).performTextInput("4k7")
        calculate()
        tester.emulateSavedInstanceStateRestore()
        assertEquals("12", text(Quantity.U1))
        assertTrue(text(Quantity.U2), text(Quantity.U2).endsWith("837V"))
        rule.onNodeWithTag("vd-list").performScrollToNode(hasTestTag("vd-calculate"))
    }
}
