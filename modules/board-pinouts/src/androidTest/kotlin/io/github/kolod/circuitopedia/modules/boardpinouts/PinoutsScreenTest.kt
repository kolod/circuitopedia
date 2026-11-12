package io.github.kolod.circuitopedia.modules.boardpinouts

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.boards
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinoutsScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = rule.setContent { MaterialTheme { PinoutsScreen() } }

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun scrollTo(container: String, tag: String) =
        rule.onNodeWithTag(container).performScrollToNode(hasTestTag(tag))

    private fun spoken(tag: String): String =
        rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString(" ")

    private fun open(id: String) {
        scrollTo("bp-list", "bp-row-$id")
        rule.onNodeWithTag("bp-row-$id").performClick()
        rule.onNodeWithTag("bp-page").assertIsDisplayed()
    }

    private fun showList() = rule.onNodeWithTag("bp-view-list").performClick()

    private fun tap(tag: String) {
        rule.onNodeWithTag(tag).performClick()
        rule.waitForIdle()
    }

    /** Scrolls an open sheet's column to [tag] and taps it. */
    private fun tapInSheet(tag: String) {
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitForIdle()
    }

    // US1

    @Test
    fun boardsAreListedInSpecOrderAndEveryPinIsShown() {
        launch()
        boards.forEach { board ->
            open(board.id.value)
            showList()
            board.pins.forEach { pin -> scrollTo("bp-page", "bp-pin-${pin.key}") }
            Espresso.pressBack()
            rule.onNodeWithTag("bp-list").assertIsDisplayed()
        }
    }

    @Test
    fun bluePillPa12ShowsUsbUseAndFiveVoltTolerance() {
        launch()
        open("blue-pill")
        rule.onNodeWithTag("bp-mark-FiveVoltTolerant").assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        showList()
        scrollTo("bp-page", "bp-pin-P4-12")
        tap("bp-pin-P4-12")
        rule.onNodeWithTag("bp-pin-sheet").assertIsDisplayed()
        rule.onNodeWithTag("bp-limit-BoardUse", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("bp-limit-FiveVoltTolerant", useUnmergedTree = true).assertExists()
        Espresso.pressBack()
        rule.waitForIdle()
        rule.onNodeWithTag("bp-page").assertIsDisplayed()
        Espresso.pressBack()
        rule.onNodeWithTag("bp-list").assertIsDisplayed()
    }

    @Test
    fun openBoardSurvivesStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { PinoutsScreen() } }
        open("nano")
        showList()
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("bp-page").assertIsDisplayed()
        scrollTo("bp-page", "bp-pin-J2-5")
    }

    // US2

    private fun openPlanner() {
        scrollTo("bp-page", "bp-planner-button")
        tap("bp-planner-button")
    }

    private fun addPlacement(kind: String, placement: String) {
        openPlanner()
        tap("bp-add")
        tap("bp-kind-$kind")
        tapInSheet("bp-placement-$placement")
        tapInSheet("bp-done")
    }

    @Test
    fun pi4I2c1BlocksI2c3OnTheSamePins() {
        launch()
        open("rpi4b")
        addPlacement("I2c", "i2c1")
        tap("bp-add")
        tap("bp-kind-I2c")
        rule.onNodeWithTag("bp-placement-i2c3-2-3").assertIsNotEnabled()
        val reason = string(R.string.board_pinouts_conflict_pin_taken, "GPIO2", "I2C1", "SDA")
        assertTrue(spoken("bp-placement-i2c3-2-3"), spoken("bp-placement-i2c3-2-3").contains(reason))
        rule.onNodeWithTag("bp-placement-i2c3-4-5").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
    }

    @Test
    fun bluePillRemapMovesAsGroupAndUsbBlocksCan() {
        launch()
        open("blue-pill")
        addPlacement("Usb", "usb")
        tap("bp-add")
        tap("bp-kind-Can")
        rule.onNodeWithTag("bp-placement-can").assertIsNotEnabled()
        rule.onNodeWithTag("bp-placement-can-remap").assertIsNotEnabled()
    }

    @Test
    fun unuseRemoveAndClearAll() {
        launch()
        open("blue-pill")
        addPlacement("I2c", "i2c1-remap")
        showList()
        scrollTo("bp-page", "bp-pin-P4-5") // PB8
        assertTrue(spoken("bp-pin-P4-5").contains(string(R.string.board_pinouts_spoken_assigned, "I2C1 (PB8, PB9)", "SCL")))
        openPlanner()
        tap("bp-remove-i2c1-remap")
        rule.onNodeWithTag("bp-assignment-i2c1-remap").assertDoesNotExist()
        tap("bp-add")
        tap("bp-kind-Uart")
        tapInSheet("bp-placement-usart2")
        tapInSheet("bp-done")
        tap("bp-clear")
        tap("bp-clear-confirm")
        rule.onNodeWithTag("bp-assignment-usart2").assertDoesNotExist()
    }

    @Test
    fun assignmentsSurviveStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { PinoutsScreen() } }
        open("pico2-w")
        addPlacement("I2c", "i2c0")
        Espresso.pressBack()
        rule.waitForIdle()
        tester.emulateSavedInstanceStateRestore()
        showList()
        scrollTo("bp-page", "bp-pin-left-1") // GP0 = default I2C0 SDA
        assertTrue(spoken("bp-pin-left-1").contains(string(R.string.board_pinouts_spoken_assigned, "I2C0", "SDA")))
    }

    @Test
    fun keepScreenOnHoldsOnlyWhileABoardIsShown() {
        var shown by mutableStateOf(true)
        rule.setContent { MaterialTheme { if (shown) PinoutsScreen() } }
        val view = rule.activity.window.decorView.findViewById<android.view.View>(android.R.id.content)
        fun held() = generateSequence(listOf(view)) { level ->
            level.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) }.orEmpty() }
                .ifEmpty { null }
        }.flatten().any { it.keepScreenOn }
        open("uno-r3")
        tap("bp-keep-screen-on")
        assertTrue(held())
        Espresso.pressBack() // back to the board list: released
        rule.waitForIdle()
        assertFalse(held())
        open("nano") // the user's choice comes back with the next board
        assertTrue(held())
        shown = false // the module is left
        rule.waitForIdle()
        assertFalse(held())
    }

    // US3

    @Test
    fun pi4PinRowIsSpokenWithAssignmentAndPullUp() {
        launch()
        open("rpi4b")
        addPlacement("I2c", "i2c1")
        Espresso.pressBack()
        rule.waitForIdle()
        showList()
        scrollTo("bp-page", "bp-pin-J8-odd-2") // pin 3, GPIO2
        val text = spoken("bp-pin-J8-odd-2")
        assertTrue(text, text.startsWith(string(R.string.board_pinouts_spoken_pin, "3", "GPIO2")))
        assertTrue(text, text.contains(string(R.string.board_pinouts_spoken_assigned, "I2C1", "SDA")))
        assertTrue(text, text.contains(string(R.string.board_pinouts_unit_kohm)))
        assertTrue(text, text.contains(string(R.string.board_pinouts_limit_threevoltonly)))
    }
}
