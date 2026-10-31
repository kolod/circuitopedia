package io.github.kolod.circuitopedia.shared.resistors

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * An exact rational number, kept reduced with a positive denominator. Resistances are rationals
 * because parallel connections are generally not finite decimals (research R3).
 */
class Rational private constructor(val num: BigInteger, val den: BigInteger) : Comparable<Rational> {

    operator fun plus(other: Rational) = of(num * other.den + other.num * den, den * other.den)

    operator fun minus(other: Rational) = of(num * other.den - other.num * den, den * other.den)

    operator fun times(other: Rational) = of(num * other.num, den * other.den)

    operator fun div(other: Rational) = of(num * other.den, den * other.num)

    fun reciprocal() = of(den, num)

    fun abs() = if (num.signum() < 0) Rational(num.negate(), den) else this

    val signum: Int get() = num.signum()

    fun toDouble(): Double = BigDecimal(num).divide(BigDecimal(den), MathContext.DECIMAL64).toDouble()

    /** Rounded HALF_UP to [significantDigits] significant digits, without a negative scale. */
    fun round(significantDigits: Int): BigDecimal {
        if (num.signum() == 0) return BigDecimal.ZERO
        val rounded = BigDecimal(num).divide(BigDecimal(den), MathContext(significantDigits, RoundingMode.HALF_UP))
        return if (rounded.scale() < 0) rounded.setScale(0) else rounded
    }

    override fun compareTo(other: Rational): Int = (num * other.den).compareTo(other.num * den)

    override fun equals(other: Any?) = other is Rational && num == other.num && den == other.den

    override fun hashCode() = 31 * num.hashCode() + den.hashCode()

    override fun toString() = if (den == BigInteger.ONE) "$num" else "$num/$den"

    companion object {
        val ZERO = Rational(BigInteger.ZERO, BigInteger.ONE)

        fun of(num: BigInteger, den: BigInteger): Rational {
            require(den.signum() != 0) { "Zero denominator" }
            val gcd = num.gcd(den).let { if (den.signum() < 0) it.negate() else it }
            return Rational(num / gcd, den / gcd)
        }

        fun of(num: Long, den: Long) = of(BigInteger.valueOf(num), BigInteger.valueOf(den))

        fun of(value: BigDecimal): Rational =
            if (value.scale() <= 0) of(value.toBigIntegerExact(), BigInteger.ONE)
            else of(value.unscaledValue(), BigInteger.TEN.pow(value.scale()))
    }
}
