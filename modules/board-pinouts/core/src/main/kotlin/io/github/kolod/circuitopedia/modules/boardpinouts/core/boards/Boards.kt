package io.github.kolod.circuitopedia.modules.boardpinouts.core.boards

import io.github.kolod.circuitopedia.modules.boardpinouts.core.Board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId

/** The boards in spec FR-002 order. */
val boards: List<Board> by lazy {
    listOf(unoR3, nano, picoW, pico2W, raspberryPi4B, bluePill, blackPill)
}

fun board(id: BoardId): Board? = boards.firstOrNull { it.id == id }
