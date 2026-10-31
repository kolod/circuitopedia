package io.github.kolod.circuitopedia.shared.colorcode.ui

import androidx.annotation.StringRes
import io.github.kolod.circuitopedia.shared.colorcode.BandColor

/** The localized name of a band color. */
@StringRes
fun BandColor.nameRes(): Int = when (this) {
    BandColor.Black -> R.string.color_code_color_black
    BandColor.Brown -> R.string.color_code_color_brown
    BandColor.Red -> R.string.color_code_color_red
    BandColor.Orange -> R.string.color_code_color_orange
    BandColor.Yellow -> R.string.color_code_color_yellow
    BandColor.Green -> R.string.color_code_color_green
    BandColor.Blue -> R.string.color_code_color_blue
    BandColor.Violet -> R.string.color_code_color_violet
    BandColor.Gray -> R.string.color_code_color_gray
    BandColor.White -> R.string.color_code_color_white
    BandColor.Gold -> R.string.color_code_color_gold
    BandColor.Silver -> R.string.color_code_color_silver
}
