package org.mcraster.converters

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.mcraster.model.BlockPos.HorPoint
import org.mcraster.model.BlockPos.HorPointRect
import org.mcraster.model.BlockPos.HorPos
import org.mcraster.model.BlockPos.HorPosRect
import org.mcraster.util.NumberUtils.roundDownToIntExact
import java.awt.geom.Area
import java.math.BigDecimal
import org.locationtech.jts.geom.Polygon as JtsPolygon

class Polygon(
    outerShellPolygonCorners: List<HorPoint>,
    polygonCornersOfHoles: List<List<HorPoint>>
) {

    class PolygonMask(val origin: HorPos, val maskZx: Array<BooleanArray>): Iterator<HorPos> { // TODO Test iterator
        private var nextZ = 0
        private var nextX = 0

        override fun hasNext(): Boolean {
            while (true) {
                if (nextZ == maskZx.size) return false
                if (nextX == maskZx[nextZ].size) {
                    nextX = 0
                    ++nextZ
                    continue
                }
                if (maskZx[nextZ][nextX]) return true
                ++nextX
            }
        }

        override fun next(): HorPos {
            if (!hasNext()) throw RuntimeException("No next element")
            return HorPos(x = origin.x + nextX++, z = origin.z + nextZ)
        }
    }

    /**
     * Approximate the polygon as a binary mask onto a fixed size canvas.
     * Zeros in the mask represent background pixels, ones represent the polygon.
     * The pixel at position (0,0) in the resulting matrix has coordinates (minX,minZ) and
     * should be retrieved as `arr[z][x]` since Z is represented as the outer and X as the inner coordinate.
     * TODO We lose some accuracy due to rounding all coordinates down to Int before rasterization.
     *  The ideal solution should rasterize with BigDecimal coordinates directly.
     */
    fun rasterize(canvas: HorPosRect, cropFirst: Boolean): PolygonMask? { // TODO Also test with cropFirst=true
        val lenX = canvas.max.x - canvas.min.x
        val lenZ = canvas.max.z - canvas.min.z
        if (lenX <= 0 || lenZ <= 0) throw RuntimeException("Invalid canvas size: $canvas")

        val polygons = if (cropFirst) crop(canvas.toHorPointRect()) else listOf(this)

        val rasterized = polygons.asSequence()
            .map { it.rasterizeNoCrop(canvas = canvas) }
            .reduceOrNull { a, b -> a.merge(b) }

        return rasterized?.run { PolygonMask(origin = canvas.min, maskZx = this) }
    }

    private fun rasterizeNoCrop(canvas: HorPosRect): Array<BooleanArray> {
        val lenX = canvas.max.x - canvas.min.x
        val lenZ = canvas.max.z - canvas.min.z

        val outerShell = getOuterShell()
        val outerX = outerShell.map { it.x.roundDownToIntExact() - canvas.min.x }.toIntArray()
        val outerZ = outerShell.map { it.z.roundDownToIntExact() - canvas.min.z }.toIntArray()
        val outerPolygon = java.awt.Polygon(outerX, outerZ, outerX.size)

        val area = Area(outerPolygon)

        val holes = getHoles()
        holes.forEach { hole ->
            val holeX = hole.map { it.x.roundDownToIntExact() - canvas.min.x }.toIntArray()
            val holeZ = hole.map { it.z.roundDownToIntExact() - canvas.min.z }.toIntArray()
            val holePolygon = java.awt.Polygon(holeX, holeZ, holeX.size)
            area.subtract(Area(holePolygon))
        }

        val mask = Array(lenZ) { BooleanArray(lenX) { false } }
        for (z in 0 until lenZ) {
            for (x in 0 until lenX) {
                mask[z][x] = area.contains(x.toDouble(), z.toDouble())
            }
        }
        return mask
    }

    /**
     * Rasterize onto multiple canvases from different regions.
     * Provide the bounds of a large area and the canvas size. The area will be divided into square canvases of the
     * desired size (canvasSize x canvasSize). Min coordinate inclusive, max coordinate exclusive.
     * This procedure is lazy and returns a DataSource from which the processing of each next canvas may be requested.
     */
    fun rasterizeMulti(area: HorPosRect, canvasSize: Int, cropFirst: Boolean): Sequence<PolygonMask> { // TODO Test
        if (canvasSize <= 0) throw RuntimeException("Expected positive canvas size, given $canvasSize")
        return (area.min.x until area.max.x step canvasSize)
            .asSequence()
            .flatMap { startX ->
                (area.min.z until area.max.z step canvasSize)
                    .asSequence()
                    .map { startZ -> HorPos(x = startX, z = startZ) }
            }.map { startPos ->
                rasterize(
                    canvas = HorPosRect(
                        min = startPos,
                        max = HorPos(x = startPos.x + canvasSize, z = startPos.z + canvasSize)
                    ),
                    cropFirst = cropFirst
                )
            }.filterNotNull()
    }

    fun crop(container: HorPointRect): List<Polygon> {
        val geometry = zeroCenterPolygon.intersection(container.toZeroCenterJtsPolygon(center))
        val polygons = when (geometry) {
            is JtsPolygon -> listOf(geometry)
            is GeometryCollection -> {
                (0 until geometry.numGeometries)
                    .map { geometry.getGeometryN(it) }
                    .map { it as JtsPolygon }
                    .toList()
            }

            else -> throw RuntimeException("Unhandled geometry type: ${geometry.geometryType}")
        }
        return polygons.filter { !it.isEmpty }
            .map {
                Polygon(
                    outerShellPolygonCorners = it.getOuterShellPolygonCorners(offset = center),
                    polygonCornersOfHoles = it.getPolygonCornersOfHoles(offset = center)
                )
            }
    }

    fun getOuterShell() = zeroCenterPolygon.getOuterShellPolygonCorners(offset = center)

    fun getHoles() = zeroCenterPolygon.getPolygonCornersOfHoles(offset = center)

    override fun toString(): String {
        return "Polygon(center=$center, zeroCenterPolygon=$zeroCenterPolygon)"
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

        private fun HorPointRect.toZeroCenterJtsPolygon(center: HorPoint): JtsPolygon {
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

        /**
         * Merge a canvas into this one.
         * The argument is not modified, but the receiver is.
         * Returns the receiver.
         */
        private fun Array<BooleanArray>.merge(other: Array<BooleanArray>): Array<BooleanArray> {
            if (this.size != other.size) throw RuntimeException("Canvases have different sizes")
            for (z in this.indices) {
                if (this[z].size != other[z].size) throw RuntimeException("Canvas rows $z have different sizes")
                for (x in this[z].indices) {
                    if (other[z][x]) this[z][x] = true
                }
            }
            return this
        }

    }

}
