package io.github.kolod.circuitopedia.modules.voltagedivider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.voltagedivider.core.DividerBuild
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity
import io.github.kolod.circuitopedia.modules.voltagedivider.core.SolveResult
import io.github.kolod.circuitopedia.modules.voltagedivider.core.builds
import io.github.kolod.circuitopedia.modules.voltagedivider.core.canCalculate
import io.github.kolod.circuitopedia.modules.voltagedivider.core.formatVolts
import io.github.kolod.circuitopedia.modules.voltagedivider.core.parseVoltage
import io.github.kolod.circuitopedia.modules.voltagedivider.core.solve
import io.github.kolod.circuitopedia.shared.resistors.ESeries
import io.github.kolod.circuitopedia.shared.resistors.Rational
import io.github.kolod.circuitopedia.shared.resistors.connectionText
import io.github.kolod.circuitopedia.shared.resistors.formatDeviation
import io.github.kolod.circuitopedia.shared.resistors.formatOhms
import io.github.kolod.circuitopedia.shared.resistors.parseResistance
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Where a field's text came from: nothing yet, the user, or Calculate (spec FR-004, FR-008). */
internal enum class Origin { Empty, Entered, Computed }

internal data class Field(val text: String = "", val origin: Origin = Origin.Empty)

/** Field text and origin per quantity, saved flat as [text, origin ordinal, text, …]. */
private val FieldsSaver = listSaver<List<Field>, Any>(
    save = { fields -> fields.flatMap { listOf(it.text, it.origin.ordinal) } },
    restore = { flat -> flat.chunked(2).map { (text, origin) -> Field(text as String, Origin.entries[origin as Int]) } },
)

internal fun Quantity.parse(text: String): Rational? = if (isVoltage) parseVoltage(text) else parseResistance(text)

internal fun Quantity.format(value: Rational, locale: Locale): String =
    if (isVoltage) formatVolts(value, locale) else value.formatOhms(locale)

