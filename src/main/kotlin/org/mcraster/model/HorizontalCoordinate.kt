package org.mcraster.model

import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.Limits.REGION_LENGTH_CHUNKS

class HorizontalCoordinate(val globalBlock: Int) {

    constructor(region: Int, localChunk: Int, localBlock: Int)
            : this(globalBlock = (region * REGION_LENGTH_CHUNKS + localChunk) * CHUNK_LENGTH_BLOCKS + localBlock)

    val globalChunk by lazy {
        if (globalBlock >= 0) globalBlock / CHUNK_LENGTH_BLOCKS
        else (globalBlock + 1) / CHUNK_LENGTH_BLOCKS - 1
    }
    val region by lazy {
        if (globalChunk >= 0) globalChunk / REGION_LENGTH_CHUNKS
        else (globalChunk + 1) / REGION_LENGTH_CHUNKS - 1
    }
    val localChunk by lazy { globalChunk - region * REGION_LENGTH_CHUNKS }
    val localBlock by lazy { globalBlock - globalChunk * CHUNK_LENGTH_BLOCKS }

}
