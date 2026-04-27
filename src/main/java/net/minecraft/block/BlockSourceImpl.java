package net.minecraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSourceImpl implements IBlockSource {

	private final World world;
	private final BlockPos pos;

	public BlockSourceImpl(World worldIn, BlockPos posIn) {

		world = worldIn;
		pos = posIn;
	}

	public World getWorld() {

		return world;
	}

	public double x() {

		return (double) pos.getX() + 0.5D;
	}

	public double y() {

		return (double) pos.getY() + 0.5D;
	}

	public double z() {

		return (double) pos.getZ() + 0.5D;
	}

	public BlockPos getBlockPos() {

		return pos;
	}

	/**
	 * Gets the block state of this position and returns it.
	 *
	 * @return Block state in this position
	 */
	public IBlockState getBlockState() {

		return world.getBlockState(pos);
	}

	public <T extends TileEntity> T getBlockTileEntity() {

		return (T) world.getTileEntity(pos);
	}

}
