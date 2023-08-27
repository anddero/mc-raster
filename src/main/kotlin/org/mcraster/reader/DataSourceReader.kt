package org.mcraster.reader

import org.mcraster.builder.DataFormat
import org.mcraster.builder.DataFormat.OBJ_3D
import org.mcraster.builder.DataFormat.SHP_POLYGON_LEST97_YX
import org.mcraster.builder.DataFormat.TEXT_POINT_LEST97_YXH
import org.mcraster.converters.BlockPosLEst97
import org.mcraster.reader.Lest97Reader.lazyReadTextPointsLest97Yxh
import org.mcraster.reader.Obj3dReader.lazyReadObj3d
import org.mcraster.reader.ShapefileReader.lazyReadShpPolygonsLestYx
import java.io.File

object DataSourceReader {

    /**
     * Read the block to be considered as origin or spawn point.
     */
    fun readOrigin(file: File, format: DataFormat) = when (format) {
        TEXT_POINT_LEST97_YXH -> lazyReadTextPointsLest97Yxh(file = file).use { it.first() }
        OBJ_3D, SHP_POLYGON_LEST97_YX -> throw RuntimeException("$format not supported for origin")
    }

    /**
     * Read the boundaries of the world.
     * Given any two coordinates, return the 3D cubic area in between them, as if they were the opposite corners of the
     * cube.
     */
    fun readBounds(file: File, format: DataFormat) = when (format) {
        TEXT_POINT_LEST97_YXH -> lazyReadTextPointsLest97Yxh(file = file).use {
            it.take(2).toList().zipWithNext().single().let { (one, two) ->
                val xMin = one.x.min(two.x)
                val yMin = one.y.min(two.y)
                val hMin = one.h.min(two.h)
                val xMax = one.x.max(two.x)
                val yMax = one.y.max(two.y)
                val hMax = one.h.max(two.h)
                val minPoint = BlockPosLEst97.PointLEst97(x = xMin, y = yMin, h = hMin)
                val maxPoint = BlockPosLEst97.PointLEst97(x = xMax, y = yMax, h = hMax)
                BlockPosLEst97.PointCubeLEst97(min = minPoint, max = maxPoint)
            }
        }

        OBJ_3D, SHP_POLYGON_LEST97_YX -> throw RuntimeException("$format not supported for bounds")
    }

    fun lazyReadTerrain(file: File, format: DataFormat) = when (format) {
        TEXT_POINT_LEST97_YXH -> lazyReadTextPointsLest97Yxh(file = file)
        OBJ_3D, SHP_POLYGON_LEST97_YX -> throw RuntimeException("$format not supported for terrain")
    }

    fun lazyReadWater(file: File, format: DataFormat) = when (format) {
        TEXT_POINT_LEST97_YXH, OBJ_3D -> throw RuntimeException("$format not supported for water")
        SHP_POLYGON_LEST97_YX -> lazyReadShpPolygonsLestYx(file)
    }

    fun lazyReadRoads(file: File, format: DataFormat) = when (format) {
        TEXT_POINT_LEST97_YXH, OBJ_3D -> throw RuntimeException("$format not supported for roads")
        SHP_POLYGON_LEST97_YX -> lazyReadShpPolygonsLestYx(file)
    }

    fun lazyReadBuildings(file: File, format: DataFormat) = when (format) {
        TEXT_POINT_LEST97_YXH, OBJ_3D -> throw RuntimeException("$format not supported for buildings")
        SHP_POLYGON_LEST97_YX -> lazyReadShpPolygonsLestYx(file)
    }

    fun lazyRead3dObjects(file: File, format: DataFormat) = when (format) {
        OBJ_3D -> lazyReadObj3d(file)
        TEXT_POINT_LEST97_YXH, SHP_POLYGON_LEST97_YX -> throw RuntimeException("$format not supported for 3D objects")
    }

}
