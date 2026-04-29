package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockSnow extends Block {

	public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
	protected static final AxisAlignedBB[] SNOW_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0D, 0D, 0D, 1D, 0D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.125D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.25D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.375D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.5D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.625D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.75D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 0.875D, 1D), new AxisAlignedBB(0D, 0D, 0D, 1D, 1D, 1D)};

	protected BlockSnow() {

		super(Material.SNOW);
		setDefaultState(blockState.getBaseState().withProperty(LAYERS, 1));
		setTickRandomly(true);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return SNOW_AABB[state.getValue(LAYERS)];
	}

	/**
	 * Determines if an entity can path through this block
	 */
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {

		return worldIn.getBlockState(pos).getValue(LAYERS) < 5;
	}

	/**
	 * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
	 */
	public boolean isTopSolid(IBlockState state) {

		return state.getValue(LAYERS) == 8;
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

		return face == Facing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {

		int i = blockState.getValue(LAYERS) - 1;
		float f = 0.125F;
		AxisAlignedBB axisalignedbb = blockState.getBoundingBox(worldIn, pos);
		return new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.maxX, (float) i * 0.125F, axisalignedbb.maxZ);
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
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		IBlockState iblockstate = worldIn.getBlockState(pos.down());
		Block block = iblockstate.getBlock();

		if (block != Blocks.ICE && block != Blocks.PACKED_ICE && block != Blocks.BARRIER) {
			BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos.down(), Facing.UP);
			return blockfaceshape == BlockFaceShape.SOLID || iblockstate.getMaterial() == Material.LEAVES || block == this && iblockstate.getValue(LAYERS) == 8;
		} else {
			return false;
		}
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		checkAndDropBlock(worldIn, pos, state);
	}

	private boolean checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (!canPlaceBlockAt(worldIn, pos)) {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
	 * Block.removedByPlayer
	 */
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {

		spawnAsEntity(worldIn, pos, new ItemStack(Items.SNOWBALL, state.getValue(LAYERS) + 1, 0));
		worldIn.setBlockToAir(pos);
		player.addStat(StatList.getBlockStats(this));
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Items.SNOWBALL;
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return 0;
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		if (worldIn.getLightFor(SkyBlock.BLOCK, pos) > 11) {
			dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
			worldIn.setBlockToAir(pos);
		}
	}

	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Facing side) {

		if (side == Facing.UP) {
			return true;
		} else {
			IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
			return (iblockstate.getBlock() != this || iblockstate.getValue(LAYERS) < blockState.getValue(LAYERS)) && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(LAYERS, (meta & 7) + 1);
	}

	/**
	 * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
	 */
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {

		return worldIn.getBlockState(pos).getValue(LAYERS) == 1;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(LAYERS) - 1;
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, LAYERS);
	}

}
