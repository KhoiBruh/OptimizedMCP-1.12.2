package net.minecraft.world.biome;

import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;

public class BiomeMushroomIsland extends Biome {

	public BiomeMushroomIsland(Biome.BiomeProperties properties) {

		super(properties);
		decorator.treesPerChunk = -100;
		decorator.flowersPerChunk = -100;
		decorator.grassPerChunk = -100;
		decorator.mushroomsPerChunk = 1;
		decorator.bigMushroomsPerChunk = 1;
		topBlock = Blocks.MYCELIUM.getDefaultState();
		spawnableMonsterList.clear();
		spawnableCreatureList.clear();
		spawnableWaterCreatureList.clear();
		spawnableCreatureList.add(new Biome.SpawnListEntry(EntityMooshroom.class, 8, 4, 8));
	}

}
