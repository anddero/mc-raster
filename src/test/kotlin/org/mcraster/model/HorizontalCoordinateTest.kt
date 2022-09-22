package org.mcraster.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HorizontalCoordinateTest {

    @Test
    fun `test positive values are correctly calculated`() {
        // The below table of coordinate values are calculated based on the following assumptions
        assertEquals(2_147_483_647, Int.MAX_VALUE)
        assertEquals(32, BinaryRegion.REGION_LENGTH_CHUNKS)
        assertEquals(16, BinaryChunk.CHUNK_LENGTH_BLOCKS)

        val actualValues = arrayOf(
            arrayOf(0, 0, 0, 0, 0),
            arrayOf(1, 0, 0, 0, 1),
            arrayOf(15, 0, 0, 0, 15),
            arrayOf(16, 1, 0, 1, 0),
            arrayOf(31, 1, 0, 1, 15),
            arrayOf(32, 2, 0, 2, 0),
            arrayOf(496, 31, 0, 31, 0),
            arrayOf(511, 31, 0, 31, 15),
            arrayOf(512, 32, 1, 0, 0),
            arrayOf(1039, 64, 2, 0, 15),
            arrayOf(105_472, 6592, 206, 0, 0),
            arrayOf(2_020_194, 126_262, 3945, 22, 2),
            arrayOf(29_999_984, 1_874_999, 58_593, 23, 0),
            arrayOf(Int.MAX_VALUE, 134_217_727, 4_194_303, 31, 15)
        )

        actualValues.forEach { arr ->
            val coord = HorizontalCoordinate(arr[0])
            assertEquals(arr[1], coord.globalChunkValue)
            assertEquals(arr[2], coord.regionValue)
            assertEquals(arr[3], coord.localChunkValue)
            assertEquals(arr[4], coord.localValue)
            assertEquals(arr[1], arr[2] * BinaryRegion.REGION_LENGTH_CHUNKS + arr[3])
            assertEquals(arr[0], arr[1] * BinaryChunk.CHUNK_LENGTH_BLOCKS + arr[4])
        }
    }

    @Test
    fun `test negative values are correctly calculated`() {
        // The below table of coordinate values are calculated based on the following assumptions
        assertEquals(-2_147_483_648, Int.MIN_VALUE)
        assertEquals(32, BinaryRegion.REGION_LENGTH_CHUNKS)
        assertEquals(16, BinaryChunk.CHUNK_LENGTH_BLOCKS)

        val actualValues = arrayOf(
            arrayOf(-1, -1, -1, 31, 15),
            arrayOf(-15, -1, -1, 31, 1),
            arrayOf(-16, -1, -1, 31, 0),
            arrayOf(-17, -2, -1, 30, 15),
            arrayOf(-32, -2, -1, 30, 0),
            arrayOf(-33, -3, -1, 29, 15),
            arrayOf(-511, -32, -1, 0, 1),
            arrayOf(-512, -32, -1, 0, 0),
            arrayOf(-513, -33, -2, 31, 15),
            arrayOf(-921_443, -57_591, -1800, 9, 13),
            arrayOf(-29_999_984, -1_874_999, -58_594, 9, 0),
            arrayOf(Int.MIN_VALUE, -134_217_728, -4_194_304, 0, 0)
        )

        actualValues.forEach { arr ->
            val coord = HorizontalCoordinate(arr[0])
            assertEquals(arr[1], coord.globalChunkValue)
            assertEquals(arr[2], coord.regionValue)
            assertEquals(arr[3], coord.localChunkValue)
            assertEquals(arr[4], coord.localValue)
            assertEquals(arr[1], arr[2] * BinaryRegion.REGION_LENGTH_CHUNKS + arr[3])
            assertEquals(arr[0], arr[1] * BinaryChunk.CHUNK_LENGTH_BLOCKS + arr[4])
        }
    }

}
