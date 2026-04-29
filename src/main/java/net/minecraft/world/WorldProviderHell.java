package net.minecraft.world;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderHell extends WorldProvider {

	/**
	 * Creates a new {@link BiomeProvider} for the WorldProvider, and also sets the values of {@link #hasSkylight} and
	 * {@link #hasNoSky} appropriately.
	 * <p>
	 * Note that subclasses generally override this method without calling the parent version.
	 */
	public void init() {

		biomeProvider = new BiomeProviderSingle(Biomes.HELL);
		doesWaterVaporize = true;
		nether = true;
	}

	/**
	 * Return Vec3D with biome specific fog color
	 */
	public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {

		return new Vec3d(0.20000000298023224D, 0.029999999329447746D, 0.029999999329447746D);
	}

	/**
	 * Creates the light to brightness table
	 */
	protected void generateLightBrightnessTable() {

		float f = 0.1F;

		for (int i = 0; i <= 15; ++i) {
			float f1 = 1F - (float) i / 15F;
			lightBrightnessTable[i] = (1F - f1) / (f1 * 3F + 1F) * 0.9F + 0.1F;
		}
	}

	public IChunkGenerator createChunkGenerator() {

		return new ChunkGeneratorHell(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed());
	}

	/**
	 * Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
	 */
	public boolean isSurfaceWorld() {

		return false;
	}

	/**
	 * Will check if the x, z position specified is alright to be set as the map spawn point
	 */
	public boolean canCoordinateBeSpawn(int x, int z) {

		return false;
	}

	/**
	 * Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
	 */
	public float calculateCelestialAngle(long worldTime, float partialTicks) {

		return 0.5F;
	}

	/**
	 * True if the player can respawn in this dimension (true = overworld, false = nether).
	 */
	public boolean canRespawnHere() {

		return false;
	}

	/**
	 * Returns true if the given X,Z coordinate should show environmental fog.
	 */
	public boolean doesXZShowFog(int x, int z) {

		return true;
	}

	public WorldBorder createWorldBorder() {

		return new WorldBorder() {
			public double getCenterX() {

				return super.getCenterX() / 8D;
			}

			public double getCenterZ() {

				return super.getCenterZ() / 8D;
			}
		};
	}

	public DimensionType getDimensionType() {

		return DimensionType.NETHER;
	}

}
