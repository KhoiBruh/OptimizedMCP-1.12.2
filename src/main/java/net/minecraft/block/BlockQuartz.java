package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQuartz extends Block {

	public static final PropertyEnum<BlockQuartz.Type> VARIANT = PropertyEnum.create("variant", BlockQuartz.Type.class);

	public BlockQuartz() {

		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockQuartz.Type.DEFAULT));
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, Facing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		if (meta == BlockQuartz.Type.LINES_Y.getMetadata()) {
			return switch (facing.getAxis()) {
				case Z -> getDefaultState().withProperty(VARIANT, Type.LINES_Z);
				case X -> getDefaultState().withProperty(VARIANT, Type.LINES_X);
				case Y -> getDefaultState().withProperty(VARIANT, Type.LINES_Y);
			};
		}

		return meta == BlockQuartz.Type.CHISELED.getMetadata() ? getDefaultState().withProperty(VARIANT, BlockQuartz.Type.CHISELED) : getDefaultState().withProperty(VARIANT, BlockQuartz.Type.DEFAULT);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		BlockQuartz.Type blockquartz$enumtype = state.getValue(VARIANT);
		return blockquartz$enumtype != BlockQuartz.Type.LINES_X && blockquartz$enumtype != BlockQuartz.Type.LINES_Z ? blockquartz$enumtype.getMetadata() : BlockQuartz.Type.LINES_Y.getMetadata();
	}

	protected ItemStack getSilkTouchDrop(IBlockState state) {

		BlockQuartz.Type blockquartz$enumtype = state.getValue(VARIANT);
		return blockquartz$enumtype != BlockQuartz.Type.LINES_X && blockquartz$enumtype != BlockQuartz.Type.LINES_Z ? super.getSilkTouchDrop(state) : new ItemStack(Item.getItemFromBlock(this), 1, BlockQuartz.Type.LINES_Y.getMetadata());
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		items.add(new ItemStack(this, 1, BlockQuartz.Type.DEFAULT.getMetadata()));
		items.add(new ItemStack(this, 1, BlockQuartz.Type.CHISELED.getMetadata()));
		items.add(new ItemStack(this, 1, BlockQuartz.Type.LINES_Y.getMetadata()));
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return MapColor.QUARTZ;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(VARIANT, BlockQuartz.Type.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return switch (rot) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(VARIANT)) {
				case LINES_X -> state.withProperty(VARIANT, Type.LINES_Z);
				case LINES_Z -> state.withProperty(VARIANT, Type.LINES_X);
				default -> state;
			};
			default -> state;
		};
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, VARIANT);
	}

	public enum Type implements IStringSerializable {
		DEFAULT(0, "default", "default"),
		CHISELED(1, "chiseled", "chiseled"),
		LINES_Y(2, "lines_y", "lines"),
		LINES_X(3, "lines_x", "lines"),
		LINES_Z(4, "lines_z", "lines");

		private static final BlockQuartz.Type[] META_LOOKUP = new BlockQuartz.Type[values().length];

		static {
			for (BlockQuartz.Type blockquartz$enumtype : values()) {
				META_LOOKUP[blockquartz$enumtype.getMetadata()] = blockquartz$enumtype;
			}
		}

		private final int meta;
		private final String serializedName;
		private final String unlocalizedName;

		Type(int meta, String name, String unlocalizedName) {

			this.meta = meta;
			serializedName = name;
			this.unlocalizedName = unlocalizedName;
		}

		public static BlockQuartz.Type byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public int getMetadata() {

			return meta;
		}

		public String toString() {

			return unlocalizedName;
		}

		public String getName() {

			return serializedName;
		}
	}

}
