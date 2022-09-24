package org.mcraster.converters

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
        assertEquals(74, posB.y)
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
    }

}
