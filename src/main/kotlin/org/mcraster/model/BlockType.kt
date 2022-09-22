package org.mcraster.model

enum class BlockType(val binaryValue: Byte) {
    NONE(0),
    STONE(1),
    DIRT(2),
    WATER(3),
    WOOD(4),
    GRASS(5),
    SAND(6),
    GRAVEL(7),
    GLASS(8),
    AIR(9);

    companion object {
        private val BLOCK_TYPES = BlockType.values()

        val MAX_BINARY_VALUE = BLOCK_TYPES.maxOf { it.binaryValue }
        val MIN_BINARY_VALUE = BLOCK_TYPES.minOf { it.binaryValue }

        operator fun get(index: Byte) = BLOCK_TYPES[index.toInt()]
    }
}
