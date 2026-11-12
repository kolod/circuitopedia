package io.github.kolod.circuitopedia.modules.boardpinouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Assignment
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Availability
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.FunctionKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Placement
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PlacementId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.availability
import io.github.kolod.circuitopedia.modules.boardpinouts.core.defaultAssignment
import io.github.kolod.circuitopedia.modules.boardpinouts.core.pinAvailability
import io.github.kolod.circuitopedia.modules.boardpinouts.core.placement
import io.github.kolod.circuitopedia.modules.boardpinouts.core.unuse
import io.github.kolod.circuitopedia.modules.boardpinouts.core.use

// The planner (FR-007 - FR-012): the board's assignments, adding a function, choosing a placement,
// and editing the pins of an assignment. The current step is saved: "summary", "kinds",
// "kind:<FunctionKind>" or "edit:<placement id>".

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlannerSheet(
    board: Board,
    assignments: List<Assignment>,
    onAssignments: (List<Assignment>) -> Unit,
    start: String,
    onDismiss: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(start) }
    fun replace(a: Assignment) = onAssignments(assignments.filter { it.placement != a.placement } + a)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("bp-planner"),
    ) {
        // Back steps out of the planner one level at a time, then the sheet closes.
        BackHandler(enabled = step != "summary" && step != start) {
            step = when {
                step.startsWith("kind:") -> "kinds"
                else -> "summary"
            }
        }
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                step == "kinds" -> Kinds(board, onPick = { step = "kind:${it.name}" }, onBack = { step = "summary" })
                step.startsWith("kind:") -> Placements(
                    board, assignments, FunctionKind.valueOf(step.removePrefix("kind:")),
                    onPick = { placement ->
                        if (assignments.none { it.placement == placement.id }) {
                            board.defaultAssignment(assignments, placement)?.let(::replace)
                        }
                        step = "edit:${placement.id.value}"
                    },
                    onBack = { step = "kinds" },
                )
                step.startsWith("edit:") -> {
                    val placement = board.placement(PlacementId(step.removePrefix("edit:")))
                    val assignment = assignments.firstOrNull { it.placement == placement?.id }
                    if (placement == null || assignment == null) {
                        LaunchedEffect(step) { step = "summary" }
                    } else {
                        Edit(board, assignments, placement, assignment, onChange = ::replace, onDone = { step = "summary" })
                    }
                }
                else -> Summary(
                    board, assignments,
                    onAdd = { step = "kinds" },
                    onEdit = { step = "edit:${it.value}" },
                    onRemove = { id -> onAssignments(assignments.filter { it.placement != id }) },
                    onClear = { onAssignments(emptyList()) },
                )
            }
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
}

private fun Board.pinsText(assignment: Assignment, unused: String): String =
    assignment.chosen.entries.joinToString(", ") { (signal, pin) -> "$signal ${pin?.name ?: unused}" }

