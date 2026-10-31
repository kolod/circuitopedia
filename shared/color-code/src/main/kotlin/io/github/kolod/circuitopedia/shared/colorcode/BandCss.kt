package io.github.kolod.circuitopedia.shared.colorcode

import java.util.Locale

/**
 * Extra CSS for a band drawing with [bandCount] SVG bands (`band-1` … `band-N`): the bands in
 * [shownBands] (1-based, one per column) get the matching color of [colors], the others are hidden.
 */
fun bandCss(shownBands: List<Int>, colors: List<BandColor>, bandCount: Int): String = buildString {
    for (band in 1..bandCount) {
        val column = shownBands.indexOf(band)
        if (column < 0) append("#band-$band{display:none}")
        else append("#band-$band{fill:#%06X}".format(Locale.ROOT, colors[column].argb and 0xFFFFFF))
    }
}
