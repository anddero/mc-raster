package org.mcraster.model

import org.mcraster.model.BinaryChunk.Companion.CHUNK_HEIGHT_BLOCKS
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BinaryChunkTest {

    @Test
    fun `all values initialized correctly`() {
        val chunk = BinaryChunk()
        chunk.forEach {
            assertEquals(BlockType.NONE, it.type)
        }
    }

    @Test
    fun `get and set work on the same element`() {
        val chunk = BinaryChunk()
        chunk[0, 0, 0] = BlockType.GRASS
        assertEquals(BlockType.GRASS, chunk[0, 0, 0])
        chunk[3, 4, 5] = BlockType.GRAVEL
        assertEquals(BlockType.GRAVEL, chunk[3, 4, 5])
    }

    @Test
    fun `iterator iterates through elements Y first, then Z, then X`() {
        val chunkIterator = BinaryChunk().iterator()
        for (i in 0 until CHUNK_HEIGHT_BLOCKS) {
            val block = chunkIterator.next()
            assertEquals(0, block.x)
            assertEquals(0, block.z)
            assertEquals(i, block.y)
        }
        var block = chunkIterator.next()
        assertEquals(0, block.x)
        assertEquals(1, block.z)
        assertEquals(0, block.y)

        block = chunkIterator.next()
        assertEquals(0, block.x)
        assertEquals(1, block.z)
        assertEquals(1, block.y)
    }

}
