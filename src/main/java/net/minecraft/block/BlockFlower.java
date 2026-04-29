package net.minecraft.block;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collection;

public abstract class BlockFlower extends BlockBush {

	protected PropertyEnum<BlockFlower.FlowerType> type;

	protected BlockFlower() {

		setDefaultState(blockState.getBaseState().withProperty(getTypeProperty(), getBlockType() == BlockFlower.FlowerColor.RED ? BlockFlower.FlowerType.POPPY : BlockFlower.FlowerType.DANDELION));
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return super.getBoundingBox(state, source, pos).offset(state.getOffset(source, pos));
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(getTypeProperty()).getMeta();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		for (BlockFlower.FlowerType blockflower$enumflowertype : BlockFlower.FlowerType.getTypes(getBlockType())) {
			items.add(new ItemStack(this, 1, blockflower$enumflowertype.getMeta()));
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(getTypeProperty(), BlockFlower.FlowerType.getType(getBlockType(), meta));
	}

	/**
	 * Get the Type of this flower (Yellow/Red)
	 */
	public abstract BlockFlower.FlowerColor getBlockType();

	public IProperty<BlockFlower.FlowerType> getTypeProperty() {

		if (type == null) {
			type = PropertyEnum.create("type", BlockFlower.FlowerType.class, p_apply_1_ -> p_apply_1_.getBlockType() == getBlockType());
		}

		return type;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(getTypeProperty()).getMeta();
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, getTypeProperty());
	}

	/**
	 * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
	 */
	public Block.OffsetType getOffsetType() {

		return Block.OffsetType.XZ;
	}

	public enum FlowerColor {
		YELLOW,
		RED;

		public BlockFlower getBlock() {

			return this == YELLOW ? Blocks.YELLOW_FLOWER : Blocks.RED_FLOWER;
		}
	}

	public enum FlowerType implements IStringSerializable {
		DANDELION(BlockFlower.FlowerColor.YELLOW, 0, "dandelion"),
		POPPY(BlockFlower.FlowerColor.RED, 0, "poppy"),
		BLUE_ORCHID(BlockFlower.FlowerColor.RED, 1, "blue_orchid", "blueOrchid"),
		ALLIUM(BlockFlower.FlowerColor.RED, 2, "allium"),
		HOUSTONIA(BlockFlower.FlowerColor.RED, 3, "houstonia"),
		RED_TULIP(BlockFlower.FlowerColor.RED, 4, "red_tulip", "tulipRed"),
		ORANGE_TULIP(BlockFlower.FlowerColor.RED, 5, "orange_tulip", "tulipOrange"),
		WHITE_TULIP(BlockFlower.FlowerColor.RED, 6, "white_tulip", "tulipWhite"),
		PINK_TULIP(BlockFlower.FlowerColor.RED, 7, "pink_tulip", "tulipPink"),
		OXEYE_DAISY(BlockFlower.FlowerColor.RED, 8, "oxeye_daisy", "oxeyeDaisy");

		private static final BlockFlower.FlowerType[][] TYPES_FOR_BLOCK = new BlockFlower.FlowerType[BlockFlower.FlowerColor.values().length][];

		static {
			for (final BlockFlower.FlowerColor blockflower$enumflowercolor : BlockFlower.FlowerColor.values()) {
				Collection<BlockFlower.FlowerType> collection = Collections2.filter(Lists.newArrayList(values()), p_apply_1_ -> p_apply_1_.getBlockType() == blockflower$enumflowercolor);
				TYPES_FOR_BLOCK[blockflower$enumflowercolor.ordinal()] = collection.toArray(new FlowerType[0]);
			}
		}

		private final BlockFlower.FlowerColor blockType;
		private final int meta;
		private final String name;
		private final String unlocalizedName;

		FlowerType(BlockFlower.FlowerColor blockType, int meta, String name) {

			this(blockType, meta, name, name);
		}

		FlowerType(BlockFlower.FlowerColor blockType, int meta, String name, String unlocalizedName) {

			this.blockType = blockType;
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public static BlockFlower.FlowerType getType(BlockFlower.FlowerColor blockType, int meta) {

			BlockFlower.FlowerType[] ablockflower$enumflowertype = TYPES_FOR_BLOCK[blockType.ordinal()];

			if (meta < 0 || meta >= ablockflower$enumflowertype.length) {
				meta = 0;
			}

			return ablockflower$enumflowertype[meta];
		}

		public static BlockFlower.FlowerType[] getTypes(BlockFlower.FlowerColor flowerColor) {

			return TYPES_FOR_BLOCK[flowerColor.ordinal()];
		}

		public BlockFlower.FlowerColor getBlockType() {

			return blockType;
		}

		public int getMeta() {

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
