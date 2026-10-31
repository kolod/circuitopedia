package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Orange
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Red
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.format
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.github.kolod.circuitopedia.shared.colorcode.ui.R as SharedR

@RunWith(AndroidJUnit4::class)
class ColorCodeScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = rule.setContent { MaterialTheme { ColorCodeScreen() } }

    /** The expected value text, formatted for the device locale (the formatting itself is JVM-tested). */
    private fun expected(layout: BandLayout, vararg colors: BandColor) =
        Selection(layout, colors.toList()).read().format(rule.activity.resources.configuration.locales[0])

    /** Band count of the tab under test; the pager may keep a neighboring page composed, so lookups stay inside this page. */
    private var page = 4

    private fun onPage(tag: String) = rule.onNode(hasTestTag(tag) and hasAnyAncestor(hasTestTag("rcc-page-$page")))

    private fun cell(band: Int, color: BandColor) = onPage("rcc-cell-$band-${color.name}")

    private fun tap(band: Int, color: BandColor) = cell(band, color).performScrollTo().performClick()

    private fun value() = onPage("rcc-value")

    private fun selectTab(bands: Int) {
        rule.onNodeWithTag("rcc-tab-$bands").performClick()
        page = bands
    }

    // US1

    @Test
    fun opensOnFourBandsWithTheDefaultSelection() {
        launch()
        rule.onNodeWithTag("rcc-tab-4").assertIsSelected()
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Red, Gold))
        cell(0, Yellow).assertIsSelected()
        cell(1, Violet).assertIsSelected()
        cell(2, Red).assertIsSelected()
        cell(3, Gold).assertIsSelected()
    }

    @Test
    fun tappingCellsUpdatesTheValue() {
        launch()
        tap(0, Brown)
        tap(1, Black)
        tap(2, Orange)
        tap(3, Brown)
        value().assertTextEquals(expected(BandLayout.Four, Brown, Black, Orange, Brown))
        cell(0, Brown).assertIsSelected()
        cell(0, Yellow).assertIsNotSelected()
        cell(3, Gold).assertIsNotSelected()
    }

    @Test
    fun tappingAnEmptyCellChangesNothing() {
        launch()
        cell(0, Black).performScrollTo().performClick()
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Red, Gold))
        cell(0, Yellow).assertIsSelected()
    }

    // US2

    @Test
    fun eachTabShowsItsDefault() {
        launch()
        selectTab(3)
        value().assertTextEquals(expected(BandLayout.Three, Yellow, Violet, Red))
        cell(3, Gold).assertDoesNotExist()
        selectTab(5)
        value().assertTextEquals(expected(BandLayout.Five, Yellow, Violet, Black, Brown, Brown))
        selectTab(6)
        value().assertTextEquals(expected(BandLayout.Six, Yellow, Violet, Black, Brown, Brown, Brown))
    }

    @Test
    fun eachTabKeepsItsOwnSelection() {
        launch()
        selectTab(5)
        tap(0, Brown)
        selectTab(4)
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Red, Gold))
        selectTab(5)
        cell(0, Brown).assertIsSelected()
        value().assertTextEquals(expected(BandLayout.Five, Brown, Violet, Black, Brown, Brown))
    }

    @Test
    fun swipingSelectsTheNextTab() {
        launch()
        rule.onNodeWithTag("rcc-pager").performTouchInput { swipeLeft() }
        page = 5
        rule.onNodeWithTag("rcc-tab-5").assertIsSelected()
    }

    @Test
    fun tabAndSelectionSurviveStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { ColorCodeScreen() } }
        selectTab(6)
        tap(5, Red)
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("rcc-tab-6").assertIsSelected()
        cell(5, Red).assertIsSelected()
        value().assertTextEquals(expected(BandLayout.Six, Yellow, Violet, Black, Brown, Brown, Red))
    }

    // US3

    @Test
    fun cellsDescribeThemselvesToScreenReaders() {
        launch()
        val description = rule.activity.getString(
            SharedR.string.color_code_cell_description,
            rule.activity.getString(SharedR.string.color_code_header_band, 1),
            rule.activity.getString(SharedR.string.color_code_color_yellow),
            "4",
        )
        cell(0, Yellow).assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(description)))
        cell(0, Black).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        value().assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }
}
