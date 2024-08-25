package org.mcraster.model

import org.mcraster.util.NumberUtils.ceilToIntExact
import org.mcraster.util.NumberUtils.floorToIntExact
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

    fun plus(dx: Int, dy: Int, dz: Int) = BlockPos(x = x + dx, y = y + dy, z = z + dz)

    fun toHorBlockPos() = HorBlockPos(x = x, z = z)

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

    data class HorBlockPos(val x: Int, val z: Int) {

        val regionX get() = getRegionIndex(globalBlockIndex = x)
        val regionZ get() = getRegionIndex(globalBlockIndex = z)

        val localChunkX get() = getLocalChunkIndex(globalBlockIndex = x)
        val localChunkZ get() = getLocalChunkIndex(globalBlockIndex = z)

        val localBlockX get() = getLocalBlockIndex(globalBlockIndex = x)
        val localBlockZ get() = getLocalBlockIndex(globalBlockIndex = z)

        /**
         * The corner with the smallest x and z of this block.
         */
        fun getMin() = HorPoint(x = x.toBigDecimal(), z = z.toBigDecimal())

        /**
         * The corner with the highest x and z of this block.
         */
        fun getMax() = HorPoint(x = (x + 1).toBigDecimal(), z = (z + 1).toBigDecimal())

    }

    /**
     * Min inclusive, max exclusive.
     */
    data class HorBlockPosRect(val min: HorBlockPos, val max: HorBlockPos) {

        init {
            if (min.x >= max.x || min.z >= max.z) throw RuntimeException("$this has no size")
        }

        fun contains(p: HorBlockPos) = p.x >= min.x && p.x < max.x &&
                                       p.z >= min.z && p.z < max.z

        fun toHorPointRect() = HorPointRect(min = min.getMin(), max = max.getMin())

    }

    /**
     * Min inclusive, max exclusive.
     */
    data class BlockPosCube(val min: BlockPos, val max: BlockPos) {

        init {
            if (min.x >= max.x || min.z >= max.z || min.y >= max.y) throw RuntimeException("$this has no size")
        }

        fun contains(p: BlockPos) = p.x >= min.x && p.x < max.x &&
                                    p.y >= min.y && p.y < max.y &&
                                    p.z >= min.z && p.z < max.z

        fun toHorBlockPosRect() = HorBlockPosRect(min = min.toHorBlockPos(), max = max.toHorBlockPos())

    }

    data class Point(val x: BigDecimal, val y: BigDecimal, val z: BigDecimal)

    data class HorPoint(val x: BigDecimal, val z: BigDecimal)

    data class HorPointRect(val min: HorPoint, val max: HorPoint) {

        init {
            if (min.x >= max.x || min.z >= max.z) throw RuntimeException("$this has no size")
        }

        fun contains(point: HorPoint) = point.x >= min.x && point.x < max.x && point.z >= min.z && point.z < max.z

    }

    data class PointCube(val min: Point, val max: Point) {

        init {
            if (min.x >= max.x || min.z >= max.z || min.y >= max.y) throw RuntimeException("$this has no size")
        }

        /**
         * Get the biggest BlockPosCube that could be represented by the bounds of this PointCube.
         */
        fun toInnerBlockPosCube(): BlockPosCube {
            val newMin = BlockPos(
                x = min.x.ceilToIntExact(),
                y = min.y.ceilToIntExact(),
                z = min.z.ceilToIntExact()
            )
            val newMax = BlockPos(
                x = max.x.floorToIntExact(),
                y = max.y.floorToIntExact(),
                z = max.z.floorToIntExact()
            )
            return BlockPosCube(min = newMin, max = newMax)
        }

    }

}
