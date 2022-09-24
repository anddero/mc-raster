package org.mcraster.model

import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.Limits.REGION_LENGTH_CHUNKS
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

/**
 * Represents a continuous block of disk space to hold a horizontally square area portion of model data.
 * The data underneath will not be held in consecutive memory when loaded.
 */
class Region : Iterable<Block> {

    private val chunksXz = List(REGION_LENGTH_CHUNKS) { List(REGION_LENGTH_CHUNKS) { Chunk() } }

    var lastAccessTime = Instant.now()
        private set

    var isChangedAfterCreateLoadOrSave = false
        private set

    operator fun get(x: HorizontalCoordinate, z: HorizontalCoordinate, y: Int): BlockType {
        lastAccessTime = Instant.now()
        return chunksXz[x.localChunk][z.localChunk][BlockPos(x = x.localBlock, z = z.localBlock, y = y)]
    }

    operator fun set(x: HorizontalCoordinate, z: HorizontalCoordinate, y: Int, value: BlockType) {
        lastAccessTime = Instant.now()
        val changed = chunksXz[x.localChunk][z.localChunk]
            .set(localPos = BlockPos(x = x.localBlock, z = z.localBlock, y = y), value = value)
        if (changed) this.isChangedAfterCreateLoadOrSave = true
    }

    override fun iterator(): Iterator<Block> = BinaryRegionIterator(this)

    fun write(outputStream: OutputStream) {
        chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> chunk.write(outputStream) } }
        isChangedAfterCreateLoadOrSave = false
    }

    fun read(inputStream: InputStream) {
        chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> chunk.read(inputStream) } }
        isChangedAfterCreateLoadOrSave = false
    }

    private class BinaryRegionIterator(region: Region) : Iterator<Block> {
        private var localChunkX = 0
        private var localChunkZ = 0
        private var chunkIterator = region.chunksXz[localChunkX][localChunkZ].iterator()

        override fun hasNext() = chunkIterator.hasNext() || localChunkX < REGION_LENGTH_CHUNKS

        override fun next(): Block {
            val chunkLocalBlock = chunkIterator.next()
            val regionLocalBlock = Block(
                BlockPos(
                    x = localChunkX * CHUNK_LENGTH_BLOCKS + chunkLocalBlock.pos.x,
                    y = chunkLocalBlock.pos.y,
                    z = localChunkZ * CHUNK_LENGTH_BLOCKS + chunkLocalBlock.pos.z
                ),
                type = chunkLocalBlock.type
            )
            if (++localChunkZ >= REGION_LENGTH_CHUNKS) {
                localChunkZ = 0
                ++localChunkX
            }
            return regionLocalBlock
        }

    }

}
