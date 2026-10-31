package io.github.kolod.circuitopedia.modules.voltagedivider

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.kolod.circuitopedia.modules.voltagedivider.core.DividerBuild
import io.github.kolod.circuitopedia.modules.voltagedivider.core.Quantity
import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import io.github.kolod.circuitopedia.shared.resistors.formatDeviation
import io.github.kolod.circuitopedia.shared.siformat.formatDecimal
import java.math.BigDecimal
import java.util.Locale

private val THOUSAND = BigDecimal(1000)

/** Unit words for screen readers (spec FR-015): "19.41 kilohm", "3.3 volt", "4.7 millivolt". */
internal class SpokenUnits(private val locale: Locale, private val volts: List<String>, private val ohms: List<String>) {
    /** [value] with 4 significant digits and a prefix word; volts use milli, none, kilo. */
    fun say(value: Rational, voltage: Boolean): String {
        val words = if (voltage) volts else ohms
        var mantissa = value.abs().round(4)
        var prefix = if (voltage) 1 else 0
        while (prefix < words.lastIndex && mantissa >= THOUSAND) {
            mantissa = mantissa.movePointLeft(3)
            prefix++
        }
        while (voltage && prefix > 0 && mantissa < BigDecimal.ONE) {
            mantissa = mantissa.movePointRight(3)
            prefix--
        }
        val sign = if (value.signum < 0) "−" else ""
        return sign + formatDecimal(mantissa.stripTrailingZeros(), locale) + " " + words[prefix]
    }
}

@Composable
internal fun rememberSpokenUnits(locale: Locale) = SpokenUnits(
    locale,
    listOf(
        stringResource(R.string.voltage_divider_spoken_millivolt),
        stringResource(R.string.voltage_divider_spoken_volt),
        stringResource(R.string.voltage_divider_spoken_kilovolt),
    ),
    listOf(
        stringResource(R.string.voltage_divider_spoken_ohm),
        stringResource(R.string.voltage_divider_spoken_kilohm),
        stringResource(R.string.voltage_divider_spoken_megohm),
        stringResource(R.string.voltage_divider_spoken_gigohm),
    ),
)

/** "R2 (lower), 19.41 kilohm": the field's name and value; its state is announced separately. */
@Composable
internal fun spokenField(quantity: Quantity, value: Rational?, text: String, units: SpokenUnits): String {
    val label = stringResource(quantity.labelRes())
    return if (value == null) "$label, $text" else "$label, ${units.say(value, quantity.isVoltage)}"
}

/** One build as a single item: both arms, then U2 with its error and R with its deviation. */
@Composable
internal fun spokenBuild(build: DividerBuild, units: SpokenUnits, locale: Locale): String {
    val plus = stringResource(R.string.voltage_divider_spoken_series)
    val parallel = stringResource(R.string.voltage_divider_spoken_parallel)
    fun arm(arm: Combination): String {
        val names = arm.values.map { units.say(it, voltage = false) }
        return when (arm.kind) {
            Kind.Parallel2 -> names.joinToString(" $parallel ")
            else -> names.joinToString(" $plus ")
        }
    }
    val error = stringResource(R.string.voltage_divider_spoken_error, formatDeviation(build.u2Error, locale).removeSuffix("%"))
    val deviation = stringResource(R.string.voltage_divider_spoken_deviation, formatDeviation(build.totalDeviation, locale).removeSuffix("%"))
    return "R1 ${arm(build.r1)}, R2 ${arm(build.r2)}, U2 ${units.say(build.u2, voltage = true)}, $error, " +
        "R ${units.say(build.total, voltage = false)}, $deviation"
}
