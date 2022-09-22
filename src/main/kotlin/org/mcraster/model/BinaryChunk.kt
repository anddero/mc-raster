package org.mcraster.model

import org.mcraster.model.BlockType.NONE
import org.mcraster.util.ByteVec3
import org.mcraster.util.ByteVec3.Companion.read
import org.mcraster.util.ByteVec3.Companion.write
import java.io.InputStream
import java.io.OutputStream

class BinaryChunk {
    private val blocksXzy = ByteVec3(CHUNK_LENGTH_BLOCKS, CHUNK_LENGTH_BLOCKS, CHUNK_HEIGHT_BLOCKS, NONE.binaryValue)

    operator fun get(localX: Int, localZ: Int, y: Int) = BlockType[blocksXzy[localX, localZ, y]]
    operator fun set(localX: Int, localZ: Int, y: Int, value: BlockType) {
        blocksXzy[localX, localZ, y] = value.binaryValue
    }

    companion object {
        const val CHUNK_LENGTH_BLOCKS = 16
        private const val CHUNK_HEIGHT_BLOCKS = 256
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
}
