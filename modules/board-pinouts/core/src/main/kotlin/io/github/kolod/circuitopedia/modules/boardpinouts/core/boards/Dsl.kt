package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.ChipPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.HeaderPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.NoteKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Placement
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PlacementId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerPin
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ResourceId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SignalSlot
import io.github.kolod.circuitopedia.modules.boardpinouts.core.SourceRef

// Small helpers that keep the transcribed board files close to the tables they copy.

internal fun gpio(header: String, position: Int, label: String, chip: String, number: String? = null) =
    HeaderPin(header, position, label, number = number, pin = ChipPin(chip))

internal fun power(header: String, position: Int, label: String, power: PowerPin, number: String? = null) =
    HeaderPin(header, position, label, number = number, power = power)

internal fun pins(vararg names: String): List<ChipPin> = names.map(::ChipPin)

/** A slot the placement needs; [pins] are its candidates. */
internal fun slot(signal: String, vararg pins: String) = SignalSlot(signal, pins.map(::ChipPin), required = true)

/** A slot the user may leave unused (CTS, NSS, one ADC or PWM channel). */
internal fun opt(signal: String, vararg pins: String) = SignalSlot(signal, pins.map(::ChipPin), required = false)

internal fun placement(
    id: String,
    instance: String,
    source: SourceRef,
    vararg slots: SignalSlot,
    group: Boolean = false,
    resources: Set<String> = emptySet(),
    notes: List<NoteKey> = emptyList(),
) = Placement(PlacementId(id), instance, slots.toList(), source, group, resources.map(::ResourceId).toSet(), notes)

/** Collects limits per chip pin in the order they are added. */
internal class Limits {
    private val map = linkedMapOf<ChipPin, MutableList<Limit>>()

    fun add(pins: Iterable<String>, limit: Limit) = pins.forEach { map.getOrPut(ChipPin(it)) { mutableListOf() } += limit }

    fun add(pin: String, limit: Limit) = add(listOf(pin), limit)

    fun build(): Map<ChipPin, List<Limit>> = map
}

internal fun limits(block: Limits.() -> Unit) = Limits().apply(block).build()

internal fun others(vararg entries: Pair<String, List<String>>): Map<ChipPin, List<String>> =
    entries.associate { (pin, names) -> ChipPin(pin) to names }
