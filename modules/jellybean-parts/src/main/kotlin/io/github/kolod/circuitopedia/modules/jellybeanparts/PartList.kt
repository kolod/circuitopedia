package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalogByCategory

/** All parts under sticky category headers, in spec order, with the forum credit at the end. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PartList(state: LazyListState, onOpen: (PartId) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag("jb-list"), state = state) {
        catalogByCategory().forEach { (category, parts) ->
            stickyHeader(key = "header-${category.name}") {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(category.label()),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics { heading() }
                            .testTag("jb-header-${category.name}"),
                    )
                }
            }
            items(parts, key = { it.id.value }) { part ->
                PartRow(part, onOpen)
                HorizontalDivider()
            }
        }
        item(key = "credit") {
            Text(
                stringResource(R.string.jellybean_parts_credit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp).testTag("jb-credit"),
            )
        }
    }
}

/** One tap target: the part name over its purpose. */
@Composable
private fun PartRow(part: PartEntry, onOpen: (PartId) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onOpen(part.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("jb-row-${part.id.value}"),
    ) {
        Text(part.name, style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(partTexts(part.id).purpose),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
