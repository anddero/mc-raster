package org.mcraster.converters

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.mcraster.model.BlockPos.HorPoint
import org.mcraster.model.BlockPos.HorRect
import java.math.BigDecimal
import org.locationtech.jts.geom.Polygon as JtsPolygon

class Polygon(
    private val outerShellPolygonCorners: List<HorPoint>,
    private val polygonCornersOfHoles: List<List<HorPoint>>
) {

    fun crop(container: HorRect): Polygon {
        val polygon = zeroCenterPolygon.intersection(container.toZeroCenterJtsPolygon(center)) as JtsPolygon
        return Polygon(
            outerShellPolygonCorners = polygon.getOuterShellPolygonCorners(offset = center),
            polygonCornersOfHoles = polygon.getPolygonCornersOfHoles(offset = center)
        )
    }

    private val center: HorPoint
    private val zeroCenterPolygon: JtsPolygon // x,y in JtsPolygon corresponds to x,z in model

    init {
        polygonCornersOfHoles.asSequence().plusElement(outerShellPolygonCorners).forEach { polygonCorners ->
            if (polygonCorners.size < 3) {
                throw RuntimeException("Polygon must be made of at least 3 corner vertices, given $polygonCorners")
            }
        }
        center = outerShellPolygonCorners.getBoundingRectCenter()
        zeroCenterPolygon = makeZeroCenterJtsPolygon(
            shellPolygonCorners = outerShellPolygonCorners,
            polygonCornersOfHoles = polygonCornersOfHoles,
            center = center
        )
        if (!zeroCenterPolygon.isValid) {
            throw RuntimeException(
                "Invalid Polygon constructed from shell: $outerShellPolygonCorners" +
                        " and holes: $polygonCornersOfHoles"
            )
        }
    }

    companion object {

        private val JTS_GEOMETRY_FACTORY = GeometryFactory(PrecisionModel(PrecisionModel.FLOATING))

        private fun mean(v1: BigDecimal, v2: BigDecimal) = (v1 + v2) / BigDecimal.valueOf(2)

        private fun List<HorPoint>.getBoundingRectCenter(): HorPoint {
            val minX = minOf { it.x }
            val maxX = maxOf { it.x }
            val minZ = minOf { it.z }
            val maxZ = maxOf { it.z }
            return HorPoint(x = mean(minX, maxX), z = mean(minZ, maxZ))
        }

        private fun HorPoint.toZeroCenterCoordinate(center: HorPoint) =
            Coordinate((x - center.x).toDouble(), (z - center.z).toDouble())

        private fun List<HorPoint>.toZeroCenterLinearRing(center: HorPoint): LinearRing {
            val zeroCenterCorners = this.asSequence().map { corner -> corner.toZeroCenterCoordinate(center) }
            val closedListOfCorners = zeroCenterCorners.plusElement(zeroCenterCorners.first())
            return LinearRing(
                CoordinateArraySequence(closedListOfCorners.toList().toTypedArray()), JTS_GEOMETRY_FACTORY
            )
        }

        private fun HorRect.toZeroCenterJtsPolygon(center: HorPoint): JtsPolygon {
            val minXMaxZCorner = HorPoint(x = this.min.x, z = this.max.z)
            val maxXMinZCorner = HorPoint(x = this.max.x, z = this.min.z)
            return JtsPolygon(
                listOf(this.min, minXMaxZCorner, this.max, maxXMinZCorner).toZeroCenterLinearRing(center),
                emptyArray(),
                JTS_GEOMETRY_FACTORY
            )
        }

        private fun makeZeroCenterJtsPolygon(
            shellPolygonCorners: List<HorPoint>,
            polygonCornersOfHoles: List<List<HorPoint>>,
            center: HorPoint
        ) = JtsPolygon(
            shellPolygonCorners.toZeroCenterLinearRing(center),
            polygonCornersOfHoles
                .map { holeCorners -> holeCorners.toZeroCenterLinearRing(center) }
                .toTypedArray(),
            JTS_GEOMETRY_FACTORY
        )

        private fun LinearRing.getPolygonCorners(offset: HorPoint): List<HorPoint> {
            val points = coordinateSequence.toCoordinateArray()
                .asSequence()
                .map { HorPoint(x = it.x.toBigDecimal(), z = it.y.toBigDecimal()) }
                .map { HorPoint(x = it.x + offset.x, z = it.z + offset.z) }
                .toList()
            if (points.first() != points.last()) throw RuntimeException("Expected closed ring of vertices")
            return points.dropLast(1)
        }

        private fun JtsPolygon.getOuterShellPolygonCorners(offset: HorPoint) =
            exteriorRing.getPolygonCorners(offset = offset)

        private fun JtsPolygon.getPolygonCornersOfHoles(offset: HorPoint) =
            (0 until numInteriorRing)
                .asSequence()
                .map { holeIndex -> getInteriorRingN(holeIndex) }
                .map { ring -> ring.getPolygonCorners(offset = offset) }
                .toList()

    }

}
