package io.github.kolod.circuitopedia.modules.inductorcolorcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandRole
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.Selection
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.cellLabel
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.format
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.ui.ColorCodePage
import io.github.kolod.circuitopedia.shared.colorcode.ui.ColorCodeScaffold
import io.github.kolod.circuitopedia.shared.colorcode.ui.TableColumn
import io.github.kolod.circuitopedia.shared.colorcode.ui.R as SharedR

/** Tabs for 3 and 4 bands over a swipeable pager; each page shows the value, a drawing and the color table. */
@Composable
fun InductorScreen(state: InductorState = rememberInductorState()) {
    val layouts = BandLayout.entries
    ColorCodeScaffold(
        bandCounts = layouts.map { it.bandCount },
        selectedTab = state.layout.ordinal,
        onTabSelected = { state.layout = layouts[it] },
        tagPrefix = "icc",
        page = { tab -> inductorPage(state.selections.getValue(layouts[tab])) },
        onSelect = { tab, column, color -> state.select(layouts[tab], column, color) },
    )
}

@Composable
private fun inductorPage(selection: Selection): ColorCodePage {
    val locale = LocalConfiguration.current.locales[0]
    val decimalPoint = stringResource(R.string.inductor_color_code_decimal_point)
    val columns = List(selection.layout.bandCount) { i ->
        val role = selection.roleOf(i)
        TableColumn(
            header = headerText(i, role),
            bandName = bandName(i, role),
            // Each cell shows what its color would mean once tapped: a digit in the band that now holds
            // the point, "." for Gold in Band 1 or 2, a digit in Band 3 while a point is set.
            labels = BandColor.entries.filter { selection.canSelect(i, it) }.associateWith { color ->
                selection.with(i, color).roleOf(i).cellLabel(color, locale)
            },
            selected = selection.colors[i],
            spokenLabels = if (i < 2) mapOf(BandColor.Gold to decimalPoint) else emptyMap(),
        )
    }
    return ColorCodePage(selection.read().format(locale), columns) { modifier -> InductorDrawing(selection, modifier) }
}

/** Short column header: "Band 1", "Band 2", "Band 3" (with a decimal point) or "Mult.", "Toler.". */
@Composable
private fun headerText(index: Int, role: BandRole): String = when (role) {
    BandRole.Digit, BandRole.DecimalPoint -> stringResource(SharedR.string.color_code_header_band, index + 1)
    BandRole.Multiplier -> stringResource(SharedR.string.color_code_header_multiplier)
    BandRole.Tolerance -> stringResource(SharedR.string.color_code_header_tolerance)
}

/** Full band name for screen readers: "Band 1", "Multiplier", "Tolerance". */
@Composable
private fun bandName(index: Int, role: BandRole): String = when (role) {
    BandRole.Digit, BandRole.DecimalPoint -> stringResource(SharedR.string.color_code_header_band, index + 1)
    BandRole.Multiplier -> stringResource(SharedR.string.color_code_band_multiplier)
    BandRole.Tolerance -> stringResource(SharedR.string.color_code_band_tolerance)
}
