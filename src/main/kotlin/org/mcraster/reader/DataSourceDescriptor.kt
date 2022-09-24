package org.mcraster.reader

import org.mcraster.model.BlockPos
import org.mcraster.model.Limits.isWithinLimits
import org.mcraster.reader.Lest97Reader.linesLest97YxhDouble
import org.mcraster.util.DataSource
import org.mcraster.util.FileUtils
import java.io.File

class DataSourceDescriptor(
    private val path: String,
    private val type: DataSourceType,
    private val format: DataFormat,
    private val pointConversionStrategy: PointConversionStrategy,
    private val seaLevelBlockBottomY: Int,
    private val softValidateBlockLimits: Boolean,
    private val blockFilter: ((BlockPos) -> Boolean)? = null
) {

    fun asDataSource(): DataSource<BlockPos> {
        val source: File = when(type) {
            DataSourceType.RESOURCE_FILE -> FileUtils.getResource(path)
        }
        var data: DataSource<BlockPos> = when(format) {
            DataFormat.LINES_LEST97_YXH_DOUBLE ->
                linesLest97YxhDouble(source, pointConversionStrategy, seaLevelBlockBottomY)
        }
        blockFilter?.let { data = data.filter(it) }
        if (softValidateBlockLimits) {
            data = data.onEach(::softValidateBlockLimit)
        }
        return data
    }

    enum class DataSourceType {
        RESOURCE_FILE
    }

    enum class DataFormat {
        LINES_LEST97_YXH_DOUBLE
    }

    enum class PointConversionStrategy {
        BOUNDING_BLOCK
    }

    companion object {
        private fun softValidateBlockLimit(blockPos: BlockPos) {
            if (!blockPos.isWithinLimits()) {
                System.err.println("Converted block not within valid range: $blockPos")
            }
        }
    }

}
