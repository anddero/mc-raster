package org.mcraster.reader

import org.mcraster.util.CoordinateUtils.lest97XyhToMc

object Lest97Reader {

    fun readLest97YxhToMc(
        spaceSeparatedYxh: Sequence<String>,
        customLest97XyhFilter: (Triple<Int, Int, Int>) -> Boolean = { true }
    ): Sequence<Array<Int>> {
        val lest97xyhDoubles = readLest97YxhToXyh(spaceSeparatedYxh)
        return lest97XyhToMc(lest97xyhDoubles = lest97xyhDoubles, customLest97XyhFilter = customLest97XyhFilter)
    }

    fun readLest97YxhToXyh(spaceSeparatedYxh: Sequence<String>): Sequence<Array<Double>> {
        val lest97yxhDoubles = spaceSeparatedYxh.map { yxhStr -> yxhStr.split(" ").map { it.toDouble() } }
        val lest97xyhDoubles = lest97yxhDoubles.map { lest97yxh -> arrayOf(lest97yxh[1], lest97yxh[0], lest97yxh[2]) }
        return lest97xyhDoubles
    }

}
