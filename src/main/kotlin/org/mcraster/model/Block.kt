package org.mcraster.model

data class Block(
    val x: Int,
    val z: Int,
    val y: Int,
    val type: BlockType
)
