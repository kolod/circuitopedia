package io.github.kolod.circuitopedia.modules.boardpinouts

import android.content.res.Resources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.HeaderPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PlacementId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.limitsOf
import io.github.kolod.circuitopedia.modules.boardpinouts.core.placement
import java.util.Locale

internal typealias Claims = Map<ChipPin, Pair<PlacementId, String>>

/** A stable key for a header position, used in saved state and test tags. */
internal val HeaderPin.key: String get() = "$header-$position"

internal fun Board.instanceOf(id: PlacementId): String = placement(id)?.let(::label) ?: id.value

/**
 * The placement's name: its instance, plus the pins of its required slots when the instance has
 * several placements ("USART1 (PB6, PB7)"), so remaps and pin sets can be told apart.
 */
internal fun Board.label(placement: io.github.kolod.circuitopedia.modules.boardpinouts.core.Placement): String {
    val siblings = placements.count { it.instance == placement.instance }
    if (siblings < 2) return placement.instance
    val pins = placement.slots.filter { it.required }.ifEmpty { placement.slots }.joinToString(", ") { slot ->
        slot.candidates.singleOrNull()?.name ?: slot.candidates.joinToString("/") { it.name }
    }
    return "${placement.instance} ($pins)"
}

/** The limit kinds of a pin, in the order of [LimitKind]. */
internal fun Board.kindsOf(pin: HeaderPin): List<LimitKind> =
    pin.pin?.let { chip -> limitsOf(chip).map { it.kind }.distinct().sortedBy { it.ordinal } }.orEmpty()

/** The onboard parts wired to a pin (LED, button, crystal...), shown next to it on the drawing and in the list. */
internal fun Board.usesOf(pin: HeaderPin): List<io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardUse> =
    pin.pin?.let { chip -> limitsOf(chip).mapNotNull { it.use } }.orEmpty()

/** "Pin 3, GPIO2, assigned to I2C1 SDA, 3.3 V only. …" — one sentence per pin (FR-015). */
internal fun spokenPin(res: Resources, board: Board, pin: HeaderPin, claims: Claims, locale: Locale): String {
    val name = pin.number?.let { res.getString(R.string.board_pinouts_spoken_pin, it, pin.label) } ?: pin.label
    val parts = mutableListOf(name)
    pin.pin?.let { chip ->
        if (chip.name != pin.label) parts += res.getString(R.string.board_pinouts_chip_pin, chip.name)
        claims[chip]?.let { (id, signal) -> parts += res.getString(R.string.board_pinouts_spoken_assigned, board.instanceOf(id), signal) }
        board.limitsOf(chip).forEach { limit ->
            parts += res.getString(limit.kind.label()) + ": " + limitExplanation(res, limit, locale, spoken = true)
        }
    }
    pin.power?.let { power ->
        parts += res.getString(power.role.label())
        power.range?.let { parts += valueText(res, it, locale, spoken = true) }
        power.current?.let { parts += res.getString(R.string.board_pinouts_current_label, valueText(res, it, locale, spoken = true)) }
        power.condition?.let { parts += res.getString(it.label()) }
    }
    return parts.joinToString(", ")
}

/** One row per header pin: number, label, chip pin, assignment and limit symbols. */
@Composable
internal fun PinRow(board: Board, pin: HeaderPin, claims: Claims, onOpen: (HeaderPin) -> Unit) {
    val res = LocalResources.current
    val locale = LocalConfiguration.current.locales[0]
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val spoken = spokenPin(res, board, pin, claims, locale)
    val claim = pin.pin?.let { claims[it] }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onOpen(pin) }
            .clearAndSetSemantics {
                contentDescription = spoken
                role = Role.Button
                testTag = "bp-pin-${pin.key}"
                onClick { onOpen(pin); true }
            }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(pin.number ?: "", style = MaterialTheme.typography.labelMedium, modifier = Modifier.widthIn(min = 24.dp))
        Column(Modifier.weight(1f)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(pin.label) }
                    pin.pin?.takeIf { it.name != pin.label }?.let { append("  ${it.name}") }
                    pin.power?.let { append("  ${res.getString(it.role.label())}") }
                    board.usesOf(pin).forEach { use ->
                        append("  ")
                        withStyle(SpanStyle(color = LimitKind.BoardUse.color(dark), fontWeight = FontWeight.Bold)) {
                            append(res.getString(use.tag()))
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            claim?.let { (id, signal) ->
                Text(
                    res.getString(R.string.board_pinouts_assigned_to, board.instanceOf(id), signal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            buildAnnotatedString {
                board.kindsOf(pin).forEach { kind ->
                    withStyle(SpanStyle(color = kind.color(dark), fontWeight = FontWeight.Bold)) { append(kind.symbol()) }
                    append(" ")
                }
            },
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
