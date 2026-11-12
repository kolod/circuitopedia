package io.github.kolod.circuitopedia.modules.boardpinouts.core

// Lookups over one board (contracts/core-api.md, "Catalog").

/** Every header position wired to [pin]: the Uno's A4 and SDA are the same chip pin. */
fun Board.headerPinsOf(pin: ChipPin): List<HeaderPin> = pins.filter { it.pin == pin }

fun Board.limitsOf(pin: ChipPin): List<Limit> = limits[pin].orEmpty()

fun Board.groupLimitsOf(pin: ChipPin): List<GroupLimit> = groupLimits.filter { pin in it.pins }

/** Every placement signal that can use [pin], in planner order. */
fun Board.functionsOf(pin: ChipPin): List<Pair<Placement, String>> =
    placements.flatMap { placement ->
        placement.slots.filter { pin in it.candidates }.map { placement to it.signal }
    }

fun Board.placement(id: PlacementId): Placement? = placements.firstOrNull { it.id == id }

fun Board.functionOf(placement: Placement): FunctionKind? =
    functions.firstOrNull { placement in it.placements }?.kind
