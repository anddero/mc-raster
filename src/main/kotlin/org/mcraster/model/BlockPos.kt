package org.mcraster.model

/**
 * In our model representation, these coordinates should be interpreted as follows:
 *      X increases towards East and decreases towards West.
 *      Y increases towards the sky and decreases towards the ground.
 *      Z increases towards South and decreases towards North.
 *
 * 1 unit corresponds to 1 meter in all the dimensions.
 *
 * If used for one cubic meter blocks, the point represents the lowest coordinate of the block in all dimensions.
 * This means, the point represents the lower North-West corner of the block.
 * For example, if the point of the block is (x = 5, y = 20, z = 10), then
 *      The West-facing side is at x = 5. The East-facing side is at x = 6. The center is x = 5.5.
 *      The ground-facing side is at y = 20. The sky-facing side is at y = 21. The center is y = 20.5.
 *      The North-facing side is at z = 10. The South-facing side is at z = 11. The center is z = 10.5.
 */
class BlockPos(val x: Int, val y: Int, val z: Int) {

    class MutableBlockPos(var x: Int, var y: Int, var z: Int)

    override fun toString() = "BlockPos(x=$x, y=$y, z=$z)"

}
