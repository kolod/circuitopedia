package io.github.kolod.circuitopedia.modules.boardpinouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Assignment
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Availability
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.HeaderPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Placement
import io.github.kolod.circuitopedia.modules.boardpinouts.core.functionsOf
import io.github.kolod.circuitopedia.modules.boardpinouts.core.groupLimitsOf
import io.github.kolod.circuitopedia.modules.boardpinouts.core.headerPinsOf
import io.github.kolod.circuitopedia.modules.boardpinouts.core.limitsOf
import io.github.kolod.circuitopedia.modules.boardpinouts.core.pinAvailability

/** Details of one header pin: names, limits, shared limits, functions and its assignment. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PinSheet(
    board: Board,
    pin: HeaderPin,
    assignments: List<Assignment>,
    claims: Claims,
    onOpenPlacement: (Placement) -> Unit,
    onRemove: (Assignment) -> Unit,
    onDismiss: () -> Unit,
) {
    val res = LocalResources.current
    val locale = LocalConfiguration.current.locales[0]
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("bp-pin-sheet"),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val title = pin.number?.let { res.getString(R.string.board_pinouts_pin_number, it) + " · " + pin.label } ?: pin.label
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            val chip = pin.pin
            if (chip != null) {
                if (chip.name != pin.label) Text(res.getString(R.string.board_pinouts_chip_pin, chip.name))
                val others = board.headerPinsOf(chip).filter { it != pin }
                if (others.isNotEmpty()) {
                    Text(res.getString(R.string.board_pinouts_also_at, others.joinToString { it.number ?: it.label }))
                }
                claims[chip]?.let { (id, signal) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            res.getString(R.string.board_pinouts_assigned_to, board.instanceOf(id), signal),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f).padding(top = 12.dp),
                        )
                        assignments.firstOrNull { it.placement == id }?.let { a ->
                            TextButton(onClick = { onRemove(a) }) { Text(res.getString(R.string.board_pinouts_remove)) }
                        }
                    }
                }
            }
            pin.power?.let { power ->
                Section(res.getString(R.string.board_pinouts_power_pin))
                Text(res.getString(power.role.label()), fontWeight = FontWeight.Bold)
                val value = listOfNotNull(
                    power.range?.let { valueText(res, it, locale) },
                    power.condition?.let { res.getString(it.label()) },
                ).joinToString(", ")
                if (value.isNotEmpty()) Text(value)
                power.current?.let { Text(res.getString(R.string.board_pinouts_current_label, valueText(res, it, locale))) }
            }
            if (chip != null) {
                val limits = board.limitsOf(chip)
                if (limits.isNotEmpty()) Section(res.getString(R.string.board_pinouts_limits))
                limits.forEach { limit ->
                    Row(Modifier.testTag("bp-limit-${limit.kind.name}"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(limit.kind.symbol(), color = limit.kind.color(dark), fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 28.dp))
                        Column {
                            Text(res.getString(limit.kind.label()), fontWeight = FontWeight.Bold)
                            Text(limitExplanation(res, limit, locale), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                val groups = board.groupLimitsOf(chip)
                if (groups.isNotEmpty()) Section(res.getString(R.string.board_pinouts_group_limits))
                groups.forEach { group ->
                    Text(res.getString(group.key.label()) + ": " + valueText(res, group.value, locale), style = MaterialTheme.typography.bodySmall)
                }
                Section(res.getString(R.string.board_pinouts_functions))
                val functions = board.functionsOf(chip)
                val others = board.otherFunctions[chip].orEmpty()
                if (functions.isEmpty() && others.isEmpty()) Text(res.getString(R.string.board_pinouts_no_functions))
                functions.forEach { (placement, signal) ->
                    val verdict = board.pinAvailability(assignments, placement, signal, chip)
                    Column(
                        Modifier.fillMaxWidth().clickable { onOpenPlacement(placement) }.padding(vertical = 4.dp)
                            .testTag("bp-function-${placement.id.value}-$signal"),
                    ) {
                        Text("${board.label(placement)} $signal", fontWeight = FontWeight.Bold)
                        VerdictText(board, verdict)
                    }
                }
                if (others.isNotEmpty()) {
                    Text(res.getString(R.string.board_pinouts_other_functions) + ": " + others.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
internal fun Section(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 8.dp).semantics { heading() },
    )
}

/** The reasons of a blocked verdict, or the warnings of a warned one; nothing when available. */
@Composable
internal fun VerdictText(board: Board, verdict: Availability) {
    val res = LocalResources.current
    when (verdict) {
        Availability.Available -> Unit
        is Availability.Warned -> verdict.warnings.forEach {
            Text("⚠ " + warningText(res, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        is Availability.Blocked -> verdict.reasons.forEach {
            Text("✕ " + conflictText(res, it, board::instanceOf), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
