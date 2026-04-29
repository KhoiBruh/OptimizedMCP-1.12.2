package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockRedstoneTorch extends BlockTorch {

	private static final Map<World, List<BlockRedstoneTorch.Toggle>> toggles = Maps.newHashMap();
	private final boolean isOn;

	protected BlockRedstoneTorch(boolean isOn) {

		this.isOn = isOn;
		setTickRandomly(true);
		setCreativeTab(null);
	}

	private boolean isBurnedOut(World worldIn, BlockPos pos, boolean turnOff) {

		if (!toggles.containsKey(worldIn)) {
			toggles.put(worldIn, Lists.newArrayList());
		}

		List<BlockRedstoneTorch.Toggle> list = toggles.get(worldIn);

		if (turnOff) {
			list.add(new BlockRedstoneTorch.Toggle(pos, worldIn.getTotalWorldTime()));
		}

		int i = 0;

		for (Toggle blockredstonetorch$toggle : list) {
			if (blockredstonetorch$toggle.pos.equals(pos)) {
				++i;

				if (i >= 8) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * How many world ticks before ticking
	 */
	public int tickRate(World worldIn) {

		return 2;
	}

	/**
	 * Called after the block is set in the Chunk data, but before the Tile Entity is set
	 */
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {

		if (isOn) {
			for (Facing enumfacing : Facing.values()) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
			}
		}
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (isOn) {
			for (Facing enumfacing : Facing.values()) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
			}
		}
	}

	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Facing side) {

		return isOn && blockState.getValue(FACING) != side ? 15 : 0;
	}

	private boolean shouldBeOff(World worldIn, BlockPos pos, IBlockState state) {

		Facing enumfacing = state.getValue(FACING).getOpposite();
		return worldIn.isSidePowered(pos.offset(enumfacing), enumfacing);
	}

	/**
	 * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
	 */
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {

	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		boolean flag = shouldBeOff(worldIn, pos, state);
		List<BlockRedstoneTorch.Toggle> list = toggles.get(worldIn);

		while (list != null && !list.isEmpty() && worldIn.getTotalWorldTime() - (list.getFirst()).time > 60L) {
			list.removeFirst();
		}

		if (isOn) {
			if (flag) {
				worldIn.setBlockState(pos, Blocks.UNLIT_REDSTONE_TORCH.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);

				if (isBurnedOut(worldIn, pos, true)) {
					worldIn.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

					for (int i = 0; i < 5; ++i) {
						double d0 = (double) pos.getX() + rand.nextDouble() * 0.6D + 0.2D;
						double d1 = (double) pos.getY() + rand.nextDouble() * 0.6D + 0.2D;
						double d2 = (double) pos.getZ() + rand.nextDouble() * 0.6D + 0.2D;
						worldIn.spawnParticle(ParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0D, 0D, 0D);
					}

					worldIn.scheduleUpdate(pos, worldIn.getBlockState(pos).getBlock(), 160);
				}
			}
		} else if (!flag && !isBurnedOut(worldIn, pos, false)) {
			worldIn.setBlockState(pos, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
		}
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (!onNeighborChangeInternal(worldIn, pos, state)) {
			if (isOn == shouldBeOff(worldIn, pos, state)) {
				worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
			}
		}
	}

	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Facing side) {

		return side == Facing.DOWN ? blockState.getWeakPower(blockAccess, pos, side) : 0;
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 */
	public boolean canProvidePower(IBlockState state) {

		return true;
	}

	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {

		if (isOn) {
			double d0 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			Facing enumfacing = stateIn.getValue(FACING);

			if (enumfacing.getAxis().isHorizontal()) {
				Facing enumfacing1 = enumfacing.getOpposite();
				double d3 = 0.27D;
				d0 += 0.27D * (double) enumfacing1.getFrontOffsetX();
				d1 += 0.22D;
				d2 += 0.27D * (double) enumfacing1.getFrontOffsetZ();
			}

			worldIn.spawnParticle(ParticleTypes.REDSTONE, d0, d1, d2, 0D, 0D, 0D);
		}
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {

		return new ItemStack(Blocks.REDSTONE_TORCH);
	}

	public boolean isAssociatedBlock(Block other) {

		return other == Blocks.UNLIT_REDSTONE_TORCH || other == Blocks.REDSTONE_TORCH;
	}

	static class Toggle {

		BlockPos pos;
		long time;

		public Toggle(BlockPos pos, long time) {

			this.pos = pos;
			this.time = time;
		}

	}

}
