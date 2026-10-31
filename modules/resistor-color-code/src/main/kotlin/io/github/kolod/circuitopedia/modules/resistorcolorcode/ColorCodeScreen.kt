package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.accepts
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.cellLabel
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.format
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.ui.ColorCodePage
import io.github.kolod.circuitopedia.shared.colorcode.ui.ColorCodeScaffold
import io.github.kolod.circuitopedia.shared.colorcode.ui.TableColumn

/** Tabs for 3–6 bands over a swipeable pager; each page shows the value, a drawing and the color table. */
@Composable
fun ColorCodeScreen(state: ColorCodeState = rememberColorCodeState()) {
    val layouts = BandLayout.entries
    ColorCodeScaffold(
        bandCounts = layouts.map { it.bandCount },
        selectedTab = state.layout.ordinal,
        onTabSelected = { state.layout = layouts[it] },
        tagPrefix = "rcc",
        page = { tab -> resistorPage(state.selections.getValue(layouts[tab])) },
        onSelect = { tab, column, color -> state.select(layouts[tab], column, color) },
    )
}

@Composable
private fun resistorPage(selection: Selection): ColorCodePage {
    val locale = LocalConfiguration.current.locales[0]
    val layout = selection.layout
    val columns = List(layout.bandCount) { i ->
        TableColumn(
            header = layout.headerText(i),
            bandName = layout.bandName(i),
            labels = BandColor.entries.filter { layout.accepts(i, it) }.associateWith {
                // "±250ppm/K" does not fit a narrow 6-band column on one line.
                layout.roles[i].cellLabel(it, locale).replace("ppm/K", "\nppm/K")
            },
            selected = selection.colors[i],
        )
    }
    return ColorCodePage(selection.read().format(locale), columns) { modifier -> ResistorDrawing(selection, modifier) }
}
