package org.mcraster.builder

import org.mcraster.converters.Polygon
import org.mcraster.generator.J2BlocksWorldConfig
import org.mcraster.generator.J2BlocksWorldGenerator
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockPos.HorBlockPos
import org.mcraster.model.BlockType
import org.mcraster.model.DiskBoundModel
import org.mcraster.reader.DataSourceReader.lazyRead3dObjects
import org.mcraster.reader.DataSourceReader.lazyReadBuildings
import org.mcraster.reader.DataSourceReader.lazyReadRoads
import org.mcraster.reader.DataSourceReader.lazyReadTerrain
import org.mcraster.reader.DataSourceReader.lazyReadWater
import org.mcraster.reader.DataSourceReader.readBounds
import org.mcraster.reader.DataSourceReader.readOrigin
import org.mcraster.util.LazyData
import org.mcraster.util.NumberUtils.clip
import org.mcraster.util.OptionalUtils.orThrow
import java.io.File
import kotlin.math.max

object WorldBuilder {

    fun buildWithJ2Blocks(buildConfig: BuildConfig) {
        fun getFile(relativeFileName: String) = File("${buildConfig.relativeInputDir}/$relativeFileName")

        // Gather data sources
        val dataSourceByType = buildConfig.dataSources.groupBy { it.dataType }
        val originSource = dataSourceByType[DataType.ORIGIN].orThrow("Expected origin").single()
        val boundsSource = dataSourceByType[DataType.BOUNDS].orThrow("Expected bounds").single()
        val terrainSources = dataSourceByType[DataType.TERRAIN].orThrow("Expected terrain")
        val roadSources = dataSourceByType[DataType.ROAD].orEmpty()
        val waterSources = dataSourceByType[DataType.WATER].orEmpty()
        val buildingSources = dataSourceByType[DataType.BUILDING].orEmpty()
        val model3dSources = dataSourceByType[DataType.MODEL_3D].orEmpty()

        // Read origin and bounds
        println("Reading origin and bounds...")
        var origin = readOrigin(getFile(originSource.relativeFileName), originSource.dataFormat)
            .getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
            .toBlockPos(buildConfig.seaLevelBlockBottomY)
        val bounds = readBounds(getFile(boundsSource.relativeFileName), boundsSource.dataFormat)
            .toPointCube(seaLevelBlockBottomY = buildConfig.seaLevelBlockBottomY)
            .toInnerBlockPosCube()

        println("Origin: $origin")
        println("Bounds: $bounds")

        // Check origin against bounds
        println("Checking origin against bounds...")
        if (!bounds.contains(origin)) throw RuntimeException("Origin $origin out of bounds $bounds")

        // Chain the converters of input data
        println("Chaining converters...")
        val lazyTerrains = terrainSources.map { terrainSource ->
            lazyReadTerrain(getFile(terrainSource.relativeFileName), terrainSource.dataFormat)
                .transform { pointLEst97Sequence ->
                    pointLEst97Sequence.map {
                        it.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
                            .toBlockPos(buildConfig.seaLevelBlockBottomY)
                    }.filter {
                        // Filter only horizontally, to not create empty spots where height is too low/high
                        bounds.toHorBlockPosRect().contains(it.toHorBlockPos())
                    }.map {
                        // If height is too low or high, just clip it TODO warn
                        BlockPos(
                            x = it.x,
                            y = it.y.clip(lowIncl = bounds.min.y, highExcl = bounds.max.y),
                            z = it.z
                        )
                    }
                }
        }
        val lazyRoads = roadSources.map { roadSource ->
            lazyReadRoads(getFile(roadSource.relativeFileName), roadSource.dataFormat)
                .rasterize(bounds = bounds)
        }
        val lazyWater = waterSources.map { waterSource ->
            lazyReadWater(getFile(waterSource.relativeFileName), waterSource.dataFormat)
                .rasterize(bounds = bounds)
        }
        val lazyBuildings = buildingSources.map { buildingSource ->
            lazyReadBuildings(getFile(buildingSource.relativeFileName), buildingSource.dataFormat)
                .rasterize(bounds = bounds)
        }
        val lazy3dModels = model3dSources.map { model3dSource ->
            lazyRead3dObjects(getFile(model3dSource.relativeFileName), model3dSource.dataFormat)
                .transform { blocks ->
                    blocks
                        .map { BlockPos(x = origin.x + it.x, y = origin.y + it.y, z = origin.z + it.z) }
                        .filter { bounds.contains(it) }
                }
        }

        // Generate and write model of world
        println("Creating model...")
        val model = DiskBoundModel(
            directory = File("${buildConfig.relativeOutputDir}/${buildConfig.modelAndWorldName}"),
            overwrite = buildConfig.removeOldModelIfPresent
        )
        model.maxCacheSizeMB = buildConfig.builderMaxCacheSizeMbWhileWriting
        println("Building all terrains...")
        lazyTerrains.forEach { model.buildTerrain(heightMap = it, minY = bounds.min.y) }
        println("Building all roads...")
        lazyRoads.forEach { model.buildRoads(it) } // TODO Currently only replacing top block
        println("Building all water bodies...")
        lazyWater.forEach { model.buildWaterBodies(it) } // TODO Currently only replacing top block
        println("Building all buildings...")
        lazyBuildings.forEach { model.buildBuildings(it, 10) } // TODO Currently only adding on top of the below block, also the same building's height may vary, also ignoring upper building bound
        println("Building all 3D objects...")
        lazy3dModels.forEach { model.buildObj3d(it, BlockType.STONE) } // TODO Hardcoded STONE
        println("Flushing model...")
        model.flush()

        // TODO Carry out some validations on the model like is it possible to spawn at origin, are there any coordinates that are empty, etc
        println("Checking spawn...")
        val idealSpawnHeight = model.getHighestBlockY(pos = origin.toHorBlockPos())?.plus(1)
        if (idealSpawnHeight == null) {
            System.err.println("Nothing found at spawn coordinate")
        } else {
            val spawnHeightOffset = origin.y - idealSpawnHeight
            if (spawnHeightOffset < 0 || spawnHeightOffset > 5) {
                origin = origin.copy(y = idealSpawnHeight)
                println("Spawn height adjusted to ${origin.y} due to offset of $spawnHeightOffset")
            }
        }

        // Read model, write Minecraft world
        model.maxCacheSizeMB = buildConfig.builderMaxCacheSizeMbWhileReading
        println("Generating with J2Blocks...")
        J2BlocksWorldGenerator.generateToDisk(
            J2BlocksWorldConfig(
                buildConfig.modelAndWorldName,
                buildConfig.generator,
                buildConfig.layers,
                buildConfig.isGeneratingStructuresEnabled,
                buildConfig.gameType,
                origin
            ),
            model
        )

        println("Finished building everything!")
    }

