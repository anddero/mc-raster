package org.mcraster.reader

import org.geotools.data.DataStoreFinder
import org.geotools.data.simple.SimpleFeatureIterator
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockPos.HorPosRect
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy.VERTICALLY_FIXED_AT_SEA_LEVEL
import org.mcraster.util.LazyData
import org.mcraster.util.LazyData.Companion.closeableLazyData
import org.mcraster.util.JtsUtil.JtsMultiPolygonUtil.fromLEstYx
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import org.locationtech.jts.geom.MultiPolygon as JtsMultiPolygon

object ShapefileReader { // Shapefile (.shp) reading capability

    fun readPolygonsFromShpFileLEstYx(shpFileLEstYx: File) = closeableLazyData {
        val dataStoreLEstYx = DataStoreFinder.getDataStore(mapOf("url" to shpFileLEstYx.toURI().toString()))
        dataStoreLEstYx
            .getFeatureSource(dataStoreLEstYx.typeNames.single())
            .features
            .features()
    }.ignoreCloseFailure { throwable ->
        if (throwable !is IllegalArgumentException) false
        else if (throwable.message.isNullOrBlank()) false
        else if (!throwable.message!!.contains("does not hold the lock for the URL")) false
        else {
            System.err.println("Ignoring exception thrown when attempted to close Shapefile, message: ${throwable.message}")
            true
        }
    }.transform { featureLEstYxIterator ->
        AsIterator(featureLEstYxIterator)
            .asSequence()
            .map { featureLEstYx ->
                val geometryLEstYx = featureLEstYx.defaultGeometryProperty.value
                if (geometryLEstYx is JtsMultiPolygon) geometryLEstYx.fromLEstYx()
                else throw RuntimeException("Unexpected geometry: $geometryLEstYx")
            }
    }

    fun readPolygonsFromShpFileLEstYx(
        shpFileLEstYx: File,
        pointConversionStrategy: PointConversionStrategy,
        seaLevelBlockBottomY: Int,
        limits: HorPosRect
    ): LazyData<BlockPos> {
        when (pointConversionStrategy) {
            VERTICALLY_FIXED_AT_SEA_LEVEL -> {} // currently only accepted strategy
            else -> throw RuntimeException("Unexpected conversion strategy: $pointConversionStrategy")
        }
        return readPolygonsFromShpFileLEstYx(shpFileLEstYx).transform { multiPolygons ->
            // TODO Log when the rasterization happens with respect to world creation, to understand how flatMap behaves in the context of a higher order sequences
            multiPolygons
                .flatMap { multiPolygon -> multiPolygon.polygons.asSequence() }
                .flatMap { polygon -> polygon.createRasterMasks(area = limits, canvasSize = 200, cropFirst = true) }
                .flatMap { it.asSequence() }
                .map { horPos -> BlockPos(x = horPos.x, y = seaLevelBlockBottomY, z = horPos.z) }
        }
    }

    private class AsIterator(private val src: SimpleFeatureIterator) : Iterator<SimpleFeature> {
        override fun hasNext() = src.hasNext()
        override fun next(): SimpleFeature = src.next()
    }

}
