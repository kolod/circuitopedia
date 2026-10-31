package io.github.kolod.circuitopedia.modules.resistorcolorcode.core

import java.math.BigDecimal

/** Resistance in ohms, exact. */
@JvmInline
value class Ohms(val value: BigDecimal) {
    init {
        require(value.signum() > 0) { "Resistance must be positive: $value" }
    }
}

/** Temperature coefficient of resistance in ppm/K (the magnitude of a ± value). */
@JvmInline
value class PpmPerKelvin(val value: Int) {
    init {
        require(value > 0) { "Temperature coefficient must be positive: $value" }
    }
}
