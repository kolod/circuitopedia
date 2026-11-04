package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.entry

/**
 * The list of parts by category, or the page of the open part. The open part and both scroll
 * positions survive rotation and process death; back on a page returns to the list where it was.
 */
@Composable
fun PartsScreen() {
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val open = openId?.let { entry(PartId(it)) }
    if (open == null) {
        PartList(listState, onOpen = { openId = it.value })
    } else {
        BackHandler { openId = null }
        key(open.id.value) { PartPage(open) }
    }
}
