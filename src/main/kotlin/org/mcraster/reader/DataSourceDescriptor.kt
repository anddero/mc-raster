package org.mcraster.reader

import org.mcraster.model.BlockPos
import org.mcraster.model.BlockPos.Cube
import org.mcraster.model.Limits.isWithinLimits
import org.mcraster.reader.DataSourceDescriptor.DataFormat.LINES_LEST97_YXH_DOUBLE
import org.mcraster.reader.DataSourceDescriptor.DataFormat.OBJ_3D
import org.mcraster.reader.DataSourceDescriptor.DataFormat.POLYGON_SHP_LEST97_YX
import org.mcraster.reader.Lest97Reader.linesLest97YxhDecimal
import org.mcraster.util.DataSource
import java.io.File

class DataSourceDescriptor(
    private val path: String,
    private val type: DataSourceType,
    private val format: DataFormat,
    private val softValidateBlockLimits: Boolean,
    private val pointConversionStrategy: PointConversionStrategy? = null,
    private val seaLevelBlockBottomY: Int? = null,
    private val worldLimits: Cube? = null, // if unset, all passed in data is rendered, otherwise both endpoints included
    private val blockTransform: ((Sequence<BlockPos>) -> Sequence<BlockPos>)? = null
) {

    fun asDataSource(): DataSource<BlockPos> {
        val source: File = when (type) {
            DataSourceType.RELATIVE_FILE -> File(path)
        }
        var data: DataSource<BlockPos> = when(format) {
            OBJ_3D -> Obj3dReader.readFile(source)
                .limited(worldLimits)
            LINES_LEST97_YXH_DOUBLE -> linesLest97YxhDecimal(source, pointConversionStrategy!!, seaLevelBlockBottomY!!)
                .limited(worldLimits)
            POLYGON_SHP_LEST97_YX -> ShapefileReader.readPolygonsFromShpFileLEstYx(source, pointConversionStrategy!!, seaLevelBlockBottomY!!, worldLimits!!.toHorPosRect())
        }
        blockTransform?.let { data = data.transform(it) }
        if (softValidateBlockLimits) {
            data = data.onEach(::softValidateBlockLimit)
        }
        return data
    }

    private fun DataSource<BlockPos>.limited(limits: Cube?): DataSource<BlockPos> {
        if (limits == null) return this
        return this.filter { limits.contains(it) }
    }

    enum class DataSourceType {
        RELATIVE_FILE
    }

    enum class DataFormat {
        OBJ_3D,
        LINES_LEST97_YXH_DOUBLE,
        POLYGON_SHP_LEST97_YX
    }

    enum class PointConversionStrategy {
        BOUNDING_BLOCK,
        BOUNDING_HORIZONTALLY_BUT_VERTICALLY_ROUNDED_TOWARDS_TOP_OF_BLOCK,
        VERTICALLY_FIXED_AT_SEA_LEVEL
    }

    companion object {
        private fun softValidateBlockLimit(blockPos: BlockPos) {
            if (!blockPos.isWithinLimits()) {
                System.err.println("Converted block not within valid range: $blockPos")
            }
        }
    }

}
