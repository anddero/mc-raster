package org.mcraster.reader

import org.mcraster.converters.PointLEst97
import org.mcraster.model.BlockPos
import org.mcraster.util.DataSource
import org.mcraster.util.FileUtils.asLinesDataSource
import java.io.File

object Lest97Reader {

    fun linesLest97YxhDouble(
        file: File,
        conversionStrategy: DataSourceDescriptor.PointConverter,
        seaLevelBlockBottomY: Int
    ): DataSource<BlockPos> {
        val pointsLEst97 = file.asLinesDataSource()
            .map { yxhLine -> yxhLine.split(" ").map { it.toDouble() } }
            .map { lest97yxh -> PointLEst97(lest97yxh[1], lest97yxh[0], lest97yxh[2]) }
        val blockPositionsLEst97 = when(conversionStrategy) {
            DataSourceDescriptor.PointConverter.BOUNDING_BLOCK -> pointsLEst97.map { it.getBoundingBlock() }
        }
        return blockPositionsLEst97
            .map { it.toBlockPos(seaLevelBlockBottomY = seaLevelBlockBottomY) }
    }

}
