package net.minecraft.block;

import com.google.common.collect.Lists;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.List;

public abstract class BlockRailBase extends Block {

	protected static final AxisAlignedBB FLAT_AABB = new AxisAlignedBB(0D, 0D, 0D, 1D, 0.125D, 1D);
	protected static final AxisAlignedBB ASCENDING_AABB = new AxisAlignedBB(0D, 0D, 0D, 1D, 0.5D, 1D);
	protected final boolean isPowered;

	protected BlockRailBase(boolean isPowered) {

		super(Material.CIRCUITS);
		this.isPowered = isPowered;
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	public static boolean isRailBlock(World worldIn, BlockPos pos) {

		return isRailBlock(worldIn.getBlockState(pos));
	}

	public static boolean isRailBlock(IBlockState state) {

		Block block = state.getBlock();
		return block == Blocks.RAIL || block == Blocks.GOLDEN_RAIL || block == Blocks.DETECTOR_RAIL || block == Blocks.ACTIVATOR_RAIL;
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

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getBlock() == this ? state.getValue(getShapeProperty()) : null;
		return blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending() ? ASCENDING_AABB : FLAT_AABB;
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

	public boolean isFullCube(IBlockState state) {

		return false;
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		return worldIn.getBlockState(pos.down()).isTopSolid();
	}

	/**
	 * Called after the block is set in the Chunk data, but before the Tile Entity is set
	 */
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {

		if (!worldIn.isRemote) {
			state = updateDir(worldIn, pos, state, true);

			if (isPowered) {
				state.neighborChanged(worldIn, pos, this, pos);
			}
		}
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (!worldIn.isRemote) {
			BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getValue(getShapeProperty());
			boolean flag = !worldIn.getBlockState(pos.down()).isTopSolid();

			if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_EAST && !worldIn.getBlockState(pos.east()).isTopSolid()) {
				flag = true;
			} else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_WEST && !worldIn.getBlockState(pos.west()).isTopSolid()) {
				flag = true;
			} else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_NORTH && !worldIn.getBlockState(pos.north()).isTopSolid()) {
				flag = true;
			} else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_SOUTH && !worldIn.getBlockState(pos.south()).isTopSolid()) {
				flag = true;
			}

