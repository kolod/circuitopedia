package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.entry

/**
 * One part: title, category and purpose, description, parameters, family members, pinout with
 * the maker warning, the equivalent note and the datasheet it was copied from.
 */
@Composable
internal fun PartPage(part: PartEntry) {
    val locale = LocalConfiguration.current.locales[0]
    val texts = partTexts(part.id)
    LazyColumn(
        Modifier.fillMaxSize().testTag("jb-page"),
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "title") {
            Text(
                part.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp).semantics { heading() },
            )
            Text(
                stringResource(part.category.label()) + " · " + stringResource(texts.purpose),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item(key = "description") {
            Text(
                stringResource(texts.description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        part.equivalent?.let { other ->
            item(key = "equivalent") {
                Text(
                    stringResource(R.string.jellybean_parts_equivalent_of, entry(other)!!.name),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp).testTag("jb-equivalent"),
                )
            }
        }
        item(key = "parameters") {
            SectionTitle(stringResource(R.string.jellybean_parts_key_parameters))
            ParameterTable(part.parameters, locale)
        }
        if (part.members.isNotEmpty()) {
            item(key = "members") {
                SectionTitle(stringResource(R.string.jellybean_parts_members))
                MembersTable(part.members, locale)
            }
        }
        item(key = "pinout") {
            SectionTitle(stringResource(R.string.jellybean_parts_pinout))
            val packageType = part.packageType
            if (packageType == null) {
                Text(
                    stringResource(R.string.jellybean_parts_family_pinout),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp).testTag("jb-no-pinout"),
                )
            } else {
                Pinout(packageType, part.pins)
            }
        }
        if (part.makerVariesPinout) {
            item(key = "warning") {
                Row(Modifier.padding(horizontal = 16.dp).testTag("jb-warning")) {
                    Text(
                        "⚠ " + stringResource(R.string.jellybean_parts_warning_pinout_varies),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        item(key = "source") {
            val source = part.source
            Text(
                stringResource(R.string.jellybean_parts_source_label, "${source.maker}, ${source.document}, ${source.revision}"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).testTag("jb-source"),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp).semantics { heading() },
    )
}
