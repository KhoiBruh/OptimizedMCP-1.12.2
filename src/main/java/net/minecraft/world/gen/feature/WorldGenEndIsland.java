package net.minecraft.world.gen.feature;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Maths;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenEndIsland extends WorldGenerator {

	public boolean generate(World worldIn, Random rand, BlockPos position) {
		float f = (float) (rand.nextInt(3) + 4);

		for (int i = 0; f > 0.5F; --i) {
			for (int j = Maths.floor(-f); j <= Maths.ceil(f); ++j) {
				for (int k = Maths.floor(-f); k <= Maths.ceil(f); ++k) {
					if ((float) (j * j + k * k) <= (f + 1F) * (f + 1F)) {
						setBlockAndNotifyAdequately(worldIn, position.add(j, i, k), Blocks.END_STONE.getDefaultState());
					}
				}
			}

			f = (float) ((double) f - ((double) rand.nextInt(2) + 0.5D));
		}

		return true;
	}

}
