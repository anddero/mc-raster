package org.mcraster.converters

import org.mcraster.model.BlockPos
import org.mcraster.util.NumberUtils.floorToIntExact
import org.mcraster.util.NumberUtils.roundToIntExact
import java.math.BigDecimal

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
data class BlockPosLEst97(val x: Int, val y: Int, val h: Int) {

    fun toBlockPos(seaLevelBlockBottomY: Int) = BlockPos(
        x = y,
        y = h + seaLevelBlockBottomY + 1,
        z = -x - 1
    )

    data class PointLEst97(val x: BigDecimal, val y: BigDecimal, val h: BigDecimal) {

        fun toPoint(seaLevelBlockBottomY: Int) = BlockPos.Point(
            x = y,
            y = h + seaLevelBlockBottomY.toBigDecimal() + BigDecimal.ONE,
            z = -x
        )

        fun getBoundingBlock() =
            BlockPosLEst97(x = x.floorToIntExact(), y = y.floorToIntExact(), h = h.floorToIntExact())

        /**
         * Take the bounding block if the height is closer to the top of the block (>= .5).
         * Take the block below if the height is closer to the bottom of the block (< .5).
         */
        fun getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock() =
            BlockPosLEst97(x = x.floorToIntExact(), y = y.floorToIntExact(), h = h.roundToIntExact() - 1)

    }

    data class HorPointLEst97(val x: BigDecimal, val y: BigDecimal) {

        fun toHorPoint() = BlockPos.HorPoint(
            x = y,
            z = -x
        )

    }

    data class PointCubeLEst97(val min: PointLEst97, val max: PointLEst97) {

        init {
            if (min.x >= max.x || min.y >= max.y || min.h >= max.h) throw RuntimeException("$min >= $max")
        }

        fun toPointCube(seaLevelBlockBottomY: Int): BlockPos.PointCube {
            val convertedMin = min.toPoint(seaLevelBlockBottomY = seaLevelBlockBottomY)
            val convertedMax = max.toPoint(seaLevelBlockBottomY = seaLevelBlockBottomY)
            // z is a negated coordinate, so the order of precedence is reversed
            if (convertedMin.z <= convertedMax.z) throw RuntimeException("Expected Z to be negated after $this conversion, actual values are $convertedMin & $convertedMax")
            return BlockPos.PointCube(
                min = convertedMin.copy(z = convertedMax.z),
                max = convertedMax.copy(z = convertedMin.z)
            )
        }

    }

}
