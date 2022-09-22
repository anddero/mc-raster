package org.mcraster.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class BlockTypeTest {

    @Test
    fun `ensure all binary values are different and nothing is left out between min and max`() {
        val usedValues = mutableSetOf<Byte>()
        BlockType.values().forEach { blockType ->
            assertFalse { usedValues.contains(blockType.binaryValue) }
            usedValues.add(blockType.binaryValue)
        }
        assertEquals(BlockType.values().size, usedValues.size)
        assertEquals(BlockType.values().size, BlockType.MAX_BINARY_VALUE - BlockType.MIN_BINARY_VALUE + 1)
    }

}
