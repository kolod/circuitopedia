package io.github.kolod.circuitopedia.modules.inductorcolorcode

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

val inductorColorCodeModule = AppModule(
    id = ModuleId("inductor-color-code"),
    category = Category.Calculations,
    titleRes = R.string.inductor_color_code_title,
    descriptionRes = R.string.inductor_color_code_description,
    iconRes = R.drawable.inductor_color_code_icon,
    content = { InductorScreen() },
)
