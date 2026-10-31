package io.github.kolod.circuitopedia

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.samples.manySampleCalculations
import io.github.kolod.circuitopedia.samples.sampleModules
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch(modules: List<AppModule>) = rule.setContent { CircuitopediaApp(modules) }

    private val calculations get() = rule.onNodeWithText(rule.str(R.string.tab_calculations))
    private val reference get() = rule.onNodeWithText(rule.str(R.string.tab_reference))

    // US1

    @Test
    fun showsAppNameAndTwoTabsWithCalculationsSelected() {
        launch(emptyList())
        rule.onNodeWithText(rule.str(R.string.app_name)).assertIsDisplayed()
        val tabs = rule.onAllNodes(isSelectable())
        tabs[0].assertTextEquals(rule.str(R.string.tab_calculations))
        tabs[1].assertTextEquals(rule.str(R.string.tab_reference))
        calculations.assertIsSelected()
        reference.assertIsNotSelected()
    }

    @Test
    fun tappingATabSelectsIt() {
        launch(emptyList())
        reference.performClick()
        reference.assertIsSelected()
        calculations.assertIsNotSelected()
        calculations.performClick()
        calculations.assertIsSelected()
    }

    @Test
    fun swipingSwitchesTabs() {
        launch(emptyList())
        rule.onNodeWithTag("catalog-pager").performTouchInput { swipeLeft() }
        reference.assertIsSelected()
        rule.onNodeWithTag("catalog-pager").performTouchInput { swipeRight() }
        calculations.assertIsSelected()
    }

    @Test
    fun selectedTabSurvivesStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { CircuitopediaApp(emptyList()) }
        reference.performClick()
        tester.emulateSavedInstanceStateRestore()
        reference.assertIsSelected()
    }

    // US2

    @Test
    fun emptyCategoriesShowTheirMessage() {
        launch(emptyList())
        rule.onNodeWithText(rule.str(R.string.empty_calculations)).assertIsDisplayed()
        reference.performClick()
        rule.onNodeWithText(rule.str(R.string.empty_reference)).assertIsDisplayed()
    }

    @Test
    fun eachTabListsOnlyItsOwnModulesSortedByTitle() {
        launch(sampleModules)
        assertEquals(
            listOf("sample-alpha", "sample-long", "sample-ohm", "sample-zener").map { ENTRY_TAG_PREFIX + it },
            rule.entryTags(),
        )
        rule.onNodeWithText("Pinouts").assertDoesNotExist()

        reference.performClick()
        rule.waitForIdle()
        assertEquals(listOf(ENTRY_TAG_PREFIX + "sample-pinouts"), rule.entryTags())
    }

    @Test
    fun longListScrollsToTheLastEntry() {
        launch(manySampleCalculations(40))
        rule.onNodeWithTag("module-list-Calculations").performScrollToIndex(39)
        rule.onNodeWithTag(ENTRY_TAG_PREFIX + "sample-n-39").assertIsDisplayed()
    }

    @Test
    fun longTitleEntryIsShown() {
        launch(sampleModules)
        rule.onNodeWithTag(ENTRY_TAG_PREFIX + "sample-long").assertIsDisplayed()
    }
}
