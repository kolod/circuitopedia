package io.github.kolod.circuitopedia.modules.resistorcombinations

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.parseResistance
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.search
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CombinationsScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = rule.setContent { MaterialTheme { CombinationsScreen() } }

    private fun type(text: String) = rule.onNodeWithTag("rcomb-target").performTextReplacement(text)

    /** Waits until the background search has filled the first row. */
    private fun awaitResults() = rule.waitUntil(5_000) {
        rule.onAllNodes(hasTestTag("rcomb-result-0")).fetchSemanticsNodes().isNotEmpty()
    }

    private fun description(index: Int): String =
        rule.onNodeWithTag("rcomb-result-$index").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    // US1

    @Test
    fun emptyFieldShowsTheHintAndNoList() {
        launch()
        rule.onNodeWithTag("rcomb-empty").assertExists()
        rule.onNodeWithTag("rcomb-result-0").assertDoesNotExist()
        rule.onNodeWithTag("rcomb-series-E24").assertIsSelected()
    }

    @Test
    fun exactSingleResistorIsListedFirst() {
        launch()
        rule.onNodeWithTag("rcomb-series-E12").performClick()
        type("4.7k")
        awaitResults()
        val first = description(0)
        assertTrue(first, first.startsWith("4.7 " + string(R.string.resistor_combinations_spoken_kilohm) + ", "))
        assertTrue(first, first.endsWith(string(R.string.resistor_combinations_spoken_deviation, "0")))
    }

    @Test
    fun invalidInputShowsTheErrorAndNoList() {
        launch()
        type("abc")
        rule.onNodeWithTag("rcomb-error", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("rcomb-result-0").assertDoesNotExist()
        rule.onNodeWithTag("rcomb-empty").assertDoesNotExist()
    }

    @Test
    fun switchingTheSeriesUpdatesTheList() {
        launch()
        type("5k")
        awaitResults()
        val e24 = description(0)
        rule.onNodeWithTag("rcomb-series-E3").performClick()
        rule.waitUntil(5_000) { description(0) != e24 }
        rule.onNodeWithTag("rcomb-series-E3").assertIsSelected()
    }

    @Test
    fun textSeriesAndKindsSurviveStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { CombinationsScreen() } }
        rule.onNodeWithTag("rcomb-target").performTextInput("1k")
        rule.onNodeWithTag("rcomb-series-E6").performClick()
        rule.onNodeWithTag("rcomb-kind-Parallel3").performClick()
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("rcomb-series-E6").assertIsSelected()
        rule.onNodeWithTag("rcomb-kind-Parallel3").assertIsNotSelected()
        rule.onNodeWithTag("rcomb-kind-Series2").assertIsSelected()
        awaitResults()
    }

    // US2

    @Test
    fun turnedOffKindsDisappear() {
        launch()
        type("1.234k")
        awaitResults()
        for (kind in listOf("Series3", "Parallel2", "Parallel3", "ParallelSeries")) rule.onNodeWithTag("rcomb-kind-$kind").performClick()
        // Only singles and 2-resistor series remain: no parallel, at most one "plus" per row.
        val parallel = string(R.string.resistor_combinations_spoken_parallel)
        val plus = " " + string(R.string.resistor_combinations_spoken_series) + " "
        rule.waitUntil(5_000) {
            val rows = composedDescriptions()
            rows.isNotEmpty() && rows.none { parallel in it || it.split(plus).size > 2 }
        }
        val expected = search(parseResistance("1.234k")!!, ESeries.E24, setOf(Kind.Series2)).size
        assertTrue("$expected", composedDescriptions().size <= expected)
    }

    /** Spoken descriptions of the result rows the lazy list has composed. */
    private fun composedDescriptions(): List<String> = (0 until 30).mapNotNull { i ->
        rule.onAllNodes(hasTestTag("rcomb-result-$i")).fetchSemanticsNodes().firstOrNull()
            ?.config?.get(SemanticsProperties.ContentDescription)?.single()
    }

    // US3

    @Test
    fun resultIsSpokenAsOneItemWithUnitWords() {
        launch()
        rule.onNodeWithTag("rcomb-series-E12").performClick()
        type("5k")
        awaitResults()
        val kilohm = string(R.string.resistor_combinations_spoken_kilohm)
        val parallel = string(R.string.resistor_combinations_spoken_parallel)
        val expected = "10 $kilohm $parallel 10 $kilohm, 5 $kilohm, " + string(R.string.resistor_combinations_spoken_deviation, "0")
        rule.onNodeWithTag("rcomb-result-0").assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(expected)))
    }
}
