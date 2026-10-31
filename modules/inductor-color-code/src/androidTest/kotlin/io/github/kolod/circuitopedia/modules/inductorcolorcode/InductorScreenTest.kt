package io.github.kolod.circuitopedia.modules.inductorcolorcode

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.Selection
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.format
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Black
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Brown
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Gold
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Silver
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Violet
import io.github.kolod.circuitopedia.shared.colorcode.BandColor.Yellow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.github.kolod.circuitopedia.shared.colorcode.ui.R as SharedR

@RunWith(AndroidJUnit4::class)
class InductorScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = rule.setContent { MaterialTheme { InductorScreen() } }

    /** The expected value text, formatted for the device locale (the formatting itself is JVM-tested). */
    private fun expected(layout: BandLayout, vararg colors: BandColor) =
        Selection(layout, colors.toList()).read().format(rule.activity.resources.configuration.locales[0])

    /** Band count of the tab under test; the pager may keep a neighboring page composed. */
    private var page = 4

    private fun onPage(tag: String) = rule.onNode(hasTestTag(tag) and hasAnyAncestor(hasTestTag("icc-page-$page")))

    private fun cell(band: Int, color: BandColor) = onPage("icc-cell-$band-${color.name}")

    private fun tap(band: Int, color: BandColor) = cell(band, color).performScrollTo().performClick()

    private fun value() = onPage("icc-value")

    private fun header(text: String) = rule.onNode(hasText(text) and hasAnyAncestor(hasTestTag("icc-page-$page")))

    private fun selectTab(bands: Int) {
        rule.onNodeWithTag("icc-tab-$bands").performClick()
        page = bands
    }

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    // US1

    @Test
    fun opensOnFourBandsWithTheDefaultSelection() {
        launch()
        rule.onNodeWithTag("icc-tab-4").assertIsSelected()
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Black, Silver))
        cell(0, Yellow).assertIsSelected()
        cell(3, Silver).assertIsSelected()
    }

    @Test
    fun tappingCellsUpdatesTheValue() {
        launch()
        tap(0, Brown)
        tap(1, Black)
        tap(2, Brown)
        tap(3, Gold)
        value().assertTextEquals(expected(BandLayout.Four, Brown, Black, Brown, Gold))
    }

    @Test
    fun tappingAnEmptyCellChangesNothing() {
        launch()
        cell(0, Black).performScrollTo().performClick()
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Black, Silver))
    }

    // US2

    @Test
    fun threeBandTabShowsItsDefault() {
        launch()
        selectTab(3)
        value().assertTextEquals(expected(BandLayout.Three, Yellow, Violet, Black))
        cell(3, Gold).assertDoesNotExist()
    }

    @Test
    fun decimalPointSwitchesTheThirdColumnAndMoves() {
        launch()
        val multiplier = string(SharedR.string.color_code_header_multiplier)
        val band3 = string(SharedR.string.color_code_header_band, 3)

        tap(1, Gold)
        tap(2, Violet)
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Gold, Violet, Silver))
        header(band3).assertExists()
        header(multiplier).assertDoesNotExist()
        // Band 2 holds the point, but its other cells still show the digit they would select.
        val violetInBand2 = string(
            SharedR.string.color_code_cell_description,
            string(SharedR.string.color_code_header_band, 2),
            string(SharedR.string.color_code_color_violet),
            "7",
        )
        cell(1, BandColor.Violet).assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(violetInBand2)))

        tap(0, Gold)
        value().assertTextEquals(expected(BandLayout.Four, Gold, Violet, Violet, Silver))

        tap(0, Yellow)
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Black, Silver))
        header(multiplier).assertExists()
    }

    @Test
    fun eachTabKeepsItsOwnSelection() {
        launch()
        selectTab(3)
        tap(0, Brown)
        selectTab(4)
        value().assertTextEquals(expected(BandLayout.Four, Yellow, Violet, Black, Silver))
        selectTab(3)
        cell(0, Brown).assertIsSelected()
    }

    @Test
    fun swipingSelectsTheOtherTab() {
        launch()
        rule.onNodeWithTag("icc-pager").performTouchInput { swipeRight() }
        page = 3
        rule.onNodeWithTag("icc-tab-3").assertIsSelected()
    }

    @Test
    fun tabAndSelectionSurviveStateRestoration() {
        val tester = StateRestorationTester(rule)
        tester.setContent { MaterialTheme { InductorScreen() } }
        selectTab(3)
        tap(1, Gold)
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("icc-tab-3").assertIsSelected()
        cell(1, Gold).assertIsSelected()
        value().assertTextEquals(expected(BandLayout.Three, Yellow, Gold, Black))
    }

    // US3

    @Test
    fun decimalPointIsSpokenAsWords() {
        launch()
        val description = string(
            SharedR.string.color_code_cell_description,
            string(SharedR.string.color_code_header_band, 2),
            string(SharedR.string.color_code_color_gold),
            string(R.string.inductor_color_code_decimal_point),
        )
        cell(1, Gold).assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(description)))
        cell(0, Black).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }
}
