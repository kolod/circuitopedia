package io.github.kolod.circuitopedia

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.modules.inductorcolorcode.inductorColorCodeModule
import io.github.kolod.circuitopedia.modules.resistorcolorcode.resistorColorCodeModule
import io.github.kolod.circuitopedia.modules.resistorcombinations.resistorCombinationsModule
import io.github.kolod.circuitopedia.modules.voltagedivider.voltageDividerModule

/** The single place where modules are added to the app: one line per module. */
object ModuleRegistry {
    val modules: List<AppModule> = listOf(
        resistorColorCodeModule,
        inductorColorCodeModule,
        resistorCombinationsModule,
        voltageDividerModule,
    )
}
