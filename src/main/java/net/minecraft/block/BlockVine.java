package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockVine extends Block {

	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool[] ALL_FACES = new PropertyBool[]{UP, NORTH, SOUTH, WEST, EAST};
	protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0625D, 1.0D, 1.0D);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.9375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.0625D);
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.9375D, 1.0D, 1.0D, 1.0D);

	public BlockVine() {

		super(Material.VINE);
		setDefaultState(blockState.getBaseState().withProperty(UP, Boolean.FALSE).withProperty(NORTH, Boolean.FALSE).withProperty(EAST, Boolean.FALSE).withProperty(SOUTH, Boolean.FALSE).withProperty(WEST, Boolean.FALSE));
		setTickRandomly(true);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	protected static boolean isExceptBlockForAttaching(Block p_193397_0_) {

		return p_193397_0_ instanceof BlockShulkerBox || p_193397_0_ == Blocks.BEACON || p_193397_0_ == Blocks.CAULDRON || p_193397_0_ == Blocks.GLASS || p_193397_0_ == Blocks.STAINED_GLASS || p_193397_0_ == Blocks.PISTON || p_193397_0_ == Blocks.STICKY_PISTON || p_193397_0_ == Blocks.PISTON_HEAD || p_193397_0_ == Blocks.TRAPDOOR;
	}

	public static PropertyBool getPropertyFor(EnumFacing side) {

		return switch (side) {
			case UP -> UP;
			case NORTH -> NORTH;
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> throw new IllegalArgumentException(side + " is an invalid choice");
		};
	}

	public static int getNumGrownFaces(IBlockState state) {

		int i = 0;

		for (PropertyBool propertybool : ALL_FACES) {
			if (state.getValue(propertybool)) {
				++i;
			}
		}

		return i;
	}

	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {

		return NULL_AABB;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		state = state.getActualState(source, pos);
		int i = 0;
		AxisAlignedBB axisalignedbb = FULL_BLOCK_AABB;

		if (state.getValue(UP)) {
			axisalignedbb = UP_AABB;
			++i;
		}

		if (state.getValue(NORTH)) {
			axisalignedbb = NORTH_AABB;
			++i;
		}

		if (state.getValue(EAST)) {
			axisalignedbb = EAST_AABB;
			++i;
		}

		if (state.getValue(SOUTH)) {
			axisalignedbb = SOUTH_AABB;
			++i;
		}

		if (state.getValue(WEST)) {
			axisalignedbb = WEST_AABB;
			++i;
		}

		return i == 1 ? axisalignedbb : FULL_BLOCK_AABB;
	}

	/**
	 * Get the actual Block state of this Block at the given position. This applies properties not visible in the
	 * metadata, such as fence connections.
	 */
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		BlockPos blockpos = pos.up();
		return state.withProperty(UP, worldIn.getBlockState(blockpos).getBlockFaceShape(worldIn, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID);
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
	 * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
	 */
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {

		return true;
	}

	/**
	 * Check whether this Block can be placed at pos, while aiming at the specified side of an adjacent block
	 */
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {

		return side != EnumFacing.DOWN && side != EnumFacing.UP && canAttachTo(worldIn, pos, side);
	}

	public boolean canAttachTo(World p_193395_1_, BlockPos p_193395_2_, EnumFacing p_193395_3_) {

		Block block = p_193395_1_.getBlockState(p_193395_2_.up()).getBlock();
		return isAcceptableNeighbor(p_193395_1_, p_193395_2_.offset(p_193395_3_.getOpposite()), p_193395_3_) && (block == Blocks.AIR || block == Blocks.VINE || isAcceptableNeighbor(p_193395_1_, p_193395_2_.up(), EnumFacing.UP));
	}

	private boolean isAcceptableNeighbor(World p_193396_1_, BlockPos p_193396_2_, EnumFacing p_193396_3_) {

		IBlockState iblockstate = p_193396_1_.getBlockState(p_193396_2_);
		return iblockstate.getBlockFaceShape(p_193396_1_, p_193396_2_, p_193396_3_) == BlockFaceShape.SOLID && !isExceptBlockForAttaching(iblockstate.getBlock());
	}

	private boolean recheckGrownSides(World worldIn, BlockPos pos, IBlockState state) {

		IBlockState iblockstate = state;

		for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
			PropertyBool propertybool = getPropertyFor(enumfacing);

			if (state.getValue(propertybool) && !canAttachTo(worldIn, pos, enumfacing.getOpposite())) {
				IBlockState iblockstate1 = worldIn.getBlockState(pos.up());

				if (iblockstate1.getBlock() != this || !iblockstate1.getValue(propertybool)) {
					state = state.withProperty(propertybool, Boolean.FALSE);
				}
			}
		}

		if (getNumGrownFaces(state) == 0) {
			return false;
		} else {
			if (iblockstate != state) {
				worldIn.setBlockState(pos, state, 2);
			}

			return true;
		}
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (!worldIn.isRemote && !recheckGrownSides(worldIn, pos, state)) {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		if (!worldIn.isRemote) {
			if (worldIn.rand.nextInt(4) == 0) {
				int i = 4;
				int j = 5;
				boolean flag = false;
				label181:

				for (int k = -4; k <= 4; ++k) {
					for (int l = -4; l <= 4; ++l) {
						for (int i1 = -1; i1 <= 1; ++i1) {
							if (worldIn.getBlockState(pos.add(k, i1, l)).getBlock() == this) {
								--j;

								if (j <= 0) {
									flag = true;
									break label181;
								}
							}
						}
					}
				}

				EnumFacing enumfacing1 = EnumFacing.random(rand);
				BlockPos blockpos2 = pos.up();

				if (enumfacing1 == EnumFacing.UP && pos.getY() < 255 && worldIn.isAirBlock(blockpos2)) {
					IBlockState iblockstate2 = state;

					for (EnumFacing enumfacing2 : EnumFacing.Plane.HORIZONTAL) {
						if (rand.nextBoolean() && canAttachTo(worldIn, blockpos2, enumfacing2.getOpposite())) {
							iblockstate2 = iblockstate2.withProperty(getPropertyFor(enumfacing2), Boolean.TRUE);
						} else {
							iblockstate2 = iblockstate2.withProperty(getPropertyFor(enumfacing2), Boolean.FALSE);
						}
					}

					if (iblockstate2.getValue(NORTH) || iblockstate2.getValue(EAST) || iblockstate2.getValue(SOUTH) || iblockstate2.getValue(WEST)) {
						worldIn.setBlockState(blockpos2, iblockstate2, 2);
					}
				} else if (enumfacing1.getAxis().isHorizontal() && !state.getValue(getPropertyFor(enumfacing1))) {
					if (!flag) {
						BlockPos blockpos4 = pos.offset(enumfacing1);
						IBlockState iblockstate3 = worldIn.getBlockState(blockpos4);
						Block block1 = iblockstate3.getBlock();

						if (block1.blockMaterial == Material.AIR) {
							EnumFacing enumfacing3 = enumfacing1.rotateY();
							EnumFacing enumfacing4 = enumfacing1.rotateYCCW();
							boolean flag1 = state.getValue(getPropertyFor(enumfacing3));
							boolean flag2 = state.getValue(getPropertyFor(enumfacing4));
							BlockPos blockpos = blockpos4.offset(enumfacing3);
							BlockPos blockpos1 = blockpos4.offset(enumfacing4);

							if (flag1 && canAttachTo(worldIn, blockpos.offset(enumfacing3), enumfacing3)) {
								worldIn.setBlockState(blockpos4, getDefaultState().withProperty(getPropertyFor(enumfacing3), Boolean.TRUE), 2);
							} else if (flag2 && canAttachTo(worldIn, blockpos1.offset(enumfacing4), enumfacing4)) {
								worldIn.setBlockState(blockpos4, getDefaultState().withProperty(getPropertyFor(enumfacing4), Boolean.TRUE), 2);
							} else if (flag1 && worldIn.isAirBlock(blockpos) && canAttachTo(worldIn, blockpos, enumfacing1)) {
								worldIn.setBlockState(blockpos, getDefaultState().withProperty(getPropertyFor(enumfacing1.getOpposite()), Boolean.TRUE), 2);
							} else if (flag2 && worldIn.isAirBlock(blockpos1) && canAttachTo(worldIn, blockpos1, enumfacing1)) {
								worldIn.setBlockState(blockpos1, getDefaultState().withProperty(getPropertyFor(enumfacing1.getOpposite()), Boolean.TRUE), 2);
							}
						} else if (iblockstate3.getBlockFaceShape(worldIn, blockpos4, enumfacing1) == BlockFaceShape.SOLID) {
							worldIn.setBlockState(pos, state.withProperty(getPropertyFor(enumfacing1), Boolean.TRUE), 2);
						}
					}
				} else {
					if (pos.getY() > 1) {
						BlockPos blockpos3 = pos.down();
						IBlockState iblockstate = worldIn.getBlockState(blockpos3);
						Block block = iblockstate.getBlock();

						if (block.blockMaterial == Material.AIR) {
							IBlockState iblockstate1 = state;

							for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
								if (rand.nextBoolean()) {
									iblockstate1 = iblockstate1.withProperty(getPropertyFor(enumfacing), Boolean.FALSE);
								}
							}

							if (iblockstate1.getValue(NORTH) || iblockstate1.getValue(EAST) || iblockstate1.getValue(SOUTH) || iblockstate1.getValue(WEST)) {
								worldIn.setBlockState(blockpos3, iblockstate1, 2);
							}
						} else if (block == this) {
							IBlockState iblockstate4 = iblockstate;

							for (EnumFacing enumfacing5 : EnumFacing.Plane.HORIZONTAL) {
								PropertyBool propertybool = getPropertyFor(enumfacing5);

								if (rand.nextBoolean() && state.getValue(propertybool)) {
									iblockstate4 = iblockstate4.withProperty(propertybool, Boolean.TRUE);
								}
							}

							if (iblockstate4.getValue(NORTH) || iblockstate4.getValue(EAST) || iblockstate4.getValue(SOUTH) || iblockstate4.getValue(WEST)) {
								worldIn.setBlockState(blockpos3, iblockstate4, 2);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		IBlockState iblockstate = getDefaultState().withProperty(UP, Boolean.FALSE).withProperty(NORTH, Boolean.FALSE).withProperty(EAST, Boolean.FALSE).withProperty(SOUTH, Boolean.FALSE).withProperty(WEST, Boolean.FALSE);
		return facing.getAxis().isHorizontal() ? iblockstate.withProperty(getPropertyFor(facing.getOpposite()), Boolean.TRUE) : iblockstate;
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Items.AIR;
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return 0;
	}

	/**
	 * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
	 * Block.removedByPlayer
	 */
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {

		if (!worldIn.isRemote && stack.getItem() == Items.SHEARS) {
			player.addStat(StatList.getBlockStats(this));
			spawnAsEntity(worldIn, pos, new ItemStack(Blocks.VINE, 1, 0));
		} else {
			super.harvestBlock(worldIn, player, pos, state, te, stack);
		}
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(SOUTH, (meta & 1) > 0).withProperty(WEST, (meta & 2) > 0).withProperty(NORTH, (meta & 4) > 0).withProperty(EAST, (meta & 8) > 0);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;

		if (state.getValue(SOUTH)) {
			i |= 1;
		}

		if (state.getValue(WEST)) {
			i |= 2;
		}

		if (state.getValue(NORTH)) {
			i |= 4;
		}

		if (state.getValue(EAST)) {
			i |= 8;
		}

		return i;
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, UP, NORTH, EAST, SOUTH, WEST);
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return switch (rot) {
			case CLOCKWISE_180 ->
					state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH)).withProperty(WEST, state.getValue(EAST));
			case COUNTERCLOCKWISE_90 ->
					state.withProperty(NORTH, state.getValue(EAST)).withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST)).withProperty(WEST, state.getValue(NORTH));
			case CLOCKWISE_90 ->
					state.withProperty(NORTH, state.getValue(WEST)).withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST)).withProperty(WEST, state.getValue(SOUTH));
			default -> state;
		};
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {

		return switch (mirrorIn) {
			case LEFT_RIGHT ->
					state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
			case FRONT_BACK -> state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
			default -> super.withMirror(state, mirrorIn);
		};
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
