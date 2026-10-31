package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection
import io.github.kolod.circuitopedia.shared.colorcode.ui.BandDrawing

/** Number of bands drawn in `res/raw/resistor_color_code_resistor.svg` (`band-1` … `band-6`). */
internal const val SVG_BAND_COUNT = 6

/**
 * The SVG bands (1-based) each layout shows, one per column. The last tolerance or TCR band sits on
 * the right end cap (`band-6`); the unused bands are hidden.
 */
internal fun BandLayout.svgBands(): List<Int> = when (this) {
    BandLayout.Three -> listOf(1, 2, 3)
    BandLayout.Four -> listOf(1, 2, 3, 6)
    BandLayout.Five -> listOf(1, 2, 3, 4, 6)
    BandLayout.Six -> listOf(1, 2, 3, 4, 5, 6)
}

/** Decorative resistor in the selected colors, with leader lines to the table header. */
@Composable
fun ResistorDrawing(selection: Selection, modifier: Modifier = Modifier) {
    BandDrawing(R.raw.resistor_color_code_resistor, selection.layout.svgBands(), selection.colors, modifier)
}
