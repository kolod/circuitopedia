package io.github.kolod.circuitopedia.modules.boardpinouts.core

// The pin planner (research R3, contracts/core-api.md). Pure functions over a board and the current
// assignments; an assignment never conflicts with itself, so a placement can be edited in place.

/** A chosen placement: for each slot signal, the pin it uses, or null when the slot is unused. */
data class Assignment(val placement: PlacementId, val chosen: Map<String, ChipPin?>)

sealed interface Availability {
    data object Available : Availability
    data class Warned(val warnings: List<Warning>) : Availability
    data class Blocked(val reasons: List<Conflict>) : Availability
}

sealed interface Conflict {
    data class SameInstance(val holder: PlacementId) : Conflict
    data class PinTaken(val pin: ChipPin, val holder: PlacementId, val signal: String) : Conflict
    data class ResourceTaken(val resource: ResourceId, val holder: PlacementId) : Conflict
}

sealed interface Warning {
    data class PinBoardUse(val pin: ChipPin, val use: BoardUse) : Warning
    data class PinBootRole(val pin: ChipPin, val effect: BootEffect) : Warning
}

/** Pins claimed by the assignments: pin → (placement, signal), used slots only. */
fun Board.claims(assignments: List<Assignment>): Map<ChipPin, Pair<PlacementId, String>> =
    assignments.flatMap { a -> a.chosen.mapNotNull { (signal, pin) -> pin?.let { it to (a.placement to signal) } } }.toMap()

/** The assignments other than [placement]'s own. */
private fun List<Assignment>.others(placement: Placement) = filter { it.placement != placement.id }

/** Conflicts that do not depend on pins: the same instance or a shared resource held elsewhere. */
private fun Board.holderConflicts(others: List<Assignment>, placement: Placement): List<Conflict> =
    others.flatMap { a ->
        val held = placement(a.placement) ?: return@flatMap emptyList()
        buildList {
            if (held.instance == placement.instance) add(Conflict.SameInstance(held.id))
            placement.resources.filter { it in held.resources }.forEach { add(Conflict.ResourceTaken(it, held.id)) }
        }
    }

private fun Board.warnings(pins: Collection<ChipPin>): List<Warning> = pins.flatMap { pin ->
    limitsOf(pin).mapNotNull { limit ->
        when (limit.kind) {
            LimitKind.BoardUse -> limit.use?.let { Warning.PinBoardUse(pin, it) }
            LimitKind.BootRole -> limit.effect?.let { Warning.PinBootRole(pin, it) }
            else -> null
        }
    }
}

private fun verdict(conflicts: List<Conflict>, warnings: List<Warning>): Availability = when {
    conflicts.isNotEmpty() -> Availability.Blocked(conflicts.distinct())
    warnings.isNotEmpty() -> Availability.Warned(warnings.distinct())
    else -> Availability.Available
}

/**
 * The default assignment for [placement]: the first free candidate per required slot, optional slots
 * unused. A placement with only optional slots starts with the first slot that has a free pin. Null
 * when a required slot (or, without required slots, every slot) has no free candidate.
 */
fun Board.defaultAssignment(assignments: List<Assignment>, placement: Placement): Assignment? {
    val taken = claims(assignments.others(placement)).keys.toMutableSet()
    val chosen = linkedMapOf<String, ChipPin?>()
    val required = placement.slots.filter { it.required }
    for (slot in placement.slots) {
        if (!slot.required) {
            chosen[slot.signal] = null
            continue
        }
        val pin = slot.candidates.firstOrNull { it !in taken } ?: return null
        chosen[slot.signal] = pin
        taken += pin
    }
    if (required.isEmpty()) {
        val slot = placement.slots.firstOrNull { s -> s.candidates.any { it !in taken } } ?: return null
        chosen[slot.signal] = slot.candidates.first { it !in taken }
    }
    return Assignment(placement.id, chosen)
}

/** Whether [placement] can be added (or kept) next to the other assignments. */
fun Board.availability(assignments: List<Assignment>, placement: Placement): Availability {
    val others = assignments.others(placement)
    val holders = holderConflicts(others, placement)
    val default = defaultAssignment(assignments, placement)
    if (default == null) {
        val claims = claims(others)
        val required = placement.slots.filter { it.required }.ifEmpty { placement.slots }
        val pinConflicts = required.flatMap { slot ->
            slot.candidates.mapNotNull { pin -> claims[pin]?.let { (holder, signal) -> Conflict.PinTaken(pin, holder, signal) } }
        }
        return verdict(holders + pinConflicts, emptyList())
    }
    return verdict(holders, warnings(default.chosen.values.filterNotNull()))
}

/** Whether [pin] can carry [signal] of [placement], including pins used by the placement's other signals. */
fun Board.pinAvailability(assignments: List<Assignment>, placement: Placement, signal: String, pin: ChipPin): Availability {
    val holders = holderConflicts(assignments.others(placement), placement)
    val holder = claims(assignments)[pin]
    val pinConflict = holder?.takeUnless { it == placement.id to signal }?.let { (id, s) -> Conflict.PinTaken(pin, id, s) }
    return verdict(holders + listOfNotNull(pinConflict), warnings(listOf(pin)))
}

/**
 * Releases [signal]. A required slot can only be released in a group placement (the remap stays).
 * Returns null when no slot would remain used.
 */
fun Assignment.unuse(signal: String, placement: Placement): Assignment? {
    val slot = placement.slot(signal) ?: return this
    if (slot.required && !placement.movesAsGroup) return this
    val next = copy(chosen = chosen + (signal to null))
    return next.takeIf { it.chosen.values.any { pin -> pin != null } }
}

fun Assignment.use(signal: String, pin: ChipPin): Assignment = copy(chosen = chosen + (signal to pin))
