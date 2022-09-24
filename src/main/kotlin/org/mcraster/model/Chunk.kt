package org.mcraster.model

import org.mcraster.model.Block.ChunkLocalBlock
import org.mcraster.model.BlockPos.ChunkLocalBlockPos
import org.mcraster.model.BlockType.NONE
import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.model.Limits.MODEL_HEIGHT_BLOCKS
import org.mcraster.util.ByteVec3
import java.io.InputStream
import java.io.OutputStream

/**
 * Represents a continuous block of memory to hold a square area portion of model data.
 */
class Chunk : Iterable<ChunkLocalBlock> {

    private val blocksXzy = ByteVec3(
        dim1 = CHUNK_LENGTH_BLOCKS,
        dim2 = CHUNK_LENGTH_BLOCKS,
        dim3 = MODEL_HEIGHT_BLOCKS,
        initialValue = NONE.value
    )

    fun getBlock(pos: BlockPos) =
        BlockType[blocksXzy.get(i1 = pos.localBlockX, i2 = pos.localBlockZ, i3 = pos.y)]

    fun setBlock(pos: BlockPos, value: BlockType) =
        blocksXzy.set(i1 = pos.localBlockX, i2 = pos.localBlockZ, i3 = pos.y, value = value.value)

    override fun iterator(): Iterator<ChunkLocalBlock> = BinaryChunkIterator(this)

    fun read(inputStream: InputStream) = blocksXzy.read(inputStream)

    fun write(outputStream: OutputStream) = blocksXzy.write(outputStream)

    private class BinaryChunkIterator(chunk: Chunk) : Iterator<ChunkLocalBlock> {
        private var localX = 0
        private var localZ = 0
        private var y = 0
        private var arrayIterator = chunk.blocksXzy.iterator()

        override fun hasNext() = arrayIterator.hasNext()

        override fun next(): ChunkLocalBlock {
            val block = ChunkLocalBlock(
                pos = ChunkLocalBlockPos(x = localX, z = localZ, y = y),
                type = BlockType[arrayIterator.next()]
            )
            if (++y >= MODEL_HEIGHT_BLOCKS) {
                y = 0
                if (++localZ >= CHUNK_LENGTH_BLOCKS) {
                    localZ = 0
                    ++localX
                }
            }
            return block
        }

    }

}
