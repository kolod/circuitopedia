package io.github.kolod.circuitopedia.modules.jellybeanparts.core

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog.allEntries

/** The 34 entries in spec FR-002 order. */
val catalog: List<PartEntry> = allEntries

private val byId: Map<PartId, PartEntry> = catalog.associateBy { it.id }

/** The entries grouped by category, in [Category] order. */
fun catalogByCategory(): Map<Category, List<PartEntry>> =
    Category.entries.associateWith { c -> catalog.filter { it.category == c } }

fun entry(id: PartId): PartEntry? = byId[id]
