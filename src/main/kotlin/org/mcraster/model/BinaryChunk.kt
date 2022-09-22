package org.mcraster.model

import org.mcraster.model.BlockType.NONE
import org.mcraster.util.ByteVec3
import org.mcraster.util.DimensionalByteArray.Companion.read
import org.mcraster.util.DimensionalByteArray.Companion.write
import java.io.InputStream
import java.io.OutputStream

class BinaryChunk : Iterable<Block> {
    private val blocksXzy = ByteVec3(CHUNK_LENGTH_BLOCKS, CHUNK_LENGTH_BLOCKS, CHUNK_HEIGHT_BLOCKS, NONE.binaryValue)

    operator fun get(localX: Int, localZ: Int, y: Int) = BlockType[blocksXzy[localX, localZ, y]]
    operator fun set(localX: Int, localZ: Int, y: Int, value: BlockType) =
        blocksXzy.set(a = localX, b = localZ, c = y, value = value.binaryValue)

    override fun iterator(): Iterator<Block> = BinaryChunkIterator(this)

    companion object {
        const val CHUNK_LENGTH_BLOCKS = 16
        const val CHUNK_HEIGHT_BLOCKS = 256
        const val BLOCKS_IN_CHUNK = CHUNK_LENGTH_BLOCKS * CHUNK_LENGTH_BLOCKS * CHUNK_HEIGHT_BLOCKS
        const val BLOCK_SIZE_BYTES = 1

        fun OutputStream.write(binaryChunk: BinaryChunk) = this.write(binaryChunk.blocksXzy)
        fun InputStream.read(binaryChunk: BinaryChunk) {
            val n = this.read(binaryChunk.blocksXzy)
            if (n != binaryChunk.blocksXzy.size()) {
                throw RuntimeException("Failed to read ${javaClass.simpleName} from stream")
            }
        }
    }

    private class BinaryChunkIterator(chunk: BinaryChunk) : Iterator<Block> {
        private var localX = 0
        private var localZ = 0
        private var localY = 0
        private var arrayIterator = chunk.blocksXzy.iterator()

        override fun hasNext() = arrayIterator.hasNext()

        override fun next(): Block {
            val block = Block(x = localX, z = localZ, y = localY, type = BlockType[arrayIterator.next()])
            if (++localY >= CHUNK_HEIGHT_BLOCKS) {
                localY = 0
                if (++localZ >= CHUNK_LENGTH_BLOCKS) {
                    localZ = 0
                    ++localX
                }
            }
            return block
        }

    }

}