			if (flag && !worldIn.isAirBlock(pos)) {
				dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			} else {
				updateState(state, worldIn, pos, blockIn);
			}
		}
	}

	protected void updateState(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {

	}

	protected IBlockState updateDir(World worldIn, BlockPos pos, IBlockState state, boolean initialPlacement) {

		return worldIn.isRemote ? state : (new BlockRailBase.Rail(worldIn, pos, state)).place(worldIn.isBlockPowered(pos), initialPlacement).getBlockState();
	}

	public EnumPushReaction getMobilityFlag(IBlockState state) {

		return EnumPushReaction.NORMAL;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT;
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		super.breakBlock(worldIn, pos, state);

		if (state.getValue(getShapeProperty()).isAscending()) {
			worldIn.notifyNeighborsOfStateChange(pos.up(), this, false);
		}

		if (isPowered) {
			worldIn.notifyNeighborsOfStateChange(pos, this, false);
			worldIn.notifyNeighborsOfStateChange(pos.down(), this, false);
		}
	}

	public abstract IProperty<BlockRailBase.EnumRailDirection> getShapeProperty();

	public enum EnumRailDirection implements IStringSerializable {
		NORTH_SOUTH(0, "north_south"),
		EAST_WEST(1, "east_west"),
		ASCENDING_EAST(2, "ascending_east"),
		ASCENDING_WEST(3, "ascending_west"),
		ASCENDING_NORTH(4, "ascending_north"),
		ASCENDING_SOUTH(5, "ascending_south"),
		SOUTH_EAST(6, "south_east"),
		SOUTH_WEST(7, "south_west"),
		NORTH_WEST(8, "north_west"),
		NORTH_EAST(9, "north_east");

		private static final BlockRailBase.EnumRailDirection[] META_LOOKUP = new BlockRailBase.EnumRailDirection[values().length];

		static {
			for (BlockRailBase.EnumRailDirection blockrailbase$enumraildirection : values()) {
				META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection;
			}
		}

		private final int meta;
		private final String name;

		EnumRailDirection(int meta, String name) {

			this.meta = meta;
			this.name = name;
		}

		public static BlockRailBase.EnumRailDirection byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public int getMetadata() {

			return meta;
		}

		public String toString() {

			return name;
		}

		public boolean isAscending() {

			return this == ASCENDING_NORTH || this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
		}

		public String getName() {

			return name;
		}
	}

	public class Rail {

		private final World world;
		private final BlockPos pos;
		private final BlockRailBase block;
		private final boolean isPowered;
		private final List<BlockPos> connectedRails = Lists.newArrayList();
		private IBlockState state;

		public Rail(World worldIn, BlockPos pos, IBlockState state) {

			world = worldIn;
			this.pos = pos;
			this.state = state;
			block = (BlockRailBase) state.getBlock();
			BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getValue(block.getShapeProperty());
			isPowered = block.isPowered;
			updateConnectedRails(blockrailbase$enumraildirection);
		}

		public List<BlockPos> getConnectedRails() {

			return connectedRails;
		}

		private void updateConnectedRails(BlockRailBase.EnumRailDirection railDirection) {

			connectedRails.clear();

			switch (railDirection) {
				case NORTH_SOUTH:
					connectedRails.add(pos.north());
					connectedRails.add(pos.south());
					break;

				case EAST_WEST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.east());
					break;

				case ASCENDING_EAST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.east().up());
					break;

				case ASCENDING_WEST:
					connectedRails.add(pos.west().up());
					connectedRails.add(pos.east());
					break;

				case ASCENDING_NORTH:
					connectedRails.add(pos.north().up());
					connectedRails.add(pos.south());
					break;

				case ASCENDING_SOUTH:
					connectedRails.add(pos.north());
					connectedRails.add(pos.south().up());
					break;

				case SOUTH_EAST:
					connectedRails.add(pos.east());
					connectedRails.add(pos.south());
					break;

				case SOUTH_WEST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.south());
					break;

				case NORTH_WEST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.north());
					break;

				case NORTH_EAST:
					connectedRails.add(pos.east());
					connectedRails.add(pos.north());
			}
		}

		private void removeSoftConnections() {

			for (int i = 0; i < connectedRails.size(); ++i) {
				BlockRailBase.Rail blockrailbase$rail = findRailAt(connectedRails.get(i));

				if (blockrailbase$rail != null && blockrailbase$rail.isConnectedToRail(this)) {
					connectedRails.set(i, blockrailbase$rail.pos);
				} else {
					connectedRails.remove(i--);
				}
			}
		}

		private boolean hasRailAt(BlockPos pos) {

			return BlockRailBase.isRailBlock(world, pos) || BlockRailBase.isRailBlock(world, pos.up()) || BlockRailBase.isRailBlock(world, pos.down());
		}

		
		private BlockRailBase.Rail findRailAt(BlockPos pos) {

			IBlockState iblockstate = world.getBlockState(pos);

			if (BlockRailBase.isRailBlock(iblockstate)) {
				return BlockRailBase.this.new Rail(world, pos, iblockstate);
			} else {
				BlockPos lvt_2_1_ = pos.up();
				iblockstate = world.getBlockState(lvt_2_1_);

				if (BlockRailBase.isRailBlock(iblockstate)) {
					return BlockRailBase.this.new Rail(world, lvt_2_1_, iblockstate);
				} else {
					lvt_2_1_ = pos.down();
					iblockstate = world.getBlockState(lvt_2_1_);
					return BlockRailBase.isRailBlock(iblockstate) ? BlockRailBase.this.new Rail(world, lvt_2_1_, iblockstate) : null;
				}
			}
		}

		private boolean isConnectedToRail(BlockRailBase.Rail rail) {

			return isConnectedTo(rail.pos);
		}

		private boolean isConnectedTo(BlockPos posIn) {

			for (BlockPos blockpos : connectedRails) {
				if (blockpos.getX() == posIn.getX() && blockpos.getZ() == posIn.getZ()) {
					return true;
				}
			}

			return false;
		}

		protected int countAdjacentRails() {

			int i = 0;

			for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
				if (hasRailAt(pos.offset(enumfacing))) {
					++i;
				}
			}

			return i;
		}

		private boolean canConnectTo(BlockRailBase.Rail rail) {

			return isConnectedToRail(rail) || connectedRails.size() != 2;
		}

		private void connectTo(BlockRailBase.Rail rail) {

			connectedRails.add(rail.pos);
			BlockPos blockpos = pos.north();
			BlockPos blockpos1 = pos.south();
			BlockPos blockpos2 = pos.west();
			BlockPos blockpos3 = pos.east();
			boolean flag = isConnectedTo(blockpos);
			boolean flag1 = isConnectedTo(blockpos1);
			boolean flag2 = isConnectedTo(blockpos2);
			boolean flag3 = isConnectedTo(blockpos3);
			BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

			if (flag || flag1) {
				blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
			}

			if (flag2 || flag3) {
				blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
			}

			if (!isPowered) {
				if (flag1 && flag3 && !flag && !flag2) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
				}

				if (flag1 && flag2 && !flag && !flag3) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
				}

				if (flag && flag2 && !flag1 && !flag3) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
				}

				if (flag && flag3 && !flag1 && !flag2) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
				}
			}

			if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
				if (BlockRailBase.isRailBlock(world, blockpos.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
				}

				if (BlockRailBase.isRailBlock(world, blockpos1.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
				}
			}

			if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST) {
				if (BlockRailBase.isRailBlock(world, blockpos3.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
				}

				if (BlockRailBase.isRailBlock(world, blockpos2.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
				}
			}

			if (blockrailbase$enumraildirection == null) {
				blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
			}

			state = state.withProperty(block.getShapeProperty(), blockrailbase$enumraildirection);
			world.setBlockState(pos, state, 3);
		}

		private boolean hasNeighborRail(BlockPos posIn) {

			BlockRailBase.Rail blockrailbase$rail = findRailAt(posIn);

			if (blockrailbase$rail == null) {
				return false;
			} else {
				blockrailbase$rail.removeSoftConnections();
				return blockrailbase$rail.canConnectTo(this);
			}
		}

		public BlockRailBase.Rail place(boolean powered, boolean initialPlacement) {

			BlockPos blockpos = pos.north();
			BlockPos blockpos1 = pos.south();
			BlockPos blockpos2 = pos.west();
			BlockPos blockpos3 = pos.east();
			boolean flag = hasNeighborRail(blockpos);
			boolean flag1 = hasNeighborRail(blockpos1);
			boolean flag2 = hasNeighborRail(blockpos2);
			boolean flag3 = hasNeighborRail(blockpos3);
			BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

			if ((flag || flag1) && !flag2 && !flag3) {
				blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
			}

			if ((flag2 || flag3) && !flag && !flag1) {
				blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
			}

			if (!isPowered) {
				if (flag1 && flag3 && !flag && !flag2) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
				}

				if (flag1 && flag2 && !flag && !flag3) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
				}

				if (flag && flag2 && !flag1 && !flag3) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
				}

				if (flag && flag3 && !flag1 && !flag2) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
				}
			}

			if (blockrailbase$enumraildirection == null) {
				if (flag || flag1) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
				}

				if (flag2 || flag3) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
				}

				if (!isPowered) {
					if (powered) {
						if (flag1 && flag3) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
						}

						if (flag2 && flag1) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
						}

						if (flag3 && flag) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
						}

						if (flag && flag2) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
						}
					} else {
						if (flag && flag2) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
						}

						if (flag3 && flag) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
						}

						if (flag2 && flag1) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
						}

						if (flag1 && flag3) {
							blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
						}
					}
				}
			}

			if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
				if (BlockRailBase.isRailBlock(world, blockpos.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
				}

				if (BlockRailBase.isRailBlock(world, blockpos1.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
				}
			}

			if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST) {
				if (BlockRailBase.isRailBlock(world, blockpos3.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
				}

				if (BlockRailBase.isRailBlock(world, blockpos2.up())) {
					blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
				}
			}

			if (blockrailbase$enumraildirection == null) {
				blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
			}

			updateConnectedRails(blockrailbase$enumraildirection);
			state = state.withProperty(block.getShapeProperty(), blockrailbase$enumraildirection);

			if (initialPlacement || world.getBlockState(pos) != state) {
				world.setBlockState(pos, state, 3);

				for (BlockPos connectedRail : connectedRails) {
					Rail blockrailbase$rail = findRailAt(connectedRail);

					if (blockrailbase$rail != null) {
						blockrailbase$rail.removeSoftConnections();

						if (blockrailbase$rail.canConnectTo(this)) {
							blockrailbase$rail.connectTo(this);
						}
					}
				}
			}

			return this;
		}

		public IBlockState getBlockState() {

			return state;
		}

	}

}
