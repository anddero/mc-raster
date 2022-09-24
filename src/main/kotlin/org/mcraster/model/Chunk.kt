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

    operator fun get(localPoint: BlockPos) =
        BlockType[blocksXzy.get(i1 = localPoint.x, i2 = localPoint.z, i3 = localPoint.y)]

    operator fun set(localPoint: BlockPos, value: BlockType) =
        blocksXzy.set(i1 = localPoint.x, i2 = localPoint.z, i3 = localPoint.y, value = value.value)

    override fun iterator(): Iterator<Block> = BinaryChunkIterator(this)

    fun read(inputStream: InputStream) = blocksXzy.read(inputStream)

    fun write(outputStream: OutputStream) = blocksXzy.write(outputStream)

    private class BinaryChunkIterator(chunk: Chunk) : Iterator<Block> {
        private val localPoint = BlockPos.MutableBlockPos(0, 0, 0)
        private var arrayIterator = chunk.blocksXzy.iterator()

        override fun hasNext() = arrayIterator.hasNext()

        override fun next(): Block {
            val block = Block(
                point = BlockPos(x = localPoint.x, z = localPoint.z, y = localPoint.y),
                type = BlockType[arrayIterator.next()]
            )
            if (++localPoint.y >= MODEL_HEIGHT_BLOCKS) {
                localPoint.y = 0
                if (++localPoint.z >= CHUNK_LENGTH_BLOCKS) {
                    localPoint.z = 0
                    ++localPoint.x
                }
            }
            return block
        }

    }

}
