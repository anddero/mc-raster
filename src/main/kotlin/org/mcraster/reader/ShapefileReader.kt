package org.mcraster.reader

import org.geotools.data.DataStoreFinder
import org.geotools.data.simple.SimpleFeatureIterator
import org.mcraster.util.LazyData.Companion.closeableLazyData
import org.mcraster.util.JtsUtil.JtsMultiPolygonUtil.fromLEstYx
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import org.locationtech.jts.geom.MultiPolygon as JtsMultiPolygon

object ShapefileReader { // Shapefile (.shp) reading capability

    fun lazyReadShpPolygonsLestYx(shpFileLEstYx: File) = closeableLazyData {
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

    private class AsIterator(private val src: SimpleFeatureIterator) : Iterator<SimpleFeature> {
        override fun hasNext() = src.hasNext()
        override fun next(): SimpleFeature = src.next()
    }

}

/*
TODO Check if shpfiles may contain useful metadata for better generation
println(
    "Read \n\t" +
            "name: ${sourceGeometry.name}\n\t" +
            "type: ${sourceGeometry.type}\n\t" +
            "value: ${sourceGeometry.value}\n\t" +
            "bounds: ${sourceGeometry.bounds}\n\t" +
            "descriptor: ${sourceGeometry.descriptor}\n\t" +
            "identifier: ${sourceGeometry.identifier}\n\t" +
            "userData: ${sourceGeometry.userData}\n\t" +
            "sourceGeometry: $sourceGeometry\n\t" *//*+
            "feature: $feature"*//*
)
*/