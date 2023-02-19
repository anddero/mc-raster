package org.mcraster.reader

import org.geotools.data.DataStoreFinder
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiLineString
import org.mcraster.converters.BlockPosLEst97.HorPointLEst97
import org.mcraster.converters.Polygon
import java.io.File
import java.math.BigDecimal

class ShapefileReader { // Shapefile (.shp) reading capability
    // TODO Check conversion to GeoJson https://sourceforge.net/p/geotools/mailman/message/32744416/

    fun read(file: String) {
        val dataStore = DataStoreFinder.getDataStore(mapOf("url" to File(file).toURI().toString()))
        val typeName = dataStore.typeNames.single()
        println("Reading $typeName")

        val featureSource = dataStore.getFeatureSource(typeName)
        val featureCollection = featureSource.features

        featureCollection.features().let {
            while (it.hasNext()) {
                val feature = it.next()
                val sourceGeometry = feature.defaultGeometryProperty
                sourceGeometry.descriptor
                sourceGeometry.identifier
                sourceGeometry.userData
                println("Read \n\t" +
                        "name: ${sourceGeometry.name}\n\t" +
                        "type: ${sourceGeometry.type}\n\t" +
                        "value: ${sourceGeometry.value}\n\t" +
                        "bounds: ${sourceGeometry.bounds}\n\t" +
                        "descriptor: ${sourceGeometry.descriptor}\n\t" +
                        "identifier: ${sourceGeometry.identifier}\n\t" +
                        "userData: ${sourceGeometry.userData}")
            }
            it.close()
        }
    }

    fun readPolygonsFromLEst97(shpFileLEst97: String): List<Polygon> {
        val dataStoreLEst97 = DataStoreFinder.getDataStore(mapOf("url" to File(shpFileLEst97).toURI().toString()))
        val polygons = mutableListOf<Polygon>()
        dataStoreLEst97.getFeatureSource(dataStoreLEst97.typeNames.single()).features.features().use {
            while (it.hasNext()) {
                val geometryValue = it.next().defaultGeometryProperty.value
                if (geometryValue is MultiLineString) polygons.add(multiLineStringLEst97ToPolygon(geometryValue))
                else throw RuntimeException("Unexpected object: $geometryValue")
            }
        }
        return polygons
    }

    companion object {

        private fun multiLineStringLEst97ToPolygon(multiLineStringLEst97: MultiLineString): Polygon {
            val polygonShapes = (0 until multiLineStringLEst97.numGeometries)
                .map { multiLineStringLEst97.getGeometryN(it) }
                .map {
                    if (it is LineString) it
                    else throw RuntimeException("Unexpected geometry: $it")
                }.map { lineStringLEstYxToHorPoints(it) }
            return Polygon(polygonShapes.first(), polygonShapes.subList(1, polygonShapes.size))
        }

        private fun lineStringLEstYxToHorPoints(lineStringLEstYx: LineString) =
            lineStringLEstYx.coordinateSequence.toCoordinateArray()
                .map { coordinateLEstYx ->
                    HorPointLEst97(
                        x = BigDecimal.valueOf(coordinateLEstYx.y),
                        y = BigDecimal.valueOf(coordinateLEstYx.x)
                    )
                }
                .map { horPointLEst97 -> horPointLEst97.toHorPoint() }

    }

}
