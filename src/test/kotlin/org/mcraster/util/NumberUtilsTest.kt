package org.mcraster.util

import org.mcraster.util.NumberUtils.roundDownToIntExact
import org.mcraster.util.NumberUtils.roundToIntHalfUpExact
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NumberUtilsTest {

    @Test
    fun roundDownToIntExact() {
        val values = arrayOf(
            "-81812663.000001", "-81812664",
            "-2314713.999999999999", "-2314714",
            "-200", "-200",
            "-10.9", "-11",
            "-4.51", "-5",
            "-4.5", "-5",
            "-4.49", "-5",
            "-0.51", "-1",
            "-0.5", "-1",
            "-0.49", "-1",
            "-0.0000000001", "-1",
            "0", "0",
            "0.000000001", "0",
            "0.1", "0",
            "0.49", "0",
            "0.5", "0",
            "0.51", "0",
            "0.999", "0",
            "10.3", "10",
            "10.29381", "10",
            "318718239.1238714192", "318718239",
            "318718239.9238714192", "318718239"
        )
        for (i in values.indices step 2) {
            assertEquals(values[i + 1], values[i].toBigDecimal().roundDownToIntExact().toString())
        }
    }

    @Test
    fun roundToIntHalfUpExact() {
        val values = arrayOf(
            "-81812663.000001", "-81812663",
            "-2314713.999999999999", "-2314714",
            "-200", "-200",
            "-10.9", "-11",
            "-4.51", "-5",
            "-4.5", "-4",
            "-4.49", "-4",
            "-0.51", "-1",
            "-0.5", "0",
            "-0.49", "0",
            "-0.0000000001", "0",
            "0", "0",
            "0.000000001", "0",
            "0.1", "0",
            "0.49", "0",
            "0.5", "1",
            "0.51", "1",
            "10.3", "10",
            "10.29381", "10",
            "318718239.1238714192", "318718239",
            "318718239.9238714192", "318718240"
        )
        for (i in values.indices step 2) {
            assertEquals(values[i + 1], values[i].toBigDecimal().roundToIntHalfUpExact().toString())
        }
    }

}
