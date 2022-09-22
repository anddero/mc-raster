package org.mcraster.model

class HorizontalCoordinate(val globalValue: Int) {
    val globalChunkValue by lazy {
        if (globalValue >= 0) globalValue / BinaryChunk.CHUNK_LENGTH_BLOCKS
        else (globalValue + 1) / BinaryChunk.CHUNK_LENGTH_BLOCKS - 1
    }
    val regionValue by lazy {
        if (globalChunkValue >= 0) globalChunkValue / BinaryRegion.REGION_LENGTH_CHUNKS
        else (globalChunkValue + 1) / BinaryRegion.REGION_LENGTH_CHUNKS - 1
    }
    val localChunkValue by lazy { globalChunkValue - regionValue * BinaryRegion.REGION_LENGTH_CHUNKS }
    val localValue by lazy { globalValue - globalChunkValue * BinaryChunk.CHUNK_LENGTH_BLOCKS }
}
