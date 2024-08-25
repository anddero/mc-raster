package org.mcraster.reader

import java.awt.Rectangle
import java.io.File


class GeoPackageReader {

    fun read(file: String) {
        /*val fileFile = File(file)
        val geoPackage = GeoPackage(fileFile)

        val reader = GeotoolsGeoPackageReader(fileFile, null)
//        val parameters = arrayOfNulls<GeneralParameterValue>(1)
        val tileEntry: TileEntry = geoPackage.tiles()[0]
        val referencedEnvelope = tileEntry.bounds
        val rectangle = Rectangle(referencedEnvelope.width.toInt(), referencedEnvelope.height.toInt())
        val gridEnvelope = GridEnvelope2D(rectangle)
//        val gridGeometry = GridGeometry2D(gridEnvelope, referencedEnvelope)
//        parameters[0] = Paramet(AbstractGridFormat.READ_GRIDGEOMETRY2D, gridGeometry)
        val tableName = tileEntry.tableName // "rivers_tiles"

        val gridCoverage: GridCoverage2D = reader.read(
            tableName
        ) // throws java.lang.IllegalArgumentException: Envelope must be at least two-dimensional and non-empty.

        println("gridCoverage: ${gridCoverage.envelope2D} \n ${gridCoverage.gridGeometry}")

        val img = gridCoverage.renderedImage*/
    }
//    EXAMPLE: GeoPackageReader().read("local-data/QGIS_Out/DEM_Test.gpkg") // fails

}