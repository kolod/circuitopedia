package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole

@StringRes
internal fun BandColor.nameRes(): Int = when (this) {
    BandColor.Black -> R.string.resistor_color_code_color_black
    BandColor.Brown -> R.string.resistor_color_code_color_brown
    BandColor.Red -> R.string.resistor_color_code_color_red
    BandColor.Orange -> R.string.resistor_color_code_color_orange
    BandColor.Yellow -> R.string.resistor_color_code_color_yellow
    BandColor.Green -> R.string.resistor_color_code_color_green
    BandColor.Blue -> R.string.resistor_color_code_color_blue
    BandColor.Violet -> R.string.resistor_color_code_color_violet
    BandColor.Gray -> R.string.resistor_color_code_color_gray
    BandColor.White -> R.string.resistor_color_code_color_white
    BandColor.Gold -> R.string.resistor_color_code_color_gold
    BandColor.Silver -> R.string.resistor_color_code_color_silver
}

/** Short column header: "Band 1", "Mult.", "Toler.", "TCR". */
@Composable
internal fun BandLayout.headerText(index: Int): String = when (roles[index]) {
    BandRole.Digit -> stringResource(R.string.resistor_color_code_header_band, index + 1)
    BandRole.Multiplier -> stringResource(R.string.resistor_color_code_header_multiplier)
    BandRole.Tolerance -> stringResource(R.string.resistor_color_code_header_tolerance)
    BandRole.TemperatureCoefficient -> stringResource(R.string.resistor_color_code_header_tcr)
}

/** Full band name for screen readers: "Band 1", "Multiplier", "Tolerance", "Temperature coefficient". */
@Composable
internal fun BandLayout.bandName(index: Int): String = when (roles[index]) {
    BandRole.Digit -> stringResource(R.string.resistor_color_code_header_band, index + 1)
    BandRole.Multiplier -> stringResource(R.string.resistor_color_code_band_multiplier)
    BandRole.Tolerance -> stringResource(R.string.resistor_color_code_band_tolerance)
    BandRole.TemperatureCoefficient -> stringResource(R.string.resistor_color_code_band_tcr)
}

/** Label above a band in the drawing: "1st", "2nd", "3rd", "MUL", "TOL", "TCR". */
@StringRes
internal fun BandLayout.drawingLabelRes(index: Int): Int = when (roles[index]) {
    BandRole.Digit -> listOf(
        R.string.resistor_color_code_label_1st,
        R.string.resistor_color_code_label_2nd,
        R.string.resistor_color_code_label_3rd,
    )[index]
    BandRole.Multiplier -> R.string.resistor_color_code_label_mul
    BandRole.Tolerance -> R.string.resistor_color_code_label_tol
    BandRole.TemperatureCoefficient -> R.string.resistor_color_code_label_tcr
}
