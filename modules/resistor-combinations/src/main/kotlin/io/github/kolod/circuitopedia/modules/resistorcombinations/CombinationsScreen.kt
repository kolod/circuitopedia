package io.github.kolod.circuitopedia.modules.resistorcombinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.Combination
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.ESeries
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.Kind
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.Rational
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.connectionText
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.formatDeviation
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.formatOhms
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.parseTarget
import io.github.kolod.circuitopedia.modules.resistorcombinations.core.search
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The kinds the user can switch on and off; a single resistor is always searched (FR-005). */
private val SELECTABLE_KINDS = Kind.entries - Kind.Single

/** Target field, series and kind chips, then the best combinations sorted by deviation. */
@Composable
fun CombinationsScreen() {
    val locale = LocalConfiguration.current.locales[0]
    var text by rememberSaveable { mutableStateOf("") }
    var seriesOrdinal by rememberSaveable { mutableIntStateOf(ESeries.E24.ordinal) }
    var allowed by rememberSaveable(stateSaver = KindSetSaver) { mutableStateOf(SELECTABLE_KINDS.toSet()) }
    val series = ESeries.entries[seriesOrdinal]
    val target = remember(text) { parseTarget(text) }
    val invalid = text.isNotBlank() && target == null

    // Recomputed off the main thread; a newer input cancels the older search (FR-010).
    var results by remember { mutableStateOf<List<Combination>>(emptyList()) }
    LaunchedEffect(target, series, allowed) {
        results = if (target == null) emptyList() else withContext(Dispatchers.Default) { search(target, series, allowed) }
    }

    // One scrolling list: the controls are its first item, so at large font sizes they scroll away
    // instead of leaving only a strip of the screen for the results.
    LazyColumn(Modifier.fillMaxSize().testTag("rcomb-list")) {
        item(key = "controls") {
            Controls(text, { text = it }, invalid, series, { seriesOrdinal = it.ordinal }, allowed, { allowed = it })
            HorizontalDivider()
        }
        if (target == null) {
            if (!invalid) {
                item(key = "empty") {
                    Text(
                        stringResource(R.string.resistor_combinations_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp).testTag("rcomb-empty"),
                    )
                }
            }
        } else {
            itemsIndexed(results) { index, combination ->
                ResultRow(combination, target, locale, "rcomb-result-$index")
                HorizontalDivider()
            }
        }
    }
}

/** Target field, series chips and kind chips (FR-002, FR-003, FR-005). */
@Composable
private fun Controls(
    text: String,
    onText: (String) -> Unit,
    invalid: Boolean,
    series: ESeries,
    onSeries: (ESeries) -> Unit,
    allowed: Set<Kind>,
    onAllowed: (Set<Kind>) -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onText,
            label = { Text(stringResource(R.string.resistor_combinations_target_label)) },
            supportingText = {
                if (invalid) {
                    Text(stringResource(R.string.resistor_combinations_target_error), Modifier.testTag("rcomb-error"))
                } else {
                    Text(stringResource(R.string.resistor_combinations_target_hint))
                }
            },
            isError = invalid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false),
            modifier = Modifier.fillMaxWidth().testTag("rcomb-target"),
        )
        Text(stringResource(R.string.resistor_combinations_series_label), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ESeries.entries.forEach { entry ->
                FilterChip(
                    selected = entry == series,
                    onClick = { onSeries(entry) },
                    label = { Text(entry.name) },
                    modifier = Modifier.testTag("rcomb-series-${entry.name}"),
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SELECTABLE_KINDS.forEach { kind ->
                FilterChip(
                    selected = kind in allowed,
                    onClick = { onAllowed(if (kind in allowed) allowed - kind else allowed + kind) },
                    label = { Text(kindLabel(kind)) },
                    modifier = Modifier.testTag("rcomb-kind-${kind.name}"),
                )
            }
        }
    }
}

@Composable
private fun ResultRow(combination: Combination, target: Rational, locale: Locale, tag: String) {
    val spoken = spokenResult(combination, target, locale)
    // One node for screen readers (FR-012): the spoken text replaces the symbols on screen.
    Column(
        Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = spoken
                testTag = tag
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(combination.connectionText(locale), style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(combination.resistance.formatOhms(locale), style = MaterialTheme.typography.bodyMedium)
            Text(
                formatDeviation(combination.deviation(target), locale),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun kindLabel(kind: Kind): String = stringResource(
    when (kind) {
        Kind.Series2 -> R.string.resistor_combinations_kind_series2
        Kind.Series3 -> R.string.resistor_combinations_kind_series3
        Kind.Parallel2 -> R.string.resistor_combinations_kind_parallel2
        Kind.Parallel3 -> R.string.resistor_combinations_kind_parallel3
        Kind.ParallelSeries -> R.string.resistor_combinations_kind_parallel_series
        Kind.Single -> error("A single resistor is always searched and has no chip")
    },
)

/** Saves the allowed kinds as their ordinals (the enum is append-only). */
private val KindSetSaver = listSaver<Set<Kind>, Int>(
    save = { kinds -> kinds.map { it.ordinal } },
    restore = { ordinals -> ordinals.map { Kind.entries[it] }.toSet() },
)
