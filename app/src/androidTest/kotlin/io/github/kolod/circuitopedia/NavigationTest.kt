package io.github.kolod.circuitopedia

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId
import io.github.kolod.circuitopedia.samples.manySampleCalculations
import io.github.kolod.circuitopedia.samples.sampleModules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val modules = sampleModules + manySampleCalculations(40)

    private fun launch(modules: List<AppModule> = this.modules) = rule.setContent { CircuitopediaApp(modules) }

    private fun entry(id: String) = rule.onNodeWithTag(ENTRY_TAG_PREFIX + id)

    /** A Calculations entry, scrolled into view first (with 40 bulk samples most entries start off screen). */
    private fun calculation(id: String) =
        rule.onNodeWithTag("module-list-Calculations").performScrollToNode(hasTestTag(ENTRY_TAG_PREFIX + id)).let { entry(id) }

    /** Waits for pending recomposition first, so a BackHandler added by the last action is registered. */
    private fun pressBack() {
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun assertCatalogShown() = rule.onNodeWithTag("catalog-pager").assertIsDisplayed()

    @Test
    fun tappingAnEntryOpensItsScreenWithTitle() {
        launch()
        calculation("sample-ohm").performClick()
        rule.onNodeWithText("Ohm's law").assertIsDisplayed()
        rule.onNodeWithTag("content-sample-ohm").assertIsDisplayed()
    }

    @Test
    fun systemBackReturnsToTheSameTab() {
        launch()
        calculation("sample-ohm").performClick()
        pressBack()
        assertCatalogShown()
        rule.onNodeWithText(rule.str(R.string.tab_calculations)).assertIsSelected()
    }

    @Test
    fun upReturnsToTheReferenceTab() {
        launch()
        rule.onNodeWithText(rule.str(R.string.tab_reference)).performClick()
        entry("sample-pinouts").performClick()
        rule.onNodeWithContentDescription(rule.str(R.string.navigate_up)).performClick()
        assertCatalogShown()
        rule.onNodeWithText(rule.str(R.string.tab_reference)).assertIsSelected()
    }

    @Test
    fun upGoesThroughTheModulesOwnBackHandlerLikeSystemBack() {
        // A module with an inner level (like a part page over its list) handles up first.
        val nested = AppModule(
            id = ModuleId("sample-nested"),
            category = Category.Reference,
            titleRes = R.string.sample_pinouts_title,
            descriptionRes = R.string.sample_pinouts_description,
            iconRes = R.drawable.sample_icon,
            content = {
                var inner by remember { mutableStateOf(true) }
                if (inner) {
                    BackHandler { inner = false }
                    Text("inner", Modifier.testTag("nested-inner"))
                } else {
                    Text("outer", Modifier.testTag("nested-outer"))
                }
            },
        )
        launch(listOf(nested))
        rule.onNodeWithText(rule.str(R.string.tab_reference)).performClick()
        entry("sample-nested").performClick()
        rule.onNodeWithTag("nested-inner").assertIsDisplayed()
        val up = rule.onNodeWithContentDescription(rule.str(R.string.navigate_up))
        up.performClick()
        rule.onNodeWithTag("nested-outer").assertIsDisplayed()
        up.performClick()
        assertCatalogShown()
    }

    @Test
    fun scrollPositionIsKeptAfterARoundTrip() {
        launch()
        // Sorted: Attenuator, 40 × "Bulk sample" (registry order), Long…, Ohm's law, Zener diode.
        rule.onNodeWithTag("module-list-Calculations").performScrollToIndex(30)
        entry("sample-n-29").performClick()
        pressBack()
        entry("sample-n-29").assertIsDisplayed()
    }

    @Test
    fun doubleTapOpensOneScreen() {
        launch()
        calculation("sample-ohm").performTouchInput { click(); click() }
        assertEquals(1, rule.onAllNodesWithTag("content-sample-ohm").fetchSemanticsNodes().size)
        pressBack()
        assertCatalogShown()
    }

    @Test
    fun openModuleSurvivesStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { CircuitopediaApp(modules) }
        calculation("sample-ohm").performClick()
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("content-sample-ohm").assertIsDisplayed()
    }

    @Test
    fun unknownOpenModuleFallsBackToTheCatalog() {
        var current by mutableStateOf(modules)
        val tester = StateRestorationTester(rule)
        tester.setContent { CircuitopediaApp(current) }
        calculation("sample-ohm").performClick()
        // As after an update that removed the module: the saved id is no longer registered.
        current = modules.filterNot { it.id.value == "sample-ohm" }
        tester.emulateSavedInstanceStateRestore()
        assertCatalogShown()
    }

    @Test
    fun catalogLeavesSystemBackToTheActivity() {
        // Since Android 12 back on a task's root activity moves the task to the background instead
        // of finishing it, so the check is that the catalog registers no back handler of its own.
        launch()
        rule.waitForIdle()
        assertFalse(rule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
        calculation("sample-ohm").performClick()
        rule.waitForIdle()
        assertTrue(rule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
    }
}
