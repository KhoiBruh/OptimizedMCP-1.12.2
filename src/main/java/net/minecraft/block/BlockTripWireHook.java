package net.minecraft.block;

import com.google.common.base.MoreObjects;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockTripWireHook extends Block {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool POWERED = PropertyBool.create("powered");
	public static final PropertyBool ATTACHED = PropertyBool.create("attached");
	protected static final AxisAlignedBB HOOK_NORTH_AABB = new AxisAlignedBB(0.3125D, 0D, 0.625D, 0.6875D, 0.625D, 1D);
	protected static final AxisAlignedBB HOOK_SOUTH_AABB = new AxisAlignedBB(0.3125D, 0D, 0D, 0.6875D, 0.625D, 0.375D);
	protected static final AxisAlignedBB HOOK_WEST_AABB = new AxisAlignedBB(0.625D, 0D, 0.3125D, 1D, 0.625D, 0.6875D);
	protected static final AxisAlignedBB HOOK_EAST_AABB = new AxisAlignedBB(0D, 0D, 0.3125D, 0.375D, 0.625D, 0.6875D);

	public BlockTripWireHook() {

		super(Material.CIRCUITS);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, false).withProperty(ATTACHED, false));
		setCreativeTab(CreativeTabs.REDSTONE);
		setTickRandomly(true);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return switch (state.getValue(FACING)) {
			case WEST -> HOOK_WEST_AABB;
			case SOUTH -> HOOK_SOUTH_AABB;
			case NORTH -> HOOK_NORTH_AABB;
			default -> HOOK_EAST_AABB;
		};
	}

	
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {

		return NULL_AABB;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	public boolean isFullCube(IBlockState state) {

		return false;
	}

	/**
	 * Check whether this Block can be placed at pos, while aiming at the specified side of an adjacent block
	 */
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {

		EnumFacing enumfacing = side.getOpposite();
		BlockPos blockpos = pos.offset(enumfacing);
		IBlockState iblockstate = worldIn.getBlockState(blockpos);
		boolean flag = isExceptBlockForAttachWithPiston(iblockstate.getBlock());
		return !flag && side.getAxis().isHorizontal() && iblockstate.getBlockFaceShape(worldIn, blockpos, side) == BlockFaceShape.SOLID && !iblockstate.canProvidePower();
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
			if (canPlaceBlockOnSide(worldIn, pos, enumfacing)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		IBlockState iblockstate = getDefaultState().withProperty(POWERED, false).withProperty(ATTACHED, false);

		if (facing.getAxis().isHorizontal()) {
			iblockstate = iblockstate.withProperty(FACING, facing);
		}

		return iblockstate;
	}

	/**
	 * Called by ItemBlocks after a block is set in the world, to allow post-place logic
	 */
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		calculateState(worldIn, pos, state, false, false, -1, null);
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (blockIn != this) {
			if (checkForDrop(worldIn, pos, state)) {
				EnumFacing enumfacing = state.getValue(FACING);

				if (!canPlaceBlockOnSide(worldIn, pos, enumfacing)) {
					dropBlockAsItem(worldIn, pos, state, 0);
					worldIn.setBlockToAir(pos);
				}
			}
		}
	}

	public void calculateState(World worldIn, BlockPos pos, IBlockState hookState, boolean p_176260_4_, boolean p_176260_5_, int p_176260_6_, IBlockState p_176260_7_) {

		EnumFacing enumfacing = hookState.getValue(FACING);
		boolean flag = hookState.getValue(ATTACHED);
		boolean flag1 = hookState.getValue(POWERED);
		boolean flag2 = !p_176260_4_;
		boolean flag3 = false;
		int i = 0;
		IBlockState[] aiblockstate = new IBlockState[42];

		for (int j = 1; j < 42; ++j) {
			BlockPos blockpos = pos.offset(enumfacing, j);
			IBlockState iblockstate = worldIn.getBlockState(blockpos);

			if (iblockstate.getBlock() == Blocks.TRIPWIRE_HOOK) {
				if (iblockstate.getValue(FACING) == enumfacing.getOpposite()) {
					i = j;
				}

				break;
			}

			if (iblockstate.getBlock() != Blocks.TRIPWIRE && j != p_176260_6_) {
				aiblockstate[j] = null;
				flag2 = false;
			} else {
				if (j == p_176260_6_) {
					iblockstate = MoreObjects.firstNonNull(p_176260_7_, iblockstate);
				}

				boolean flag4 = !iblockstate.getValue(BlockTripWire.DISARMED);
				boolean flag5 = iblockstate.getValue(BlockTripWire.POWERED);
				flag3 |= flag4 && flag5;
				aiblockstate[j] = iblockstate;

				if (j == p_176260_6_) {
					worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
					flag2 &= flag4;
				}
			}
		}

		flag2 = flag2 & i > 1;
		flag3 = flag3 & flag2;
		IBlockState iblockstate1 = getDefaultState().withProperty(ATTACHED, flag2).withProperty(POWERED, flag3);

		if (i > 0) {
			BlockPos blockpos1 = pos.offset(enumfacing, i);
			EnumFacing enumfacing1 = enumfacing.getOpposite();
			worldIn.setBlockState(blockpos1, iblockstate1.withProperty(FACING, enumfacing1), 3);
			notifyNeighbors(worldIn, blockpos1, enumfacing1);
			playSound(worldIn, blockpos1, flag2, flag3, flag, flag1);
		}

		playSound(worldIn, pos, flag2, flag3, flag, flag1);

		if (!p_176260_4_) {
			worldIn.setBlockState(pos, iblockstate1.withProperty(FACING, enumfacing), 3);

			if (p_176260_5_) {
				notifyNeighbors(worldIn, pos, enumfacing);
			}
		}

		if (flag != flag2) {
			for (int k = 1; k < i; ++k) {
				BlockPos blockpos2 = pos.offset(enumfacing, k);
				IBlockState iblockstate2 = aiblockstate[k];

				if (iblockstate2 != null && worldIn.getBlockState(blockpos2).getMaterial() != Material.AIR) {
					worldIn.setBlockState(blockpos2, iblockstate2.withProperty(ATTACHED, flag2), 3);
				}
			}
		}
	}

	/**
	 * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
	 */
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {

	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		calculateState(worldIn, pos, state, false, true, -1, null);
	}

	private void playSound(World worldIn, BlockPos pos, boolean p_180694_3_, boolean p_180694_4_, boolean p_180694_5_, boolean p_180694_6_) {

		if (p_180694_4_ && !p_180694_6_) {
			worldIn.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4F, 0.6F);
		} else if (!p_180694_4_ && p_180694_6_) {
			worldIn.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 0.5F);
		} else if (p_180694_3_ && !p_180694_5_) {
			worldIn.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4F, 0.7F);
		} else if (!p_180694_3_ && p_180694_5_) {
			worldIn.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4F, 1.2F / (worldIn.rand.nextFloat() * 0.2F + 0.9F));
		}
	}

	private void notifyNeighbors(World worldIn, BlockPos pos, EnumFacing side) {

		worldIn.notifyNeighborsOfStateChange(pos, this, false);
		worldIn.notifyNeighborsOfStateChange(pos.offset(side.getOpposite()), this, false);
	}

	private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {

		if (!canPlaceBlockAt(worldIn, pos)) {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		boolean flag = state.getValue(ATTACHED);
		boolean flag1 = state.getValue(POWERED);

		if (flag || flag1) {
			calculateState(worldIn, pos, state, true, false, -1, null);
		}

		if (flag1) {
			worldIn.notifyNeighborsOfStateChange(pos, this, false);
			worldIn.notifyNeighborsOfStateChange(pos.offset(state.getValue(FACING).getOpposite()), this, false);
		}

		super.breakBlock(worldIn, pos, state);
	}

	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {

		return blockState.getValue(POWERED) ? 15 : 0;
	}

	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {

		if (!blockState.getValue(POWERED)) {
			return 0;
		} else {
			return blockState.getValue(FACING) == side ? 15 : 0;
		}
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 */
	public boolean canProvidePower(IBlockState state) {

		return true;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(POWERED, (meta & 8) > 0).withProperty(ATTACHED, (meta & 4) > 0);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;
		i = i | state.getValue(FACING).getHorizontalIndex();

		if (state.getValue(POWERED)) {
			i |= 8;
		}

		if (state.getValue(ATTACHED)) {
			i |= 4;
		}

		return i;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {

		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, FACING, POWERED, ATTACHED);
	}

	/**
	 * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
	 * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
	 * <p>
	 * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
	 * does not fit the other descriptions and will generally cause other things not to connect to the face.
	 *
	 * @return an approximation of the form of the given face
	 */
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {

		return BlockFaceShape.UNDEFINED;
	}

}
