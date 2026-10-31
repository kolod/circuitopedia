package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.format
import kotlinx.coroutines.launch

/** Tabs for 3–6 bands over a swipeable pager; each page shows the value, a drawing and the color table. */
@Composable
fun ColorCodeScreen(state: ColorCodeState = rememberColorCodeState()) {
    val layouts = BandLayout.entries
    val pagerState = rememberPagerState(initialPage = state.layout.ordinal) { layouts.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { state.layout = layouts[it] }
    }

    Column {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            layouts.forEachIndexed { index, layout ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(pluralStringResource(R.plurals.resistor_color_code_bands, layout.bandCount, layout.bandCount), maxLines = 1) },
                    modifier = Modifier.testTag("rcc-tab-${layout.bandCount}"),
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            key = { layouts[it].name },
            modifier = Modifier.fillMaxSize().testTag("rcc-pager"),
        ) { page ->
            val layout = layouts[page]
            ColorCodePage(state.selections.getValue(layout)) { index, color -> state.select(layout, index, color) }
        }
    }
}

/** Value, drawing and table header stay fixed; only the color rows scroll. */
@Composable
private fun ColorCodePage(selection: Selection, onSelect: (Int, BandColor) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    Column(Modifier.fillMaxSize().testTag("rcc-page-${selection.layout.bandCount}")) {
        Text(
            text = selection.read().format(locale),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .testTag("rcc-value"),
        )
        ResistorDrawing(selection, Modifier.fillMaxWidth().padding(horizontal = TableSidePadding))
        ColorTableHeader(selection.layout, Modifier.padding(horizontal = TableSidePadding))
        ColorTableBody(
            selection,
            onSelect,
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TableSidePadding)
                .padding(top = CellGap, bottom = TableSidePadding),
        )
    }
}
