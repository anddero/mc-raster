package org.mcraster.builder

import org.mcraster.model.DiskBoundModel
import org.mcraster.model.BlockType
import org.mcraster.model.BlockPos
import org.mcraster.util.DataSource
import java.io.File

object DiskBoundModelBuilder {

    // TODO 1. Specify one-by-one (builder style) all source files that are included into the process.
    // TODO   1.1. Information needed for reading: file path, file format (Shapefile/XYZ/...)
    // TODO   1.2. Information needed for building: data nature (DEM/Water/Road/...), coordinate system (L-EST/...)
    // TODO 2. Specify build configuration
    // TODO   2.1. World boundaries
    // TODO   2.2. Point of origin (real coordinates that would be considered (0,0))
    // TODO 3. Initialize build process
    // TODO   3.1. Input file list will be sorted (DEM data, then water data, then road data, ...)
    // TODO     3.1.1. Each element could be optional ideally (can have flat worlds, or worlds without water, etc)
    // TODO   3.2. Building will be done gradually, each file contributing one by one
    // TODO     3.2.1. File must be streamed in chunks, not loading the entire file contents to memory
    // TODO     3.2.2. Build output must also be streamed to files (TODO Create FS structure and file formats)
    // TODO     3.2.3. When streaming input, can immediately filter out objects outside of the world boundaries
    // TODO   3.3. Carry out post-build checks on the generated data (world intact without missing blocks, etc)
    // TODO     3.3.1. Can carry out simple corrections by interpolation

    fun build(
        directory: File,
        heightMap: DataSource<BlockPos>,
        markerPoleCoordinates: DataSource<BlockPos>,
        waterPoolCentroids: DataSource<BlockPos>,
        islandCentroids: DataSource<BlockPos> = DataSource.emptyDataSource()
    ): DiskBoundModel {
        val model = DiskBoundModel(directory)

        model.buildTerrain(heightMap = heightMap)
        model.buildMarkerPoles(markerPoles = markerPoleCoordinates)
        model.buildWaterPools(waterPools = waterPoolCentroids)
        model.buildIslands(islandCentroids = islandCentroids)

        model.flush()

        return model
    }

    private fun DiskBoundModel.buildTerrain(heightMap: DataSource<BlockPos>) {
        heightMap.use { highestPoints ->
            highestPoints.forEach { highestPoint ->
                this[BlockPos(x = highestPoint.x, y = 0, z = highestPoint.z)] = BlockType.UNBREAKABLE_STONE
                for (y in 1 until highestPoint.y) {
                    this[BlockPos(x = highestPoint.x, y = y, z = highestPoint.z)] = BlockType.SOIL
                }
            }
        }
    }

    private fun DiskBoundModel.buildMarkerPoles(markerPoles: DataSource<BlockPos>) {
        markerPoles.use { highestPoints ->
            highestPoints.forEach { highestPoint ->
                this[BlockPos(x = highestPoint.x, y = 0, z = highestPoint.z)] = BlockType.UNBREAKABLE_STONE
                for (y in 1 until highestPoint.y) {
                    this[BlockPos(x = highestPoint.x, y = y, z = highestPoint.z)] = BlockType.STONE
                }
            }
        }
    }

    private fun DiskBoundModel.buildWaterPools(waterPools: DataSource<BlockPos>) {
        val poolWidth = 10
        val poolHeight = 5
        waterPools.use { highestPoints ->
            highestPoints.forEach { highestPoint ->
                for (x in -poolWidth / 2 until poolWidth / 2) {
                    for (z in -poolWidth / 2 until poolWidth / 2) {
                        for (y in highestPoint.y downTo highestPoint.y - poolHeight + 1) {
                            this[BlockPos(x = highestPoint.x + x, y = y, z = highestPoint.z + z)] = BlockType.WATER
                        }
                    }
                }
            }
        }
    }

    private fun DiskBoundModel.buildIslands(islandCentroids: DataSource<BlockPos>) {
        val spawnIslandWidth = 50
        val spawnIslandHeight = 5
        islandCentroids.use { centroids ->
            centroids.forEach { centroid ->
                for (x in -spawnIslandWidth / 2 until spawnIslandWidth / 2) {
                    for (z in -spawnIslandWidth / 2 until spawnIslandWidth / 2) {
                        for (y in 0 downTo -spawnIslandHeight + 1) {
                            this[BlockPos(x = centroid.x + x, y = y, centroid.z + z)] = BlockType.SOIL_WITH_GRASS
                        }
                    }
                }
            }
        }
    }

}