/** The divider: schematic, five fields, Calculate, then standard-resistor builds (spec 005). */
@Composable
fun DividerScreen() {
    val locale = LocalConfiguration.current.locales[0]
    var fields by rememberSaveable(stateSaver = FieldsSaver) { mutableStateOf(List(Quantity.entries.size) { Field() }) }
    var seriesOrdinal by rememberSaveable { mutableIntStateOf(ESeries.E24.ordinal) }
    val series = ESeries.entries[seriesOrdinal]

    val entered = Quantity.entries.filter { fields[it.ordinal].origin == Origin.Entered }
    val parsed = entered.associateWith { it.parse(fields[it.ordinal].text) }
    val unparsed = parsed.filterValues { it == null }.keys
    val values = parsed.filterValues { it != null }.mapValues { it.value!! }
    val result = if (unparsed.isEmpty()) solve(values) else null
    // Builds belong to a calculation that is on screen: some fields show computed values.
    val solved = (result as? SolveResult.Solved)?.takeIf { fields.any { it.origin == Origin.Computed } }
    val offerBuilds = solved != null && (Quantity.R1 in solved.computed || Quantity.R2 in solved.computed)

    var buildList by remember { mutableStateOf<List<DividerBuild>?>(null) }
    LaunchedEffect(solved, series) {
        buildList = null
        if (solved != null && offerBuilds) {
            buildList = withContext(Dispatchers.Default) { builds(solved, series) { ensureActive() } }
        }
    }

    fun edit(quantity: Quantity, text: String) {
        fields = fields.mapIndexed { i, field ->
            when {
                i == quantity.ordinal -> Field(text, if (text.isBlank()) Origin.Empty else Origin.Entered)
                // Any edit makes earlier results stale (FR-008).
                field.origin == Origin.Computed -> Field()
                else -> field
            }
        }
    }

    fun calculate() {
        val done = solve(values) as? SolveResult.Solved ?: return
        fields = fields.mapIndexed { i, field ->
            val quantity = Quantity.entries[i]
            if (quantity in done.computed) Field(quantity.format(done.values.getValue(quantity), locale), Origin.Computed) else field
        }
    }

    val units = rememberSpokenUnits(locale)
    LazyColumn(Modifier.fillMaxSize().testTag("vd-list")) {
        item(key = "inputs") {
            Column(
                Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Schematic()
                Text(
                    stringResource(R.string.voltage_divider_note_unloaded),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Quantity.entries.forEach { quantity ->
                    val field = fields[quantity.ordinal]
                    val shown = solved?.values?.get(quantity) ?: values[quantity]
                    QuantityField(
                        quantity,
                        field,
                        spoken = spokenField(quantity, shown, field.text, units),
                        onChange = { edit(quantity, it) },
                    )
                }
                Message(unparsed, result)
                Button(
                    onClick = ::calculate,
                    enabled = unparsed.isEmpty() && canCalculate(values),
                    modifier = Modifier.fillMaxWidth().testTag("vd-calculate"),
                ) { Text(stringResource(R.string.voltage_divider_calculate)) }
            }
        }
        if (offerBuilds) {
            item(key = "series") {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider()
                    Text(stringResource(R.string.voltage_divider_builds_header), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.voltage_divider_series_label), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ESeries.entries.forEach { entry ->
                            FilterChip(
                                selected = entry == series,
                                onClick = { seriesOrdinal = entry.ordinal },
                                label = { Text(entry.name) },
                                modifier = Modifier.testTag("vd-series-${entry.name}"),
                            )
                        }
                    }
                }
            }
            val list = buildList
            if (list != null && list.isEmpty()) {
                item(key = "none") {
                    Text(
                        stringResource(R.string.voltage_divider_no_builds),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp).testTag("vd-no-builds"),
                    )
                }
            }
            if (list != null) {
                itemsIndexed(list) { index, build ->
                    BuildRow(build, spokenBuild(build, units, locale), locale, "vd-build-$index")
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun QuantityField(quantity: Quantity, field: Field, spoken: String, onChange: (String) -> Unit) {
    val computed = field.origin == Origin.Computed
    val state = stringResource(if (computed) R.string.voltage_divider_state_computed else R.string.voltage_divider_state_entered)
    OutlinedTextField(
        value = field.text,
        onValueChange = onChange,
        label = { Text(stringResource(quantity.labelRes())) },
        suffix = { Text(if (quantity.isVoltage) "V" else "Ω") },
        supportingText = if (computed) ({ Text(stringResource(R.string.voltage_divider_computed)) }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false),
        colors = if (computed) {
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        } else {
            OutlinedTextFieldDefaults.colors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                // Name and value with unit words, then entered or computed (FR-015).
                if (field.text.isNotBlank()) {
                    contentDescription = spoken
                    stateDescription = state
                }
            }
            .testTag("vd-field-${quantity.name}"),
    )
}

@Composable
private fun BuildRow(build: DividerBuild, spoken: String, locale: Locale, tag: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = spoken
                testTag = tag
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text("R1: ${build.r1.connectionText(locale)}    R2: ${build.r2.connectionText(locale)}", style = MaterialTheme.typography.bodyLarge)
        Text(
            stringResource(R.string.voltage_divider_build_u2, formatVolts(build.u2, locale), formatDeviation(build.u2Error, locale)),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.voltage_divider_build_r, build.total.formatOhms(locale), formatDeviation(build.totalDeviation, locale)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The note or error under the fields, by priority (contracts/ui-contract.md). */
@Composable
private fun Message(unparsed: Set<Quantity>, result: SolveResult?) {
    val text = when {
        unparsed.isNotEmpty() -> stringResource(R.string.voltage_divider_error_parse, stringResource(unparsed.first().labelRes()))
        result is SolveResult.Invalid -> stringResource(
            when (result.reason) {
                SolveResult.Reason.RatioOutOfRange -> R.string.voltage_divider_error_ratio
                SolveResult.Reason.MixedSigns -> R.string.voltage_divider_error_signs
                SolveResult.Reason.ResistanceNotBelowTotal -> R.string.voltage_divider_error_total
            },
        )
        result is SolveResult.NotEnough -> pluralStringResource(R.plurals.voltage_divider_need_more, result.missing, result.missing)
        result is SolveResult.TooMany -> pluralStringResource(R.plurals.voltage_divider_too_many, result.extra, result.extra)
        result is SolveResult.NoVoltage -> stringResource(R.string.voltage_divider_no_voltage)
        else -> return
    }
    val isError = unparsed.isNotEmpty() || result is SolveResult.Invalid
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag("vd-message"),
    )
}

internal fun Quantity.labelRes(): Int = when (this) {
    Quantity.U1 -> R.string.voltage_divider_label_u1
    Quantity.U2 -> R.string.voltage_divider_label_u2
    Quantity.R1 -> R.string.voltage_divider_label_r1
    Quantity.R2 -> R.string.voltage_divider_label_r2
    Quantity.R -> R.string.voltage_divider_label_r
}
