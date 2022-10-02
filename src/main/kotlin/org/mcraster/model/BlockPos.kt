package org.mcraster.model

import java.math.BigDecimal

/**
 * In our model representation, these coordinates should be interpreted as follows:
 *      X increases towards East and decreases towards West.
 *      Y increases towards the sky and decreases towards the ground.
 *      Z increases towards South and decreases towards North.
 *
 * 1 unit corresponds to 1 meter in all the dimensions.
 *
 * If used for one cubic meter blocks, the values represent the lowest points of the block in all dimensions.
 * This means, the position represents the lower North-West corner of the block.
 * For example, if the position of the block is (x = 5, y = 20, z = 10), then
 *      The West-facing side is at x = 5. The East-facing side is at x = 6. The center is x = 5.5.
 *      The ground-facing side is at y = 20. The sky-facing side is at y = 21. The center is y = 20.5.
 *      The North-facing side is at z = 10. The South-facing side is at z = 11. The center is z = 10.5.
 */
data class BlockPos(val x: Int, val y: Int, val z: Int) {

    val regionX get() = getRegionIndex(globalBlockIndex = x)
    val regionZ get() = getRegionIndex(globalBlockIndex = z)

    val localChunkX get() = getLocalChunkIndex(globalBlockIndex = x)
    val localChunkZ get() = getLocalChunkIndex(globalBlockIndex = z)

    val localBlockX get() = getLocalBlockIndex(globalBlockIndex = x)
    val localBlockZ get() = getLocalBlockIndex(globalBlockIndex = z)

    companion object {

        fun getGlobalBlockIndex(regionIndex: Int, localChunkIndex: Int, localBlockIndex: Int) =
            (regionIndex * Limits.REGION_LENGTH_CHUNKS + localChunkIndex) * Limits.CHUNK_LENGTH_BLOCKS +
                    localBlockIndex

        fun getGlobalChunkIndex(globalBlockIndex: Int): Int {
            return if (globalBlockIndex >= 0) globalBlockIndex / Limits.CHUNK_LENGTH_BLOCKS
            else (globalBlockIndex + 1) / Limits.CHUNK_LENGTH_BLOCKS - 1
        }

        fun getRegionIndex(globalBlockIndex: Int): Int {
            val globalChunkIndex = getGlobalChunkIndex(globalBlockIndex = globalBlockIndex)
            return if (globalChunkIndex >= 0) globalChunkIndex / Limits.REGION_LENGTH_CHUNKS
            else (globalChunkIndex + 1) / Limits.REGION_LENGTH_CHUNKS - 1
        }

        fun getLocalChunkIndex(globalBlockIndex: Int) = getGlobalChunkIndex(globalBlockIndex = globalBlockIndex) -
                getRegionIndex(globalBlockIndex = globalBlockIndex) * Limits.REGION_LENGTH_CHUNKS

        fun getLocalBlockIndex(globalBlockIndex: Int) = globalBlockIndex -
                getGlobalChunkIndex(globalBlockIndex = globalBlockIndex) * Limits.CHUNK_LENGTH_BLOCKS

    }

    data class ChunkLocalBlockPos(val x: Int, val y: Int, val z: Int) {

        fun toRegionLocalBlockPos(localChunkX: Int, localChunkZ: Int) = RegionLocalBlockPos(
            x = localChunkX * Limits.CHUNK_LENGTH_BLOCKS + this.x,
            y = this.y,
            z = localChunkZ * Limits.CHUNK_LENGTH_BLOCKS + this.z
        )

    }

    data class RegionLocalBlockPos(val x: Int, val y: Int, val z: Int) {

        fun toBlockPos(regionX: Int, regionZ: Int) = BlockPos(
            x = regionX * Limits.REGION_LENGTH_BLOCKS + this.x,
            y = this.y,
            z = regionZ * Limits.REGION_LENGTH_BLOCKS + this.z
        )

    }

    data class HorPoint(val x: BigDecimal, val z: BigDecimal)

    data class HorRect(val min: HorPoint, val max: HorPoint) {

        fun contains(point: HorPoint) = point.x >= min.x && point.x <= max.x && point.z >= min.z && point.z <= max.z

    }

}
