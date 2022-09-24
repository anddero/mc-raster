package org.mcraster.model

import org.mcraster.model.Block.RegionLocalBlock
import org.mcraster.model.Limits.MODEL_HEIGHT_BLOCKS
import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.Limits.CHUNK_SIZE_BLOCKS
import org.mcraster.model.Limits.DISK_REGION_SIZE_BYTES
import org.mcraster.model.Limits.REGION_LENGTH_BLOCKS
import org.mcraster.model.Limits.REGION_LENGTH_CHUNKS
import org.mcraster.model.Limits.REGION_SIZE_BLOCKS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RegionTest {

    @Test
    fun `initial values are set correctly`() {
        val region = Region()
        region.forEach {
            assertEquals(BlockType.NONE, it.type)
        }
    }

    @Test
    fun `getLastAccessTime shows accurate access time`() {
        val region = Region()

        var timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 0..100)

        Thread.sleep(1000)
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 1000 .. 1100)

        region.setBlock(BlockPos(x = 123, z = 234, y = 1), BlockType.WATER)
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 0 .. 100)

        Thread.sleep(2000)
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 2000..2200)

        region.getBlock(BlockPos(x = 12, z = 34, y = 4))
        timePassedMs = region.lastAccessTime.until(Instant.now(), ChronoUnit.MILLIS)
        assertTrue(timePassedMs in 0 .. 100)
    }

    @Test
    fun `isChanged returns true only if it was changed`() {
        val region = Region()
        assertFalse(region.isChangedAfterCreateLoadOrSave)

        region.getBlock(BlockPos(x = 12, z = 34, y = 4))
        assertFalse(region.isChangedAfterCreateLoadOrSave)

        region.setBlock(BlockPos(x = 132, z = 3214, y = 4), BlockType.NONE)
        assertFalse(region.isChangedAfterCreateLoadOrSave)

        region.setBlock(BlockPos(x = 123, z = 234, y = 1), BlockType.WATER)
        assertTrue(region.isChangedAfterCreateLoadOrSave)
    }

    @Test
    fun `get and set work on the same element`() {
        val region = Region()
        region.setBlock(
            BlockPos(x = 0, z = 0, y = 1),
            BlockType.SOIL_WITH_GRASS
        )
        assertEquals(BlockType.SOIL_WITH_GRASS,
            region.getBlock(BlockPos(x = 0, z = 0, y = 1))
        )
        region.setBlock(
            BlockPos(x = 231, z = 49382, y = 52),
            BlockType.GRAVEL
        )
        assertEquals(BlockType.GRAVEL,
            region.getBlock(BlockPos(x = 231, z = 49382, y = 52))
        )
    }

    @Test
    fun `reading and writing to stream works`() {
        // Expecting one region file to be exactly 64 MB. If block/chunk/region sizes change, update this test.
        assertEquals(64 * 1024 * 1024, DISK_REGION_SIZE_BYTES)

        val savedRegion = Region()
        val loadedRegion = Region()

        for (x in 0 until REGION_LENGTH_BLOCKS) {
            for (z in 0 until REGION_LENGTH_BLOCKS) {
                for (y in 0 until MODEL_HEIGHT_BLOCKS) {
                    savedRegion.setBlock(
                        pos = BlockPos(x = x, z = z, y = y),
                        value = Random.nextBlock()
                    )
                }
            }
        }

        with(ByteArrayOutputStream(DISK_REGION_SIZE_BYTES)) {
            savedRegion.write(this)
            loadedRegion.read(ByteArrayInputStream(toByteArray()))
        }

        val savedRegionIterator = savedRegion.iterator()
        val loadedRegionIterator = loadedRegion.iterator()

        repeat(REGION_SIZE_BLOCKS) {
            assertEquals(savedRegionIterator.next(), loadedRegionIterator.next())
        }
    }

    @Test
    fun `iterator iterates chunk-by-chunk returning all chunks of the same Z first before increasing X`() {
        val region = Region()

        // create a list of random blocks to use for validation (only create as many values as needed)
        val totalBlocksToCheck = CHUNK_SIZE_BLOCKS * 4 // generating for a 2x2 region only
        val randomBlockValues = (0 until totalBlocksToCheck)
            .map { Random.nextBlock().value }
            .toByteArray()

        // apply random blocks to a chosen area of chunks (2 x 2), keeping everything around them empty
        with(randomBlockValues.iterator()) {
            for (chunkX in 1 ..2) {
                for (chunkZ in 2..3) {
                    for (localX in 0 until CHUNK_LENGTH_BLOCKS) {
                        for (localZ in 0 until CHUNK_LENGTH_BLOCKS) {
                            for (y in 0 until MODEL_HEIGHT_BLOCKS) {
                                val x = HorPos(0, chunkX, localX)
                                val z = HorPos(0, chunkZ, localZ)
                                region.setBlock(
                                    pos = BlockPos(x = x.block, z = z.block, y = y),
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
        for (chunkZ in 0 until REGION_LENGTH_CHUNKS) { // all chunks of chunkX = 0 should be empty
            assertEmptyChunk(regionIterator)
        }
        for (chunkZ in 0 .. 1) { // if chunkX = 1, then chunkZ {0, 1} should be empty
            assertEmptyChunk(regionIterator)
        }
        for (chunkZ in 2 .. 3) { // if chunkX = 1, then chunkZ {2, 3} are the first two chunks with set values
            assertSetBytes(expectedValuesIterator, regionIterator)
        }
        for (chunkZ in 4 until REGION_LENGTH_CHUNKS) { // all the rest of the chunks of chunkX = 1
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

    private fun assertSetBytes(expectedValuesIterator: ByteIterator, regionIterator: Iterator<RegionLocalBlock>) {
        repeat(CHUNK_SIZE_BLOCKS) {
            assertEquals(expectedValuesIterator.next(), regionIterator.next().type.value)
        }
    }

    private fun assertEmptyChunk(regionIterator: Iterator<RegionLocalBlock>) {
        repeat(CHUNK_SIZE_BLOCKS) { assertEquals(BlockType.NONE, regionIterator.next().type) }
    }

    private fun Random.nextBlock() =
        BlockType[nextInt(BlockType.MIN_BINARY_VALUE.toInt(), BlockType.MAX_BINARY_VALUE + 1).toByte()]

}
