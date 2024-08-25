package org.mcraster.reader

import org.mcraster.converters.BlockPosLEst97.PointLEst97
import org.mcraster.util.FileUtils.asLazyLines
import java.io.File

object Lest97Reader {

    fun lazyReadTextPointsLest97Yxh(file: File) = file.asLazyLines().transform { lines ->
        lines
            .map { yxhLine -> yxhLine.split(" ").map { it.toBigDecimal() } }
            .map { lest97yxh -> PointLEst97(x = lest97yxh[1], y = lest97yxh[0], h = lest97yxh[2]) }
    }

}
