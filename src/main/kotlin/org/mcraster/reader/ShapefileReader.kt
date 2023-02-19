package org.mcraster.reader

import org.geotools.data.DataStoreFinder
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.MultiPolygon
import org.mcraster.converters.BlockPosLEst97.HorPointLEst97
import org.mcraster.converters.Polygon
import java.io.File
import org.locationtech.jts.geom.Polygon as JtsPolygon

class ShapefileReader { // Shapefile (.shp) reading capability

    fun readWithPrint(file: String) { // TODO Remove this function, just used for testing shpfile properties
        val dataStore = DataStoreFinder.getDataStore(mapOf("url" to File(file).toURI().toString()))
        val typeName = dataStore.typeNames.single()
        println("Reading $typeName")

        val featureSource = dataStore.getFeatureSource(typeName)
        val featureCollection = featureSource.features

        featureCollection.features().let {
            while (it.hasNext()) {
                val feature = it.next()
                val sourceGeometry = feature.defaultGeometryProperty
                println(
                    "Read \n\t" +
                            "name: ${sourceGeometry.name}\n\t" +
                            "type: ${sourceGeometry.type}\n\t" +
                            "value: ${sourceGeometry.value}\n\t" +
                            "bounds: ${sourceGeometry.bounds}\n\t" +
                            "descriptor: ${sourceGeometry.descriptor}\n\t" +
                            "identifier: ${sourceGeometry.identifier}\n\t" +
                            "userData: ${sourceGeometry.userData}\n\t" +
                            "sourceGeometry: $sourceGeometry\n\t" +
                            "feature: $feature"
                )
                if (sourceGeometry.value is MultiPolygon) {
                    val multiPolygons = sourceGeometry.value as MultiPolygon
                    println("Polygons: ${multiPolygonLEstYxToPolygons(multiPolygons)}")
//                    val polygon = Polygon(horPoints.first(), horPoints.subList(1, horPoints.size))
//                    println("Polygon: $polygon")
                }
            }
            it.close()
        }
    }

    companion object {

        fun readPolygonsFromLEstYx(shpFileLEstYx: String): List<List<Polygon>> {
            val dataStoreLEstYx = DataStoreFinder.getDataStore(mapOf("url" to File(shpFileLEstYx).toURI().toString()))
            val polygons = mutableListOf<List<Polygon>>()
            dataStoreLEstYx.getFeatureSource(dataStoreLEstYx.typeNames.single()).features.features().use {
                while (it.hasNext()) {
                    val geometryValue = it.next().defaultGeometryProperty.value
                    if (geometryValue is MultiPolygon) polygons.add(multiPolygonLEstYxToPolygons(geometryValue))
                    else throw RuntimeException("Unexpected geometry: $geometryValue")
                }
            }
            return polygons
        }

        /*fun readPointListsFromLEstYx(shpFileLEstYx: String): List<List<HorPoint>> {
            val multiLineGeometries = readPointListListsFromLEstYx(shpFileLEstYx)
            return multiLineGeometries.map { it.single() }
        }*/

        /*private fun readPointListListsFromLEstYx(shpFileLEstYx: String): List<List<List<HorPoint>>> {
            val dataStoreLEstYx = DataStoreFinder.getDataStore(mapOf("url" to File(shpFileLEstYx).toURI().toString()))
            val lines = mutableListOf<List<List<HorPoint>>>()
            dataStoreLEstYx.getFeatureSource(dataStoreLEstYx.typeNames.single()).features.features().use {
                while (it.hasNext()) {
                    val geometryValue = it.next().defaultGeometryProperty.value
                    if (geometryValue is MultiLineString) lines.add(multiLineStringLEstYxToPointList(geometryValue))
                    else throw RuntimeException("Unexpected object: $geometryValue")
                }
            }
            return lines
        }*/

        /*private fun multiLineStringLEstYxToPointList(multiLineStringLEstYx: MultiLineString): List<List<HorPoint>> {
            val polygonShapes = (0 until multiLineStringLEstYx.numGeometries)
                .map { multiLineStringLEstYx.getGeometryN(it) }
                .map {
                    if (it is LineString) it
                    else throw RuntimeException("Unexpected geometry: $it")
                }.map { lineStringLEstYxToHorPoints(it) }
            return polygonShapes
        }*/

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

        /*private fun lineStringLEstYxToHorPoints(lineStringLEstYx: LineString) =
            lineStringLEstYx.coordinateSequence.toCoordinateArray()
                .map { coordinateLEstYx ->
                    HorPointLEst97(
                        x = BigDecimal.valueOf(coordinateLEstYx.y),
                        y = BigDecimal.valueOf(coordinateLEstYx.x)
                    )
                }
                .map { horPointLEst97 -> horPointLEst97.toHorPoint() }*/

    }

}
