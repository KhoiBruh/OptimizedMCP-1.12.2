package net.minecraft.block;

import com.google.common.cache.LoadingCache;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockPortal extends BlockBreakable {

	public static final PropertyEnum<Facing.Axis> AXIS = PropertyEnum.create("axis", Facing.Axis.class, Facing.Axis.X, Facing.Axis.Z);
	protected static final AxisAlignedBB X_AABB = new AxisAlignedBB(0D, 0D, 0.375D, 1D, 1D, 0.625D);
	protected static final AxisAlignedBB Z_AABB = new AxisAlignedBB(0.375D, 0D, 0D, 0.625D, 1D, 1D);
	protected static final AxisAlignedBB Y_AABB = new AxisAlignedBB(0.375D, 0D, 0.375D, 0.625D, 1D, 0.625D);

	public BlockPortal() {

		super(Material.PORTAL, false);
		setDefaultState(blockState.getBaseState().withProperty(AXIS, Facing.Axis.X));
		setTickRandomly(true);
	}

	public static int getMetaForAxis(Facing.Axis axis) {

		if (axis == Facing.Axis.X) {
			return 1;
		} else {
			return axis == Facing.Axis.Z ? 2 : 0;
		}
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return switch (state.getValue(AXIS)) {
			case X -> X_AABB;
			case Z -> Z_AABB;
			default -> Y_AABB;
		};
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		super.updateTick(worldIn, pos, state, rand);

		if (worldIn.provider.isSurfaceWorld() && worldIn.getGameRules().getBoolean("doMobSpawning") && rand.nextInt(2000) < worldIn.getDifficulty().getDifficultyId()) {
			int i = pos.getY();
			BlockPos blockpos;

			for (blockpos = pos; !worldIn.getBlockState(blockpos).isTopSolid() && blockpos.getY() > 0; blockpos = blockpos.down()) {
			}

			if (i > 0 && !worldIn.getBlockState(blockpos.up()).isNormalCube()) {
				Entity entity = ItemMonsterPlacer.spawnCreature(worldIn, EntityList.getKey(EntityPigZombie.class), (double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 1.1D, (double) blockpos.getZ() + 0.5D);

				if (entity != null) {
					entity.timeUntilPortal = entity.getPortalCooldown();
				}
			}
		}
	}

	
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {

		return NULL_AABB;
	}

	public boolean isFullCube(IBlockState state) {

		return false;
	}

	public boolean trySpawnPortal(World worldIn, BlockPos pos) {

		BlockPortal.Size blockportal$size = new BlockPortal.Size(worldIn, pos, Facing.Axis.X);

		if (blockportal$size.isValid() && blockportal$size.portalBlockCount == 0) {
			blockportal$size.placePortalBlocks();
			return true;
		} else {
			BlockPortal.Size blockportal$size1 = new BlockPortal.Size(worldIn, pos, Facing.Axis.Z);

			if (blockportal$size1.isValid() && blockportal$size1.portalBlockCount == 0) {
				blockportal$size1.placePortalBlocks();
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		Facing.Axis enumfacing$axis = state.getValue(AXIS);

		if (enumfacing$axis == Facing.Axis.X) {
			BlockPortal.Size blockportal$size = new BlockPortal.Size(worldIn, pos, Facing.Axis.X);

			if (!blockportal$size.isValid() || blockportal$size.portalBlockCount < blockportal$size.width * blockportal$size.height) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		} else if (enumfacing$axis == Facing.Axis.Z) {
			BlockPortal.Size blockportal$size1 = new BlockPortal.Size(worldIn, pos, Facing.Axis.Z);

			if (!blockportal$size1.isValid() || blockportal$size1.portalBlockCount < blockportal$size1.width * blockportal$size1.height) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
	}

	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Facing side) {

		pos = pos.offset(side);
		Facing.Axis enumfacing$axis = null;

		if (blockState.getBlock() == this) {
			enumfacing$axis = blockState.getValue(AXIS);

			if (enumfacing$axis == null) {
				return false;
			}

			if (enumfacing$axis == Facing.Axis.Z && side != Facing.EAST && side != Facing.WEST) {
				return false;
			}

			if (enumfacing$axis == Facing.Axis.X && side != Facing.SOUTH && side != Facing.NORTH) {
				return false;
			}
		}

		boolean flag = blockAccess.getBlockState(pos.west()).getBlock() == this && blockAccess.getBlockState(pos.west(2)).getBlock() != this;
		boolean flag1 = blockAccess.getBlockState(pos.east()).getBlock() == this && blockAccess.getBlockState(pos.east(2)).getBlock() != this;
		boolean flag2 = blockAccess.getBlockState(pos.north()).getBlock() == this && blockAccess.getBlockState(pos.north(2)).getBlock() != this;
		boolean flag3 = blockAccess.getBlockState(pos.south()).getBlock() == this && blockAccess.getBlockState(pos.south(2)).getBlock() != this;
		boolean flag4 = flag || flag1 || enumfacing$axis == Facing.Axis.X;
		boolean flag5 = flag2 || flag3 || enumfacing$axis == Facing.Axis.Z;

		if (flag4 && side == Facing.WEST) {
			return true;
		} else if (flag4 && side == Facing.EAST) {
			return true;
		} else if (flag5 && side == Facing.NORTH) {
			return true;
		} else {
			return flag5 && side == Facing.SOUTH;
		}
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return 0;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.TRANSLUCENT;
	}

	/**
	 * Called When an Entity Collided with the Block
	 */
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {

		if (!entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn.isNonBoss()) {
			entityIn.setPortal(pos);
		}
	}

	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {

		if (rand.nextInt(100) == 0) {
			worldIn.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
		}

		for (int i = 0; i < 4; ++i) {
			double d0 = (float) pos.getX() + rand.nextFloat();
			double d1 = (float) pos.getY() + rand.nextFloat();
			double d2 = (float) pos.getZ() + rand.nextFloat();
			double d3 = ((double) rand.nextFloat() - 0.5D) * 0.5D;
			double d4 = ((double) rand.nextFloat() - 0.5D) * 0.5D;
			double d5 = ((double) rand.nextFloat() - 0.5D) * 0.5D;
			int j = rand.nextInt(2) * 2 - 1;

			if (worldIn.getBlockState(pos.west()).getBlock() != this && worldIn.getBlockState(pos.east()).getBlock() != this) {
				d0 = (double) pos.getX() + 0.5D + 0.25D * (double) j;
				d3 = rand.nextFloat() * 2F * (float) j;
			} else {
				d2 = (double) pos.getZ() + 0.5D + 0.25D * (double) j;
				d5 = rand.nextFloat() * 2F * (float) j;
			}

			worldIn.spawnParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
		}
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {

		return ItemStack.EMPTY;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(AXIS, (meta & 3) == 2 ? Facing.Axis.Z : Facing.Axis.X);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return getMetaForAxis(state.getValue(AXIS));
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return switch (rot) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
				case X -> state.withProperty(AXIS, Facing.Axis.Z);
				case Z -> state.withProperty(AXIS, Facing.Axis.X);
				default -> state;
			};
			default -> state;
		};
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, AXIS);
	}

	public BlockPattern.PatternHelper createPatternHelper(World worldIn, BlockPos p_181089_2_) {

		Facing.Axis enumfacing$axis = Facing.Axis.Z;
		BlockPortal.Size blockportal$size = new BlockPortal.Size(worldIn, p_181089_2_, Facing.Axis.X);
		LoadingCache<BlockPos, BlockWorldState> loadingcache = BlockPattern.createLoadingCache(worldIn, true);

		if (!blockportal$size.isValid()) {
			enumfacing$axis = Facing.Axis.X;
			blockportal$size = new BlockPortal.Size(worldIn, p_181089_2_, Facing.Axis.Z);
		}

		if (!blockportal$size.isValid()) {
			return new BlockPattern.PatternHelper(p_181089_2_, Facing.NORTH, Facing.UP, loadingcache, 1, 1, 1);
		} else {
			int[] aint = new int[Facing.AxisDirection.values().length];
			Facing enumfacing = blockportal$size.rightDir.rotateYCCW();
			BlockPos blockpos = blockportal$size.bottomLeft.up(blockportal$size.getHeight() - 1);

			for (Facing.AxisDirection enumfacing$axisdirection : Facing.AxisDirection.values()) {
				BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper(enumfacing.getAxisDirection() == enumfacing$axisdirection ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), Facing.getFacingFromAxis(enumfacing$axisdirection, enumfacing$axis), Facing.UP, loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);

				for (int i = 0; i < blockportal$size.getWidth(); ++i) {
					for (int j = 0; j < blockportal$size.getHeight(); ++j) {
						BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, j, 1);

						if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getMaterial() != Material.AIR) {
							++aint[enumfacing$axisdirection.ordinal()];
						}
					}
				}
			}

			Facing.AxisDirection enumfacing$axisdirection1 = Facing.AxisDirection.POSITIVE;

			for (Facing.AxisDirection enumfacing$axisdirection2 : Facing.AxisDirection.values()) {
				if (aint[enumfacing$axisdirection2.ordinal()] < aint[enumfacing$axisdirection1.ordinal()]) {
					enumfacing$axisdirection1 = enumfacing$axisdirection2;
				}
			}

			return new BlockPattern.PatternHelper(enumfacing.getAxisDirection() == enumfacing$axisdirection1 ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), Facing.getFacingFromAxis(enumfacing$axisdirection1, enumfacing$axis), Facing.UP, loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
		}
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, Facing face) {

		return BlockFaceShape.UNDEFINED;
	}

	public static class Size {

		private final World world;
		private final Facing.Axis axis;
		private final Facing rightDir;
		private final Facing leftDir;
		private int portalBlockCount;
		private BlockPos bottomLeft;
		private int height;
		private int width;

		public Size(World worldIn, BlockPos p_i45694_2_, Facing.Axis p_i45694_3_) {

			world = worldIn;
			axis = p_i45694_3_;

			if (p_i45694_3_ == Facing.Axis.X) {
				leftDir = Facing.EAST;
				rightDir = Facing.WEST;
			} else {
				leftDir = Facing.NORTH;
				rightDir = Facing.SOUTH;
			}

			for (BlockPos blockpos = p_i45694_2_; p_i45694_2_.getY() > blockpos.getY() - 21 && p_i45694_2_.getY() > 0 && isEmptyBlock(worldIn.getBlockState(p_i45694_2_.down()).getBlock()); p_i45694_2_ = p_i45694_2_.down()) {
			}

			int i = getDistanceUntilEdge(p_i45694_2_, leftDir) - 1;

			if (i >= 0) {
				bottomLeft = p_i45694_2_.offset(leftDir, i);
				width = getDistanceUntilEdge(bottomLeft, rightDir);

				if (width < 2 || width > 21) {
					bottomLeft = null;
					width = 0;
				}
			}

			if (bottomLeft != null) {
				height = calculatePortalHeight();
			}
		}

		protected int getDistanceUntilEdge(BlockPos p_180120_1_, Facing p_180120_2_) {

			int i;

			for (i = 0; i < 22; ++i) {
				BlockPos blockpos = p_180120_1_.offset(p_180120_2_, i);

				if (!isEmptyBlock(world.getBlockState(blockpos).getBlock()) || world.getBlockState(blockpos.down()).getBlock() != Blocks.OBSIDIAN) {
					break;
				}
			}

			Block block = world.getBlockState(p_180120_1_.offset(p_180120_2_, i)).getBlock();
			return block == Blocks.OBSIDIAN ? i : 0;
		}

		public int getHeight() {

			return height;
		}

		public int getWidth() {

			return width;
		}

		protected int calculatePortalHeight() {

			label56:

			for (height = 0; height < 21; ++height) {
				for (int i = 0; i < width; ++i) {
					BlockPos blockpos = bottomLeft.offset(rightDir, i).up(height);
					Block block = world.getBlockState(blockpos).getBlock();

					if (!isEmptyBlock(block)) {
						break label56;
					}

					if (block == Blocks.PORTAL) {
						++portalBlockCount;
					}

					if (i == 0) {
						block = world.getBlockState(blockpos.offset(leftDir)).getBlock();

						if (block != Blocks.OBSIDIAN) {
							break label56;
						}
					} else if (i == width - 1) {
						block = world.getBlockState(blockpos.offset(rightDir)).getBlock();

						if (block != Blocks.OBSIDIAN) {
							break label56;
						}
					}
				}
			}

			for (int j = 0; j < width; ++j) {
				if (world.getBlockState(bottomLeft.offset(rightDir, j).up(height)).getBlock() != Blocks.OBSIDIAN) {
					height = 0;
					break;
				}
			}

			if (height <= 21 && height >= 3) {
				return height;
			} else {
				bottomLeft = null;
				width = 0;
				height = 0;
				return 0;
			}
		}

		protected boolean isEmptyBlock(Block blockIn) {

			return blockIn.blockMaterial == Material.AIR || blockIn == Blocks.FIRE || blockIn == Blocks.PORTAL;
		}

		public boolean isValid() {

			return bottomLeft != null && width >= 2 && width <= 21 && height >= 3 && height <= 21;
		}

		public void placePortalBlocks() {

			for (int i = 0; i < width; ++i) {
				BlockPos blockpos = bottomLeft.offset(rightDir, i);

				for (int j = 0; j < height; ++j) {
					world.setBlockState(blockpos.up(j), Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, axis), 2);
				}
			}
		}

	}

}
