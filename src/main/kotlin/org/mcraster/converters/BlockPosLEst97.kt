package org.mcraster.converters

import org.mcraster.model.BlockPos
import kotlin.math.roundToInt

/**
 * X and Y stand for coordinates from the L-EST97 system, H stands for height from sea level (bottom -1, top 0).
 * In the L-EST97 coordinate system, X increases towards North, Y increases towards East.
 *
 * In terms of one cubic meter blocks, the values represent the lowest points of the block in all dimensions, that
 * is, the bottom South-West corner of the block.
 * For example, if the position of a block would be (x = 3, y = 8, h = 20), then:
 *      The South-facing side would be x = 3, but the North-facing side x = 4. Center x = 3.5.
 *      The West-facing side would be y = 8, but the East-facing side y = 9. Center y = 8.5.
 *      The ground-facing side would be h = 20, but the sky-facing side h = 21. Center h = 20.5.
 */
class BlockPosLEst97(val x: Int, val y: Int, val h: Int) {

    fun toBlockPos(seaLevelBlockBottomY: Int) = BlockPos(
        x = y,
        y = h + seaLevelBlockBottomY + 1,
        z = -x - 1
    )

    companion object {
        fun fromPointOnBlock(x: Double, y: Double, h: Double) =
            BlockPosLEst97(x = x.toInt(), y = y.toInt(), h = h.toInt())
        fun fromPointOnBlockWithClosestRealisticHeight(x: Double, y: Double, h: Double) =
            BlockPosLEst97(x = x.toInt(), y = y.toInt(), h = h.roundToInt() - 1)
    }

}
