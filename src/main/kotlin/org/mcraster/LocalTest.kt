package org.mcraster

import org.mcraster.builder.DiskBoundModelBuilder
import org.mcraster.generator.J2BlocksWorldGenerator
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockType
import org.mcraster.model.DiskBoundModel
import org.mcraster.reader.DataSourceDescriptor
import org.mcraster.reader.DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE
import org.mcraster.reader.DataSourceDescriptor.DataSourceType.RELATIVE_FILE
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy.BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK
import org.mcraster.util.MinecraftConstants.MC_SEA_BLOCK_LEVEL
import org.mcraster.world.WorldConfig
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min

object LocalTest {

    fun generateCustomArea1() {
        val seaLevelBlockBottomY = MC_SEA_BLOCK_LEVEL
        val inputFilesDir = "input-resources/customArea1"
        val outputModelsDir = "output-models"
        val modelAndWorldName = "CustomArea1-Debug"

        val spawnPosDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/debug-spawn.txt",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asDataSource()
        val heightMapDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/debug-heightmap.txt",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true,
//            blockFilter = loadHeightMapFilter("$inputFilesDir/minMaxFilter.xyz")
        ).asDataSource()
        val markerPolesDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/poles.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asDataSource().filter { false }
        val waterPoolDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/pools.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asDataSource().filter { false }

        val worldConfig = WorldConfig(
            worldName = modelAndWorldName,
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

        val model = DiskBoundModel(File("$outputModelsDir/$modelAndWorldName"), true)
        model.maxCacheSizeMB = 5120
        val diskBoundModel = DiskBoundModelBuilder.build(
            model = model,
            heightMap = heightMapDataSource,
            markerPoleCoordinates = markerPolesDataSource,
            waterPoolCentroids = waterPoolDataSource
        )
        diskBoundModel.maxCacheSizeMB = 256
        J2BlocksWorldGenerator.generateToDisk(worldConfig, diskBoundModel)
    }

    private fun loadHeightMapFilter(filterFilePath: String): (BlockPos) -> Boolean {
        val (limit1, limit2) = DataSourceDescriptor(
            path = filterFilePath,
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = MC_SEA_BLOCK_LEVEL,
            softValidateBlockLimits = false
        ).asDataSource().firstTwo()
        val xMin = min(limit1.x, limit2.x)
        val yMin = min(limit1.y, limit2.y)
        val zMin = min(limit1.z, limit2.z)
        val xMax = max(limit1.x, limit2.x)
        val yMax = max(limit1.y, limit2.y)
        val zMax = max(limit1.z, limit2.z)
        val blockPosFilter: (BlockPos) -> Boolean = {
            it.x in xMin..xMax && it.y in yMin..yMax && it.z in zMin..zMax
        }
        return blockPosFilter
    }

}
