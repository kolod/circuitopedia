package io.github.kolod.circuitopedia.modules.boardpinouts.core

import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.board

// Compact text for the saved planner state (research R7): boards separated by ';', each
// "boardId=assignment|assignment", each assignment "placementId:signal@pin,signal@-" ('-' = unused).
// None of the separators occur in board, placement, signal or pin names.

fun encode(assignments: Map<BoardId, List<Assignment>>): String =
    assignments.filterValues { it.isNotEmpty() }.entries.joinToString(";") { (id, list) ->
        id.value + "=" + list.joinToString("|") { a ->
            a.placement.value + ":" + a.chosen.entries.joinToString(",") { (signal, pin) -> "$signal@${pin?.name ?: "-"}" }
        }
    }

/** The inverse of [encode]. Unknown boards, placements, signals or pins and malformed parts are dropped. */
fun decode(text: String): Map<BoardId, List<Assignment>> =
    text.split(';').mapNotNull { part ->
        val (id, body) = part.split('=', limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
        val board = board(BoardId(id)) ?: return@mapNotNull null
        val list = body.split('|').mapNotNull { decodeAssignment(board, it) }
        board.id to list
    }.groupBy({ it.first }, { it.second }).mapValues { (_, lists) -> lists.flatten() }.filterValues { it.isNotEmpty() }

private fun decodeAssignment(board: Board, text: String): Assignment? {
    val (id, body) = text.split(':', limit = 2).takeIf { it.size == 2 } ?: return null
    val placement = board.placement(PlacementId(id)) ?: return null
    val chosen = body.split(',').associate { entry ->
        val (signal, pin) = entry.split('@', limit = 2).takeIf { it.size == 2 } ?: return null
        val slot = placement.slot(signal) ?: return null
        val chip = if (pin == "-") null else ChipPin(pin).takeIf { it in slot.candidates } ?: return null
        signal to chip
    }
    return Assignment(placement.id, chosen).takeIf { chosen.size == placement.slots.size && chosen.values.any { it != null } }
}
