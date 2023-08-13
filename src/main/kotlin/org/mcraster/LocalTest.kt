package org.mcraster

import org.mcraster.builder.DiskBoundModelBuilder
import org.mcraster.generator.J2BlocksWorldGenerator
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockPos.Cube
import org.mcraster.model.BlockType
import org.mcraster.model.DiskBoundModel
import org.mcraster.reader.DataSourceDescriptor
import org.mcraster.reader.DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE
import org.mcraster.reader.DataSourceDescriptor.DataFormat.OBJ_3D
import org.mcraster.reader.DataSourceDescriptor.DataFormat.POLYGON_SHP_LEST97_YX
import org.mcraster.reader.DataSourceDescriptor.DataSourceType.RELATIVE_FILE
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy.BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy.VERTICALLY_FIXED_AT_SEA_LEVEL
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

        val spawnPos = DataSourceDescriptor(
            path = "$inputFilesDir/spawn.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asLazyData().use { it.first() }

        val worldLimits = DataSourceDescriptor(
            path = "$inputFilesDir/minMaxFilter.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = MC_SEA_BLOCK_LEVEL,
            softValidateBlockLimits = false
        ).asLazyData().firstTwo().let { (limit1, limit2) ->
            val xMin = min(limit1.x, limit2.x)
            val yMin = min(limit1.y, limit2.y)
            val zMin = min(limit1.z, limit2.z)
            val xMax = max(limit1.x, limit2.x)
            val yMax = max(limit1.y, limit2.y)
            val zMax = max(limit1.z, limit2.z)
            Cube(min = BlockPos(x = xMin, y = yMin, z = zMin), max = BlockPos(x = xMax, y = yMax, z = zMax))
        }

        val heightMapDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/heightmap.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true,
            worldLimits = worldLimits
        ).asLazyData()

        val waterBodyDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/waterbody.shp",
            type = RELATIVE_FILE,
            format = POLYGON_SHP_LEST97_YX,
            pointConversionStrategy = VERTICALLY_FIXED_AT_SEA_LEVEL,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true,
            worldLimits = worldLimits
        ).asLazyData()

        val stoneObj3dDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/obj3d.dump",
            type = RELATIVE_FILE,
            format = OBJ_3D,
            softValidateBlockLimits = true,
            blockTransform = { blocks ->
                blocks.map { block -> block.plus(dx = spawnPos.x + 15, dy = spawnPos.y + 15, dz = spawnPos.z + 15) }
            }
        ).asLazyData()

        val markerPolesDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/poles.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asLazyData()
            .transform { it.filter { false } }

        val waterPoolDataSource = DataSourceDescriptor(
            path = "$inputFilesDir/pools.xyz",
            type = RELATIVE_FILE,
            format = LINES_LEST97_YXH_DOUBLE,
            pointConversionStrategy = BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
            seaLevelBlockBottomY = seaLevelBlockBottomY,
            softValidateBlockLimits = true
        ).asLazyData()
            .transform { it.filter { false } }

        val worldConfig = WorldConfig(
            worldName = modelAndWorldName,
            generator = WorldConfig.GeneratorType.FLAT,
            layers = listOf(
                WorldConfig.Layer(BlockType.UNBREAKABLE_STONE, 0, 0),
                WorldConfig.Layer(BlockType.STONE, 1, 4),
                WorldConfig.Layer(BlockType.SOIL, 5, 7),
                WorldConfig.Layer(BlockType.WATER, 8, seaLevelBlockBottomY)
            ),
            spawnPos = spawnPos,
            isGeneratingStructuresEnabled = false,
            gameType = WorldConfig.GameType.CREATIVE
        )

        val model = DiskBoundModel(File("$outputModelsDir/$modelAndWorldName"), true)
        model.maxCacheSizeMB = 1024
        val diskBoundModel = DiskBoundModelBuilder.build(
            model = model,
            heightMap = heightMapDataSource,
            waterBodies = waterBodyDataSource,
            stoneObj3d = stoneObj3dDataSource,
            markerPoleCoordinates = markerPolesDataSource,
            waterPoolCentroids = waterPoolDataSource
        )
        diskBoundModel.maxCacheSizeMB = 256
        J2BlocksWorldGenerator.generateToDisk(worldConfig, diskBoundModel)
    }

}
