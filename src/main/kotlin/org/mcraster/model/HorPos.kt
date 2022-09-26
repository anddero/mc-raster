package org.mcraster.model

import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.Limits.REGION_LENGTH_CHUNKS

/**
 * Represents the horizontal position (x or z) globally and its chunk and region indices to which the position belongs.
 */
data class HorPos(val block: Int) {

    constructor(region: Int, localChunk: Int, localBlock: Int)
            : this(block = (region * REGION_LENGTH_CHUNKS + localChunk) * CHUNK_LENGTH_BLOCKS + localBlock)

    val globalChunk: Int
        get() {
            return if (block >= 0) block / CHUNK_LENGTH_BLOCKS
            else (block + 1) / CHUNK_LENGTH_BLOCKS - 1
        }

    val region: Int
        get() {
            return if (globalChunk >= 0) globalChunk / REGION_LENGTH_CHUNKS
            else (globalChunk + 1) / REGION_LENGTH_CHUNKS - 1
        }

    val localChunk get() = globalChunk - region * REGION_LENGTH_CHUNKS

    val localBlock get() = block - globalChunk * CHUNK_LENGTH_BLOCKS

}
