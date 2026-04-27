package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStainedGlassPane extends BlockPane {

	public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.create("color", EnumDyeColor.class);

	public BlockStainedGlassPane() {

		super(Material.GLASS, false);
		setDefaultState(blockState.getBaseState().withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false).withProperty(COLOR, EnumDyeColor.WHITE));
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(COLOR).getMetadata();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		for (int i = 0; i < EnumDyeColor.values().length; ++i) {
			items.add(new ItemStack(this, 1, i));
		}
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return MapColor.getBlockColor(state.getValue(COLOR));
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.TRANSLUCENT;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(COLOR).getMetadata();
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

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH, COLOR);
	}

	/**
	 * Called after the block is set in the Chunk data, but before the Tile Entity is set
	 */
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {

		if (!worldIn.isRemote) {
			BlockBeacon.updateColorAsync(worldIn, pos);
		}
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (!worldIn.isRemote) {
			BlockBeacon.updateColorAsync(worldIn, pos);
		}
	}

}
