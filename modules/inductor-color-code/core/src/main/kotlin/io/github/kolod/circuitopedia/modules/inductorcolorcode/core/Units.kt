package io.github.kolod.circuitopedia.modules.inductorcolorcode.core

import java.math.BigDecimal

/** Inductance in microhenries, exact. */
@JvmInline
value class Microhenries(val value: BigDecimal) {
    init {
        require(value.signum() > 0) { "Inductance must be positive: $value" }
    }
}
