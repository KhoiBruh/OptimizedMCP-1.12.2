package net.minecraft.world.biome;

import net.minecraft.init.Blocks;

public class BiomeBeach extends Biome {

	public BiomeBeach(Biome.BiomeProperties properties) {

		super(properties);
		spawnableCreatureList.clear();
		topBlock = Blocks.SAND.getDefaultState();
		fillerBlock = Blocks.SAND.getDefaultState();
		decorator.treesPerChunk = -999;
		decorator.deadBushPerChunk = 0;
		decorator.reedsPerChunk = 0;
		decorator.cactiPerChunk = 0;
	}

}
