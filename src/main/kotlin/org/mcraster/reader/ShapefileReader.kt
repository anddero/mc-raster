package org.mcraster.reader

import org.geotools.data.DataStoreFinder
import org.geotools.data.simple.SimpleFeatureIterator
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.MultiPolygon
import org.mcraster.converters.BlockPosLEst97.HorPointLEst97
import org.mcraster.converters.Polygon
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockPos.HorPosRect
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy.VERTICALLY_FIXED_AT_SEA_LEVEL
import org.mcraster.util.DataSource
import org.mcraster.util.DataSource.Companion.asDataSource
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import org.locationtech.jts.geom.Polygon as JtsPolygon

object ShapefileReader { // Shapefile (.shp) reading capability

    fun readPolygonsFromShpFileLEstYx(shpFileLEstYx: File): DataSource<List<Polygon>> {
        val dataStoreLEstYx = DataStoreFinder.getDataStore(mapOf("url" to shpFileLEstYx.toURI().toString()))
        return dataStoreLEstYx
            .getFeatureSource(dataStoreLEstYx.typeNames.single())
            .features
            .features()
            .asDataSource { featureLEstYxIterator ->
                OpenedSimpleFeatureIteratorWrapper(featureLEstYxIterator).asSequence()
            }.map { featureLEstYx ->
                val geometryLEstYx = featureLEstYx.defaultGeometryProperty.value
                if (geometryLEstYx is MultiPolygon) multiPolygonLEstYxToPolygons(geometryLEstYx)
                else throw RuntimeException("Unexpected geometry: $geometryLEstYx")
            }
    }

    fun readPolygonsFromShpFileLEstYx(
        shpFileLEstYx: File,
        pointConversionStrategy: PointConversionStrategy,
        seaLevelBlockBottomY: Int,
        limits: HorPosRect
    ): DataSource<BlockPos> {
        when (pointConversionStrategy) {
            VERTICALLY_FIXED_AT_SEA_LEVEL -> {} // currently only accepted strategy
            else -> throw RuntimeException("Unexpected conversion strategy: $pointConversionStrategy")
        }
        return readPolygonsFromShpFileLEstYx(shpFileLEstYx)
            .map { polygonList ->
                polygonList
                    .asSequence()
                    .flatMap { polygon ->
                        polygon
                            .rasterizeMulti(area = limits, canvasSize = 200, cropFirst = true)
                            .flatMap { it.asSequence() }
                    }
            }.transform { horPosSeqSeq ->
                horPosSeqSeq.flatMap { horPosSeq ->
                    horPosSeq.map { horPos -> BlockPos(x = horPos.x, y = seaLevelBlockBottomY, z = horPos.z) }
                }
            }
    }

    private class OpenedSimpleFeatureIteratorWrapper(private val src: SimpleFeatureIterator) : Iterator<SimpleFeature> {
        override fun hasNext() = src.hasNext()
        override fun next(): SimpleFeature = src.next()
    }

    private fun multiPolygonLEstYxToPolygons(multiPolygon: MultiPolygon) =
        (0 until multiPolygon.numGeometries)
            .map { multiPolygon.getGeometryN(it) }
            .map {
                if (it is JtsPolygon) it
                else throw RuntimeException("Unexpected geometry: $it")
            }.map { polygonLEstYx ->
                val exteriorRingLEstYx = getPolygonLEstYxOuterShellCorners(polygonLEstYx)
                val interiorRingsLEstYx = getPolygonLEstYxCornersOfHoles(polygonLEstYx)
                Polygon(
                    outerShellPolygonCorners = exteriorRingLEstYx.map(HorPointLEst97::toHorPoint),
                    polygonCornersOfHoles = interiorRingsLEstYx.map { it.map(HorPointLEst97::toHorPoint) }
                )
            }

    private fun getPolygonLEstYxCorners(linearRingLEstYx: LinearRing): List<HorPointLEst97> {
        // TODO This is a duplicate of a similar function from Polygon impl, move to JtsUtil?
        val coordinatesLEstYx = linearRingLEstYx.coordinateSequence.toCoordinateArray()
        val points = coordinatesLEstYx.map { HorPointLEst97(x = it.y.toBigDecimal(), y = it.x.toBigDecimal()) }
        if (points.first() != points.last()) throw RuntimeException("Expected closed ring of vertices")
        return points.dropLast(1)
    }

    private fun getPolygonLEstYxOuterShellCorners(polygonLEstYx: JtsPolygon) =
        getPolygonLEstYxCorners(linearRingLEstYx = polygonLEstYx.exteriorRing)

    private fun getPolygonLEstYxCornersOfHoles(polygonLEstYx: JtsPolygon) =
        (0 until polygonLEstYx.numInteriorRing)
            .map { holeIndex -> polygonLEstYx.getInteriorRingN(holeIndex) }
            .map { getPolygonLEstYxCorners(linearRingLEstYx = it) }

}
