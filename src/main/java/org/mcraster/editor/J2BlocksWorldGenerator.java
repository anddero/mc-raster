package org.mcraster.editor;

import kotlin.sequences.Sequence;
import net.morbz.minecraft.blocks.DirtBlock;
import net.morbz.minecraft.blocks.Material;
import net.morbz.minecraft.blocks.SimpleBlock;
import net.morbz.minecraft.blocks.StoneBlock;
import net.morbz.minecraft.level.FlatGenerator;
import net.morbz.minecraft.level.GameType;
import net.morbz.minecraft.level.IGenerator;
import net.morbz.minecraft.level.Level;
import net.morbz.minecraft.world.DefaultLayers;
import net.morbz.minecraft.world.World;

import java.io.IOException;

import static org.mcraster.util.CoordinateUtils.Y_DIRT_LEVEL;
import static org.mcraster.util.CoordinateUtils.Y_MIN;
import static org.mcraster.util.CoordinateUtils.Y_SEA_LEVEL;
import static org.mcraster.util.CoordinateUtils.Y_STONE_LEVEL;

/*
J2Blocks tested and working with Minecraft versions: Release 1.10, 1.12, 1.12.2.
Not working with Minecraft Release 1.13 and many later versions, not all have been tested though.
*/
public class J2BlocksWorldGenerator {

	public static void execute(String worldName,
							   Sequence<Integer[]> xyzLandscape,
							   Sequence<Integer[]> markerPoles,
							   Sequence<Integer[]> waterPools,
							   Integer[] spawn,
							   boolean createIslandUnderSpawn) throws IOException {

		DefaultLayers layers = new DefaultLayers();
		layers.setLayer(Y_MIN, Material.BEDROCK);
		layers.setLayers(Y_MIN + 1, Y_STONE_LEVEL, Material.STONE);
		layers.setLayers(Y_STONE_LEVEL + 1, Y_DIRT_LEVEL, Material.DIRT);
		layers.setLayers(Y_DIRT_LEVEL + 1, Y_SEA_LEVEL, Material.WATER);

		IGenerator generator = new FlatGenerator(layers);

		Level level = new Level(worldName, generator);
		level.setGameType(GameType.CREATIVE);
		level.setSpawnPoint(spawn[0], spawn[1], spawn[2]);
		level.setMapFeatures(false); // Disable generating structures

		World world = new World(level, layers);

		xyzLandscape.iterator().forEachRemaining(xyz -> {
			for (int y = Y_DIRT_LEVEL + 1; y < xyz[1]; ++y) {
				world.setBlock(xyz[0], y, xyz[2], DirtBlock.DIRT);
			}
		});

		markerPoles.iterator().forEachRemaining(xyz -> {
			for (int y = Y_DIRT_LEVEL + 1; y < xyz[1]; ++y) {
				world.setBlock(xyz[0], y, xyz[2], StoneBlock.STONE);
			}
		});

		waterPools.iterator().forEachRemaining(xyz -> {
			int poolWidth = 10;
			int poolHeight = 5;
			for(int x = -poolWidth/2; x < poolWidth/2; x++) {
				for(int z = -poolWidth/2; z < poolWidth/2; z++) {
					for (int y = xyz[1]; y > xyz[1] - poolHeight; --y) {
						world.setBlock(xyz[0] + x, y, xyz[2] + z, SimpleBlock.WATER);
					}
				}
			}
		});

		if (createIslandUnderSpawn) {
			int spawnIslandWidth = 50;
			int spawnIslandHeight = 5;
			for(int x = -spawnIslandWidth/2; x < spawnIslandWidth/2; x++) {
				for(int z = -spawnIslandWidth/2; z < spawnIslandWidth/2; z++) {
					for (int y = Y_SEA_LEVEL; y > Y_SEA_LEVEL - spawnIslandHeight; --y) {
						world.setBlock(spawn[0] + x, y, spawn[2] + z, SimpleBlock.GRASS);
					}
				}
			}
		}

		world.save();
	}

}
