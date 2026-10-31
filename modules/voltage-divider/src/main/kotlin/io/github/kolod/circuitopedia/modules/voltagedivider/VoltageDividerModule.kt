package io.github.kolod.circuitopedia.modules.voltagedivider

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

val voltageDividerModule = AppModule(
    id = ModuleId("voltage-divider"),
    category = Category.Calculations,
    titleRes = R.string.voltage_divider_title,
    descriptionRes = R.string.voltage_divider_description,
    iconRes = R.drawable.voltage_divider_icon,
    content = { DividerScreen() },
)