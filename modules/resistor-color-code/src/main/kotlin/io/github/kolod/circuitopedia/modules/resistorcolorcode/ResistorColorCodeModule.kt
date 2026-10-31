package io.github.kolod.circuitopedia.modules.resistorcolorcode

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

val resistorColorCodeModule = AppModule(
    id = ModuleId("resistor-color-code"),
    category = Category.Calculations,
    titleRes = R.string.resistor_color_code_title,
    descriptionRes = R.string.resistor_color_code_description,
    iconRes = R.drawable.resistor_color_code_icon,
    content = { ColorCodeScreen() },
)
