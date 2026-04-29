package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public class BlockRedSandstone extends Block {

	public static final PropertyEnum<BlockRedSandstone.Type> TYPE = PropertyEnum.create("type", BlockRedSandstone.Type.class);

	public BlockRedSandstone() {

		super(Material.ROCK, BlockSand.Type.RED_SAND.getMapColor());
		setDefaultState(blockState.getBaseState().withProperty(TYPE, BlockRedSandstone.Type.DEFAULT));
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(TYPE).getMetadata();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		for (BlockRedSandstone.Type blockredsandstone$enumtype : BlockRedSandstone.Type.values()) {
			items.add(new ItemStack(this, 1, blockredsandstone$enumtype.getMetadata()));
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(TYPE, BlockRedSandstone.Type.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(TYPE).getMetadata();
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, TYPE);
	}

	public enum Type implements IStringSerializable {
		DEFAULT(0, "red_sandstone", "default"),
		CHISELED(1, "chiseled_red_sandstone", "chiseled"),
		SMOOTH(2, "smooth_red_sandstone", "smooth");

		private static final BlockRedSandstone.Type[] META_LOOKUP = new BlockRedSandstone.Type[values().length];

		static {
			for (BlockRedSandstone.Type blockredsandstone$enumtype : values()) {
				META_LOOKUP[blockredsandstone$enumtype.getMetadata()] = blockredsandstone$enumtype;
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

		public static BlockRedSandstone.Type byMetadata(int meta) {

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
