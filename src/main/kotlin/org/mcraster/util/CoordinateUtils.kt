package org.mcraster.util

import kotlin.math.roundToInt

/**
 * This configuration is based on the combination of limitations imposed by J2Blocks and Minecraft Release 1.12.2.
 * Using different world editing software or building for a different Minecraft version may or may not work properly.
 * In that case, it could also be possible to increase the ranges of many of these limits.
 */
object CoordinateUtils {
    const val Y_MIN = 0 // bedrock level
    const val Y_STONE_LEVEL = 4
    const val Y_DIRT_LEVEL = 7
    const val Y_SEA_LEVEL = 62
    const val Y_MAX = 255 // inclusive

    // Circumference of Earth is 40'075'017m around equator and 40'007'863m around poles. Below are min and max block coordinates (both ends inclusive).
    const val X_MIN = -20_037_508
    const val X_MAX = 20_037_508
    const val Z_MIN = -20_003_931
    const val Z_MAX = 20_003_931

    /**
     * Take coordinates in L-EST97 system, where h stands for height from sea level 0.
     * Return an array of 3 coordinates X,Y,Z of the MC world (sea level configured).
     * In the L-EST97 coordinate system, X increases towards north, Y increases towards east.
     * In our preferred view of the MC world, X increases towards east, Z increases towards south.
     * So the conversion would look as follows: L-EST97 X becomes MC negative Z and L-EST97 Y becomes MC X.
     */
    fun lest97ToMc(x: Int, y: Int, h: Int): Triple<Int, Int, Int> {
        val mcX = y
        val mcY = h + Y_SEA_LEVEL
        val mcZ = -x
        if (!mcXInRange(mcX) || !mcYInRange(mcY) || !mcZInRange(mcZ)) {
            System.err.println("Converted MC coordinate not in valid range: XYZ $mcX, $mcY, $mcZ")
        }
        return Triple(mcX, mcY, mcZ)
    }

    fun lest97XyhToMc(
        lest97xyhDoubles: Sequence<Array<Double>>,
        customLest97XyhFilter: (Triple<Int, Int, Int>) -> Boolean = { true }
    ) = lest97xyhDoubles
        .map { lest97xyh -> lest97xyh.map { it.roundToInt() } }
        .map { lest97xyh -> Triple(lest97xyh[0], lest97xyh[1], lest97xyh[2]) }.filter(customLest97XyhFilter)
        .map { lest97xyh ->
            lest97ToMc(x = lest97xyh.first, y = lest97xyh.second, h = lest97xyh.third)
                .toList()
                .toTypedArray()
        }

    private fun mcXInRange(x: Int) = x in X_MIN..X_MAX

    private fun mcYInRange(y: Int) = y in Y_MIN..Y_MAX

    private fun mcZInRange(z: Int) = z in Z_MIN..Z_MAX

}
