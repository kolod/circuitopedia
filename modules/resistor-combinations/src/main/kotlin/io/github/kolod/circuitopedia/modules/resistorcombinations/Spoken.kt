package io.github.kolod.circuitopedia.modules.resistorcombinations

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.kolod.circuitopedia.shared.resistors.Combination
import io.github.kolod.circuitopedia.shared.resistors.Kind
import io.github.kolod.circuitopedia.shared.resistors.Rational
import io.github.kolod.circuitopedia.shared.resistors.formatDeviation
import io.github.kolod.circuitopedia.shared.siformat.formatDecimal
import java.math.BigDecimal
import java.util.Locale

private val THOUSAND = BigDecimal(1000)

/**
 * What a screen reader says for one result (spec FR-012), with unit words instead of symbols:
 * "10 kilohm parallel 10 kilohm, 5 kilohm, deviation 0 percent".
 */
@Composable
internal fun spokenResult(combination: Combination, target: Rational, locale: Locale): String {
    val units = listOf(
        stringResource(R.string.resistor_combinations_spoken_ohm),
        stringResource(R.string.resistor_combinations_spoken_kilohm),
        stringResource(R.string.resistor_combinations_spoken_megohm),
        stringResource(R.string.resistor_combinations_spoken_gigohm),
    )
    val plus = stringResource(R.string.resistor_combinations_spoken_series)
    val parallel = stringResource(R.string.resistor_combinations_spoken_parallel)
    fun ohms(value: Rational): String {
        var mantissa = value.round(4)
        var prefix = 0
        while (prefix < units.lastIndex && mantissa >= THOUSAND) {
            mantissa = mantissa.movePointLeft(3)
            prefix++
        }
        return formatDecimal(mantissa, locale) + " " + units[prefix]
    }
    val names = combination.values.map(::ohms)
    val connection = when (combination.kind) {
        Kind.Single -> names.single()
        Kind.Series2, Kind.Series3 -> names.joinToString(" $plus ")
        Kind.Parallel2, Kind.Parallel3 -> names.joinToString(" $parallel ")
        Kind.ParallelSeries -> "${names[0]} $parallel ${names[1]} $plus ${names[2]}"
    }
    val deviation = formatDeviation(combination.deviation(target), locale).removeSuffix("%")
    return "$connection, ${ohms(combination.resistance)}, " +
        stringResource(R.string.resistor_combinations_spoken_deviation, deviation)
}
