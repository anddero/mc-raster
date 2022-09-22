package org.mcraster.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class BlockTypeTest {

    @Test
    fun `ensure all binary values are different`() {
        val usedValues = mutableSetOf<Byte>()
        BlockType.values().forEach { blockType ->
            assertFalse { usedValues.contains(blockType.binaryValue) }
            usedValues.add(blockType.binaryValue)
        }
        assertEquals(BlockType.values().size, usedValues.size)
    }

}
