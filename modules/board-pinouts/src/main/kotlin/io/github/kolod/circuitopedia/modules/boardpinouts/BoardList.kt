package io.github.kolod.circuitopedia.modules.boardpinouts

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.boards

/** The seven boards in spec FR-002 order: name, chip and a one-line description. */
@Composable
internal fun BoardList(state: LazyListState, onOpen: (BoardId) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag("bp-list"), state = state) {
        items(boards, key = { it.id.value }) { board ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(board.id) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("bp-row-${board.id.value}"),
            ) {
                Text(board.name, style = MaterialTheme.typography.titleMedium)
                Text(board.chip, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(board.id.description()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider()
        }
    }
}
