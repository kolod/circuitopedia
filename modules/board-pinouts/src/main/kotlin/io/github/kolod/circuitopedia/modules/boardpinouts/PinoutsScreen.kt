package io.github.kolod.circuitopedia.modules.boardpinouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.boards.board
import io.github.kolod.circuitopedia.modules.boardpinouts.core.decode
import io.github.kolod.circuitopedia.modules.boardpinouts.core.encode

/**
 * The board list, or the page of the open board (research R7). The open board, the list position and
 * the planner assignments of every board survive rotation and process death; the assignments are
 * saved as the compact text of [encode]. "Keep the screen on" applies while a
 * board is shown, and the choice is restored when a board is opened again.
 */
@Composable
fun PinoutsScreen() {
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var planned by rememberSaveable { mutableStateOf("") }
    var keepScreenOn by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val assignments = remember(planned) { decode(planned) }
    val open = openId?.let { board(BoardId(it)) }
    // "Keep the screen on" holds only while a board is shown. The user's choice is kept, so it comes
    // back with the next board; the board list and leaving the module release the screen.
    val hold = keepScreenOn && open != null
    val view = LocalView.current
    DisposableEffect(view, hold) {
        view.keepScreenOn = hold
        onDispose { view.keepScreenOn = false }
    }
    if (open == null) {
        BoardList(listState, onOpen = { openId = it.value })
    } else {
        BackHandler { openId = null }
        key(open.id.value) {
            BoardPage(
                board = open,
                assignments = assignments[open.id].orEmpty(),
                onAssignments = { list -> planned = encode(assignments + (open.id to list)) },
                keepScreenOn = keepScreenOn,
                onKeepScreenOn = { keepScreenOn = it },
            )
        }
    }
}
