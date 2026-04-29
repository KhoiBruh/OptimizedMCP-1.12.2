package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public class BlockStoneBrick extends Block {

	public static final PropertyEnum<BlockStoneBrick.Type> VARIANT = PropertyEnum.create("variant", BlockStoneBrick.Type.class);
	public static final int DEFAULT_META = BlockStoneBrick.Type.DEFAULT.getMetadata();
	public static final int MOSSY_META = BlockStoneBrick.Type.MOSSY.getMetadata();
	public static final int CRACKED_META = BlockStoneBrick.Type.CRACKED.getMetadata();
	public static final int CHISELED_META = BlockStoneBrick.Type.CHISELED.getMetadata();

	public BlockStoneBrick() {

		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockStoneBrick.Type.DEFAULT));
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		for (BlockStoneBrick.Type blockstonebrick$enumtype : BlockStoneBrick.Type.values()) {
			items.add(new ItemStack(this, 1, blockstonebrick$enumtype.getMetadata()));
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(VARIANT, BlockStoneBrick.Type.byMetadata(meta));
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

	public enum Type implements IStringSerializable {
		DEFAULT(0, "stonebrick", "default"),
		MOSSY(1, "mossy_stonebrick", "mossy"),
		CRACKED(2, "cracked_stonebrick", "cracked"),
		CHISELED(3, "chiseled_stonebrick", "chiseled");

		private static final BlockStoneBrick.Type[] META_LOOKUP = new BlockStoneBrick.Type[values().length];

		static {
			for (BlockStoneBrick.Type blockstonebrick$enumtype : values()) {
				META_LOOKUP[blockstonebrick$enumtype.getMetadata()] = blockstonebrick$enumtype;
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

		public static BlockStoneBrick.Type byMetadata(int meta) {

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
