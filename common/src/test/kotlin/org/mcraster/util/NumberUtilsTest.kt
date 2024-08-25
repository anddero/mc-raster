package org.mcraster.util

import org.mcraster.util.NumberUtils.ceilToIntExact
import org.mcraster.util.NumberUtils.clip
import org.mcraster.util.NumberUtils.floorToIntExact
import org.mcraster.util.NumberUtils.roundToIntExact
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NumberUtilsTest { // TODO Test for throw cases

    @Test
    fun floorToIntExact() {
        val values = arrayOf(
            "-999292922", "-999292922",
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
            "318718239.9238714192", "318718239",
            "8358128413", "8358128413"
        )
        for (i in values.indices step 2) {
            assertEquals(values[i + 1], values[i].toBigDecimal().floorToIntExact().toString())
        }
    }

    @Test
    fun roundToIntExact() {
        val values = arrayOf(
            "-998231749", "-998231749",
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
            "318718239.9238714192", "318718240",
            "8728661843", "8728661843"
        )
        for (i in values.indices step 2) {
            assertEquals(values[i + 1], values[i].toBigDecimal().roundToIntExact().toString())
        }
    }

    @Test
    fun ceilToIntExact() {
        val values = arrayOf(
            "-999292922", "-999292922",
            "-81812663.000001", "-81812663",
            "-2314713.999999999999", "-2314713",
            "-200", "-200",
            "-10.9", "-10",
            "-4.51", "-4",
            "-4.5", "-4",
            "-4.49", "-4",
            "-0.51", "0",
            "-0.5", "0",
            "-0.49", "0",
            "-0.0000000001", "0",
            "0", "0",
            "0.000000001", "1",
            "0.1", "1",
            "0.49", "1",
            "0.5", "1",
            "0.51", "1",
            "0.999", "1",
            "10.3", "11",
            "10.29381", "11",
            "318718239.1238714192", "318718240",
            "318718239.9238714192", "318718240",
            "8358128413", "8358128413"
        )
        for (i in values.indices step 2) {
            assertEquals(values[i + 1], values[i].toBigDecimal().ceilToIntExact().toString())
        }
    }

    @Test
    fun clip() {
        // value, min, max, result
        val values = arrayOf(
            // simple cases - keep original
            0, -1, 1, 0,
            3, 1, 7, 3,
            -7, -9, -6, -7,
            // edge cases - keep original
            0, 0, 1, 0,
            7, 7, 9, 7,
            4, 3, 5, 4,
            -1, -1, 0, -1,
            -5, -6, -4, -5,
            // take min
            0, 1, 2, 1,
            4, 235, 500, 235,
            -99, -3, 0, -3,
            // take max-1
            0, -5, 0, -1,
            7, -4, 4, 3,
            -9, -15, -14, -15
        )
        for (i in values.indices step 4) {
            assertEquals(values[i + 3], values[i].clip(values[i + 1], values[i + 2]))
        }
    }

}
