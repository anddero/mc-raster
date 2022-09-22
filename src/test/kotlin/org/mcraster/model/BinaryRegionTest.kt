package org.mcraster.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BinaryRegionTest {

    @Test
    fun `initial values are set correctly`() {
        val region = BinaryRegion()
        region.forEach {
            assertEquals(BlockType.NONE, it.type)
        }
    }

    @Test
    fun `getLastAccessTime shows accurate access time`() {
        val region = BinaryRegion()

        var timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 1..100)

        Thread.sleep(1000)
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 1000 .. 1100)

        region[HorizontalCoordinate(123), HorizontalCoordinate(234), 1] = BlockType.WATER
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 1 .. 100)

        Thread.sleep(2000)
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 2000..2200)

        region[HorizontalCoordinate(12), HorizontalCoordinate(34), 4]
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 1 .. 100)
    }

    @Test
    fun `isChanged returns true only if it was changed`() {
        val region = BinaryRegion()
        assertFalse(region.isChanged)

        region[HorizontalCoordinate(12), HorizontalCoordinate(34), 4]
        assertFalse(region.isChanged)

        region[HorizontalCoordinate(132), HorizontalCoordinate(3214), 4] = BlockType.NONE
        assertFalse(region.isChanged)

        region[HorizontalCoordinate(123), HorizontalCoordinate(234), 1] = BlockType.WATER
        assertTrue(region.isChanged)
    }

    @Test
    fun `get and set work on the same element`() {
        val region = BinaryRegion()
        region[hCo(0), hCo(0), 1] = BlockType.GRASS
        assertEquals(BlockType.GRASS, region[hCo(0), hCo(0), 1])
        region[hCo(231), hCo(49382), 52] = BlockType.GRAVEL
        assertEquals(BlockType.GRAVEL, region[hCo(231), hCo(49382), 52])
    }

    @Test
    fun `iterator iterates chunk-by-chunk returning all chunks of the same Z first before increasing X`() {
        val region = BinaryRegion()

        // create a list of random blocks to use for validation (only create as many values as needed)
        val totalBlocksToCheck = BinaryChunk.BLOCKS_IN_CHUNK * 4 // generating for a 2x2 region only
        val randomBlockValues = (0 until totalBlocksToCheck)
            .map { Random.nextInt(BlockType.MIN_BINARY_VALUE.toInt(), BlockType.MAX_BINARY_VALUE + 1) }
            .map { it.toByte() }
            .toByteArray()

        // apply random blocks to a chosen area of chunks (2 x 2), keeping everything around them empty
        with(randomBlockValues.iterator()) {
            for (chunkX in 1 ..2) {
                for (chunkZ in 2..3) {
                    for (localX in 0 until BinaryChunk.CHUNK_LENGTH_BLOCKS) {
                        for (localZ in 0 until BinaryChunk.CHUNK_LENGTH_BLOCKS) {
                            for (y in 0 until BinaryChunk.CHUNK_HEIGHT_BLOCKS) {
                                region.set(
                                    x = HorizontalCoordinate(0, chunkX, localX),
                                    z = HorizontalCoordinate(0, chunkZ, localZ),
                                    y = y,
                                    value = BlockType[next()]
                                )
                            }
                        }
                    }
                }
            }
            assertFalse(hasNext())
        }

        // Knowing that the chunks (1,2) - (1,3) - (2,2) - (2,3) are set with predefined random values,
        // check the values returned by the region iterator.
        val expectedValuesIterator = randomBlockValues.iterator()
        val regionIterator = region.iterator()
        for (chunkZ in 0 until BinaryRegion.REGION_LENGTH_CHUNKS) { // all chunks of chunkX = 0 should be empty
            assertEmptyChunk(regionIterator)
        }
        for (chunkZ in 0 .. 1) { // if chunkX = 1, then chunkZ {0, 1} should be empty
            assertEmptyChunk(regionIterator)
        }
        for (chunkZ in 2 .. 3) { // if chunkX = 1, then chunkZ {2, 3} are the first two chunks with set values
            assertSetBytes(expectedValuesIterator, regionIterator)
        }
        for (chunkZ in 4 until BinaryRegion.REGION_LENGTH_CHUNKS) { // all the rest of the chunks of chunkX = 1
            assertEmptyChunk(regionIterator)
        }
        for (chunkZ in 0 .. 1) { // if chunkX = 2, then chunkZ {0, 1} should be empty
            assertEmptyChunk(regionIterator)
        }
        for (chunkZ in 2 .. 3) { // if chunkX = 2, then chunkZ {2, 3} should have set values
            assertSetBytes(expectedValuesIterator, regionIterator)
        }
        assertEmptyChunk(regionIterator) // assert one more empty chunk after the last one with set values
        assertFalse(expectedValuesIterator.hasNext())
    }

    private fun assertSetBytes(expectedValuesIterator: ByteIterator, regionIterator: Iterator<Block>) {
        repeat(BinaryChunk.BLOCKS_IN_CHUNK) {
            assertEquals(expectedValuesIterator.next(), regionIterator.next().type.binaryValue)
        }
    }

    private fun assertEmptyChunk(regionIterator: Iterator<Block>) {
        repeat(BinaryChunk.BLOCKS_IN_CHUNK) { assertEquals(BlockType.NONE, regionIterator.next().type) }
    }

    private fun hCo(i: Int) = HorizontalCoordinate(i)

}