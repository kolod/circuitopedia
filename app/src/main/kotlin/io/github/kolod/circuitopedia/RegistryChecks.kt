package io.github.kolod.circuitopedia

import io.github.kolod.circuitopedia.module.ModuleId

/** Every id that occurs more than once in [ids]. */
fun duplicateIds(ids: List<ModuleId>): Set<ModuleId> =
    ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
