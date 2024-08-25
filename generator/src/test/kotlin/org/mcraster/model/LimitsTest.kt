package org.mcraster.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class LimitsTest {

    @Test
    fun `validate model size in memory and disk`() {
        // Make sure that if the block type changes, we don't forget to update an important constant.
        assertEquals("byte", BlockType.NONE.value.javaClass.simpleName)
        assertEquals(Byte.SIZE_BYTES, Limits.DISK_BLOCK_SIZE_BYTES)

        // Make sure the blocks of continuous allocated memory are limited to 1 MB to avoid allocation errors.
        assertTrue(Limits.DISK_CHUNK_SIZE_BYTES < 1024 * 1024)

        // Make sure the individual saved files on disk are limited to 500 MB to ensure cache efficiency.
        assertTrue(Limits.DISK_REGION_SIZE_BYTES < 500 * 1024 * 1024)
    }

}
