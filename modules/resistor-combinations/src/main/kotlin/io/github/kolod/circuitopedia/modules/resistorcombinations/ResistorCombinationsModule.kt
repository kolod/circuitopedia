package io.github.kolod.circuitopedia.modules.resistorcombinations

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

val resistorCombinationsModule = AppModule(
    id = ModuleId("resistor-combinations"),
    category = Category.Calculations,
    titleRes = R.string.resistor_combinations_title,
    descriptionRes = R.string.resistor_combinations_description,
    iconRes = R.drawable.resistor_combinations_icon,
    content = { CombinationsScreen() },
)
