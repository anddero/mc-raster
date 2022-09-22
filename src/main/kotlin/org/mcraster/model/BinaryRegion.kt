package org.mcraster.model

import org.mcraster.model.BinaryChunk.Companion.read
import org.mcraster.model.BinaryChunk.Companion.write
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.Instant

class BinaryRegion {
    private val chunksXz = List(REGION_LENGTH_CHUNKS) { List(REGION_LENGTH_CHUNKS) { BinaryChunk() } }
    var lastAccessTime = Instant.now()
        private set

    operator fun get(x: HorizontalCoordinate, z: HorizontalCoordinate, y: Int): BlockType {
        lastAccessTime = Instant.now()
        return chunksXz[x.localChunkValue][z.localChunkValue]
            .get(localX = x.localValue, localZ = z.localValue, y = y)
    }
    operator fun set(x: HorizontalCoordinate, z: HorizontalCoordinate, y: Int, value: BlockType) {
        lastAccessTime = Instant.now()
        chunksXz[x.localChunkValue][z.localChunkValue]
            .set(localX = x.localValue, localZ = z.localValue, y = y, value = value)
    }

    companion object {

        const val REGION_LENGTH_CHUNKS = 32
        const val CHUNKS_IN_REGION = REGION_LENGTH_CHUNKS * REGION_LENGTH_CHUNKS

        /**
         * Writes a new region file or overwrites the existing one.
         */
        fun File.write(binaryRegion: BinaryRegion) {
            FileOutputStream(this, false).use { stream ->
                binaryRegion.chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> stream.write(chunk) } }
            }
        }

        fun File.read(binaryRegion: BinaryRegion) {
            FileInputStream(this).use { stream ->
                binaryRegion.chunksXz.forEach { regionLineX -> regionLineX.forEach { chunk -> stream.read(chunk) } }
            }
        }

    }
}
