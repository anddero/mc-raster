package org.mcraster.world

import org.mcraster.model.BlockType
import org.mcraster.model.BlockPos

class WorldConfig(
    val worldName: String,
    val generator: GeneratorType,
    val layers: List<Layer>,
    val spawnPoint: BlockPos,
    val isGeneratingStructuresEnabled: Boolean,
    val gameType: GameType
) {

    class Layer(
        val blockType: BlockType,
        val yMin: Int,
        val yMax: Int
    )

    enum class GeneratorType { FLAT }

    enum class GameType { CREATIVE }

}
