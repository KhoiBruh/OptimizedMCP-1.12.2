package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.List;
import java.util.Random;

public class BlockChorusPlant extends Block {

	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");

	protected BlockChorusPlant() {

		super(Material.PLANTS, MapColor.PURPLE);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setDefaultState(blockState.getBaseState().withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false).withProperty(UP, false).withProperty(DOWN, false));
	}

	/**
	 * Get the actual Block state of this Block at the given position. This applies properties not visible in the
	 * metadata, such as fence connections.
	 */
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		Block block = worldIn.getBlockState(pos.down()).getBlock();
		Block block1 = worldIn.getBlockState(pos.up()).getBlock();
		Block block2 = worldIn.getBlockState(pos.north()).getBlock();
		Block block3 = worldIn.getBlockState(pos.east()).getBlock();
		Block block4 = worldIn.getBlockState(pos.south()).getBlock();
		Block block5 = worldIn.getBlockState(pos.west()).getBlock();
		return state.withProperty(DOWN, block == this || block == Blocks.CHORUS_FLOWER || block == Blocks.END_STONE).withProperty(UP, block1 == this || block1 == Blocks.CHORUS_FLOWER).withProperty(NORTH, block2 == this || block2 == Blocks.CHORUS_FLOWER).withProperty(EAST, block3 == this || block3 == Blocks.CHORUS_FLOWER).withProperty(SOUTH, block4 == this || block4 == Blocks.CHORUS_FLOWER).withProperty(WEST, block5 == this || block5 == Blocks.CHORUS_FLOWER);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		state = state.getActualState(source, pos);
		float f = 0.1875F;
		float f1 = state.getValue(WEST) ? 0.0F : 0.1875F;
		float f2 = state.getValue(DOWN) ? 0.0F : 0.1875F;
		float f3 = state.getValue(NORTH) ? 0.0F : 0.1875F;
		float f4 = state.getValue(EAST) ? 1.0F : 0.8125F;
		float f5 = state.getValue(UP) ? 1.0F : 0.8125F;
		float f6 = state.getValue(SOUTH) ? 1.0F : 0.8125F;
		return new AxisAlignedBB(f1, f2, f3, f4, f5, f6);
	}

	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {

		if (!isActualState) {
			state = state.getActualState(worldIn, pos);
		}

		float f = 0.1875F;
		float f1 = 0.8125F;
		addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D));

		if (state.getValue(WEST)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D));
		}

		if (state.getValue(EAST)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.8125D, 0.1875D, 0.1875D, 1.0D, 0.8125D, 0.8125D));
		}

		if (state.getValue(UP)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.1875D, 0.8125D, 0.1875D, 0.8125D, 1.0D, 0.8125D));
		}

		if (state.getValue(DOWN)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.1875D, 0.8125D));
		}

		if (state.getValue(NORTH)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.1875D, 0.1875D, 0.0D, 0.8125D, 0.8125D, 0.1875D));
		}

		if (state.getValue(SOUTH)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D, 1.0D));
		}
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return 0;
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		if (!canSurviveAt(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		}
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Items.CHORUS_FRUIT;
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return random.nextInt(2);
	}

	public boolean isFullCube(IBlockState state) {

		return false;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		return super.canPlaceBlockAt(worldIn, pos) && canSurviveAt(worldIn, pos);
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (!canSurviveAt(worldIn, pos)) {
			worldIn.scheduleUpdate(pos, this, 1);
		}
	}

	public boolean canSurviveAt(World wordIn, BlockPos pos) {

		boolean flag = wordIn.isAirBlock(pos.up());
		boolean flag1 = wordIn.isAirBlock(pos.down());

		for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.offset(enumfacing);
			Block block = wordIn.getBlockState(blockpos).getBlock();

			if (block == this) {
				if (!flag && !flag1) {
					return false;
				}

				Block block1 = wordIn.getBlockState(blockpos.down()).getBlock();

				if (block1 == this || block1 == Blocks.END_STONE) {
					return true;
				}
			}
		}

		Block block2 = wordIn.getBlockState(pos.down()).getBlock();
		return block2 == this || block2 == Blocks.END_STONE;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT;
	}

	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {

		Block block = blockAccess.getBlockState(pos.offset(side)).getBlock();
		return block != this && block != Blocks.CHORUS_FLOWER && (side != EnumFacing.DOWN || block != Blocks.END_STONE);
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	/**
	 * Determines if an entity can path through this block
	 */
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {

		return false;
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
