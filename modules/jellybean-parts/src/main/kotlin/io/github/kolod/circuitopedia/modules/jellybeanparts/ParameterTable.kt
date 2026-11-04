package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.res.Resources
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.FamilyMember
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Parameter
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Value
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * A value as printed in the datasheet, with only the decimal separator localized: trailing zeros
 * stay ("0.600"), a leading "-" becomes a real minus sign and [plusMinus] adds "±".
 */
internal fun printed(text: String, plusMinus: Boolean, locale: Locale): String {
    val separator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
    val digits = text.removePrefix("-").replace('.', separator)
    val sign = when {
        text.startsWith("-") -> "−"
        plusMinus -> "±"
        else -> ""
    }
    return sign + digits
}

/** "minimum 3, maximum 30 volt": the bounds that are printed, then the unit in words. */
internal fun spokenValue(res: Resources, value: Value, locale: Locale): String {
    fun number(text: String): String {
        val digits = printed(text.removePrefix("-"), false, locale)
        return when {
            text.startsWith("-") -> res.getString(R.string.jellybean_parts_spoken_negative, digits)
            value.plusMinus -> res.getString(R.string.jellybean_parts_spoken_plus_minus, digits)
            else -> digits
        }
    }
    val bounds = listOfNotNull(
        value.min?.let { res.getString(R.string.jellybean_parts_spoken_min, number(it)) },
        value.typ?.let { res.getString(R.string.jellybean_parts_spoken_typ, number(it)) },
        value.max?.let { res.getString(R.string.jellybean_parts_spoken_max, number(it)) },
    ).joinToString(", ")
    val unit = unitWords(value.unit)?.let { res.getString(it) }
    return if (unit == null) bounds else "$bounds $unit"
}

/** The spoken text of a parameter row: its name, its bounds and unit, then its condition. */
internal fun spokenParameter(res: Resources, parameter: Parameter, locale: Locale): String =
    listOfNotNull(
        res.getString(parameter.kind.label()),
        spokenValue(res, parameter.value, locale),
        parameter.condition,
    ).joinToString(", ")

private val NAME_WEIGHT = 2.4f

/** Column headers, then one row per parameter; the cells wrap at large font sizes. */
@Composable
internal fun ParameterTable(parameters: List<Parameter>, locale: Locale) {
    val res = LocalResources.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { invisibleToUser() },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HeaderCell(stringResource(R.string.jellybean_parts_col_parameter), NAME_WEIGHT)
            HeaderCell(stringResource(R.string.jellybean_parts_col_min))
            HeaderCell(stringResource(R.string.jellybean_parts_col_typ))
            HeaderCell(stringResource(R.string.jellybean_parts_col_max))
            HeaderCell(stringResource(R.string.jellybean_parts_col_unit))
        }
        HorizontalDivider()
        val seen = mutableMapOf<ParameterKind, Int>()
        parameters.forEach { parameter ->
            val n = seen.merge(parameter.kind, 1, Int::plus)!!
            val tag = if (n == 1) "jb-param-${parameter.kind.name}" else "jb-param-${parameter.kind.name}-$n"
            val spoken = spokenParameter(res, parameter, locale)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clearAndSetSemantics {
                        contentDescription = spoken
                        testTag = tag
                    },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Column(Modifier.weight(NAME_WEIGHT)) {
                    Text(stringResource(parameter.kind.label()), style = MaterialTheme.typography.bodyMedium)
                    parameter.condition?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val v = parameter.value
                ValueCell(v.min?.let { printed(it, v.plusMinus, locale) })
                ValueCell(v.typ?.let { printed(it, v.plusMinus, locale) })
                ValueCell(v.max?.let { printed(it, v.plusMinus, locale) })
                ValueCell(v.unit)
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float = 1f) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun RowScope.ValueCell(text: String?) {
    Text(text.orEmpty(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
}

/** Each family member with its distinguishing values, one block per member. */
@Composable
internal fun MembersTable(members: List<FamilyMember>, locale: Locale) {
    val res = LocalResources.current
    val min = stringResource(R.string.jellybean_parts_col_min)
    val typ = stringResource(R.string.jellybean_parts_col_typ)
    val max = stringResource(R.string.jellybean_parts_col_max)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        members.forEach { member ->
            val lines = member.values.map { (kind, v) ->
                val bounds = listOfNotNull(
                    v.min?.let { "$min ${printed(it, v.plusMinus, locale)}" },
                    v.typ?.let { "$typ ${printed(it, v.plusMinus, locale)}" },
                    v.max?.let { "$max ${printed(it, v.plusMinus, locale)}" },
                ).joinToString(" · ")
                Triple(res.getString(kind.label()), "$bounds ${v.unit}", spokenValue(res, v, locale))
            }
            val spoken = (listOf(member.name) + lines.map { "${it.first}, ${it.third}" }).joinToString(". ")
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clearAndSetSemantics {
                        contentDescription = spoken
                        testTag = "jb-member-${member.name}"
                    },
            ) {
                Text(member.name, style = MaterialTheme.typography.titleSmall)
                lines.forEach { (label, text, _) ->
                    Text("$label: $text", style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider()
        }
    }
}
