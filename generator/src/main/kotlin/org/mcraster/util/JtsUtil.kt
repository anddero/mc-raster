package org.mcraster.util

import org.mcraster.converters.BlockPosLEst97.HorPointLEst97
import org.mcraster.converters.Polygon
import org.mcraster.pos.BlockPos
import org.mcraster.pos.BlockPos.HorPoint
import org.mcraster.util.JtsUtil.JtsCoordinateUtil.toZeroCenterJtsCoordinate
import org.mcraster.util.JtsUtil.JtsLinearRingUtil.getLEstYxVertices
import org.mcraster.util.JtsUtil.JtsLinearRingUtil.getVertices
import org.mcraster.util.JtsUtil.JtsLinearRingUtil.toZeroCenterJtsLinearRing
import org.mcraster.util.JtsUtil.JtsPolygonUtil.getHolesLEstYxVertices
import org.mcraster.util.JtsUtil.JtsPolygonUtil.getOuterShellLEstYxVertices
import org.locationtech.jts.geom.Coordinate as JtsCoordinate
import org.locationtech.jts.geom.GeometryFactory as JtsGeometryFactory
import org.locationtech.jts.geom.LinearRing as JtsLinearRing
import org.locationtech.jts.geom.MultiPolygon as JtsMultiPolygon
import org.locationtech.jts.geom.Polygon as JtsPolygon
import org.locationtech.jts.geom.PrecisionModel as JtsPrecisionModel
import org.locationtech.jts.geom.impl.CoordinateArraySequence as JtsCoordinateArraySequence

object JtsUtil {

    private val JTS_GEOMETRY_FACTORY = JtsGeometryFactory(JtsPrecisionModel(JtsPrecisionModel.FLOATING))

    object JtsMultiPolygonUtil {

        fun JtsMultiPolygon.fromLEstYx() =
            (0 until numGeometries)
                .asSequence()
                .map { getGeometryN(it) }
                .map {
                    if (it is JtsPolygon) it
                    else throw RuntimeException("Unexpected geometry: $it")
                }.map { polygonLEstYx ->
                    val exteriorRingLEstYx = polygonLEstYx.getOuterShellLEstYxVertices()
                    val interiorRingsLEstYx = polygonLEstYx.getHolesLEstYxVertices()
                    Polygon(
                        outerShellVertices = exteriorRingLEstYx.map(HorPointLEst97::toHorPoint),
                        holesVertices = interiorRingsLEstYx.map { ring -> ring.map(HorPointLEst97::toHorPoint) }
                    )
                }.toList()
                .let { polygons -> Polygon.MultiPolygon(polygons) }

    }

    object JtsPolygonUtil {

        fun JtsPolygon.getOuterShellLEstYxVertices() = exteriorRing.getLEstYxVertices()

        fun JtsPolygon.getOuterShellVertices(offset: HorPoint) = exteriorRing.getVertices(offset = offset)

        fun JtsPolygon.getHolesLEstYxVertices() =
            (0 until numInteriorRing)
                .asSequence()
                .map { holeIndex -> getInteriorRingN(holeIndex) }
                .map { linearRingLEstYx -> linearRingLEstYx.getLEstYxVertices() }
                .toList()

        fun JtsPolygon.getHolesVertices(offset: HorPoint) =
            (0 until numInteriorRing)
                .asSequence()
                .map { holeIndex -> getInteriorRingN(holeIndex) }
                .map { ring -> ring.getVertices(offset = offset) }
                .toList()

        fun BlockPos.HorPointRect.toZeroCenterJtsPolygon(center: HorPoint): JtsPolygon {
            val minXMaxZCorner = HorPoint(x = this.min.x, z = this.max.z)
            val maxXMinZCorner = HorPoint(x = this.max.x, z = this.min.z)
            val rectCorners = listOf(this.min, minXMaxZCorner, this.max, maxXMinZCorner)
            return JtsPolygon(
                rectCorners.toZeroCenterJtsLinearRing(center = center),
                emptyArray(),
                JTS_GEOMETRY_FACTORY
            )
        }

        fun makeZeroCenterJtsPolygon(
            outerShellVertices: List<HorPoint>,
            holesVertices: List<List<HorPoint>>,
            center: HorPoint
        ) = JtsPolygon(
            outerShellVertices.toZeroCenterJtsLinearRing(center),
            holesVertices
                .map { holeVertices -> holeVertices.toZeroCenterJtsLinearRing(center) }
                .toTypedArray(),
            JTS_GEOMETRY_FACTORY
        )

    }

    private object JtsLinearRingUtil {

        fun JtsLinearRing.getLEstYxVertices(): List<HorPointLEst97> {
            val points = coordinateSequence.toCoordinateArray()
                .map { HorPointLEst97(x = it.y.toBigDecimal(), y = it.x.toBigDecimal()) }
            if (points.first() != points.last()) throw RuntimeException("Expected closed ring of vertices")
            return points.dropLast(1)
        }

        fun JtsLinearRing.getVertices(offset: HorPoint): List<HorPoint> {
            val points = coordinateSequence.toCoordinateArray()
                .asSequence()
                .map { HorPoint(x = it.x.toBigDecimal(), z = it.y.toBigDecimal()) }
                .map { HorPoint(x = it.x + offset.x, z = it.z + offset.z) }
                .toList()
            if (points.first() != points.last()) throw RuntimeException("Expected closed ring of vertices")
            return points.dropLast(1)
        }

        fun List<HorPoint>.toZeroCenterJtsLinearRing(center: HorPoint): JtsLinearRing {
            val zeroCenterVertices = this.asSequence().map { corner -> corner.toZeroCenterJtsCoordinate(center) }
            val closedRingOfVertices = zeroCenterVertices.plusElement(zeroCenterVertices.first())
            return closedRingOfVertices
                .toList()
                .toTypedArray()
                .let { JtsCoordinateArraySequence(it) }
                .let { JtsLinearRing(it, JTS_GEOMETRY_FACTORY) }
        }

    }

    private object JtsCoordinateUtil {

        fun HorPoint.toZeroCenterJtsCoordinate(center: HorPoint) = JtsCoordinate(
            (x - center.x).toDouble(),
            (z - center.z).toDouble()
        )

    }

}
