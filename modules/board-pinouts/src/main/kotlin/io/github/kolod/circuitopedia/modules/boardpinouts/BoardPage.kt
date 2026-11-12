package io.github.kolod.circuitopedia.modules.boardpinouts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Assignment
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.claims

private val defaultMarks = setOf(LimitKind.FiveVoltTolerant, LimitKind.ThreeVoltOnly)

/**
 * One board: title, Drawing | List switch, planner button, limit marks, the view note, the drawing or
 * the pin list, notes and sources. The view, the marks, the open pin and the planner step are saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoardPage(
    board: Board,
    assignments: List<Assignment>,
    onAssignments: (List<Assignment>) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOn: (Boolean) -> Unit,
) {
    val res = LocalResources.current
    val largeFont = LocalDensity.current.fontScale > 1.3f
    var showList by rememberSaveable { mutableStateOf(largeFont) }
    var marksText by rememberSaveable { mutableStateOf(defaultMarks.joinToString(",") { it.name }) }
    var openPin by rememberSaveable { mutableStateOf<String?>(null) }
    var planner by rememberSaveable { mutableStateOf<String?>(null) }
    val marks = marksText.split(',').filter { it.isNotEmpty() }.map(LimitKind::valueOf).toSet()
    val claims = board.claims(assignments)
    val kinds = board.limits.values.flatten().map { it.kind }.distinct().sortedBy { it.ordinal }

    LazyColumn(Modifier.fillMaxSize().testTag("bp-page"), state = rememberLazyListState()) {
        item(key = "title") {
            Text(
                board.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp).semantics { heading() },
            )
            Text(board.chip, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp))
            Text(res.getString(board.id.description()), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        item(key = "controls") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                    SegmentedButton(
                        selected = !showList, onClick = { showList = false },
                        shape = SegmentedButtonDefaults.itemShape(0, 2), modifier = Modifier.testTag("bp-view-drawing"),
                    ) { Text(res.getString(R.string.board_pinouts_drawing)) }
                    SegmentedButton(
                        selected = showList, onClick = { showList = true },
                        shape = SegmentedButtonDefaults.itemShape(1, 2), modifier = Modifier.testTag("bp-view-list"),
                    ) { Text(res.getString(R.string.board_pinouts_list)) }
                }
                val count = assignments.size
                val spoken = res.getQuantityString(R.plurals.board_pinouts_planner_count, count, count)
                Button(onClick = { planner = "summary" }, modifier = Modifier.testTag("bp-planner-button").semantics { contentDescription = spoken }) {
                    Text(res.getString(R.string.board_pinouts_planner) + if (count > 0) " ($count)" else "")
                }
            }
        }
        item(key = "screen") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(value = keepScreenOn, role = Role.Switch, onValueChange = onKeepScreenOn)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("bp-keep-screen-on"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(res.getString(R.string.board_pinouts_keep_screen_on), modifier = Modifier.weight(1f))
                Switch(checked = keepScreenOn, onCheckedChange = null)
            }
        }
        item(key = "marks") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                kinds.forEach { kind ->
                    FilterChip(
                        selected = kind in marks,
                        onClick = { marksText = (if (kind in marks) marks - kind else marks + kind).joinToString(",") { it.name } },
                        label = { Text(kind.symbol() + " " + res.getString(kind.label())) },
                        modifier = Modifier.testTag("bp-mark-${kind.name}"),
                    )
                }
            }
        }
        item(key = "view") {
            Text(
                res.getString(board.layout.view.label()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (showList) {
            items(board.pins, key = { "pin-${it.key}" }) { pin -> PinRow(board, pin, claims) { openPin = it.key } }
        } else {
            item(key = "drawing") { BoardDrawing(board, marks, claims) { openPin = it.key } }
        }
        if (board.notes.isNotEmpty()) {
            item(key = "notes") {
                Section(res.getString(R.string.board_pinouts_notes), Modifier.padding(horizontal = 16.dp))
                board.notes.forEach { note ->
                    Text("ⓘ " + res.getString(note.label()), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                }
            }
        }
        item(key = "sources") {
            val sources = board.sources.joinToString("; ") { "${it.maker}, ${it.title}, ${it.revision}" }
            Text(
                res.getString(R.string.board_pinouts_sources_label, sources),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp).testTag("bp-sources"),
            )
        }
    }

    openPin?.let { key -> board.pins.firstOrNull { it.key == key } }?.let { pin ->
        PinSheet(
            board, pin, assignments, claims,
            onOpenPlacement = { placement ->
                openPin = null
                planner = if (assignments.any { it.placement == placement.id }) "edit:${placement.id.value}" else "kind:${board.functions.first { placement in it.placements }.kind.name}"
            },
            onRemove = { a -> onAssignments(assignments.filter { it.placement != a.placement }) },
            onDismiss = { openPin = null },
        )
    }
    planner?.let { start ->
        PlannerSheet(board, assignments, onAssignments, start, onDismiss = { planner = null })
    }
}
