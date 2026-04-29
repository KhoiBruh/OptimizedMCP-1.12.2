package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLever extends Block {

	public static final PropertyEnum<BlockLever.EnumOrientation> FACING = PropertyEnum.create("facing", BlockLever.EnumOrientation.class);
	public static final PropertyBool POWERED = PropertyBool.create("powered");
	protected static final AxisAlignedBB LEVER_NORTH_AABB = new AxisAlignedBB(0.3125D, 0.20000000298023224D, 0.625D, 0.6875D, 0.800000011920929D, 1.0D);
	protected static final AxisAlignedBB LEVER_SOUTH_AABB = new AxisAlignedBB(0.3125D, 0.20000000298023224D, 0.0D, 0.6875D, 0.800000011920929D, 0.375D);
	protected static final AxisAlignedBB LEVER_WEST_AABB = new AxisAlignedBB(0.625D, 0.20000000298023224D, 0.3125D, 1.0D, 0.800000011920929D, 0.6875D);
	protected static final AxisAlignedBB LEVER_EAST_AABB = new AxisAlignedBB(0.0D, 0.20000000298023224D, 0.3125D, 0.375D, 0.800000011920929D, 0.6875D);
	protected static final AxisAlignedBB LEVER_UP_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.6000000238418579D, 0.75D);
	protected static final AxisAlignedBB LEVER_DOWN_AABB = new AxisAlignedBB(0.25D, 0.4000000059604645D, 0.25D, 0.75D, 1.0D, 0.75D);

	protected BlockLever() {

		super(Material.CIRCUITS);
		setDefaultState(blockState.getBaseState().withProperty(FACING, BlockLever.EnumOrientation.NORTH).withProperty(POWERED, false));
		setCreativeTab(CreativeTabs.REDSTONE);
	}

	protected static boolean canAttachTo(World worldIn, BlockPos p_181090_1_, EnumFacing p_181090_2_) {

		return BlockButton.canPlaceBlock(worldIn, p_181090_1_, p_181090_2_);
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

		return canAttachTo(worldIn, pos, side);
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		for (EnumFacing enumfacing : EnumFacing.values()) {
			if (canAttachTo(worldIn, pos, enumfacing)) {
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

		IBlockState iblockstate = getDefaultState().withProperty(POWERED, false);

		if (canAttachTo(worldIn, pos, facing)) {
			return iblockstate.withProperty(FACING, BlockLever.EnumOrientation.forFacings(facing, placer.getHorizontalFacing()));
		} else {
			for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
				if (enumfacing != facing && canAttachTo(worldIn, pos, enumfacing)) {
					return iblockstate.withProperty(FACING, BlockLever.EnumOrientation.forFacings(enumfacing, placer.getHorizontalFacing()));
				}
			}

			if (worldIn.getBlockState(pos.down()).isTopSolid()) {
				return iblockstate.withProperty(FACING, BlockLever.EnumOrientation.forFacings(EnumFacing.UP, placer.getHorizontalFacing()));
			} else {
				return iblockstate;
			}
		}
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (checkCanSurvive(worldIn, pos, state) && !canAttachTo(worldIn, pos, state.getValue(FACING).getFacing())) {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}

	private boolean checkCanSurvive(World worldIn, BlockPos pos, IBlockState state) {

		if (canPlaceBlockAt(worldIn, pos)) {
			return true;
		} else {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return false;
		}
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return switch (state.getValue(FACING)) {
			case WEST -> LEVER_WEST_AABB;
			case SOUTH -> LEVER_SOUTH_AABB;
			case NORTH -> LEVER_NORTH_AABB;
			case UP_Z, UP_X -> LEVER_UP_AABB;
			case DOWN_X, DOWN_Z -> LEVER_DOWN_AABB;
			default -> LEVER_EAST_AABB;
		};
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (worldIn.isRemote) {
			return true;
		} else {
			state = state.cycleProperty(POWERED);
			worldIn.setBlockState(pos, state, 3);
			float f = state.getValue(POWERED) ? 0.6F : 0.5F;
			worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
			worldIn.notifyNeighborsOfStateChange(pos, this, false);
			EnumFacing enumfacing = state.getValue(FACING).getFacing();
			worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing.getOpposite()), this, false);
			return true;
		}
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (state.getValue(POWERED)) {
			worldIn.notifyNeighborsOfStateChange(pos, this, false);
			EnumFacing enumfacing = state.getValue(FACING).getFacing();
			worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing.getOpposite()), this, false);
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
			return blockState.getValue(FACING).getFacing() == side ? 15 : 0;
		}
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 */
	public boolean canProvidePower(IBlockState state) {

		return true;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(FACING, BlockLever.EnumOrientation.byMetadata(meta & 7)).withProperty(POWERED, (meta & 8) > 0);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;
		i = i | state.getValue(FACING).getMetadata();

		if (state.getValue(POWERED)) {
			i |= 8;
		}

		return i;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return switch (rot) {
			case CLOCKWISE_180 -> switch (state.getValue(FACING)) {
				case EAST -> state.withProperty(FACING, EnumOrientation.WEST);
				case WEST -> state.withProperty(FACING, EnumOrientation.EAST);
				case SOUTH -> state.withProperty(FACING, EnumOrientation.NORTH);
				case NORTH -> state.withProperty(FACING, EnumOrientation.SOUTH);
				default -> state;
			};
			case COUNTERCLOCKWISE_90 -> switch (state.getValue(FACING)) {
				case EAST -> state.withProperty(FACING, EnumOrientation.NORTH);
				case WEST -> state.withProperty(FACING, EnumOrientation.SOUTH);
				case SOUTH -> state.withProperty(FACING, EnumOrientation.EAST);
				case NORTH -> state.withProperty(FACING, EnumOrientation.WEST);
				case UP_Z -> state.withProperty(FACING, EnumOrientation.UP_X);
				case UP_X -> state.withProperty(FACING, EnumOrientation.UP_Z);
				case DOWN_X -> state.withProperty(FACING, EnumOrientation.DOWN_Z);
				case DOWN_Z -> state.withProperty(FACING, EnumOrientation.DOWN_X);
			};
			case CLOCKWISE_90 -> switch (state.getValue(FACING)) {
				case EAST -> state.withProperty(FACING, EnumOrientation.SOUTH);
				case WEST -> state.withProperty(FACING, EnumOrientation.NORTH);
				case SOUTH -> state.withProperty(FACING, EnumOrientation.WEST);
				case NORTH -> state.withProperty(FACING, EnumOrientation.EAST);
				case UP_Z -> state.withProperty(FACING, EnumOrientation.UP_X);
				case UP_X -> state.withProperty(FACING, EnumOrientation.UP_Z);
				case DOWN_X -> state.withProperty(FACING, EnumOrientation.DOWN_Z);
				case DOWN_Z -> state.withProperty(FACING, EnumOrientation.DOWN_X);
			};
			default -> state;
		};
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {

		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING).getFacing()));
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, FACING, POWERED);
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

	public enum EnumOrientation implements IStringSerializable {
		DOWN_X(0, "down_x", EnumFacing.DOWN),
		EAST(1, "east", EnumFacing.EAST),
		WEST(2, "west", EnumFacing.WEST),
		SOUTH(3, "south", EnumFacing.SOUTH),
		NORTH(4, "north", EnumFacing.NORTH),
		UP_Z(5, "up_z", EnumFacing.UP),
		UP_X(6, "up_x", EnumFacing.UP),
		DOWN_Z(7, "down_z", EnumFacing.DOWN);

		private static final BlockLever.EnumOrientation[] META_LOOKUP = new BlockLever.EnumOrientation[values().length];

		static {
			for (BlockLever.EnumOrientation blocklever$enumorientation : values()) {
				META_LOOKUP[blocklever$enumorientation.getMetadata()] = blocklever$enumorientation;
			}
		}

		private final int meta;
		private final String name;
		private final EnumFacing facing;

		EnumOrientation(int meta, String name, EnumFacing facing) {

			this.meta = meta;
			this.name = name;
			this.facing = facing;
		}

		public static BlockLever.EnumOrientation byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public static BlockLever.EnumOrientation forFacings(EnumFacing clickedSide, EnumFacing entityFacing) {

			return switch (clickedSide) {
				case DOWN -> switch (entityFacing.getAxis()) {
					case X -> DOWN_X;
					case Z -> DOWN_Z;
					default ->
							throw new IllegalArgumentException("Invalid entityFacing " + entityFacing + " for facing " + clickedSide);
				};
				case UP -> switch (entityFacing.getAxis()) {
					case X -> UP_X;
					case Z -> UP_Z;
					default ->
							throw new IllegalArgumentException("Invalid entityFacing " + entityFacing + " for facing " + clickedSide);
				};
				case NORTH -> NORTH;
				case SOUTH -> SOUTH;
				case WEST -> WEST;
				case EAST -> EAST;
				default -> throw new IllegalArgumentException("Invalid facing: " + clickedSide);
			};
		}

		public int getMetadata() {

			return meta;
		}

		public EnumFacing getFacing() {

			return facing;
		}

		public String toString() {

			return name;
		}

		public String getName() {

			return name;
		}
	}

}
