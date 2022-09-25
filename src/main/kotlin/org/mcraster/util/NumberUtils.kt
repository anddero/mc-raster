package org.mcraster.util

import java.math.BigDecimal
import java.math.BigInteger

object NumberUtils {

    private val BIG_DECIMAL_0p5 = "0.5".toBigDecimal()
    private val BIG_DECIMAL_m0p5 = "-0.5".toBigDecimal()

    fun BigDecimal.roundDownToIntExact(): Int {
        // Get rid of fractional part
        var resultInt = this.toBigInteger()
        // If number is exactly an integer, we are done
        if (this.isEqualTo(resultInt)) return this.intValueExact()
        // If number is negative, then removing fractional part rounded up, but we need to round down, so subtract one
        if (this.signum() < 0) --resultInt
        // Check that the original number is in range [result, result+1), otherwise throw
        if (this.isEqualOrBiggerThan(resultInt) && this.isSmallerThan(resultInt + BigInteger.ONE)) {
            return resultInt.intValueExact()
        }
        throw RuntimeException("Exact lowering to Int not possible for: $this")
    }

    fun BigDecimal.roundToIntHalfUpExact(): Int {
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
        if (change > BIG_DECIMAL_m0p5 && change <= BIG_DECIMAL_0p5) return resultInt.intValueExact()
        throw RuntimeException("Exact rounding to Int not possible for: $this")
    }

    private fun BigDecimal.isEqualTo(x: BigInteger) = this.compareTo(x.toBigDecimal()) == 0
    private fun BigDecimal.isNotEqualTo(x: BigInteger) = this.compareTo(x.toBigDecimal()) != 0
    private fun BigDecimal.isEqualOrBiggerThan(x: BigInteger) = this >= x.toBigDecimal()
    private fun BigDecimal.isSmallerThan(x: BigInteger) = this < x.toBigDecimal()

}
