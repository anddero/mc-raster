package org.mcraster.model

class HorizontalCoordinate(val globalBlock: Int) {

    constructor(region: Int, localChunk: Int, localBlock: Int)
            : this(globalBlock = (region * BinaryRegion.REGION_LENGTH_CHUNKS + localChunk) * BinaryChunk.CHUNK_LENGTH_BLOCKS + localBlock)

    val globalChunk by lazy {
        if (globalBlock >= 0) globalBlock / BinaryChunk.CHUNK_LENGTH_BLOCKS
        else (globalBlock + 1) / BinaryChunk.CHUNK_LENGTH_BLOCKS - 1
    }
    val region by lazy {
        if (globalChunk >= 0) globalChunk / BinaryRegion.REGION_LENGTH_CHUNKS
        else (globalChunk + 1) / BinaryRegion.REGION_LENGTH_CHUNKS - 1
    }
    val localChunk by lazy { globalChunk - region * BinaryRegion.REGION_LENGTH_CHUNKS }
    val localBlock by lazy { globalBlock - globalChunk * BinaryChunk.CHUNK_LENGTH_BLOCKS }
}
