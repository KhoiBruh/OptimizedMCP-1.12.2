package net.minecraft.world.gen.feature;

import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.Facing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenVines extends WorldGenerator {

	public boolean generate(World worldIn, Random rand, BlockPos position) {

		for (; position.getY() < 128; position = position.up()) {
			if (worldIn.isAirBlock(position)) {
				for (Facing enumfacing : Facing.Plane.HORIZONTAL.facings()) {
					if (Blocks.VINE.canPlaceBlockOnSide(worldIn, position, enumfacing)) {
						IBlockState iblockstate = Blocks.VINE.getDefaultState().withProperty(BlockVine.NORTH, enumfacing == Facing.NORTH).withProperty(BlockVine.EAST, enumfacing == Facing.EAST).withProperty(BlockVine.SOUTH, enumfacing == Facing.SOUTH).withProperty(BlockVine.WEST, enumfacing == Facing.WEST);
						worldIn.setBlockState(position, iblockstate, 2);
						break;
					}
				}
			} else {
				position = position.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));
			}
		}

		return true;
	}

}
