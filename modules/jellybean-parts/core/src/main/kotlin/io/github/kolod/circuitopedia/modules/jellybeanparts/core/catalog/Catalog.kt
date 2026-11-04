package io.github.kolod.circuitopedia.modules.jellybeanparts.core.catalog

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartEntry

/** All entries in spec FR-002 order, assembled from the per-category lists. */
internal val allEntries: List<PartEntry> =
    opAmps + comparators + timers + bipolarTransistors + mosfets + diodes +
        voltageRegulators + voltageReferences + logic + temperatureSensors + powerControllers
