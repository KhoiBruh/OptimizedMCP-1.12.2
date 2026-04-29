package net.minecraft.world.biome;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class BiomePlains extends Biome {

	protected boolean sunflowers;

	protected BiomePlains(boolean p_i46699_1_, Biome.BiomeProperties properties) {

		super(properties);
		sunflowers = p_i46699_1_;
		spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 5, 2, 6));
		spawnableCreatureList.add(new Biome.SpawnListEntry(EntityDonkey.class, 1, 1, 3));
		decorator.treesPerChunk = 0;
		decorator.extraTreeChance = 0.05F;
		decorator.flowersPerChunk = 4;
		decorator.grassPerChunk = 10;
	}

	public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos) {

		double d0 = GRASS_COLOR_NOISE.getValue((double) pos.getX() / 200D, (double) pos.getZ() / 200D);

		if (d0 < -0.8D) {
			int j = rand.nextInt(4);

			return switch (j) {
				case 0 -> BlockFlower.EnumFlowerType.ORANGE_TULIP;
				case 1 -> BlockFlower.EnumFlowerType.RED_TULIP;
				case 2 -> BlockFlower.EnumFlowerType.PINK_TULIP;
				default -> BlockFlower.EnumFlowerType.WHITE_TULIP;
			};
		} else if (rand.nextInt(3) > 0) {
			int i = rand.nextInt(3);

			if (i == 0) {
				return BlockFlower.EnumFlowerType.POPPY;
			} else {
				return i == 1 ? BlockFlower.EnumFlowerType.HOUSTONIA : BlockFlower.EnumFlowerType.OXEYE_DAISY;
			}
		} else {
			return BlockFlower.EnumFlowerType.DANDELION;
		}
	}

	public void decorate(World worldIn, Random rand, BlockPos pos) {

		double d0 = GRASS_COLOR_NOISE.getValue((double) (pos.getX() + 8) / 200D, (double) (pos.getZ() + 8) / 200D);

		if (d0 < -0.8D) {
			decorator.flowersPerChunk = 15;
			decorator.grassPerChunk = 5;
		} else {
			decorator.flowersPerChunk = 4;
			decorator.grassPerChunk = 10;
			DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);

			for (int i = 0; i < 7; ++i) {
				int j = rand.nextInt(16) + 8;
				int k = rand.nextInt(16) + 8;
				int l = rand.nextInt(worldIn.getHeight(pos.add(j, 0, k)).getY() + 32);
				DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j, l, k));
			}
		}

		if (sunflowers) {
			DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SUNFLOWER);

			for (int i1 = 0; i1 < 10; ++i1) {
				int j1 = rand.nextInt(16) + 8;
				int k1 = rand.nextInt(16) + 8;
				int l1 = rand.nextInt(worldIn.getHeight(pos.add(j1, 0, k1)).getY() + 32);
				DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j1, l1, k1));
			}
		}

		super.decorate(worldIn, rand, pos);
	}

	public WorldGenAbstractTree getRandomTreeFeature(Random rand) {

		return rand.nextInt(3) == 0 ? BIG_TREE_FEATURE : TREE_FEATURE;
	}

}
