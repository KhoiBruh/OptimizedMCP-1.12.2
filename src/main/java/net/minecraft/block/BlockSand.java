package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockSand extends BlockFalling {

	public static final PropertyEnum<BlockSand.Type> VARIANT = PropertyEnum.create("variant", BlockSand.Type.class);

	public BlockSand() {

		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockSand.Type.SAND));
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

		for (BlockSand.Type blocksand$enumtype : BlockSand.Type.values()) {
			items.add(new ItemStack(this, 1, blocksand$enumtype.getMetadata()));
		}
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return state.getValue(VARIANT).getMapColor();
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(VARIANT, BlockSand.Type.byMetadata(meta));
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

	public int getDustColor(IBlockState state) {

		BlockSand.Type blocksand$enumtype = state.getValue(VARIANT);
		return blocksand$enumtype.getDustColor();
	}

	public enum Type implements IStringSerializable {
		SAND(0, "sand", "default", MapColor.SAND, -2370656),
		RED_SAND(1, "red_sand", "red", MapColor.ADOBE, -5679071);

		private static final BlockSand.Type[] META_LOOKUP = new BlockSand.Type[values().length];

		static {
			for (BlockSand.Type blocksand$enumtype : values()) {
				META_LOOKUP[blocksand$enumtype.getMetadata()] = blocksand$enumtype;
			}
		}

		private final int meta;
		private final String name;
		private final MapColor mapColor;
		private final String unlocalizedName;
		private final int dustColor;

		Type(int p_i47157_3_, String p_i47157_4_, String p_i47157_5_, MapColor p_i47157_6_, int p_i47157_7_) {

			meta = p_i47157_3_;
			name = p_i47157_4_;
			mapColor = p_i47157_6_;
			unlocalizedName = p_i47157_5_;
			dustColor = p_i47157_7_;
		}

		public static BlockSand.Type byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public int getDustColor() {

			return dustColor;
		}

		public int getMetadata() {

			return meta;
		}

		public String toString() {

			return name;
		}

		public MapColor getMapColor() {

			return mapColor;
		}

		public String getName() {

			return name;
		}

		public String getUnlocalizedName() {

			return unlocalizedName;
		}
	}

}