@Composable
private fun Summary(
    board: Board,
    assignments: List<Assignment>,
    onAdd: () -> Unit,
    onEdit: (PlacementId) -> Unit,
    onRemove: (PlacementId) -> Unit,
    onClear: () -> Unit,
) {
    val res = LocalResources.current
    var confirm by rememberSaveable { mutableStateOf(false) }
    Title(res.getString(R.string.board_pinouts_planner))
    if (assignments.isEmpty()) Text(res.getString(R.string.board_pinouts_no_assignments))
    assignments.forEach { a ->
        val placement = board.placement(a.placement) ?: return@forEach
        Column(Modifier.fillMaxWidth().testTag("bp-assignment-${a.placement.value}")) {
            Text(board.label(placement), fontWeight = FontWeight.Bold)
            Text(board.pinsText(a, res.getString(R.string.board_pinouts_unused)), style = MaterialTheme.typography.bodyMedium)
            if (placement.movesAsGroup) Text(res.getString(R.string.board_pinouts_moves_as_group), style = MaterialTheme.typography.bodySmall)
            placement.notes.forEach { Text("ⓘ " + res.getString(it.label()), style = MaterialTheme.typography.bodySmall) }
            VerdictText(board, board.availability(assignments, placement).let { if (it is Availability.Warned) it else Availability.Available })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onEdit(a.placement) }) { Text(res.getString(R.string.board_pinouts_edit)) }
                TextButton(onClick = { onRemove(a.placement) }, modifier = Modifier.testTag("bp-remove-${a.placement.value}")) {
                    Text(res.getString(R.string.board_pinouts_remove))
                }
            }
        }
        HorizontalDivider()
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onAdd, modifier = Modifier.testTag("bp-add")) { Text(res.getString(R.string.board_pinouts_add_function)) }
        if (assignments.isNotEmpty()) {
            OutlinedButton(onClick = { confirm = true }, modifier = Modifier.testTag("bp-clear")) { Text(res.getString(R.string.board_pinouts_clear_all)) }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            text = { Text(res.getString(R.string.board_pinouts_clear_all_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirm = false; onClear() }, modifier = Modifier.testTag("bp-clear-confirm")) {
                    Text(res.getString(R.string.board_pinouts_clear_all))
                }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(res.getString(R.string.board_pinouts_cancel)) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Kinds(board: Board, onPick: (FunctionKind) -> Unit, onBack: () -> Unit) {
    val res = LocalResources.current
    Title(res.getString(R.string.board_pinouts_choose_function))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        board.functions.forEach { f ->
            OutlinedButton(onClick = { onPick(f.kind) }, modifier = Modifier.testTag("bp-kind-${f.kind.name}")) { Text(res.getString(f.kind.label())) }
        }
    }
    TextButton(onClick = onBack) { Text(res.getString(R.string.board_pinouts_cancel)) }
}

@Composable
private fun Placements(board: Board, assignments: List<Assignment>, kind: FunctionKind, onPick: (Placement) -> Unit, onBack: () -> Unit) {
    val res = LocalResources.current
    val function = board.functions.first { it.kind == kind }
    Title(res.getString(R.string.board_pinouts_choose_placement, res.getString(kind.label())))
    function.placements.forEach { placement ->
        val verdict = board.availability(assignments, placement)
        val blocked = verdict is Availability.Blocked
        val pins = placement.slots.joinToString("; ") { slot -> "${slot.signal}: ${slot.candidates.joinToString(", ") { it.name }}" }
        val spoken = listOf(board.label(placement), pins).joinToString(". ") +
            if (verdict is Availability.Blocked) ". " + res.getString(R.string.board_pinouts_spoken_blocked, verdict.reasons.joinToString(" ") { conflictText(res, it, board::instanceOf) }) else ""
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = !blocked) { onPick(placement) }
                .semantics(mergeDescendants = true) {
                    contentDescription = spoken
                    if (blocked) disabled()
                }
                .testTag("bp-placement-${placement.id.value}")
                .padding(vertical = 6.dp),
        ) {
            Text(
                board.label(placement),
                fontWeight = FontWeight.Bold,
                color = if (blocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Text(res.getString(R.string.board_pinouts_pins_of, pins), style = MaterialTheme.typography.bodySmall)
            if (placement.movesAsGroup) Text(res.getString(R.string.board_pinouts_moves_as_group), style = MaterialTheme.typography.bodySmall)
            placement.notes.forEach { Text("ⓘ " + res.getString(it.label()), style = MaterialTheme.typography.bodySmall) }
            VerdictText(board, verdict)
        }
        HorizontalDivider()
    }
    TextButton(onClick = onBack) { Text(res.getString(R.string.board_pinouts_cancel)) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Edit(
    board: Board,
    assignments: List<Assignment>,
    placement: Placement,
    assignment: Assignment,
    onChange: (Assignment) -> Unit,
    onDone: () -> Unit,
) {
    val res = LocalResources.current
    Title(board.label(placement))
    if (placement.movesAsGroup) Text(res.getString(R.string.board_pinouts_moves_as_group), style = MaterialTheme.typography.bodySmall)
    placement.notes.forEach { Text("ⓘ " + res.getString(it.label()), style = MaterialTheme.typography.bodySmall) }
    placement.slots.forEach { slot ->
        val chosen = assignment.chosen[slot.signal]
        val canUnuse = !slot.required || placement.movesAsGroup
        Column(Modifier.fillMaxWidth().testTag("bp-slot-${slot.signal}")) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(slot.signal, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (canUnuse) {
                    Text(res.getString(R.string.board_pinouts_used), style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = chosen != null,
                        onCheckedChange = { on ->
                            if (on) {
                                slot.candidates.firstOrNull { board.pinAvailability(assignments, placement, slot.signal, it) !is Availability.Blocked }
                                    ?.let { onChange(assignment.use(slot.signal, it)) }
                            } else {
                                assignment.unuse(slot.signal, placement)?.let(onChange)
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp).testTag("bp-used-${slot.signal}"),
                    )
                }
            }
            if (chosen != null && slot.candidates.size > 1) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    slot.candidates.forEach { pin ->
                        val verdict = board.pinAvailability(assignments, placement, slot.signal, pin)
                        FilterChip(
                            selected = pin == chosen,
                            enabled = verdict !is Availability.Blocked,
                            onClick = { onChange(assignment.use(slot.signal, pin)) },
                            label = { Text(pin.name + if (verdict is Availability.Warned) " ⚠" else "") },
                            modifier = Modifier.testTag("bp-candidate-${slot.signal}-${pin.name}"),
                        )
                    }
                }
            } else if (chosen != null) {
                Text(chosen.name)
            }
            chosen?.let { VerdictText(board, board.pinAvailability(assignments, placement, slot.signal, it)) }
        }
    }
    if (placement.movesAsGroup && assignment.chosen.values.any { it == null }) {
        Text(res.getString(R.string.board_pinouts_remap_stays), style = MaterialTheme.typography.bodySmall)
    }
    Button(onClick = onDone, modifier = Modifier.testTag("bp-done")) { Text(res.getString(R.string.board_pinouts_done)) }
}
