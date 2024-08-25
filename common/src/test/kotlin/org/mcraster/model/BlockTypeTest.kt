package org.mcraster.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class BlockTypeTest {

    @Test
    fun `ensure all binary values are different and nothing is left out between min and max`() {
        val usedValues = mutableSetOf<Byte>()
        BlockType.entries.forEach { blockType ->
            assertFalse { usedValues.contains(blockType.value) }
            usedValues.add(blockType.value)
        }
        assertEquals(BlockType.entries.size, usedValues.size)
        assertEquals(BlockType.entries.size, BlockType.MAX_BINARY_VALUE - BlockType.MIN_BINARY_VALUE + 1)
        assertEquals(BlockType.MIN_BINARY_VALUE, 0)
        assertEquals(BlockType.MAX_BINARY_VALUE, 10) // take new highest values when it's updated
    }

}
