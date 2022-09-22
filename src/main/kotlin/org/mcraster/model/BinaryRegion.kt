package org.mcraster.model

import org.mcraster.model.BinaryChunk.Companion.BLOCKS_IN_CHUNK
import org.mcraster.model.BinaryChunk.Companion.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.BinaryChunk.Companion.read
import org.mcraster.model.BinaryChunk.Companion.write
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

class BinaryRegion : Iterable<Block> {
    private val chunksXz = List(REGION_LENGTH_CHUNKS) { List(REGION_LENGTH_CHUNKS) { BinaryChunk() } }
    var lastAccessTime = Instant.now()
        private set
    var isChangedAfterCreateLoadOrSave = false
        private set

    operator fun get(x: HorizontalCoordinate, z: HorizontalCoordinate, y: Int): BlockType {
        lastAccessTime = Instant.now()
        return chunksXz[x.localChunk][z.localChunk]
            .get(localX = x.localBlock, localZ = z.localBlock, y = y)
    }
    operator fun set(x: HorizontalCoordinate, z: HorizontalCoordinate, y: Int, value: BlockType) {
        lastAccessTime = Instant.now()
        val changed = chunksXz[x.localChunk][z.localChunk]
            .set(localX = x.localBlock, localZ = z.localBlock, y = y, value = value)
        if (changed) this.isChangedAfterCreateLoadOrSave = true
    }
    override fun iterator(): Iterator<Block> = BinaryRegionIterator(this)

    companion object {

        const val REGION_LENGTH_CHUNKS = 32
        const val REGION_LENGTH_BLOCKS = REGION_LENGTH_CHUNKS * CHUNK_LENGTH_BLOCKS
        const val CHUNKS_IN_REGION = REGION_LENGTH_CHUNKS * REGION_LENGTH_CHUNKS
        const val BLOCKS_IN_REGION = CHUNKS_IN_REGION * BLOCKS_IN_CHUNK

        fun OutputStream.write(binaryRegion: BinaryRegion) {
            binaryRegion.chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> write(chunk) } }
            binaryRegion.isChangedAfterCreateLoadOrSave = false
        }

        fun InputStream.read(binaryRegion: BinaryRegion) {
            binaryRegion.chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> read(chunk) } }
            binaryRegion.isChangedAfterCreateLoadOrSave = false
        }

    }

    private class BinaryRegionIterator(region: BinaryRegion) : Iterator<Block> {
        private var localChunkX = 0
        private var localChunkZ = 0
        private var chunkIterator = region.chunksXz[localChunkX][localChunkZ].iterator()

        override fun hasNext() = chunkIterator.hasNext() || localChunkX < REGION_LENGTH_CHUNKS

        override fun next(): Block {
            val chunkLocalBlock = chunkIterator.next()
            val regionLocalBlock = chunkLocalBlock.copy(
                x = localChunkX * CHUNK_LENGTH_BLOCKS + chunkLocalBlock.x,
                z = localChunkZ * CHUNK_LENGTH_BLOCKS + chunkLocalBlock.z
            )
            if (++localChunkZ >= REGION_LENGTH_CHUNKS) {
                localChunkZ = 0
                ++localChunkX
            }
            return regionLocalBlock
        }

    }

}
