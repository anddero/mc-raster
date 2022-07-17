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
                println("Read ${sourceGeometry.name} ${sourceGeometry.type} ${sourceGeometry.value}")
            }
            it.close()
        }
    }
//    EXAMPLE: ShapefileReader().read("local-data/Topo250T_Maaamet_SHP/Vooluvesi.shp") // works

}
