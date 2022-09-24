package org.mcraster.model

import org.mcraster.model.BlockType.NONE
import org.mcraster.model.Limits.MODEL_HEIGHT_BLOCKS
import org.mcraster.model.Limits.CHUNK_LENGTH_BLOCKS
import org.mcraster.util.ByteVec3
import java.io.InputStream
import java.io.OutputStream

/**
 * Represents a continuous block of memory to hold a square area portion of model data.
 */
class Chunk : Iterable<Block> {

    private val blocksXzy = ByteVec3(
        dim1 = CHUNK_LENGTH_BLOCKS,
        dim2 = CHUNK_LENGTH_BLOCKS,
        dim3 = MODEL_HEIGHT_BLOCKS,
        initialValue = NONE.value
    )

    operator fun get(localPos: BlockPos) =
        BlockType[blocksXzy.get(i1 = localPos.x, i2 = localPos.z, i3 = localPos.y)]

    operator fun set(localPos: BlockPos, value: BlockType) =
        blocksXzy.set(i1 = localPos.x, i2 = localPos.z, i3 = localPos.y, value = value.value)

    override fun iterator(): Iterator<Block> = BinaryChunkIterator(this)

    fun read(inputStream: InputStream) = blocksXzy.read(inputStream)

    fun write(outputStream: OutputStream) = blocksXzy.write(outputStream)

    private class BinaryChunkIterator(chunk: Chunk) : Iterator<Block> {
        private val localPos = BlockPos.MutableBlockPos(0, 0, 0)
        private var arrayIterator = chunk.blocksXzy.iterator()

        override fun hasNext() = arrayIterator.hasNext()

        override fun next(): Block {
            val block = Block(
                pos = BlockPos(x = localPos.x, z = localPos.z, y = localPos.y),
                type = BlockType[arrayIterator.next()]
            )
            if (++localPos.y >= MODEL_HEIGHT_BLOCKS) {
                localPos.y = 0
                if (++localPos.z >= CHUNK_LENGTH_BLOCKS) {
                    localPos.z = 0
                    ++localPos.x
                }
            }
            return block
        }

    }

}
