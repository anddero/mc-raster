package org.mcraster.converters

import org.junit.jupiter.api.assertThrows
import org.mcraster.model.BlockPos
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PolygonTest {

    @Test
    fun constructPolygonsWithInvalidOuterRing() {
        // Zero points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = emptyList(),
                polygonCornersOfHoles = emptyList()
            )
        }
        // One point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = listOf(p(0.0, 0.0)),
                polygonCornersOfHoles = emptyList()
            )
        }
        // Two points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = listOf(p(0.0, 1.0), p(3.0, 4.0)),
                polygonCornersOfHoles = emptyList()
            )
        }
        // Three points with repeated point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = listOf(p(2.0, 0.5), p(1.0, 3.2), p(2.0, 0.5)),
                polygonCornersOfHoles = emptyList()
            )
        }
        // Shell lines intersect
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = listOf(p(0.0, 0.0), p(0.0, 3.0), p(3.0, 0.0), p(3.0, 3.0)),
                polygonCornersOfHoles = emptyList()
            )
        }
    }

    @Test
    fun constructPolygonsWithInvalidHoles() {
        val square = listOf(p(0.0, 0.0), p(0.0, 5.0), p(5.0, 5.0), p(5.0, 0.0))
        // Zero points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(emptyList())
            )
        }
        // One point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(listOf(p(1.0, 3.0)))
            )
        }
        // Two points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(listOf(p(2.0, 3.5), p(3.0, 2.3)))
            )
        }
        // Three points with repeated point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(listOf(p(4.0, 2.0), p(3.0, 5.1), p(3.0, 5.1)))
            )
        }
        // Hole equals shell
        assertThrows<RuntimeException> {
            Polygon(outerShellPolygonCorners = square, polygonCornersOfHoles = listOf(square))
        }
        // Holes make up the shell
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(
                    listOf(p(0.0, 0.0), p(0.0, 5.0), p(3.0, 5.0), p(3.0, 0.0)),
                    listOf(p(3.0, 0.0), p(3.0, 5.0), p(5.0, 5.0), p(5.0, 0.0))
                )
            )
        }
        // Hole intersects shell bounds
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(listOf(p(-1.0, 2.0), p(-1.0, 3.0), p(1.0, 3.0), p(1.0, 2.0)))
            )
        }
        // Hole at the side of the shell
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(listOf(p(0.0, 2.0), p(0.0, 3.0), p(1.0, 3.0), p(1.0, 2.0)))
            )
        }
        // Hole outside the shell
        assertThrows<RuntimeException> {
            Polygon(
                outerShellPolygonCorners = square,
                polygonCornersOfHoles = listOf(listOf(p(-1.0, -1.0), p(-1.0, 0.0), p(0.0, -1.0)))
            )
        }
    }

    @Test
    fun constructTriangle() {
        val triangle = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 0.0))
        val polygon = Polygon(
            outerShellPolygonCorners = triangle,
            polygonCornersOfHoles = emptyList()
        )
        val tolerance = BigDecimal.valueOf(0.0)
        assertTrue { areSameRings(triangle, polygon.getOuterShell(), tolerance) }
        assertTrue { polygon.getHoles().isEmpty() }
    }

    @Test
    fun constructRect() {
        val rect = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 1.0), p(1.0, 0.0))
        val polygon = Polygon(
            outerShellPolygonCorners = rect,
            polygonCornersOfHoles = emptyList()
        )
        val tolerance = BigDecimal.valueOf(0.0)
        assertTrue { areSameRings(rect, polygon.getOuterShell(), tolerance) }
        assertTrue { polygon.getHoles().isEmpty() }
    }

    @Test
    fun constructRectWithTriangleHole() {
        val rect = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 1.0), p(1.0, 0.0))
        val triangle = listOf(p(0.5, 0.5), p(0.6, 0.7), p(0.7, 0.5))
        val polygon = Polygon(
            outerShellPolygonCorners = rect,
            polygonCornersOfHoles = listOf(triangle)
        )
        val tolerance = BigDecimal.valueOf(0.0)
        assertTrue { areSameRings(rect, polygon.getOuterShell(), tolerance) }
        val holes = polygon.getHoles()
        assertEquals(1, holes.size)
        assertTrue { areSameRings(triangle, holes[0], tolerance) }
    }

    @Test
    fun cropRectangleInHalfByExactBounds() {
        val rect = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 1.0), p(1.0, 0.0))
        val cropRectLeft = BlockPos.HorRect(p(0.0, 0.0), p(0.5, 1.0))
        val cropRectRight = BlockPos.HorRect(p(0.5, 0.0), p(1.0, 1.0))
        val expectedLeft = listOf(p(0.0, 0.0), p(0.0, 1.0), p(0.5, 1.0), p(0.5, 0.0))
        val expectedRight = listOf(p(0.5, 0.0), p(0.5, 1.0), p(1.0, 1.0), p(1.0, 0.0))

        val polygon = Polygon(outerShellPolygonCorners = rect, polygonCornersOfHoles = emptyList())
        val croppedListLeft = polygon.crop(cropRectLeft)
        val croppedListRight = polygon.crop(cropRectRight)

        assertEquals(1, croppedListLeft.size)
        assertEquals(1, croppedListRight.size)
        val croppedLeft = croppedListLeft[0]
        val croppedRight = croppedListRight[0]
        assertTrue { croppedLeft.getHoles().isEmpty() }
        assertTrue { croppedRight.getHoles().isEmpty() }
        val tolerance = BigDecimal.valueOf(0.0)
        assertTrue { areSameRings(expectedLeft, croppedLeft.getOuterShell(), tolerance) }
        assertTrue { areSameRings(expectedRight, croppedRight.getOuterShell(), tolerance) }
    }

    @Test
    fun cropPolygonIntoSeveralPieces() {
        val lake =
            listOf(p(-1.0, -1.0), p(-1.5, 0.0), p(-0.5, 1.0), p(0.3, 0.5), p(0.2, -0.5), p(1.0, 2.0), p(1.5, -1.0))
        val cropRect = BlockPos.HorRect(p(0.0, 0.0), p(1.0, 1.0))
        val expectedOne = listOf(p(0.0, 0.0), p(0.0, 0.6875), p(0.3, 0.5), p(0.25, 0.0))
        val expectedTwo = listOf(p(0.36, 0.0), p(0.68, 1.0), p(1.0, 1.0), p(1.0, 0.0))

        val polygon = Polygon(outerShellPolygonCorners = lake, polygonCornersOfHoles = emptyList())
        val croppedList = polygon.crop(cropRect)

        assertEquals(2, croppedList.size)
        croppedList.forEach { assertTrue { it.getHoles().isEmpty() } }
        val shells = croppedList.map { it.getOuterShell() }
        println("shells $shells")
        val err = BigDecimal.valueOf(0.0000001)
        assertTrue {
            (areSameRings(expectedOne, shells[0], err) && areSameRings(expectedTwo, shells[1], err)) ||
                    (areSameRings(expectedOne, shells[1], err) && areSameRings(expectedTwo, shells[0], err))
        }
    }

    private fun p(x: Double, z: Double) = BlockPos.HorPoint(x = BigDecimal.valueOf(x), z = BigDecimal.valueOf(z))

    private fun areSameRings(a: List<BlockPos.HorPoint>, b: List<BlockPos.HorPoint>, tolerance: BigDecimal): Boolean {
        if (a.size < 3 || b.size < 3) throw RuntimeException("Not valid rings")
        if (a.size != b.size) return false
        return equals(adjustRingA(a, b), b, tolerance) || equals(adjustRingA(a.reversed(), b), b, tolerance)
    }

    private fun adjustRingA(a: List<BlockPos.HorPoint>, b: List<BlockPos.HorPoint>): List<BlockPos.HorPoint> {
        val aOffset = a.indexOf(b[0])
        if (aOffset < 0) return a
        return a.subList(aOffset, a.size) + a.subList(0, aOffset)
    }

    private fun equals(a: BigDecimal, b: BigDecimal, tolerance: BigDecimal) = a.minus(b).abs() < tolerance

    private fun equals(a: BlockPos.HorPoint, b: BlockPos.HorPoint, tolerance: BigDecimal) =
        equals(a.x, b.x, tolerance) && equals(a.z, b.z, tolerance)

    private fun equals(a: List<BlockPos.HorPoint>, b: List<BlockPos.HorPoint>, tolerance: BigDecimal) =
        a.size == b.size && a.indices.all { equals(a[it], b[it], tolerance) }

}
