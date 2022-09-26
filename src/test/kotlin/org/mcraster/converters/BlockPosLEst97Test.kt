package org.mcraster.converters

import org.mcraster.converters.BlockPosLEst97.PointLEst97
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BlockPosLEst97Test {

    @Test
    fun toBlockPos() {
        var posL = BlockPosLEst97(x = -2, y = 3, h = -1)
        var posB = posL.toBlockPos(-1)
        assertEquals(3, posB.x)
        assertEquals(-1, posB.y)
        assertEquals(1, posB.z)

        posL = BlockPosLEst97(x = 2, y = 2, h = -1)
        posB = posL.toBlockPos(0)
        assertEquals(2, posB.x)
        assertEquals(0, posB.y)
        assertEquals(-3, posB.z)

        posL = BlockPosLEst97(x = 0, y = -2, h = 0)
        posB = posL.toBlockPos(-1)
        assertEquals(-2, posB.x)
        assertEquals(0, posB.y)
        assertEquals(-1, posB.z)

        posL = BlockPosLEst97(x = 0, y = 0, h = -1)
        posB = posL.toBlockPos(62)
        assertEquals(0, posB.x)
        assertEquals(62, posB.y)
        assertEquals(-1, posB.z)

        posL = BlockPosLEst97(x = 1, y = 0, h = 10)
        posB = posL.toBlockPos(62)
        assertEquals(0, posB.x)
        assertEquals(73, posB.y)
        assertEquals(-2, posB.z)

        posL = BlockPosLEst97(x = 1, y = 1, h = -20)
        posB = posL.toBlockPos(30)
        assertEquals(1, posB.x)
        assertEquals(11, posB.y)
        assertEquals(-2, posB.z)

        posL = BlockPosLEst97(x = 1, y = 1, h = -990)
        posB = posL.toBlockPos(30)
        assertEquals(1, posB.x)
        assertEquals(-959, posB.y)
        assertEquals(-2, posB.z)

        posL = BlockPosLEst97(x = 6473214, y = 660057, h = 37)
        posB = posL.toBlockPos(30)
        assertEquals(660057, posB.x)
        assertEquals(68, posB.y)
        assertEquals(-6473215, posB.z)
    }

    @Test
    fun fromPointOnBlock() {
        var p = PointLEst97(x = "6473214.5".toBigDecimal(), y = "660060.5".toBigDecimal(), h = "37.712".toBigDecimal())
        var b = p.getBoundingBlock()
        assertEquals(6473214, b.x)
        assertEquals(660060, b.y)
        assertEquals(37, b.h)

        p = PointLEst97(x = "647321.999".toBigDecimal(), y = "6600608.001".toBigDecimal(), h = "66.0".toBigDecimal())
        b = p.getBoundingBlock()
        assertEquals(647321, b.x)
        assertEquals(6600608, b.y)
        assertEquals(66, b.h)

        p = PointLEst97(x = "1".toBigDecimal(), y = "0".toBigDecimal(), h = "-1".toBigDecimal())
        b = p.getBoundingBlock()
        assertEquals(1, b.x)
        assertEquals(0, b.y)
        assertEquals(-1, b.h)

        p = PointLEst97(x = "-128734.231".toBigDecimal(), y = "-2873.93".toBigDecimal(), h = "-192.5".toBigDecimal())
        b = p.getBoundingBlock()
        assertEquals(-128735, b.x)
        assertEquals(-2874, b.y)
        assertEquals(-193, b.h)
    }

    @Test
    fun fromPointOnBlockWithHeightRealisticRounded() {
        var p = PointLEst97(x = "6473214.5".toBigDecimal(), y =  "660057.5".toBigDecimal(), h = "37.704".toBigDecimal())
        var b = p.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
        assertEquals(6473214, b.x)
        assertEquals(660057, b.y)
        assertEquals(37, b.h)

        p = PointLEst97(x = "6473214.5".toBigDecimal(), y = "660060.5".toBigDecimal(), h = "37.712".toBigDecimal())
        b = p.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
        assertEquals(6473214, b.x)
        assertEquals(660060, b.y)
        assertEquals(37, b.h)

        p = PointLEst97(x = "647321.999".toBigDecimal(), y = "6600608.001".toBigDecimal(), h = "66.0".toBigDecimal())
        b = p.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
        assertEquals(647321, b.x)
        assertEquals(6600608, b.y)
        assertEquals(65, b.h)

        p = PointLEst97(x = "1".toBigDecimal(), y = "0".toBigDecimal(), h = "-1.1".toBigDecimal())
        b = p.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
        assertEquals(1, b.x)
        assertEquals(0, b.y)
        assertEquals(-2, b.h)

        p = PointLEst97(x = "-128734.231".toBigDecimal(), y = "-2873.93".toBigDecimal(), h = "-192.5".toBigDecimal())
        b = p.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock()
        assertEquals(-128735, b.x)
        assertEquals(-2874, b.y)
        assertEquals(-193, b.h)
    }

}
