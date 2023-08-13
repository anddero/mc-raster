package org.mcraster.converters

import org.locationtech.jts.geom.GeometryCollection
import org.mcraster.model.BlockPos.HorPoint
import org.mcraster.model.BlockPos.HorPointRect
import org.mcraster.model.BlockPos.HorPos
import org.mcraster.model.BlockPos.HorPosRect
import org.mcraster.util.JtsUtil.JtsPolygonUtil.getHolesVertices
import org.mcraster.util.JtsUtil.JtsPolygonUtil.getOuterShellVertices
import org.mcraster.util.JtsUtil.JtsPolygonUtil.makeZeroCenterJtsPolygon
import org.mcraster.util.JtsUtil.JtsPolygonUtil.toZeroCenterJtsPolygon
import org.mcraster.util.NumberUtils.roundDownToIntExact
import java.awt.geom.Area
import java.math.BigDecimal
import org.locationtech.jts.geom.Polygon as JtsPolygon

class Polygon(
    outerShellVertices: List<HorPoint>,
    holesVertices: List<List<HorPoint>>
) {

    class MultiPolygon(val polygons: List<Polygon>)

    class PolygonRasterMask(val origin: HorPos, val maskZx: Array<BooleanArray>): Iterator<HorPos> { // TODO Test iterator
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
    fun createRasterMask(canvas: HorPosRect, cropFirst: Boolean): PolygonRasterMask? { // TODO Also test with cropFirst=true
        val lenX = canvas.max.x - canvas.min.x
        val lenZ = canvas.max.z - canvas.min.z
        if (lenX <= 0 || lenZ <= 0) throw RuntimeException("Invalid canvas size: $canvas")

        val polygons = if (cropFirst) crop(canvas.toHorPointRect()) else listOf(this)

        val rasterized = polygons.asSequence()
            .map { it.createRasterMaskNoCrop(canvas = canvas) }
            .reduceOrNull { a, b -> a.merge(b) }

        return rasterized?.run { PolygonRasterMask(origin = canvas.min, maskZx = this) }
    }

    private fun createRasterMaskNoCrop(canvas: HorPosRect): Array<BooleanArray> {
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
     * This procedure is lazy and returns a LazyData from which the processing of each next canvas may be requested.
     */
    fun createRasterMasks(area: HorPosRect, canvasSize: Int, cropFirst: Boolean): Sequence<PolygonRasterMask> { // TODO Test
        if (canvasSize <= 0) throw RuntimeException("Expected positive canvas size, given $canvasSize")
        return (area.min.x until area.max.x step canvasSize)
            .asSequence()
            .flatMap { startX ->
                (area.min.z until area.max.z step canvasSize)
                    .asSequence()
                    .map { startZ -> HorPos(x = startX, z = startZ) }
            }.map { startPos ->
                createRasterMask(
                    canvas = HorPosRect(
                        min = startPos,
                        max = HorPos(x = startPos.x + canvasSize, z = startPos.z + canvasSize)
                    ),
                    cropFirst = cropFirst
                )
            }.filterNotNull()
    }

    fun crop(container: HorPointRect): List<Polygon> {
        val jtsIntersection = zeroCenterPolygon.intersection(container.toZeroCenterJtsPolygon(center))
        val polygons = when (jtsIntersection) {
            is JtsPolygon -> sequenceOf(jtsIntersection)
            is GeometryCollection -> {
                (0 until jtsIntersection.numGeometries)
                    .asSequence()
                    .map { jtsIntersection.getGeometryN(it) }
                    .map { it as JtsPolygon }
            }
            else -> throw RuntimeException("Unhandled geometry type: ${jtsIntersection.geometryType}")
        }
        return polygons
            .filter { !it.isEmpty }
            .map {
                Polygon(
                    outerShellVertices = it.getOuterShellVertices(offset = center),
                    holesVertices = it.getHolesVertices(offset = center)
                )
            }.toList()
    }

    fun getOuterShell() = zeroCenterPolygon.getOuterShellVertices(offset = center)

    fun getHoles() = zeroCenterPolygon.getHolesVertices(offset = center)

    override fun toString(): String {
        return "Polygon(center=$center, zeroCenterPolygon=$zeroCenterPolygon)"
    }

    private val center: HorPoint
    private val zeroCenterPolygon: JtsPolygon // x,y in JtsPolygon corresponds to x,z in model

    init {
        holesVertices
            .asSequence()
            .plusElement(outerShellVertices)
            .forEach { vertices ->
                if (vertices.size < 3) {
                    throw RuntimeException("Polygon must be made of at least 3 corner vertices, given $vertices")
                }
            }
        center = outerShellVertices.getBoundingRectCenter()
        zeroCenterPolygon = makeZeroCenterJtsPolygon(
            outerShellVertices = outerShellVertices,
            holesVertices = holesVertices,
            center = center
        )
        if (!zeroCenterPolygon.isValid) {
            throw RuntimeException("Invalid Polygon constructed from shell: $outerShellVertices and holes: $holesVertices")
        }
    }

    companion object {

        private fun mean(v1: BigDecimal, v2: BigDecimal) = (v1 + v2) / BigDecimal.valueOf(2)

        private fun List<HorPoint>.getBoundingRectCenter(): HorPoint {
            val minX = minOf { it.x }
            val maxX = maxOf { it.x }
            val minZ = minOf { it.z }
            val maxZ = maxOf { it.z }
            return HorPoint(x = mean(minX, maxX), z = mean(minZ, maxZ))
        }

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
