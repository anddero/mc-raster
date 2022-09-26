package org.mcraster.reader

import org.mcraster.converters.BlockPosLEst97.PointLEst97
import org.mcraster.model.BlockPos
import org.mcraster.reader.DataSourceDescriptor.PointConversionStrategy
import org.mcraster.util.DataSource
import org.mcraster.util.FileUtils.asLinesDataSource
import java.io.File

object Lest97Reader {

    fun linesLest97YxhDecimal(
        file: File,
        pointConversionStrategy: PointConversionStrategy,
        seaLevelBlockBottomY: Int
    ): DataSource<BlockPos> {
        val pointsLEst97 = file.asLinesDataSource()
            .map { yxhLine -> yxhLine.split(" ").map { it.toBigDecimal() } }
            .map { lest97yxh -> PointLEst97(lest97yxh[1], lest97yxh[0], lest97yxh[2]) }
        val blockPositionsLEst97 = when(pointConversionStrategy) {
            PointConversionStrategy.BOUNDING_BLOCK -> pointsLEst97.map { it.getBoundingBlock() }
            PointConversionStrategy.BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK ->
                pointsLEst97.map { it.getBoundingHorizontallyButVerticallyRoundedTowardsTopOfBlock() }
        }
        return blockPositionsLEst97
            .map { it.toBlockPos(seaLevelBlockBottomY = seaLevelBlockBottomY) }
    }

}
