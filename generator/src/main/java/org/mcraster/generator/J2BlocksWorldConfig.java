package org.mcraster.generator;

import org.mcraster.builder.GameType;
import org.mcraster.builder.GeneratorType;
import org.mcraster.builder.Layer;
import org.mcraster.pos.BlockPos;

import java.util.List;

public record J2BlocksWorldConfig(
        String worldName,
        GeneratorType generator,
        List<Layer> layers,
        boolean isGeneratingStructuresEnabled,
        GameType gameType,
        BlockPos spawnPos
) {

}
