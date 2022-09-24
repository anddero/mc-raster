package org.mcraster.model

import org.mcraster.model.BlockPos.ChunkLocalBlockPos
import org.mcraster.model.BlockPos.RegionLocalBlockPos

data class Block(val pos: BlockPos, val type: BlockType) {

    data class ChunkLocalBlock(val pos: ChunkLocalBlockPos, val type: BlockType) {

        fun toRegionLocalBlock(localChunkX: Int, localChunkZ: Int) = RegionLocalBlock(
            pos = this.pos.toRegionLocalBlockPos(localChunkX = localChunkX, localChunkZ = localChunkZ),
            type = this.type
        )

    }

    data class RegionLocalBlock(val pos: RegionLocalBlockPos, val type: BlockType) {

        fun toBlock(regionX: Int, regionZ: Int) = Block(
            pos = this.pos.toBlockPos(regionX = regionX, regionZ = regionZ),
            type = this.type
        )

    }

}
