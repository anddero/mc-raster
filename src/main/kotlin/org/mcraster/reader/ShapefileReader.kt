package org.mcraster.reader

import org.geotools.data.DataStoreFinder
import java.io.File

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

}
