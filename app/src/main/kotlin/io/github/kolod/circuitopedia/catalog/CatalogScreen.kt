package io.github.kolod.circuitopedia.catalog

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.R
import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId
import java.text.Collator
import kotlinx.coroutines.launch

/** Main screen: category tabs above a swipeable pager with one module list per category. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(modules: List<AppModule>, onOpen: (ModuleId) -> Unit) {
    val categories = Category.entries
    val pagerState = rememberPagerState { categories.size }
    // Hoisted above the pager, which disposes off-screen pages and would lose their scroll position.
    val listStates = categories.map { rememberLazyListState() }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { padding ->
        Column(Modifier.padding(padding).consumeWindowInsets(padding)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = stringResource(category.labelRes()),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                key = { categories[it].name },
                modifier = Modifier.fillMaxSize().testTag("catalog-pager"),
            ) { page ->
                CategoryPage(categories[page], modules, listStates[page], onOpen)
            }
        }
    }
}

@Composable
private fun CategoryPage(
    category: Category,
    modules: List<AppModule>,
    listState: LazyListState,
    onOpen: (ModuleId) -> Unit,
) {
    val resources = LocalResources.current
    val locale = LocalConfiguration.current.locales[0]
    val entries = remember(category, modules, resources, locale) {
        entriesFor(
            category = category,
            items = modules,
            categoryOf = { it.category },
            titleOf = { resources.getString(it.titleRes) },
            collator = Collator.getInstance(locale),
        )
    }

    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(category.emptyRes()),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().testTag("module-list-${category.name}")) {
            items(entries, key = { it.id.value }) { ModuleEntry(it, onOpen) }
        }
    }
}

@Composable
private fun ModuleEntry(module: AppModule, onOpen: (ModuleId) -> Unit) {
    ListItem(
        headlineContent = {
            Text(stringResource(module.titleRes), maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(stringResource(module.descriptionRes), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = { Icon(painterResource(module.iconRes), contentDescription = null) },
        modifier = Modifier
            .clickable { onOpen(module.id) }
            .testTag("module-entry:${module.id.value}"),
    )
}

@StringRes
private fun Category.labelRes(): Int = when (this) {
    Category.Calculations -> R.string.tab_calculations
    Category.Reference -> R.string.tab_reference
}

@StringRes
private fun Category.emptyRes(): Int = when (this) {
    Category.Calculations -> R.string.empty_calculations
    Category.Reference -> R.string.empty_reference
}
