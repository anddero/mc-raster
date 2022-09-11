package org.mcraster

import org.mcraster.editor.J2BlocksWorldGenerator
import org.mcraster.reader.Lest97Reader
import org.mcraster.util.FileUtils
import kotlin.math.roundToInt

object LocalTest {

    fun generateCustomArea1() {
        val lest97MinMaxFilters = Lest97Reader.readLest97YxhToXyh(
            FileUtils.getResource("customArea1/minMaxFilter.xyz").readLines().iterator().asSequence()
        ).iterator()
        val lest97MinFilter = lest97MinMaxFilters.next()
        val lest97MaxFilter = lest97MinMaxFilters.next()
        val spawn = Lest97Reader.readLest97YxhToMc(
            FileUtils.getResource("customArea1/spawn.xyz").readLines().iterator().asSequence()
        ).first()
        val poles = Lest97Reader.readLest97YxhToMc(
            FileUtils.getResource("customArea1/poles.xyz").readLines().iterator().asSequence()
        )
        val pools = Lest97Reader.readLest97YxhToMc(
            FileUtils.getResource("customArea1/pools.xyz").readLines().iterator().asSequence()
        )
        val lest97XfilterRange = lest97MinFilter[0].roundToInt()..lest97MaxFilter[0].roundToInt()
        val lest97YfilterRange = lest97MinFilter[1].roundToInt()..lest97MaxFilter[1].roundToInt()
        FileUtils.getResource("customArea1/heightmap.xyz").useLines { fileLines ->
            val landscape = Lest97Reader.readLest97YxhToMc(spaceSeparatedYxh = fileLines) { lest97xyh ->
                lest97xyh.first in lest97XfilterRange && lest97xyh.second in lest97YfilterRange
            }
            J2BlocksWorldGenerator.execute("TestTerrainCustomArea1", landscape, poles, pools, spawn, false)
        }
    }

}
