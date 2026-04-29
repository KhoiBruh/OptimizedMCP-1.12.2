package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockNewLog extends BlockLog {

	public static final PropertyEnum<BlockPlanks.Type> VARIANT = PropertyEnum.create("variant", BlockPlanks.Type.class, p_apply_1_ -> p_apply_1_.getMetadata() >= 4);

	public BlockNewLog() {

		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockPlanks.Type.ACACIA).withProperty(LOG_AXIS, BlockLog.Axis.Y));
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		BlockPlanks.Type blockplanks$enumtype = state.getValue(VARIANT);

		return switch (state.getValue(LOG_AXIS)) {
			case Y -> blockplanks$enumtype.getMapColor();
			default -> switch (blockplanks$enumtype) {
				case DARK_OAK -> BlockPlanks.Type.DARK_OAK.getMapColor();
				default -> MapColor.STONE;
			};
		};
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		items.add(new ItemStack(this, 1, BlockPlanks.Type.ACACIA.getMetadata() - 4));
		items.add(new ItemStack(this, 1, BlockPlanks.Type.DARK_OAK.getMetadata() - 4));
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		IBlockState iblockstate = getDefaultState().withProperty(VARIANT, BlockPlanks.Type.byMetadata((meta & 3) + 4));

		iblockstate = switch (meta & 12) {
			case 0 -> iblockstate.withProperty(LOG_AXIS, Axis.Y);
			case 4 -> iblockstate.withProperty(LOG_AXIS, Axis.X);
			case 8 -> iblockstate.withProperty(LOG_AXIS, Axis.Z);
			default -> iblockstate.withProperty(LOG_AXIS, Axis.NONE);
		};

		return iblockstate;
	}

	@SuppressWarnings("incomplete-switch")

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;
		i = i | state.getValue(VARIANT).getMetadata() - 4;

		switch (state.getValue(LOG_AXIS)) {
			case X:
				i |= 4;
				break;

			case Z:
				i |= 8;
				break;

			case NONE:
				i |= 12;
		}

		return i;
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, VARIANT, LOG_AXIS);
	}

	protected ItemStack getSilkTouchDrop(IBlockState state) {

		return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata() - 4);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(VARIANT).getMetadata() - 4;
	}

}
