package org.mcraster.generator;

import net.morbz.minecraft.blocks.DirtBlock;
import net.morbz.minecraft.blocks.IBlock;
import net.morbz.minecraft.blocks.Material;
import net.morbz.minecraft.blocks.SandBlock;
import net.morbz.minecraft.blocks.SimpleBlock;
import net.morbz.minecraft.blocks.StoneBlock;
import net.morbz.minecraft.level.FlatGenerator;
import net.morbz.minecraft.level.GameType;
import net.morbz.minecraft.level.IGenerator;
import net.morbz.minecraft.level.Level;
import net.morbz.minecraft.world.DefaultLayers;
import net.morbz.minecraft.world.World;
import org.mcraster.model.DiskBoundModel;
import org.mcraster.model.BlockType;

import java.io.IOException;
import java.util.Optional;

/*
J2Blocks tested and working with Minecraft versions: Release 1.10, 1.12, 1.12.2.
Not working with Minecraft Release 1.13 and many later versions, not all have been tested though.
*/
public class J2BlocksWorldGenerator {

    public static void generateToDisk(J2BlocksWorldConfig worldConfig, DiskBoundModel diskBoundModel) throws IOException {
        World world = createWorld(worldConfig);
        buildWorld(diskBoundModel, world);
        world.save();
    }

    private static World createWorld(J2BlocksWorldConfig worldConfig) {
        DefaultLayers layers = new DefaultLayers();
        if (worldConfig.layers().isEmpty()) throw new RuntimeException("No layers defined");
        worldConfig.layers().forEach(
                layer -> layers.setLayers(layer.getYMin(), layer.getYMax(), getMaterial(layer.getBlockType()))
        );
        IGenerator generator = switch (worldConfig.generator()) {
            case FLAT -> new FlatGenerator(layers);
        };
        Level level = new Level(worldConfig.worldName(), generator);
        level.setGameType(getGameType(worldConfig.gameType()));
        level.setSpawnPoint(
                worldConfig.spawnPos().getX(),
                worldConfig.spawnPos().getY(),
                worldConfig.spawnPos().getZ()
        );
        level.setMapFeatures(worldConfig.isGeneratingStructuresEnabled());

        return new World(level, layers);
    }

    private static void buildWorld(DiskBoundModel diskBoundModel, World world) {
        diskBoundModel.iterator().forEachRemaining(block ->
                getBlock(block.getType()).ifPresent(iBlock ->
                        world.setBlock(
                                block.getPos().getX(),
                                block.getPos().getY(),
                                block.getPos().getZ(),
                                iBlock
                        )
                ));
    }

    private static Optional<IBlock> getBlock(BlockType blockType) {
        return switch (blockType) {
            case NONE -> Optional.empty();
            case STONE -> Optional.of(StoneBlock.STONE);
            case SOIL -> Optional.of(DirtBlock.DIRT);
            case WATER -> Optional.of(SimpleBlock.WATER);
            case SOIL_WITH_GRASS -> Optional.of(SimpleBlock.GRASS);
            case SAND -> Optional.of(SandBlock.SAND);
            case GRAVEL -> Optional.of(SimpleBlock.GRAVEL);
            case GLASS -> Optional.of(SimpleBlock.GLASS);
            case AIR -> Optional.of(SimpleBlock.AIR);
            case UNBREAKABLE_STONE -> Optional.of(SimpleBlock.BEDROCK);
            default -> throw new UnsupportedOperationException("Unhandled block type: " + blockType);
        };
    }

    private static Material getMaterial(BlockType blockType) {
        return switch (blockType) {
            case UNBREAKABLE_STONE -> Material.BEDROCK;
            case STONE -> Material.STONE;
            case SOIL -> Material.DIRT;
            case WATER -> Material.WATER;
            default -> throw new UnsupportedOperationException("Unhandled material type: " + blockType);
        };
    }

    private static GameType getGameType(org.mcraster.builder.GameType gameType) {
        return switch (gameType) {
            case CREATIVE -> GameType.CREATIVE;
        };
    }

}
