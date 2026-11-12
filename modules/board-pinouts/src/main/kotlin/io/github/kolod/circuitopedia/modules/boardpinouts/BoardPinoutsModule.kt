package io.github.kolod.circuitopedia.modules.boardpinouts

import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

val boardPinoutsModule = AppModule(
    id = ModuleId("board-pinouts"),
    category = Category.Reference,
    titleRes = R.string.board_pinouts_title,
    descriptionRes = R.string.board_pinouts_description,
    iconRes = R.drawable.board_pinouts_icon,
    content = { PinoutsScreen() },
)
