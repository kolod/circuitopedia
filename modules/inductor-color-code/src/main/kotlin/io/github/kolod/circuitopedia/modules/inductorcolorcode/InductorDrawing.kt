package io.github.kolod.circuitopedia.modules.inductorcolorcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.inductorcolorcode.core.Selection
import io.github.kolod.circuitopedia.shared.colorcode.ui.BandDrawing

/** Number of bands drawn in `res/raw/inductor_color_code_inductor.svg` (`band-1` … `band-4`). */
internal const val SVG_BAND_COUNT = 4

/** The SVG bands (1-based) each layout shows, one per column; 3 bands hide the tolerance band. */
internal fun BandLayout.svgBands(): List<Int> = when (this) {
    BandLayout.Three -> listOf(1, 2, 3)
    BandLayout.Four -> listOf(1, 2, 3, 4)
}

/** Decorative inductor in the selected colors, with leader lines to the table header. */
@Composable
fun InductorDrawing(selection: Selection, modifier: Modifier = Modifier) {
    BandDrawing(R.raw.inductor_color_code_inductor, selection.layout.svgBands(), selection.colors, modifier)
}
