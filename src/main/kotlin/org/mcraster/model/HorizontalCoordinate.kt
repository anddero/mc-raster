package org.mcraster.model

import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.Limits.REGION_LENGTH_CHUNKS

class HorizontalCoordinate(val global: Int) {

    constructor(region: Int, localChunk: Int, localBlock: Int)
            : this(global = (region * REGION_LENGTH_CHUNKS + localChunk) * CHUNK_LENGTH_BLOCKS + localBlock)

    val globalChunk by lazy {
        if (global >= 0) global / CHUNK_LENGTH_BLOCKS
        else (global + 1) / CHUNK_LENGTH_BLOCKS - 1
    }
    val region by lazy {
        if (globalChunk >= 0) globalChunk / REGION_LENGTH_CHUNKS
        else (globalChunk + 1) / REGION_LENGTH_CHUNKS - 1
    }
    val localChunk by lazy { globalChunk - region * REGION_LENGTH_CHUNKS }
    val localBlock by lazy { global - globalChunk * CHUNK_LENGTH_BLOCKS }

}
