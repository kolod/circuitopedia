package io.github.kolod.circuitopedia.shared.colorcode.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import kotlinx.coroutines.launch

/** What one tab shows: the formatted reading, the drawing and the table columns. */
class ColorCodePage(
    /** The formatted reading, e.g. "4.7kΩ ±5%". */
    val value: String,
    val columns: List<TableColumn>,
    /** Usually a [BandDrawing]; laid out with the table's width and side padding. */
    val drawing: @Composable (Modifier) -> Unit,
)

/**
 * Tabs for each entry of [bandCounts] over a swipeable pager. Each page shows the value, the
 * drawing and the table header fixed, and only the color rows scroll. Test tags start with
 * [tagPrefix]: "-tab-N", "-pager", "-page-N", "-value" and "-cell-<column>-<Color>", N being the
 * tab's band count.
 */
@Composable
fun ColorCodeScaffold(
    bandCounts: List<Int>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tagPrefix: String,
    page: @Composable (tab: Int) -> ColorCodePage,
    onSelect: (tab: Int, column: Int, color: BandColor) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = selectedTab) { bandCounts.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect(onTabSelected)
    }

    Column {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            bandCounts.forEachIndexed { index, count ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(pluralStringResource(R.plurals.color_code_bands, count, count), maxLines = 1) },
                    modifier = Modifier.testTag("$tagPrefix-tab-$count"),
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            key = { bandCounts[it] },
            modifier = Modifier.fillMaxSize().testTag("$tagPrefix-pager"),
        ) { tab ->
            ColorCodePageContent(page(tab), bandCounts[tab], tagPrefix) { column, color -> onSelect(tab, column, color) }
        }
    }
}

/** Value, drawing and table header stay fixed; only the color rows scroll. */
@Composable
private fun ColorCodePageContent(page: ColorCodePage, bandCount: Int, tagPrefix: String, onSelect: (Int, BandColor) -> Unit) {
    Column(Modifier.fillMaxSize().testTag("$tagPrefix-page-$bandCount")) {
        Text(
            text = page.value,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .testTag("$tagPrefix-value"),
        )
        page.drawing(Modifier.fillMaxWidth().padding(horizontal = TableSidePadding))
        ColorTableHeader(page.columns, Modifier.padding(horizontal = TableSidePadding))
        ColorTableBody(
            page.columns,
            onSelect,
            tagPrefix,
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TableSidePadding)
                .padding(top = CellGap, bottom = TableSidePadding),
        )
    }
}
