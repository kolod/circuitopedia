package io.github.kolod.circuitopedia.shared.colorcode

import java.math.BigDecimal

/** Tolerance in percent, exact (for example 0.05 for ±0.05%). */
@JvmInline
value class Percent(val value: BigDecimal) {
    init {
        require(value.signum() > 0) { "Tolerance must be positive: $value" }
    }
}
