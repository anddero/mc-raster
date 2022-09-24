package org.mcraster.model

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

    private val posX get() = HorPos(x)
    private val posZ get() = HorPos(z)

    val regionX get() = posX.region
    val regionZ get() = posZ.region

    val localChunkX get() = posX.localChunk
    val localChunkZ get() = posZ.localChunk

    val localBlockX get() = posX.localBlock
    val localBlockZ get() = posZ.localBlock

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

}
