package org.mcraster.model

import org.mcraster.model.Block.ChunkLocalBlock
import org.mcraster.model.Block.RegionLocalBlock
import org.mcraster.model.Limits.REGION_LENGTH_CHUNKS
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

/**
 * Represents a continuous block of disk space to hold a horizontally square area portion of model data.
 * The data underneath will not be held in consecutive memory when loaded.
 */
class Region : Iterable<RegionLocalBlock> {

    private val chunksXz = List(REGION_LENGTH_CHUNKS) { List(REGION_LENGTH_CHUNKS) { Chunk() } }

    var lastAccessTime: Instant = Instant.now()
        private set

    var isChangedAfterCreateLoadOrSave = false
        private set

    fun getBlock(pos: BlockPos): BlockType {
        lastAccessTime = Instant.now()
        return getChunk(pos).getBlock(pos)
    }

    fun setBlock(pos: BlockPos, value: BlockType) {
        lastAccessTime = Instant.now()
        val changed = getChunk(pos).setBlock(pos = pos, value = value)
        if (changed) this.isChangedAfterCreateLoadOrSave = true
    }

    override fun iterator(): Iterator<RegionLocalBlock> = BinaryRegionIterator(this)

    fun write(outputStream: OutputStream) {
        chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> chunk.write(outputStream) } }
        isChangedAfterCreateLoadOrSave = false
    }

    fun read(inputStream: InputStream) {
        chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> chunk.read(inputStream) } }
        isChangedAfterCreateLoadOrSave = false
    }

    private fun getChunk(pos: BlockPos) = chunksXz[pos.localChunkX][pos.localChunkZ]

    private class BinaryRegionIterator(private val region: Region) : Iterator<RegionLocalBlock> {
        private var localChunkX = 0
        private var localChunkZ = 0
        private var chunkIterator = region.chunksXz[localChunkX][localChunkZ].iterator()

        override fun hasNext() = chunkIterator.hasNext()

        override fun next(): RegionLocalBlock {
            val chunkLocalBlock = chunkIterator.next()
            val regionLocalBlock = chunkLocalBlock.toRegionLocalBlock(
                localChunkX = localChunkX,
                localChunkZ = localChunkZ
            )
            if (!chunkIterator.hasNext()) {
                if (++localChunkZ >= REGION_LENGTH_CHUNKS) {
                    localChunkZ = 0
                    ++localChunkX
                }
                chunkIterator = if (localChunkX < REGION_LENGTH_CHUNKS) {
                    region.chunksXz[localChunkX][localChunkZ].iterator()
                } else {
                    emptySequence<ChunkLocalBlock>().iterator()
                }
            }
            return regionLocalBlock
        }

    }

}