    private fun LazyData<Polygon.MultiPolygon>.rasterize(bounds: BlockPos.BlockPosCube) = transform { multiPolygons ->
        val area = bounds.toHorBlockPosRect()
        multiPolygons
            .flatMap { multiPolygon -> multiPolygon.polygons.asSequence() }
            .flatMap { polygon ->
                polygon.createRasterMasks(area = area, canvasSize = 200, cropFirst = true)
            }.flatMap { rasterMask -> rasterMask.asSequence() }
    }

    private fun DiskBoundModel.buildTerrain(heightMap: LazyData<BlockPos>, minY: Int) = heightMap.use { topBlocks ->
        println("Building terrain...")
        val minBuildableBlock = max(minY, 0)
        topBlocks.forEach { topPos ->
            setBlock(BlockPos(x = topPos.x, y = minBuildableBlock, z = topPos.z), BlockType.UNBREAKABLE_STONE)
            for (y in minBuildableBlock + 1..topPos.y) {
                setBlock(BlockPos(x = topPos.x, y = y, z = topPos.z), BlockType.SOIL)
            }
        }
    }

    private fun DiskBoundModel.buildWaterBodies(waterBodies: LazyData<HorBlockPos>) = waterBodies.use { horBlocks ->
        println("Building water bodies...")
        horBlocks.forEach { horBlock ->
            val highestBlockY = getHighestBlockY(pos = horBlock)
            if (highestBlockY == null) {
                System.err.println("Nothing found at coordinate $horBlock, skipping water")
                return@forEach
            }
            val blockPos = BlockPos(x = horBlock.x, y = highestBlockY, z = horBlock.z)
            val prevBlock = getBlock(blockPos)
            if (prevBlock != BlockType.SOIL) {
                System.err.println("Expected SOIL at $blockPos but found $prevBlock, replacing it with water anyway")
            }
            setBlock(blockPos, BlockType.WATER)
        }
    }

    // TODO Duplicate of buildWaterBodies
    private fun DiskBoundModel.buildRoads(roads: LazyData<HorBlockPos>) = roads.use { horBlocks ->
        println("Building roads...")
        horBlocks.forEach { horBlock ->
            val highestBlockY = getHighestBlockY(pos = horBlock)
            if (highestBlockY == null) {
                System.err.println("Nothing found at coordinate $horBlock, skipping road")
                return@forEach
            }
            val blockPos = BlockPos(x = horBlock.x, y = highestBlockY, z = horBlock.z)
            val prevBlock = getBlock(blockPos)
            if (prevBlock != BlockType.SOIL) {
                System.err.println("Expected SOIL at $blockPos but found $prevBlock, replacing it with road anyway")
            }
            setBlock(blockPos, BlockType.STONE)
        }
    }

    // TODO Duplicate of buildWaterBodies
    private fun DiskBoundModel.buildBuildings(buildings: LazyData<HorBlockPos>, height: Int) = buildings.use { horBlocks ->
        println("Building buildings...")
        horBlocks.forEach { horBlock ->
            val highestBlockY = getHighestBlockY(pos = horBlock)
            if (highestBlockY == null) {
                System.err.println("Nothing found at coordinate $horBlock, skipping building")
                return@forEach
            }
            val blockPos = BlockPos(x = horBlock.x, y = highestBlockY, z = horBlock.z)
            val prevBlock = getBlock(blockPos)
            if (prevBlock != BlockType.SOIL && prevBlock != BlockType.STONE) {
                System.err.println("Expected SOIL/STONE at $blockPos but found $prevBlock, adding a building on top anyway")
            }
            for (y in blockPos.y + 1 .. blockPos.y + height) {
                setBlock(BlockPos(x = blockPos.x, y = y, z = blockPos.z), BlockType.STONE)
            }
        }
    }

    private fun DiskBoundModel.buildObj3d(obj3d: LazyData<BlockPos>, blockType: BlockType) = obj3d.use { blocks ->
        println("Building obj3d...")
        blocks.forEach { pos -> setBlock(pos, blockType) }
    }


}


