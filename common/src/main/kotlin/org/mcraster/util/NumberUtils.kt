package org.mcraster.util

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

object NumberUtils {

    private val BIG_DECIMAL_0p5 = "0.5".toBigDecimal()
    private val BIG_DECIMAL_n0p5 = "-0.5".toBigDecimal()

    fun BigDecimal.floorToIntExact(): Int {
        // Get rid of fractional part
        var resultInt = this.toBigInteger()
        // If number is exactly an integer, we are done
        if (this.isEqualTo(resultInt)) return this.intValueExact()
        // If number is negative, then removing fractional part rounded up, but we need to round down, so subtract one
        if (this.signum() < 0) --resultInt
        // Check that the original number is in range (result, result+1), otherwise throw
        if (this.isBiggerThan(resultInt) && this.isSmallerThan(resultInt + BigInteger.ONE)) {
            return resultInt.intValueExact()
        }
        throw RuntimeException("Exact floor to Int not possible for: $this")
    }

    fun BigDecimal.roundToIntExact(): Int {
        // Add 0.5 to original decimal
        val originalPlus0p5 = this + BIG_DECIMAL_0p5
        // Get rid of fractional part
        var resultInt = originalPlus0p5.toBigInteger()
        // If the original number +0.5 is negative, then removing fractional part, rounded it higher by one,
        // unless the result is exactly an integer, in which case there was no rounding effect.
        if (originalPlus0p5.signum() < 0 && originalPlus0p5.isNotEqualTo(resultInt)) {
            --resultInt
        }
        // Check that the final difference of the result and the original number is in range (-0.5; 0.5], or throw
        val change = resultInt.toBigDecimal() - this
        if (change > BIG_DECIMAL_n0p5 && change <= BIG_DECIMAL_0p5) return resultInt.intValueExact()
        throw RuntimeException("Exact rounding to Int not possible for: $this")
    }

    fun BigDecimal.ceilToIntExact(): Int {
        // Get rid of fractional part
        var resultInt = this.toBigInteger()
        // If number is exactly an integer, we are done
        if (this.isEqualTo(resultInt)) return this.intValueExact()
        // If number is positive, then removing fractional part rounded down, but we need to round up, so add one
        if (this.signum() > 0) ++resultInt
        // Check that the original number is in range (result-1, result), otherwise throw
        if (this.isBiggerThan(resultInt - BigInteger.ONE) && this.isSmallerThan(resultInt)) {
            return resultInt.intValueExact()
        }
        throw RuntimeException("Exact ceiling to Int not possible for: $this")
    }

    fun BigDecimal.isEqualTo(other: Int, tolerance: Int): Boolean {
        return isEqualTo(other.toBigDecimal(), tolerance.toBigDecimal())
    }

    fun Int.isEqualTo(other: Int, tolerance: Int): Boolean {
        return this.minus(other).absoluteValue <= tolerance
    }

    fun BigDecimal.isEqualTo(other: BigDecimal, tolerance: BigDecimal): Boolean {
        return this.minus(other).abs() <= tolerance
    }

    fun Int.clip(lowIncl: Int, highExcl: Int): Int {
        if (highExcl <= lowIncl) throw RuntimeException("Invalid clip params: $lowIncl and $highExcl")
        return max(lowIncl, min(highExcl - 1, this))
    }

    private fun BigDecimal.isEqualTo(x: BigInteger) = this.compareTo(x.toBigDecimal()) == 0
    private fun BigDecimal.isNotEqualTo(x: BigInteger) = this.compareTo(x.toBigDecimal()) != 0
    private fun BigDecimal.isBiggerThan(x: BigInteger) = this > x.toBigDecimal()
    private fun BigDecimal.isSmallerThan(x: BigInteger) = this < x.toBigDecimal()

}
