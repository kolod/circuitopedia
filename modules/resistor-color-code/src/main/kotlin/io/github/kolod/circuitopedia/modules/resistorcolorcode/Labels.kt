package io.github.kolod.circuitopedia.modules.resistorcolorcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandLayout
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandRole
import io.github.kolod.circuitopedia.shared.colorcode.ui.R as SharedR

/** Short column header: "Band 1", "Mult.", "Toler.", "TCR". */
@Composable
internal fun BandLayout.headerText(index: Int): String = when (roles[index]) {
    BandRole.Digit -> stringResource(SharedR.string.color_code_header_band, index + 1)
    BandRole.Multiplier -> stringResource(SharedR.string.color_code_header_multiplier)
    BandRole.Tolerance -> stringResource(SharedR.string.color_code_header_tolerance)
    BandRole.TemperatureCoefficient -> stringResource(R.string.resistor_color_code_header_tcr)
}

/** Full band name for screen readers: "Band 1", "Multiplier", "Tolerance", "Temperature coefficient". */
@Composable
internal fun BandLayout.bandName(index: Int): String = when (roles[index]) {
    BandRole.Digit -> stringResource(SharedR.string.color_code_header_band, index + 1)
    BandRole.Multiplier -> stringResource(SharedR.string.color_code_band_multiplier)
    BandRole.Tolerance -> stringResource(SharedR.string.color_code_band_tolerance)
    BandRole.TemperatureCoefficient -> stringResource(R.string.resistor_color_code_band_tcr)
}
