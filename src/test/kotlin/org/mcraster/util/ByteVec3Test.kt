package org.mcraster.util

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ByteVec3Test {

    @Test
    fun `all values initialized correctly`() {
        val vec = ByteVec3(3, 10, 5, 23)
        vec.forEach {
            assertEquals(23, it)
        }
    }

    @Test
    fun `get and set work with same element if given same index`() {
        val vec = ByteVec3(5, 7, 3, 0)
        vec[3, 5, 2] = 4
        assertEquals(4, vec[3, 5, 2])
        vec[0, 1, 0] = 107
        assertEquals(107, vec[0, 1, 0])
    }

    @Test
    fun `size return product of dimensions`() {
        val vec = ByteVec3(12, 22, 3, 0)
        assertEquals(12 * 22 * 3, vec.size())
    }

    @Test
    fun `iterator iterates through elements by latest dimensions first`() {
        val vec = ByteVec3(8, 3, 5, 0)
        val byteRange = (0 until Byte.MAX_VALUE).map { it.toByte() }

        // fill in by latest dimensions first
        val byteIterator = byteRange.iterator()
        for (a in 0 until 8) {
            for (b in 0 until 3) {
                for (c in 0 until 5) {
                    vec[a, b, c] = byteIterator.next()
                }
            }
        }

        // check that iterator returns values in ascending order
        vec.zip(byteRange).forEach {
            assertEquals(it.second, it.first)
        }
    }

}
