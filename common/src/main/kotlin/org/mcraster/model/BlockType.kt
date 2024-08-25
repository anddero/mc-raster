package org.mcraster.model

enum class BlockType(val value: Byte) {
    NONE(0),
    STONE(1),
    SOIL(2),
    WATER(3),
    WOOD(4),
    SOIL_WITH_GRASS(5),
    SAND(6),
    GRAVEL(7),
    GLASS(8),
    AIR(9),
    UNBREAKABLE_STONE(10);

    companion object {
        val MAX_BINARY_VALUE = entries.maxOf { it.value }
        val MIN_BINARY_VALUE = entries.minOf { it.value }

        operator fun get(index: Byte) = entries[index.toInt()]
    }
}
