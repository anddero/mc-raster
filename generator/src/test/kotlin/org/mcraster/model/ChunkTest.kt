package org.mcraster.model

import org.mcraster.model.Limits.MODEL_HEIGHT_BLOCKS
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChunkTest {

    @Test
    fun `all values initialized correctly`() {
        val chunk = Chunk()
        chunk.forEach {
            assertEquals(BlockType.NONE, it.type)
        }
    }

    @Test
    fun `get and set work on the same element`() {
        val chunk = Chunk()
        chunk.setBlock(BlockPos(0, 0, 0), BlockType.SOIL_WITH_GRASS)
        assertEquals(BlockType.SOIL_WITH_GRASS, chunk.getBlock(BlockPos(0, 0, 0)))
        chunk.setBlock(BlockPos(x = 3, y = 4, z = 5), BlockType.GRAVEL)
        assertEquals(BlockType.GRAVEL, chunk.getBlock(BlockPos(x = 3, y = 4, z = 5)))
    }

    @Test
    fun `iterator iterates through elements Y first, then Z, then X`() {
        val chunkIterator = Chunk().iterator()
        for (i in 0 until MODEL_HEIGHT_BLOCKS) {
            val block = chunkIterator.next()
            assertEquals(0, block.pos.x)
            assertEquals(0, block.pos.z)
            assertEquals(i, block.pos.y)
        }
        var block = chunkIterator.next()
        assertEquals(0, block.pos.x)
        assertEquals(1, block.pos.z)
        assertEquals(0, block.pos.y)

        block = chunkIterator.next()
        assertEquals(0, block.pos.x)
        assertEquals(1, block.pos.z)
        assertEquals(1, block.pos.y)
    }

}
