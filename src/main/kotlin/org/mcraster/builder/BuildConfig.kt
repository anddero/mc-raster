package org.mcraster.builder

import org.mcraster.model.BlockType

data class BuildConfig(
    val modelAndWorldName: String,
    val relativeInputDir: String = "",
    val relativeOutputDir: String,
    val builderMaxCacheSizeMbWhileWriting: Int,
    val builderMaxCacheSizeMbWhileReading: Int,
    val generator: GeneratorType,
    val seaLevelBlockBottomY: Int,
    val isGeneratingStructuresEnabled: Boolean,
    val gameType: GameType,
    val layers: List<Layer>,
    val dataSources: Set<DataSource>
)

data class DataSource(
    val relativeFileName: String,
    val dataType: DataType,
    val dataFormat: DataFormat
)

enum class DataType {
    ORIGIN,
    BOUNDS,
    TERRAIN,
    WATER,
    ROAD,
    BUILDING,
    MODEL_3D
}

enum class DataFormat {
    /**
     * Custom format, a local 3D model without any global coordinate system.
     */
    OBJ_3D,

    /**
     * Plaintext file. Each line contains three space-delimited floating-point numbers in the order Y X H.
     * Y and X represent a horizontal geographical point in the L-Est97 coordinate system.
     * H represents the height from sea level in meters.
     */
    TEXT_POINT_LEST97_YXH,

    /**
     * Shapefile (.shp), where each feature is a 2D polygon or multi-polygon.
     * Each polygon vertex is composed of two floating-point numbers in the order Y X, which represent a horizontal
     * geographical point in the L-Est97 coordinate system.
     */
    SHP_POLYGON_LEST97_YX
}

data class Layer(val blockType: BlockType, val yMin: Int, val yMax: Int)

enum class GeneratorType { FLAT }

enum class GameType { CREATIVE }
