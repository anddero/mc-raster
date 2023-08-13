package org.mcraster.converters

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.assertThrows
import org.mcraster.model.BlockPos
import org.mcraster.model.BlockPos.HorPos
import org.mcraster.model.BlockPos.HorPosRect
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
                outerShellVertices = emptyList(),
                holesVertices = emptyList()
            )
        }
        // One point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = listOf(p(0.0, 0.0)),
                holesVertices = emptyList()
            )
        }
        // Two points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = listOf(p(0.0, 1.0), p(3.0, 4.0)),
                holesVertices = emptyList()
            )
        }
        // Three points with repeated point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = listOf(p(2.0, 0.5), p(1.0, 3.2), p(2.0, 0.5)),
                holesVertices = emptyList()
            )
        }
        // Shell lines intersect
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = listOf(p(0.0, 0.0), p(0.0, 3.0), p(3.0, 0.0), p(3.0, 3.0)),
                holesVertices = emptyList()
            )
        }
    }

    @Test
    fun constructPolygonsWithInvalidHoles() {
        val square = listOf(p(0.0, 0.0), p(0.0, 5.0), p(5.0, 5.0), p(5.0, 0.0))
        // Zero points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(emptyList())
            )
        }
        // One point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(listOf(p(1.0, 3.0)))
            )
        }
        // Two points
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(listOf(p(2.0, 3.5), p(3.0, 2.3)))
            )
        }
        // Three points with repeated point
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(listOf(p(4.0, 2.0), p(3.0, 5.1), p(3.0, 5.1)))
            )
        }
        // Hole equals shell
        assertThrows<RuntimeException> {
            Polygon(outerShellVertices = square, holesVertices = listOf(square))
        }
        // Holes make up the shell
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(
                    listOf(p(0.0, 0.0), p(0.0, 5.0), p(3.0, 5.0), p(3.0, 0.0)),
                    listOf(p(3.0, 0.0), p(3.0, 5.0), p(5.0, 5.0), p(5.0, 0.0))
                )
            )
        }
        // Hole intersects shell bounds
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(listOf(p(-1.0, 2.0), p(-1.0, 3.0), p(1.0, 3.0), p(1.0, 2.0)))
            )
        }
        // Hole at the side of the shell
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(listOf(p(0.0, 2.0), p(0.0, 3.0), p(1.0, 3.0), p(1.0, 2.0)))
            )
        }
        // Hole outside the shell
        assertThrows<RuntimeException> {
            Polygon(
                outerShellVertices = square,
                holesVertices = listOf(listOf(p(-1.0, -1.0), p(-1.0, 0.0), p(0.0, -1.0)))
            )
        }
    }

    @Test
    fun constructTriangle() {
        val triangle = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 0.0))
        val polygon = Polygon(
            outerShellVertices = triangle,
            holesVertices = emptyList()
        )
        val tolerance = BigDecimal.valueOf(0.0)
        assertTrue { areSameRings(triangle, polygon.getOuterShell(), tolerance) }
        assertTrue { polygon.getHoles().isEmpty() }
    }

    @Test
    fun constructRect() {
        val rect = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 1.0), p(1.0, 0.0))
        val polygon = Polygon(
            outerShellVertices = rect,
            holesVertices = emptyList()
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
            outerShellVertices = rect,
            holesVertices = listOf(triangle)
        )
        val tolerance = BigDecimal.valueOf(0.0)
        assertTrue { areSameRings(rect, polygon.getOuterShell(), tolerance) }
        val holes = polygon.getHoles()
        assertEquals(1, holes.size)
        assertTrue { areSameRings(triangle, holes[0], tolerance) }
    }

    @Test
    fun cropPolygonIntoNothing() {
        val rect = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 1.0), p(1.0, 0.0))
        val cropRect = BlockPos.HorPointRect(p(5.0, 5.0), p(6.0, 6.0))

        val polygon = Polygon(outerShellVertices = rect, holesVertices = emptyList())
        val croppedList = polygon.crop(cropRect)

        assertTrue { croppedList.isEmpty() }
    }

    @Test
    fun cropRectangleInHalfByExactBounds() {
        val rect = listOf(p(0.0, 0.0), p(0.0, 1.0), p(1.0, 1.0), p(1.0, 0.0))
        val cropRectLeft = BlockPos.HorPointRect(p(0.0, 0.0), p(0.5, 1.0))
        val cropRectRight = BlockPos.HorPointRect(p(0.5, 0.0), p(1.0, 1.0))
        val expectedLeft = listOf(p(0.0, 0.0), p(0.0, 1.0), p(0.5, 1.0), p(0.5, 0.0))
        val expectedRight = listOf(p(0.5, 0.0), p(0.5, 1.0), p(1.0, 1.0), p(1.0, 0.0))

        val polygon = Polygon(outerShellVertices = rect, holesVertices = emptyList())
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
        val cropRect = BlockPos.HorPointRect(p(0.0, 0.0), p(1.0, 1.0))
        val expectedOne = listOf(p(0.0, 0.0), p(0.0, 0.6875), p(0.3, 0.5), p(0.25, 0.0))
        val expectedTwo = listOf(p(0.36, 0.0), p(0.68, 1.0), p(1.0, 1.0), p(1.0, 0.0))

        val polygon = Polygon(outerShellVertices = lake, holesVertices = emptyList())
        val croppedList = polygon.crop(cropRect)

        assertEquals(2, croppedList.size)
        croppedList.forEach { assertTrue { it.getHoles().isEmpty() } }
        val shells = croppedList.map { it.getOuterShell() }
        val err = BigDecimal.valueOf(0.0000001)
        assertTrue {
            (areSameRings(expectedOne, shells[0], err) && areSameRings(expectedTwo, shells[1], err)) ||
                    (areSameRings(expectedOne, shells[1], err) && areSameRings(expectedTwo, shells[0], err))
        }
    }

    @Test
    fun rasterizeSinglePixelSquare() {
        /**
         *   (0, 0) ------- (1, 0)
         *      |              |
         *      |              |
         *      |              |
         *   (0, 1) ------- (1, 1)
         */
        val shell = listOf(p(0.0, 0.0), p(1.0, 0.0), p(1.0, 1.0), p(0.0, 1.0))
        val polygon = Polygon(outerShellVertices = shell, holesVertices = emptyList())
        val rasterized = polygon.createRasterMask(HorPosRect(HorPos(-1, -1), HorPos(3, 3)), false)
        /**
         *    -1 0 1 2
         * -1  . . . .
         *  0  . X X .
         *  1  . X X .
         *  2  . . . .
         */
        assertArrayEquals(
            arrayOf(
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, true, false, false),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false)
            ),
            rasterized!!.maskZx
        )
    }

    @Test
    fun rasterizeTooSmallSquare() {
        val shell = listOf(p(0.0, 0.0), p(0.5, 0.0), p(0.5, 0.5), p(0.0, 0.5))
        val polygon = Polygon(outerShellVertices = shell, holesVertices = emptyList())
        val rasterized = polygon.createRasterMask(HorPosRect(HorPos(-1, -1), HorPos(3, 3)), false)
        assertArrayEquals(
            arrayOf(
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false)
            ),
            rasterized!!.maskZx
        )
    }

    @Test
    fun rasterize2x2Square() {
        val shell = listOf(p(0.99, 0.99), p(2.01, 0.99), p(2.01, 2.01), p(0.99, 2.01))
        val polygon = Polygon(outerShellVertices = shell, holesVertices = emptyList())
        val rasterized = polygon.createRasterMask(HorPosRect(HorPos(-1, -1), HorPos(4, 4)), false)
        assertArrayEquals(
            arrayOf(
                booleanArrayOf(false, false, false, false, false),
                booleanArrayOf(false, true, true, false, false),
                booleanArrayOf(false, true, true, false, false),
                booleanArrayOf(false, false, false, false, false),
                booleanArrayOf(false, false, false, false, false)
            ),
            rasterized!!.maskZx
        )
    }

    @Test
    fun rasterizeIntTriangles() {
        assertIntTriangleRasterization(
            p(2, 4), p(9, 3), p(9, 9),
            0, 2, 10, 10,
            arrayOf(
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 1, 1, 1, 1, 1, 1, 1, 0),
                L(0, 0, 0, 0, 1, 1, 1, 1, 1, 0),
                L(0, 0, 0, 0, 0, 1, 1, 1, 1, 0),
                L(0, 0, 0, 0, 0, 0, 0, 1, 1, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
        )
        assertIntTriangleRasterization(
            p(2, 4), p(7, 12), p(9, 9),
            0, 0, 10, 13,
            arrayOf(
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
                L(0, 0, 0, 0, 1, 1, 1, 0, 0, 0),
                L(0, 0, 0, 0, 0, 1, 1, 1, 0, 0),
                L(0, 0, 0, 0, 0, 0, 1, 1, 1, 0),
                L(0, 0, 0, 0, 0, 0, 1, 1, 1, 0),
                L(0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
                L(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
        )
        assertIntTriangleRasterization(
            p(0, 0), p(1, 0), p(1, 1),
            -1, -1, 2, 2,
            arrayOf(
                L(0, 0, 0),
                L(0, 1, 0),
                L(0, 0, 0)
            )
        )
    }

    @Test
    fun rasterizeIntQuadrilaterals() {
        assertIntQuadrilateralRasterization(
            p(2, 5), p(14, 13), p(12, 7), p(21, 4),
            1, 3, 22, 14,
            arrayOf(
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
                L(0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0),
                L(0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0),
                L(0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            )
        )
        assertIntQuadrilateralRasterization(
            p(20, 12), p(14, 13), p(12, 7), p(21, 4),
            1, 3, 22, 14,
            arrayOf(
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            )
        )
    }

    @Test
    fun rasterizeTriangleWithHole() {
        val shell = listOf(p(15, 10), p(3,13), p(6,2))
        val hole = listOf(p(10, 9), p(6, 10), p(8, 6))
        val polygon = Polygon(outerShellVertices = shell, holesVertices = listOf(hole))
        val rasterized = polygon.createRasterMask(canvas = HorPosRect(min = HorPos(x = 2, z = 1), max = HorPos(16, 14)), cropFirst = false)
        assertArrayEquals(
            arrayOf(
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,1,1,0,0,0,0,0,0,0,0),
                L(0,0,0,0,1,1,1,0,0,0,0,0,0,0),
                L(0,0,0,0,1,1,1,1,0,0,0,0,0,0),
                L(0,0,0,1,1,1,1,1,1,0,0,0,0,0),
                L(0,0,0,1,1,1,0,1,1,1,0,0,0,0),
                L(0,0,0,1,1,0,0,0,1,1,1,0,0,0),
                L(0,0,0,1,1,0,0,0,1,1,1,1,0,0),
                L(0,0,1,1,1,1,1,1,1,1,1,1,1,0),
                L(0,0,1,1,1,1,1,1,1,0,0,0,0,0),
                L(0,0,1,1,1,0,0,0,0,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            ),
            rasterized!!.maskZx
        )
    }

    @Test
    fun rasterizeOutOfBoundsTriangle() {
        val shell = listOf(p(15, 10), p(3,13), p(6,2))
        val polygon = Polygon(outerShellVertices = shell, holesVertices = emptyList())
        val rasterized = polygon.createRasterMask(canvas = HorPosRect(min = HorPos(x = 2, z = 5), max = HorPos(12, 14)), cropFirst = false)
        assertArrayEquals(
            arrayOf(
                L(0,0,0,0,1,1,1,1,0,0),
                L(0,0,0,1,1,1,1,1,1,0),
                L(0,0,0,1,1,1,1,1,1,1),
                L(0,0,0,1,1,1,1,1,1,1),
                L(0,0,0,1,1,1,1,1,1,1),
                L(0,0,1,1,1,1,1,1,1,1),
                L(0,0,1,1,1,1,1,1,1,0),
                L(0,0,1,1,1,0,0,0,0,0),
                L(0,0,0,0,0,0,0,0,0,0)
            ),
            rasterized!!.maskZx
        )
    }

    private fun assertIntTriangleRasterization(p1: BlockPos.HorPoint, p2: BlockPos.HorPoint, p3: BlockPos.HorPoint,
                                               minX: Int, minZ: Int, maxX: Int, maxZ: Int, expected: Array<BooleanArray>) {
        val shell = listOf(p1, p2, p3)
        val polygon = Polygon(outerShellVertices = shell, holesVertices = emptyList())
        val min = HorPos(x = minX, z = minZ)
        val rasterized = polygon.createRasterMask(canvas = HorPosRect(min = min, max = HorPos(maxX, maxZ)), cropFirst = false)
        assertEquals(min, rasterized!!.origin)
        assertArrayEquals(expected, rasterized.maskZx)
    }

    private fun assertIntQuadrilateralRasterization(p1: BlockPos.HorPoint, p2: BlockPos.HorPoint, p3: BlockPos.HorPoint, p4: BlockPos.HorPoint,
                                                    minX: Int, minZ: Int, maxX: Int, maxZ: Int, expected: Array<BooleanArray>) {
        val shell = listOf(p1, p2, p3, p4)
        val polygon = Polygon(outerShellVertices = shell, holesVertices = emptyList())
        val min = HorPos(x = minX, z = minZ)
        val rasterized = polygon.createRasterMask(canvas = HorPosRect(min = min, max = HorPos(maxX, maxZ)), cropFirst = false)
        assertEquals(min, rasterized!!.origin)
        assertArrayEquals(expected, rasterized.maskZx)
    }

    private fun p(x: Double, z: Double) = BlockPos.HorPoint(x = BigDecimal.valueOf(x), z = BigDecimal.valueOf(z))

    private fun p(x: Int, z: Int) = BlockPos.HorPoint(x = BigDecimal.valueOf(x.toDouble()), z = BigDecimal.valueOf(z.toDouble()))

    private fun L(vararg x: Int) = x.map {
        when (it) {
            0 -> false
            1 -> true
            else -> throw RuntimeException("Only 0 or 1 values accepted")
        }
    }.toBooleanArray()

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

    private fun equals(a: BigDecimal, b: BigDecimal, tolerance: BigDecimal) = a.minus(b).abs() <= tolerance

    private fun equals(a: BlockPos.HorPoint, b: BlockPos.HorPoint, tolerance: BigDecimal) =
        equals(a.x, b.x, tolerance) && equals(a.z, b.z, tolerance)

    private fun equals(a: List<BlockPos.HorPoint>, b: List<BlockPos.HorPoint>, tolerance: BigDecimal) =
        a.size == b.size && a.indices.all { equals(a[it], b[it], tolerance) }

}
