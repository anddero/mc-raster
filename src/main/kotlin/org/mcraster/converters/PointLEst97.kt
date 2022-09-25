package org.mcraster.converters

import java.math.BigDecimal

/**
 * X and Y stand for coordinates from the L-EST97 system, H stands for height from sea level (in meters).
 * In the L-EST97 coordinate system, X increases towards North, Y increases towards East.
 */
class PointLEst97(val x: BigDecimal, val y: BigDecimal, val h: BigDecimal) {

    fun getBoundingBlock() = BlockPosLEst97.fromPointOnBlock(x = x, y = y, h = h)

    fun getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock() =
        BlockPosLEst97.fromPointOnBlockWithClosestRealisticHeight(x = x, y = y, h = h)

}
