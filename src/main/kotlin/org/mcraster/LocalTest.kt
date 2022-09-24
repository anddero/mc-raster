package org.mcraster

import org.mcraster.builder.DiskBoundModelBuilder
import org.mcraster.generator.J2BlocksWorldGenerator
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockType
import org.mcraster.reader.DataSourceDescriptor
import org.mcraster.util.MinecraftConstants.MC_SEA_BLOCK_LEVEL
import org.mcraster.world.WorldConfig
import java.io.File

object LocalTest {

    fun generateCustomArea1() {
        val seaLevelBlockBottomY = MC_SEA_BLOCK_LEVEL
        val spawnPosDataSource = DataSourceDescriptor(
            path = "customArea1/spawn.xyz",
            type = DataSourceDescriptor.DataSourceType.RESOURCE_FILE,
            format = DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = DataSourceDescriptor.PointConversionStrategy.BOUNDING_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asDataSource()
        val heightMapDataSource = DataSourceDescriptor(
            path = "customArea1/heightmap.xyz",
            type = DataSourceDescriptor.DataSourceType.RESOURCE_FILE,
            format = DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = DataSourceDescriptor.PointConversionStrategy.BOUNDING_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true,
            blockFilter = loadHeightMapFilter()
        ).asDataSource()
        val markerPolesDataSource = DataSourceDescriptor(
            path = "customArea1/poles.xyz",
            type = DataSourceDescriptor.DataSourceType.RESOURCE_FILE,
            format = DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = DataSourceDescriptor.PointConversionStrategy.BOUNDING_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asDataSource()
        val waterPoolDataSource = DataSourceDescriptor(
            path = "customArea1/pools.xyz",
            type = DataSourceDescriptor.DataSourceType.RESOURCE_FILE,
            format = DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = DataSourceDescriptor.PointConversionStrategy.BOUNDING_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asDataSource()

        val worldConfig = WorldConfig(
            worldName = "CustomArea1",
            generator = WorldConfig.GeneratorType.FLAT,
            layers = listOf(
                WorldConfig.Layer(BlockType.UNBREAKABLE_STONE, 0, 0),
                WorldConfig.Layer(BlockType.STONE, 1, 4),
                WorldConfig.Layer(BlockType.SOIL, 5, 7),
                WorldConfig.Layer(BlockType.WATER, 8, seaLevelBlockBottomY)
            ),
            spawnPos = spawnPosDataSource.first(),
            isGeneratingStructuresEnabled = false,
            gameType = WorldConfig.GameType.CREATIVE
        )

        val diskBoundModel = DiskBoundModelBuilder.build(
            directory = File("McRasterCustomArea1"),
            heightMap = heightMapDataSource,
            markerPoleCoordinates = markerPolesDataSource,
            waterPoolCentroids = waterPoolDataSource
        )
        J2BlocksWorldGenerator.generateToDisk(worldConfig, diskBoundModel)
    }

    private fun loadHeightMapFilter(): (BlockPos) -> Boolean {
        val (minBlock, maxBlock) = DataSourceDescriptor(
            path = "customArea1/minMaxFilter.xyz",
            type = DataSourceDescriptor.DataSourceType.RESOURCE_FILE,
            format = DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = DataSourceDescriptor.PointConversionStrategy.BOUNDING_BLOCK,
            seaLevelBlockBottomY = MC_SEA_BLOCK_LEVEL,
            softValidateBlockLimits = true
        ).asDataSource().firstTwo()
        val blockPosFilter: (BlockPos) -> Boolean = {
            it.x in minBlock.x..maxBlock.x &&
                    it.y in minBlock.y..maxBlock.y &&
                    it.z in minBlock.z..maxBlock.z
        }
        return blockPosFilter
    }

}
