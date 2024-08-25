package org.mcraster.pos

object Limits {

    const val MODEL_HEIGHT_BLOCKS = 256

    const val CHUNK_LENGTH_BLOCKS = 16
    const val CHUNK_SIZE_BLOCKS = CHUNK_LENGTH_BLOCKS * CHUNK_LENGTH_BLOCKS * MODEL_HEIGHT_BLOCKS

    const val REGION_LENGTH_CHUNKS = 32
    const val REGION_SIZE_CHUNKS = REGION_LENGTH_CHUNKS * REGION_LENGTH_CHUNKS
    const val REGION_LENGTH_BLOCKS = REGION_LENGTH_CHUNKS * CHUNK_LENGTH_BLOCKS
    const val REGION_SIZE_BLOCKS = REGION_SIZE_CHUNKS * CHUNK_SIZE_BLOCKS

    // Circumference of Earth is 40'075'017m around equator (X) and 40'007'863m (Z) around poles.
    // Below are the min and max block coordinates (both ends inclusive) if we centered ourselves such that the range
    // of positive and negative coordinates would be equal.
    val MIN_BLOCK_POS = BlockPos(
        x = -20_037_508, // This is the smallest West-facing side coordinate of any block and thus the Western model border.
        y = 0, // This is the lowest bottom side of any block and thus the bottom model border. Using only non-negative integers for heights.
        z = -20_003_931 // This is the smallest North-facing side coordinate of any block and thus the Northern model border.
    )
    val MAX_BLOCK_POS = BlockPos(
        x = 20_037_508, // This is the largest West-facing side coordinate of any block. The largest East-facing side (and thus the Eastern model border) would be one unit higher.
        y = MODEL_HEIGHT_BLOCKS - 1, // This is the highest bottom side of any block, the highest top side would be one unit higher, which would be the highest position where a person can stand.
        z = 20_003_931 // This is the largest North-facing side coordinate of any block. The South-facing side (and thus the Southern model border) would be one unit higher.
    )

    const val DISK_BLOCK_SIZE_BYTES = 1 // valid only if the block type is a Byte
    const val DISK_CHUNK_SIZE_BYTES = CHUNK_SIZE_BLOCKS * DISK_BLOCK_SIZE_BYTES
    const val DISK_REGION_SIZE_BYTES = REGION_SIZE_CHUNKS * DISK_CHUNK_SIZE_BYTES

    const val DISK_REGION_SIZE_MB_APPROX = DISK_REGION_SIZE_BYTES / 1024 / 1024
    const val DEFAULT_MAX_CACHE_SIZE_MB = 1024

    fun BlockPos.isWithinLimits() = x in MIN_BLOCK_POS.x..MAX_BLOCK_POS.x &&
            y in MIN_BLOCK_POS.y..MAX_BLOCK_POS.y &&
            z in MIN_BLOCK_POS.z..MAX_BLOCK_POS.z

}
