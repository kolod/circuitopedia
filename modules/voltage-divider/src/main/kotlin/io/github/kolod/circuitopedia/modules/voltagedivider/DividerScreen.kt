package io.github.kolod.circuitopedia.modules.voltagedivider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity
import io.github.kolod.circuitopedia.modules.voltagedivider.core.SolveResult
import io.github.kolod.circuitopedia.modules.voltagedivider.core.canCalculate
import io.github.kolod.circuitopedia.modules.voltagedivider.core.formatVolts
import io.github.kolod.circuitopedia.modules.voltagedivider.core.parseVoltage
import io.github.kolod.circuitopedia.modules.voltagedivider.core.solve
import io.github.kolod.circuitopedia.shared.resistors.Rational
import io.github.kolod.circuitopedia.shared.resistors.formatOhms
import io.github.kolod.circuitopedia.shared.resistors.parseResistance
import java.util.Locale

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

    val entered = Quantity.entries.filter { fields[it.ordinal].origin == Origin.Entered }
    val parsed = entered.associateWith { it.parse(fields[it.ordinal].text) }
    val unparsed = parsed.filterValues { it == null }.keys
    val values = parsed.filterValues { it != null }.mapValues { it.value!! }
    val result = if (unparsed.isEmpty()) solve(values) else null

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
        val solved = solve(values) as? SolveResult.Solved ?: return
        fields = fields.mapIndexed { i, field ->
            val quantity = Quantity.entries[i]
            if (quantity in solved.computed) Field(quantity.format(solved.values.getValue(quantity), locale), Origin.Computed) else field
        }
    }

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
                    QuantityField(quantity, fields[quantity.ordinal], { edit(quantity, it) })
                }
                Message(unparsed, result, values.size)
                Button(
                    onClick = ::calculate,
                    enabled = unparsed.isEmpty() && canCalculate(values),
                    modifier = Modifier.fillMaxWidth().testTag("vd-calculate"),
                ) { Text(stringResource(R.string.voltage_divider_calculate)) }
            }
        }
    }
}

@Composable
private fun QuantityField(quantity: Quantity, field: Field, onChange: (String) -> Unit) {
    val computed = field.origin == Origin.Computed
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
        modifier = Modifier.fillMaxWidth().testTag("vd-field-${quantity.name}"),
    )
}

/** The note or error under the fields, by priority (contracts/ui-contract.md). */
@Composable
private fun Message(unparsed: Set<Quantity>, result: SolveResult?, enteredCount: Int) {
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
        result is SolveResult.TooMany && enteredCount > 3 -> pluralStringResource(R.plurals.voltage_divider_too_many, result.extra, result.extra)
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
