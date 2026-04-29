package net.minecraft.world.biome;

import net.minecraft.util.math.BlockPos;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BiomeProviderSingle extends BiomeProvider {

	/**
	 * The biome generator object.
	 */
	private final Biome biome;

	public BiomeProviderSingle(Biome biomeIn) {

		biome = biomeIn;
	}

	/**
	 * Returns the biome generator
	 */
	public Biome getBiome(BlockPos pos) {

		return biome;
	}

	/**
	 * Returns an array of biomes for the location input.
	 */
	public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {

		if (biomes == null || biomes.length < width * height) {
			biomes = new Biome[width * height];
		}

		Arrays.fill(biomes, 0, width * height, biome);
		return biomes;
	}

	/**
	 * Gets biomes to use for the blocks and loads the other data like temperature and humidity onto the
	 * WorldChunkManager.
	 */
	public Biome[] getBiomes(Biome[] oldBiomeList, int x, int z, int width, int depth) {

		if (oldBiomeList == null || oldBiomeList.length < width * depth) {
			oldBiomeList = new Biome[width * depth];
		}

		Arrays.fill(oldBiomeList, 0, width * depth, biome);
		return oldBiomeList;
	}

	/**
	 * Gets a list of biomes for the specified blocks.
	 */
	public Biome[] getBiomes(Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {

		return getBiomes(listToReuse, x, z, width, length);
	}

	
	public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {

		return biomes.contains(biome) ? new BlockPos(x - range + random.nextInt(range * 2 + 1), 0, z - range + random.nextInt(range * 2 + 1)) : null;
	}

	/**
	 * checks given Chunk's Biomes against List of allowed ones
	 */
	public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {

		return allowed.contains(biome);
	}

	public boolean isFixedBiome() {

		return true;
	}

	public Biome getFixedBiome() {

		return biome;
	}

}
