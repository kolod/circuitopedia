# Contract: Board Pinouts Core API

Package: `io.github.kolod.circuitopedia.modules.boardpinouts.core`, in the pure-Kotlin module
`:modules:board-pinouts:core`. This file is normative. The data shapes are described in
[data-model.md](../data-model.md).

## Catalog

```kotlin
val boards: List<Board>                       // FR-002 order
fun board(id: BoardId): Board?

@JvmInline value class BoardId(val value: String)
@JvmInline value class ChipPin(val name: String)
@JvmInline value class PlacementId(val value: String)
@JvmInline value class ResourceId(val value: String)

fun Board.headerPinsOf(pin: ChipPin): List<HeaderPin>   // duplicates: Uno A4 and SDA
fun Board.limitsOf(pin: ChipPin): List<Limit>
fun Board.groupLimitsOf(pin: ChipPin): List<GroupLimit>
fun Board.functionsOf(pin: ChipPin): List<Pair<Placement, String>>   // placement + signal
fun Board.placement(id: PlacementId): Placement?
```

## Planner (research R3)

```kotlin
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

fun Board.availability(assignments: List<Assignment>, placement: Placement): Availability
fun Board.pinAvailability(
    assignments: List<Assignment>, placement: Placement, signal: String, pin: ChipPin,
): Availability

/** The default assignment for [placement]: the first free candidate per required slot. */
fun Board.defaultAssignment(assignments: List<Assignment>, placement: Placement): Assignment?

/** Pins claimed by the assignments: pin → (placement, signal). */
fun Board.claims(assignments: List<Assignment>): Map<ChipPin, Pair<PlacementId, String>>

/** Releases a slot; returns null if the result would have no used slot. */
fun Assignment.unuse(signal: String, placement: Placement): Assignment?
fun Assignment.use(signal: String, pin: ChipPin): Assignment

/** Compact text for saved state, and its inverse. Unknown ids are dropped on decode. */
fun encode(assignments: Map<BoardId, List<Assignment>>): String
fun decode(text: String): Map<BoardId, List<Assignment>>
```

Guarantees:
- **Pure:** these functions have no side effects and do no I/O.
- **Exclusive verdicts:** `Blocked` is returned whenever a conflict exists. Warnings are reported
  only when nothing blocks.
- **Self-exclusion:** an assignment never conflicts with itself, so a placement that is already
  assigned can be edited.
- **Round trip:** `decode(encode(x)) == x` for valid inputs.
