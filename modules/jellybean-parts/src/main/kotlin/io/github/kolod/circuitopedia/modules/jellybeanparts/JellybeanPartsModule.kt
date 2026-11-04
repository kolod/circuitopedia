package io.github.kolod.circuitopedia.modules.jellybeanparts

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

val jellybeanPartsModule = AppModule(
    id = ModuleId("jellybean-parts"),
    category = Category.Reference,
    titleRes = R.string.jellybean_parts_title,
    descriptionRes = R.string.jellybean_parts_description,
    iconRes = R.drawable.jellybean_parts_icon,
    content = { PartsScreen() },
)
