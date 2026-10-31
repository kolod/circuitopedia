package io.github.kolod.circuitopedia.modules.inductorcolorcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.Selection
import io.github.kolod.circuitopedia.shared.colorcode.BandColor

/** The selected tab and, per tab, its own selection (FR-015). */
class InductorState(layout: BandLayout, selections: Map<BandLayout, Selection>) {
    var layout by mutableStateOf(layout)
    val selections = mutableStateMapOf<BandLayout, Selection>().apply { putAll(selections) }

    fun select(layout: BandLayout, index: Int, color: BandColor) {
        selections[layout] = selections.getValue(layout).with(index, color)
    }

    companion object {
        /** Saved as [layout ordinal, then the color ordinals of every layout in order]. */
        val Saver: Saver<InductorState, ArrayList<Int>> = Saver(
            save = { state ->
                ArrayList<Int>().apply {
                    add(state.layout.ordinal)
                    BandLayout.entries.forEach { layout -> state.selections.getValue(layout).colors.forEach { add(it.ordinal) } }
                }
            },
            restore = { saved ->
                var next = 1
                val selections = BandLayout.entries.associateWith { layout ->
                    Selection(layout, List(layout.bandCount) { BandColor.entries[saved[next++]] })
                }
                InductorState(BandLayout.entries[saved[0]], selections)
            },
        )
    }
}

@Composable
fun rememberInductorState(): InductorState = rememberSaveable(saver = InductorState.Saver) {
    InductorState(BandLayout.Four, BandLayout.entries.associateWith(Selection::default))
}
