package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;

public class BlockPrismarine extends Block {

	public static final PropertyEnum<BlockPrismarine.Type> VARIANT = PropertyEnum.create("variant", BlockPrismarine.Type.class);
	public static final int ROUGH_META = BlockPrismarine.Type.ROUGH.getMetadata();
	public static final int BRICKS_META = BlockPrismarine.Type.BRICKS.getMetadata();
	public static final int DARK_META = BlockPrismarine.Type.DARK.getMetadata();

	public BlockPrismarine() {

		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockPrismarine.Type.ROUGH));
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	/**
	 * Gets the localized name of this block. Used for the statistics page.
	 */
	public String getLocalizedName() {

		return I18n.translateToLocal(getUnlocalizedName() + "." + BlockPrismarine.Type.ROUGH.getUnlocalizedName() + ".name");
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return state.getValue(VARIANT) == BlockPrismarine.Type.ROUGH ? MapColor.CYAN : MapColor.DIAMOND;
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, VARIANT);
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(VARIANT, BlockPrismarine.Type.byMetadata(meta));
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		items.add(new ItemStack(this, 1, ROUGH_META));
		items.add(new ItemStack(this, 1, BRICKS_META));
		items.add(new ItemStack(this, 1, DARK_META));
	}

	public enum Type implements IStringSerializable {
		ROUGH(0, "prismarine", "rough"),
		BRICKS(1, "prismarine_bricks", "bricks"),
		DARK(2, "dark_prismarine", "dark");

		private static final BlockPrismarine.Type[] META_LOOKUP = new BlockPrismarine.Type[values().length];

		static {
			for (BlockPrismarine.Type blockprismarine$enumtype : values()) {
				META_LOOKUP[blockprismarine$enumtype.getMetadata()] = blockprismarine$enumtype;
			}
		}

		private final int meta;
		private final String name;
		private final String unlocalizedName;

		Type(int meta, String name, String unlocalizedName) {

			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public static BlockPrismarine.Type byMetadata(int meta) {

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

		public String getName() {

			return name;
		}

		public String getUnlocalizedName() {

			return unlocalizedName;
		}
	}

}
